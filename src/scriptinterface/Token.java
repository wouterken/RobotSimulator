package scriptinterface;
import java.awt.Graphics;

import parser.ParseTreeNode;

import simulator.World;


/**
 * A token is an end node in a parse tree.
 * @author wouterken
 *
 */
public class Token extends ParseTreeNode{
	
	private String value;
	protected String type;
	
	/**
	 * Creates a new token with a string as value.
	 * @param value
	 */
	public Token(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the appropriate value for this token.
	 * Checks to see if it needs to substituted for a boolean or integer
	 * from the world class first.
	 * If not attempts to return it as an integer, if that fails
	 * it must be a string.
	 */
	@Override
	public Object execute(int robotID, World world, Graphics g) {
		
		if(WorldInterface.worldBooleans.containsKey(value))
			return WorldInterface.worldBooleans.get(value).evaluate(robotID, world, g);
		
		if(WorldInterface.worldInts.containsKey(value))
			return WorldInterface.worldInts.get(value).evaluate(robotID, world, g);
		
		try{
			int num = Integer.parseInt(value);
			return num;
		}
		catch (Exception e) {
			return value;
		}
	}

	@Override
	public Object execute(int robotID, World world, Graphics g,
			boolean convertToBool) {
		return execute(robotID, world, g);
	}

	@Override
	public ParseTreeNode get(int index) {
		return null;
	}
	
	@Override
	public int size(){
		return 1;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
