package parser;
import java.awt.Graphics;

import simulator.World;

/**
 * A middle or end node in the parse=tree.
 * Can be executed to return a result.
 * @author wouterken
 *
 */
public abstract class ParseTreeNode {

	public abstract Object execute(int robotID, World world, Graphics g);
	
	public abstract Object execute(int robotID, World world, Graphics g, boolean convertToBool);

	public abstract int size();
	
	public abstract ParseTreeNode get(int index);
	

}
