package simulator;
/**
 * COMP261 Assignment 5 2012
 * Name:
 * Usercode:
 * ID:
 **/

import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import parser.InstructionList;
import parser.ParseTreeNode;
import parser.ParserGenerator;

public class RobotProgram {
	
	private boolean validProgramLoaded;
	private ParseTreeNode ptree;
	private ParserGenerator parser;
	public static String grammarName = ("grammar");
	private static Pattern tokenizer = Pattern.compile("[^\\S\\n]*(?=[{}(),;\\n\"])|(?<=[{}(),;\\n\"])[^\\S\\n]*|" +
														"[^\\S\\n]+|" +
														"(?=-)(?<=[^-])|(?<=-)(?=[^-|=|0-9])|(?<=[0-9]\\s?\\s?\\s?-)(?=[^-|=])|" +
														"(?=\\+=?)(?<=[^\\+])|(?<=\\+=?)(?=[^\\+|=])|" +
														"(?=\\*)(?<=[^\\*])|(?<=\\*)(?=[^\\*|=])|" +
														"(?=/)(?<=[^/])|(?<=/)(?=[^/|=])|" +
														"(?<=[^=!\\*\\+/-])(?=<|>|=+|!=|\\+=|-=|\\*=|/=)|" +
														"(?<==)(?=[^=|\\+|-|\\*])");

    
	

    
	public RobotProgram(){
		parser = new ParserGenerator(grammarName);
	}

   
   

	/** Parses a program from the scanner,
     *  If successful, stores the abstract syntax field in a field, and returns true
     *  Otherwise, returns false.
     *  It should report errors in the parsing (you will be unable to debug without this)
	 * @throws FileNotFoundException 
     */
    public boolean parse(File f) throws FileNotFoundException {
    	
    	this.validProgramLoaded = false;
    	Scanner s = null;
    	
    	try{
    		s = new Scanner(f);
    		
	    	s.useDelimiter(tokenizer);
	    	
	    	//Parse and execute!
	    	ptree = parser.execute(s);
	    	
	        //If there is input left over parsing has failed.
	        if(s.hasNext()){
	        	int errorPosition = 0;
	        	String error, errorLocation, errorLine;
	        	
	        	//Record the error + linenumber to notify the user.
	        	error = String.format("Syntax error on line :%s\n",parser.lineNumber);
	        	errorLocation = s.nextLine();
	        	
	        	s.close();
	        	s = new Scanner(f);
	        	
	        	for(int i = 1; i < parser.lineNumber; i++)
	        		s.nextLine();
	        	
	        	errorLine = s.nextLine().trim();
	        	errorPosition = errorLine.indexOf(errorLocation);
	        	
	        	error += String.format("'%s'\n", errorLine);
	        	error += String.format("%"+((errorPosition!=0)?errorPosition:"")+"s\n","^");
	        	
	        	//Let user know
	        	JOptionPane.showMessageDialog(null, error,"Parse Error", JOptionPane.ERROR_MESSAGE);
	        }
	        //Otherwise success!
	        else
	        	this.validProgramLoaded = true;
	    	}
    	catch (Exception e) {
    		throw new FileNotFoundException();
		}
    	finally{
			if(s!= null)
				s.close();
		}

        return this.validProgramLoaded;
    }

    /** Execute the parse tree stored in the field, if a valid program has been loaded.
	Should return true when successfully completed.
     */
    public boolean execute(int robotID, World world, Graphics g) {
        if (!this.validProgramLoaded) { return false; }
        System.out.println("TODO: Execute a program using your parse tree representation.");
        
        for(ParseTreeNode pnode : (InstructionList)ptree){
        	pnode.execute(robotID, world, g);
        }
          
	return true;  
    }

	public boolean validProgramLoaded() {
        return this.validProgramLoaded;
    }


}
