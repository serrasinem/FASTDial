package fastdial.slots;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Currency slot type implementation. It is an example slot type
 * that is specific to the Banking domain. Any other slot type 
 * with complex requirements can be handled similarly. 
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class CurrencySlot extends Slot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot type
	final static String slotType = "Currency";

	// default currency type is "€"
	String currencyType = "€";

	// prerequisite slot name
	String dependency = "";

	/**
	 * Constructor of an empty currency slot
	 */
	public CurrencySlot() {
		super();
	}

	/**
	 * Constructor of currency slot with all parameters
	 * 
	 * @param name
	 * @param constraint
	 * @param dependency
	 * @param baseQuestion
	 * @param APICall
	 * @param extraInfoAPI
	 * @param errorMessage
	 * @param mandatory
	 * @param regex
	 */
	public CurrencySlot(String name, String constraint, String dependency,
			String baseQuestion, String APICall, String extraInfoAPI, String errorMessage,
			String mandatory, String regex) {
		super(name, constraint, baseQuestion, null, APICall, extraInfoAPI, errorMessage,
				mandatory, regex);
		super.setSlotType(slotType);
		this.dependency = dependency;
		// "€,$"
		if (!constraint.equals("")) {
			String[] consts = constraint.split(";");
			this.currencyType = consts[0];
		}
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#getMachineUtterance()
	 */
	@Override
	public String getMachineUtterance() {
		return this.getBaseQuestion();
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#matchUtterance(java.lang.String)
	 */
	@Override
	public String matchUtterance(String utterance) {
		String amount = findAmount(utterance);
		return amount;
	}

	/**
	 * Extracts the amount value from the given user utterance
	 * 
	 * @param utterance
	 * @return amount
	 */
	public String findAmount(String utterance) {
		List<Pattern> patterns = new ArrayList<Pattern>();

		String patternString = "(([1-9]\\d{0,2}(,\\d{3})*)|(([1-9]\\d*)?\\d))(\\.\\d\\d)?\\s*(dollar|eur|dolar|\\$|\\€|ft|forint)";
		Pattern patternx = Pattern.compile(patternString);
		patterns.add(patternx);

		patternString = "(\\$|\\€|ft)\\s*(([1-9]\\d{0,2}(,\\d{3})*)|(([1-9]\\d*)?\\d))(\\.\\d\\d)?";
		patternx = Pattern.compile(patternString);
		patterns.add(patternx);

		patternString = "\\s*[0-9]+\\s*";
		patternx = Pattern.compile(patternString);
		patterns.add(patternx);

		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(utterance);
			while (matcher.find()) {
				String val = matcher.group().trim().replaceAll("[^0-9]", "");
				this.currencyType = matcher.group().trim().replace(val, "");
				return val;
			}
		}

		return null;

	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return super.getErrorMessage();
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#getDependency()
	 */
	public String getDependency() {
		return this.dependency;
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#matchRegex(java.lang.String)
	 */
	@Override
	public String matchRegex(String utterance) {
		return null;
	}

	/* (non-Javadoc)
	 * @see fastdial.slots.Slot#getValueSample()
	 */
	@Override
	public String getValueSample() {
		return "100 "+currencyType+", 300 euro";
	}

}
