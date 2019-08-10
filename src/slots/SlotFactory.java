package fastdial.slots;

import java.util.logging.Logger;

/**
 * Factory for creating slot instances
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class  SlotFactory {

	// logger
	final static Logger log = Logger.getLogger("FastDial");

	/**
	 * Creates a new slot based on the provided type.
	 *  
	 * @param str the string representation for the value
	 * @return the resulting value
	 */
	public static Slot create(String type, String name, String constraint, 
			String dependency, String base_question, String API_call, 
			String extra_info_API, String error_message, String mandatory, 
			String regex) {

		if (type == null) {
			return new StringSlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);
		}
		Slot newSlot; 
		switch(type.toLowerCase().trim()) {
		case  "currency":
			newSlot = new CurrencySlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);
			break;
		case  "stringlist":
			newSlot = new StringListSlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);
			break;
		case  "numeric":
			newSlot = new NumericSlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);
			break;
		case  "date":
			newSlot = new DateSlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);
			break;

		case  "confirmation":
			newSlot = new ConfirmationSlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);
			break;
		default: 
			newSlot = new StringSlot(name, constraint, dependency, base_question,
					API_call, extra_info_API, error_message, mandatory.toString(),
					regex);

		}
		return newSlot;
	}



}
