package parser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import scriptinterface.Token;


/**
 * A parser generator, creates a fully functioning
 * parser out of a grammer file. The grammar file must be deterministic and formatted as follows
 * - rule names are uppercase .eg 'RULE'
 * - rule and content are separated by '::='
 * - rules can refer to other rules and terminals.
 * - you can use '*', '+' and '?' to refer to zero or more, one or more, and zero or one of any rule.
 * - terminals can either be a string(must have at least one lowercase letter) or any regex that a java Regex object would accept.
 * @author wouterken
 *
 */
public class ParserGenerator {
	public int lineNumber;
	private String start;
	private Scanner input;
	private String lastLine;
	private boolean EOF = false;
	
	
	public HashMap<String, Command> rules = new HashMap<String, Command>();
	
	/**
	 * Constructs a new parser generator by scanning the file
	 * and creating new rules for each rule item found.
	 * @param filename
	 */
	public ParserGenerator(String filename){
		String grammar;
    	lineNumber = 1;
		try {
			grammar = readFile(filename);
			if(grammar != null){
				String [] rules = grammar.split("\n(?=[A-Z])");
				for (String s: rules)
					parseRule(s);
				start = rules[0].substring(0, rules[0].indexOf("::=") - 1);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
    	
	}
	
	/**
	 * Splits a rule into tidied segments, parses the rule
	 * and puts it in the rule map.
	 * @param s
	 */
	 private void parseRule(String s) {
	    	String[][] args = getTidiedArguments(s);
	    	String rulename = args[0][0];
	    	String [] parts = args[1];
	    	rules.put(rulename, parseRuleBody(parts, rulename));
		}	


	   /**
	    * Parses a rule body and turns it into 
	    * one or more dynamically created parsing
	    * commands.
	    * @param parts
	    * @param name
	    * @return
	    */
		private Command parseRuleBody(String[] parts, String name) {
			
			Command [][] commandsList = new Command[parts.length][];
			int commandIdx = 0;
			for (String expression: parts){
				String [] tokens = expression.split("\\s");
				Command [] commands = new Command[tokens.length];
				int tokenIdx = 0;
				
				/**
				 * Loop through rule tokens.
				 */
				for(String tok: tokens){
					
					//If uppercase, it refers to another rule.
					if(tok.toUpperCase().equals(tok) && !tok.toLowerCase().equals(tok)){
						final String KEY;
						
						//Handle *, +, and ?
						Boolean zeroOrMore = tok.substring(tok.length() -1).equals("*");
						Boolean oneOrMore = tok.substring(tok.length() -1).equals("+");
						Boolean zeroOrOne = tok.substring(tok.length() -1).equals("?");
						if(zeroOrMore || oneOrMore || zeroOrOne)
							KEY = tok.substring(0, tok.length() -1);
						else
							KEY = tok;
						commands[tokenIdx++] = 
								
						/**
						 * Create the matcher for this segment.
						 */
						new AbstractCommand(KEY, zeroOrMore, oneOrMore, zeroOrOne) {
							@Override
							public ParseTreeNode execute() {
								InstructionList instructions = new InstructionList();
								instructions.type = this.rulename;
								Boolean success = true;
								Command rule = rules.get(this.rulename);
								if(this.zeroOrMore){
									while(success){
										ParseTreeNode addedNode = rule.execute();
										success = addedNode != null;
										if(success && addedNode.size() > 0) instructions.addNode(addedNode);
									}
									return instructions;
								}else if(this.zeroOrOne){
									ParseTreeNode addedNode = rule.execute();
									success = addedNode != null;
									if(success && addedNode.size() > 0) instructions.addNode(addedNode);
									return instructions;
								}
								else if(this.oneOrMore){
									ParseTreeNode addedNode = rule.execute();
									success = addedNode != null;
									if(!success) return null;
									while(success){
										if(addedNode.size() > 0) instructions.addNode(addedNode);
										addedNode = rule.execute();
										success = addedNode != null;
									}
									return instructions;
								}else{
									return rule.execute();
								}
							}
						};
						
						
					}else{
						//Rule is a terminal, we are matching a string or regex.
						final String TOKEN = tok;
						
						/**
						 * Create the matcher for this segment.
						 */
						commands[tokenIdx++] = new AbstractCommand() {
							@Override
							public ParseTreeNode execute() {
								return parseToken(TOKEN);
							}

							private ParseTreeNode parseToken(String TOKEN) {
								Boolean hasNext = false;
								
								try{
									hasNext = input.hasNext(TOKEN);
									if(!input.hasNext() && TOKEN.equals("[\\n\\r]") && !EOF){
										EOF = true;
										return new Token("\n");
										
									}
								}catch(Exception e){
									hasNext = input.hasNext(Pattern.quote(TOKEN));
									TOKEN = Pattern.quote(TOKEN);
								}
								if(hasNext){
									
									String next = input.next(TOKEN);

									if(next.contains("\n")){
										lastLine = "";
										lineNumber++;
									}else{
										lastLine += next;
									}
									return new Token(next);
								}

								return null;
							}
						};
					}
					
				}
				commandsList[commandIdx++] = commands;
			

			}
			/**
			 * Create the encompassing matcher for this segment.
			 */
			return new AbstractCommand(commandsList, name) {
				
				@Override
				public ParseTreeNode execute() {
					InstructionList instructions = new InstructionList();
					for (Command [] commandList : this.args){
						Boolean matched = true;
						for(Command command : commandList){
							ParseTreeNode addedNode = command.execute();
							if(addedNode != null && addedNode.size() > 0) instructions.addNode(addedNode);
							matched &= addedNode != null;
							if(! matched) break;
						}
						if(matched){
							
							return instructions;
						}
					}
					return null;
				}
			};
		}


	/**
	 * Cleans a set of string arguments for use in the grammar generation.
	 * @param s
	 * @return
	 */
    private String[][] getTidiedArguments(String s) {
    	String [] rule = s.split(" ::= ");
		String name = rule[0].trim();
		String[] parts = rule[1].trim().split("\\|");
		for(int idx = 0; idx < parts.length; idx ++)
			parts[idx] = parts[idx].trim();
		return new String[][]{{name},parts};
	}

    /**
     * Reads an entire file into a String object.
     * @param path
     * @return
     * @throws IOException
     */
    private static String readFile(String path) throws IOException {
    		int levels = 4;
	    	while(!new File(path).exists() && levels-- >= 0){
	    		path = ".."+File.separator+path;
	    		System.out.println(path);
	    	}
	    	if(!new File(path).exists()){
	    		JOptionPane.showMessageDialog(null, "Couldn't load grammar\nEnsure the 'grammar' file is present\nSomewhere in the project directory","Parse Error", JOptionPane.ERROR_MESSAGE);
	    		return null;
	    	}
    	  FileInputStream stream = new FileInputStream(new File(path));
    	  try {
    	    FileChannel filechannel = stream.getChannel();
    	    MappedByteBuffer bb = filechannel.map(FileChannel.MapMode.READ_ONLY, 0, filechannel.size());
    	    return Charset.defaultCharset().decode(bb).toString();
    	  }
    	  finally {
    	    stream.close();
    	  }
    	}
    
    /**
     * Executes a parsed tree.
     * @param s
     * @return
     */
	public ParseTreeNode execute(Scanner s) {
		this.EOF = false;
		this.input = s;
		return this.rules.get(start).execute();
	}
	
}
