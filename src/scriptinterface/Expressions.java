package scriptinterface;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulator.World;


/**
 * Expressions is just a wrapper class for handy static
 * functions that help in evaluating expressions.
 * @author wouterken
 *
 */
public class Expressions {
		
	public static HashMap<String, Integer> memory = new HashMap<String, Integer>();

	/**
	 * Evaluate takes an array of arguments (pieces of input from the script)
	 * and returns an evaluated value if possible.
	 * e.g 
	 * x = 1 + 2
	 * move 5
	 * y++
	 * true and x and not false
	 * @param arguments
	 * @param robotID
	 * @param world
	 * @param g
	 * @param convertToBool
	 * @return
	 */
	public static Object evaluate(ArrayList<Object> arguments, int robotID, World world, Graphics g, boolean convertToBool) {
		
		Object[] args = arguments.toArray();
		
		
		//Parsing a call to a built-in function.
		if(args.length > 1 && args[0] instanceof String && WorldInterface.worldActions.containsKey(args[0]))
			performWorldAction(robotID, world, g, arguments);

		
		//Parsing a boolean composition.
		else if(args.length > 2 && args[1] instanceof String && (arguments.contains("or") || arguments.contains("and")))
			return evaluateBooleanComposition(arguments);
		
		//Parsing a boolean negation.
		else if(args.length > 1 && args[0] instanceof String && args[0].equals("not"))
			return !toBoolean(args[1]);
		
		
		//Something more complex..
		else{
			
			try{
				//Parse a boolean expressions ( < | > | == | != )
				if(arguments.subList(1, arguments.size()).contains("<")){
					int left = evaluateMathExpression(arguments.subList(0, arguments.indexOf("<")));
					int right = evaluateMathExpression(arguments.subList(arguments.indexOf("<") + 1, arguments.size()));
					return left < right;
				}
				else if(arguments.subList(1, arguments.size()).contains("==")){
					int left = evaluateMathExpression(arguments.subList(0, arguments.indexOf("==")));
					int right = evaluateMathExpression(arguments.subList(arguments.indexOf("==") + 1, arguments.size()));
					return left == right;				
								}
				else if(arguments.subList(1, arguments.size()).contains(">")){
					int left = evaluateMathExpression(arguments.subList(0, arguments.indexOf(">")));
					int right = evaluateMathExpression(arguments.subList(arguments.indexOf(">") + 1, arguments.size()));
					return left > right;
				}
				else if(arguments.subList(1, arguments.size()).contains("!=")){
					int left = evaluateMathExpression(arguments.subList(0, arguments.indexOf("!=")));
					int right = evaluateMathExpression(arguments.subList(arguments.indexOf("!=") + 1, arguments.size()));
					return left != right;
				}
				else if(args[0] instanceof Integer){
					return arguments;
				}
				
				// Parse an assignment/operation.
				String currentVal = (String) args[0];
				if(args.length > 1 && Terminals.termMap.containsKey(args[1])){
					int value = 0;
					switch (Terminals.termMap.get(args[1])) {
						case DECREMENT:
							value = (memory.containsKey(currentVal)?memory.get(currentVal):0) - 1;
							break;
						case INCREMENT:
							value = (memory.containsKey(currentVal)?memory.get(currentVal):0) + 1;
							break;
						case EQL:
							value = evaluateMathExpression(arguments.subList(2, arguments.size()));
							break;
						case MIN_EQL:
							value = (memory.containsKey(currentVal)?memory.get(currentVal):0);
							value -= evaluateMathExpression(arguments.subList(2, arguments.size()));
							break;
						case PLS_EQL:
							value = (memory.containsKey(currentVal)?memory.get(currentVal):0);
							value += evaluateMathExpression(arguments.subList(2, arguments.size()));
							break;
						case TIMES_EQL:
							value = (memory.containsKey(currentVal)?memory.get(currentVal):0);
							value *= evaluateMathExpression(arguments.subList(2, arguments.size()));
							break;
						case DIV_EQL:
							value = (memory.containsKey(currentVal)?memory.get(currentVal):0);
							value /= evaluateMathExpression(arguments.subList(2, arguments.size()));
							break;
					}
					memory.put(currentVal, value);
					return null;
				}
				else if(args[0].equals("print"))
					printStatement(arguments);
				//Optional parameters that forces the results of a math expression to a boolean.
				else if(convertToBool){
					try{
						return evaluateMathExpression(arguments) != 0;
					}catch (Exception e) {
						return arguments;
					}
				}
				
				else return arguments;
				
			}catch(Exception e){}
		}
		return null;
	}
	
	
	/**
	 * This function evalutes a boolean composition.
	 * takes a list of parameters (of type string, int or boolean)
	 * separated by a single "not" or "and" token
	 * and returns the resulting boolean.
	 * @param arguments
	 * @return
	 */
	private static Boolean evaluateBooleanComposition(ArrayList<Object> arguments) {
		
		Object[] args = arguments.toArray();
		
		int splitPos = ((arguments.contains("or")?arguments.indexOf("or"):arguments.indexOf("and")));
		
		List<Object> desc1 = arguments.subList(0, splitPos);
		List<Object> desc2 = arguments.subList(splitPos + 1, arguments.size());
		
		Boolean arg1 = toBoolean(((desc1.size() == 1)?desc1.get(0):desc1));
		Boolean arg2 = toBoolean(((desc2.size() == 1)?desc2.get(0):desc2));
		
		if(args[1].equals("or"))
			return arg1 || arg2;
		else if(args[1].equals("and"))
			return arg1 && arg2;
		else return null;
	}
	
	/**
	 * This function takes a scripted function call
	 * and calls its mapped equivalent in the world class.
	 * @param robotID
	 * @param w
	 * @param g
	 * @param arguments
	 */
	private static void performWorldAction(int robotID, World w, Graphics g, ArrayList<Object> arguments) {
		Object[] args = arguments.toArray();
		int val = 0;
		
		if(args[1] instanceof String)
			val = getMemVal(args[1]);
		else if (args.length >= 2 && args[1] instanceof Integer)
			val = (Integer) args[1];
		else if(args.length >= 4)
			try{
				val = evaluateMathExpression(arguments.subList(1, arguments.size()));
			}catch (Exception e) { /** Val is not a math expression. That is ok.**/}
		
		
		WorldInterface.worldActions.get(args[0]).evaluate(robotID, w, g, val);
	}
	
	/**
	 * Returns a value from our script-memory
	 * (A hash map of strings to ints).
	 * @param var
	 * @return
	 */
	private static int getMemVal(Object var) {
		int val = 0;
		try{
			if(memory.containsKey(var))
				val = memory.get(var);
		}catch (Exception e) {
			val = 0;
		}
		return val;
	}
	
	/**
	 * Prints a statement, if it is surrounded by quotes it assumes it is a string and prints each
	 * value in the list separate.
	 * 
	 * Otherwise it assumes a maths expression and evaluates it before printing.
	 * e.g
	 * print 5 + 2
	 * '7'
	 * @param arguments
	 */
	private static void printStatement(List<Object> arguments) {
		Object [] args = arguments.toArray();
		if(args[1].equals("\"") && args[args.length - 1].equals("\"")){
			for(int i = 2; i < args.length; i++){
				if(args[i].equals("\""))
					break;
				System.out.print(args[i]+" ");
			}
			System.out.println();
		}else{
			int val = evaluateMathExpression(arguments.subList(1, arguments.size()));
			System.out.println(val);
		}
	}
	
	/**
	 * Forces conversion of unkown object to boolean.
	 * Attempts to do this gracefully.
	 * 
	 * e,g
	 * ints != 0 are true
	 * ints == 0 are false.
	 * 
	 * variables in memory = (assigned value != 0)
	 * variables not in memory = false.
	 * 
	 * 
	 * @param object
	 * @return
	 */
	public static Boolean toBoolean(Object object) {
		if(object instanceof Boolean)
			return (Boolean) object;
		if(object instanceof Integer)
			return (Integer) object != 0;
		if(object instanceof String && memory.containsKey(object))
			return memory.get(object) != 0;
		if(object instanceof List &&  ((List<Object>)object).size() > 1){
			List<Object> args = (List<Object>)object; 
			if(args.get(0).equals("not"))
				return evaluateMathExpression(args.subList(1, args.size())) == 0;
			return evaluateMathExpression((List<Object>) object) != 0;
		}
		return false;
	}
	
	/**
	 * Forces conversion of unkown object to Integer.
	 * Attempts to do this gracefully.
	 * 
	 * e,g
	 * true = 1
	 * false = 0
	 * 
	 * variables in memory = assigned value
	 * variables not in memory = 0.
	 * 
	 * 
	 * @param object
	 * @return
	 */
	public static Integer toInt(Object object){
		if(object instanceof Boolean)
			return (((Boolean) object)?1:0);
		if(object instanceof Integer)
			return (Integer) object;
		if(object instanceof String && memory.containsKey(object))
			return memory.get(object);
		
		return 0;
	}
	
	
	public static int evaluateMathExpression(List<Object> args){
		Object[] arguments = args.toArray();
		int value = toInt(arguments[0]);
		int operand;
		String currentOp = "";
		
		for(int i = 1; i < arguments.length; i++){
			if(i % 2 == 1){
				currentOp = (String) arguments[i];
			}
			else{
				operand = toInt(arguments[i]);
				if(currentOp.equals("-"))
					value -= operand;
				else if(currentOp.equals("+"))
					value += operand;
				else if(currentOp.equals("/"))
					value /= operand;
				else if(currentOp.equals("*"))
					value *= operand;
			}
		}
		return value;
	}

}
