package parser;
import java.util.Scanner;


/**
 * A Command is used to try and match a piece or more
 * of an input when parsing data.
 * 
 *  The execute method must be overwritten to parse the specific
 *  type of input you require.
 * @author wouterken
 *
 */
public abstract class AbstractCommand implements Command {

	protected boolean requiresArgs;
	protected Command[][] args;
	protected TYPE type;
	protected String rulename;
	protected Boolean oneOrMore;
	protected Boolean zeroOrMore;
	protected Boolean zeroOrOne;
	
	public static enum TYPE{
		TOKEN,
		RULE, 
		EXC_RULE
	}
	public AbstractCommand(){
		this.type = TYPE.TOKEN;
	}
	public AbstractCommand(Command[][] commandsList, String name){
		this.rulename = name;
		this.type = TYPE.RULE;
		this.args = commandsList;
	}
	public AbstractCommand(String rulename, Boolean zeroOrMore, Boolean oneOrMore, Boolean zeroOrOne){
		this.type = TYPE.EXC_RULE;
		this.rulename = rulename;
		this.zeroOrMore = zeroOrMore;
		this.oneOrMore = oneOrMore;
		this.zeroOrOne = zeroOrOne;
	}
}
