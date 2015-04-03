package parser;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scriptinterface.Expressions;
import scriptinterface.Terminals;
import simulator.World;


/**
 * An instruction list is a non-terminal node of a parse tree,
 * it may have multiple instructions to execute in order,
 * or more complex loops and conditionals.
 * @author wouterken
 *
 */
public class InstructionList extends ParseTreeNode implements Iterable<ParseTreeNode> {

	public String type;
	private static final String IF_BLOCK = "if";
	private static final String WHILE_LOOP = "while";
	private ArrayList<ParseTreeNode> nodes = new ArrayList<ParseTreeNode>();
	
	/**
	 * Executes a list of instructions, one-by-one
	 */
	public Object execute(int robotID, World world, Graphics g,boolean convertToBool) {
		
		ArrayList<Object> arguments = new ArrayList<Object>();

		//Iterate through instructions.
		for (ParseTreeNode pnode : nodes) {
			
			Object returnValue = pnode.execute(robotID, world, g, convertToBool);
			
			if (returnValue != null) 
				if (returnValue instanceof ArrayList<?>) 
					arguments.addAll((Collection<? extends Object>) returnValue);
				else
					arguments.add(returnValue);
			
			if (returnValue != null)
				try {
					//Handle while loop
					if (returnValue.equals(WHILE_LOOP))
						return enterWhileLoop(
								nodes.get(nodes.indexOf(pnode) + 1),
								nodes.subList(nodes.indexOf(pnode) + 1,
										nodes.size()), robotID, world, g);
					//Handle if-else block.
					else if (returnValue.equals(IF_BLOCK))
						return ifBlock(
								nodes.get(nodes.indexOf(pnode) + 1),
								nodes.subList(nodes.indexOf(pnode) + 2,
										nodes.size()), robotID, world, g);
	
				} catch (Exception e) {}
		}
		
		if (arguments.size() == 1)
			return arguments.get(0);
		else
			return Expressions.evaluate(arguments, robotID, world, g,convertToBool);
	}
	/**
	 * Execute a while loop
	 * @param condition
	 * @param subList
	 * @param robotID
	 * @param world
	 * @param g
	 * @return
	 */
	
	private Object enterWhileLoop(ParseTreeNode condition, List<ParseTreeNode> subList, int robotID, World world, Graphics g) {
		try {
			Boolean cond = false;
			
			ParseTreeNode while_body = subList.get(2);
			cond = checkBoolValue(condition.execute(robotID, world, g, true));
			//Loop while condition holds.
			while (cond) {
				while_body.execute(robotID, world, g);
				//Reevaluate condition.
				cond = checkBoolValue(condition.execute(robotID, world, g, true));
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Execute an if block.
	 * @param condition
	 * @param subList
	 * @param robotID
	 * @param world
	 * @param g
	 * @return
	 */
	private Boolean ifBlock(ParseTreeNode condition, List<ParseTreeNode> subList, int robotID, World world, Graphics g) {
			
		ParseTreeNode if_body = subList.get(0).get(0);
		ParseTreeNode else_body = null;
		Boolean cond = false;
		
		try {
			//Check if it is followed by an else.
			ParseTreeNode else_statement = ((subList.get(0) != null && subList.get(0).get(1) != null)?
												subList.get(0).get(1):null);
			
			else_body = ((else_statement != null && else_statement.get(0) != null && else_statement.get(0).get(1) != null)?
												else_statement.get(0).get(1):null); 
				
			} catch (Exception e) {
		}

		try {
			//Evaluate conditional.
			cond = checkBoolValue(condition.execute(robotID, world, g, true));
			
			//If - ... else ...
			if (cond) 
				if_body.execute(robotID, world, g);
			
			else if (else_body != null) 
				else_body.execute(robotID, world, g);
			
			} 
		catch (Exception e) {}
		
		return !cond;
	}

	/**
	 * Evaluates a boolean value for use in a while or if
	 * construct.
	 * @param conditionValue
	 * @return
	 */
	private Boolean checkBoolValue(Object conditionValue) {
		
		Boolean cond = false;
		
		try {

			List<Object> args = ((conditionValue instanceof List) ? (List<Object>) conditionValue : Arrays.asList(new Object[] { conditionValue }));
			
			boolean negate = false;
			// Check if this is a 'not' value.
			if (args.get(0).equals(Terminals.NOT.val)) {
				negate = true;
				args.remove(0);
			}
			
			// Check to see if it needs evaluating. e.g
			// 5 + 3 < 11 + x
			int numberValue = Expressions.evaluateMathExpression(args);
			cond = ((negate) ? numberValue == 0 : numberValue != 0);
			
		} 
		catch (Exception e) 
		{
			//Otherwise it is most likely a boolean.
			cond = (Boolean) conditionValue;
		}
		return cond;
	}

	public void addNode(ParseTreeNode addedNode) {
		this.nodes.add(addedNode);
	}

	/**
	 * Return neat string representation of list
	 */
	@Override
	public String toString() {
		String ret = "";
		for (ParseTreeNode node : nodes)
			if (node != null)
				ret += node.toString();
		return ret;
	}

	public int size() {
		return nodes.size();
	}

	public ParseTreeNode get(int index) {
		if (index < size()) {
			return nodes.get(index);
		}
		return null;
	}

	@Override
	public Iterator<ParseTreeNode> iterator() {
		return nodes.iterator();
	}

	@Override
	public Object execute(int robotID, World world, Graphics g) {
		return execute(robotID, world, g, false);
	}

}
