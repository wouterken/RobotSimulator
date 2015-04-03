package scriptinterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * The Terminals enum functions simply as a mapping
 * between boolean comparisons and number increment/decrement
 * operators for use in Expressions. 
 * @author wouterken
 *
 */
public enum Terminals{
		EQL_TO("=="), 
		LESS_THAN("<"), 
		GREATER_THAN(">"), 
		NOT_EQL("!="), 
		EQL("="), 
		MIN_EQL("-="), 
		PLS_EQL("+="), 
		TIMES_EQL("*="), 
		DIV_EQL("/="), 
		INCREMENT("++"), 
		DECREMENT("--"), 
		PRINT("print"), 
		NOT("not");
		
		public static Map<String, Terminals> termMap = Collections.unmodifiableMap(new HashMap<String, Terminals>() {
			{
				put("==", EQL_TO);
				put("<", LESS_THAN);
				put(">", GREATER_THAN);
				put("!=", NOT_EQL);
				put("=", EQL);
				put("-=", MIN_EQL);
				put("+=", PLS_EQL);
				put("*=", TIMES_EQL);
				put("/=", DIV_EQL);
				put("--", DECREMENT);
				put("++", INCREMENT);
				put("print", PRINT);
			}
		});
		
		public final String val;
		
		Terminals(String equiv){
			val = equiv;
		}
	}