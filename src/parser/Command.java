package parser;

/**
 * A Command is used to try and match a piece or more
 * of an input when parsing data.
 * 
 *  The execute method must be overwritten to parse the specific
 *  type of input you require.
 * @author wouterken
 *
 */
public interface Command {
	ParseTreeNode execute();
}