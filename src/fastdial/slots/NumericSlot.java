package fastdial.slots;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericSlot extends Slot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot type
	final static String slotType = "Numeric";

	// smaller than constraint
	Long smallerThan = null;

	// bigger than constraint
	Long biggerThan = null;

	// number of digits 
	int digits = 0;

	// prerequisite slot name
	String dependency;

	/**
	 * Constructor of NumericSlot
	 */
	public NumericSlot() {
		super();
	}

	/**
	 * Constructor of Numeric Slot with slot parameters
	 * <a> constraints can be comma separated: 
	 * "<" for assigning a upper limit, e.g., <100
	 * ">" for assigning a lower limit, e.g., >10
	 * "digits" for assigning the number of the digits that the value should have. It is 
	 * useful for id detection, e.g., digits:4
	 * 
	 * @param name
	 * @param constraint
	 * @param dependency
	 * @param base_question
	 * @param API_call
	 * @param extra_info_API
	 * @param error_message
	 * @param mandatory
	 * @param regex
	 */
	public NumericSlot(String name, String constraint, String dependency,
			String base_question, String API_call, String extra_info_API,
			String error_message, String mandatory, String regex) {
		super(name, constraint, base_question, null, API_call, extra_info_API,
				error_message, mandatory, regex);
		super.setSlotType(slotType);
		String[] consts = constraint.split(",");
		for (String s : consts) {
			if (s.contains("<")) {
				smallerThan = Long.parseLong(s.replace("<", ""));
			}

			else if (s.contains(">")) {
				biggerThan = Long.parseLong(s.replace(">", ""));
			}

			else if (s.contains("digits")) {
				digits = Integer.parseInt(s.replace("digits:", ""));
			}
		}

		this.dependency = dependency;
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
		String id = "";
		if (digits != 0)
			id = findDigits(utterance);
		else {
			id = findFirstNumericValue(utterance);
		}
		return id;
	}


	/**
	 * Finds the digits in a given text. Users tend to declare a long sequence of numeric 
	 * values such as ids, part by part, such as "123 and 234 and 452 and 4"
	 * For this purpose, it puts all digits in the text together. If the found number of 
	 * digits are higher than the digits constraint, the method returns null
	 * 
	 * @param utterance
	 * @return digits
	 */
	public String findDigits(String utterance) {
		String patternString = "\\s*[0-9]+\\s*";
		Pattern patternx = Pattern.compile(patternString);
		Matcher matcher = patternx.matcher(utterance);
		String id = "";

		while (matcher.find()) {
			String t = matcher.group().trim();
			id += t;
		}

		if (id.length() == digits)
			return id;

		return null;
	}

	/**
	 * Finds the first numeric string in the utterance
	 * 
	 * @param utterance
	 * @return numeric String
	 */
	public String findFirstNumericValue(String utterance) {

		String patternString = "\\s*[0-9]\\s*";
		Pattern patternx = Pattern.compile(patternString);

		Matcher matcher = patternx.matcher(utterance);

		while (matcher.find()) 
			return matcher.group().trim();

		return null;
	}

	/**
	 * Finds the numeric strings in the utterance. Returns them as a list of Strings
	 * 
	 * @param utterance
	 * @return list of numbers
	 */
	public List<String> findNumericValues(String utterance) {

		String patternString = "\\s*[0-9]\\s*";
		Pattern patternx = Pattern.compile(patternString);

		Matcher matcher = patternx.matcher(utterance);
		List<String> nums = new ArrayList<String>();
		while (matcher.find()) 
			nums.add(matcher.group().trim());
		if(nums.isEmpty())
			return null;

		return nums;
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
	@Override
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

	@Override
	public String getValueSample() {
		return "";
	}

}
