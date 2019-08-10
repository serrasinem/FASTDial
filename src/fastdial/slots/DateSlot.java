package fastdial.slots;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date slot type implementation. It can handle both proper
 * date strings such as 31-01-2019 or date phrases such as
 * today, now, tomorrow, etc.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class DateSlot extends Slot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot type
	final static String slotType = "Date";

	// date mappings from text to date value
	HashMap<String, LocalDate> dateMap = new HashMap<String, LocalDate>();

	// earlier than constraint
	LocalDate earlierThan = null;

	//later than constraint
	LocalDate laterThan = null;

	// prerequisite slot name
	String dependency;

	/**
	 * Constructor of an empty date slot
	 */
	public DateSlot() {
		super();
	}

	/**
	 * Constructor of date slot with all required parameters
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
	public DateSlot(String name, String constraint, String dependency,
			String baseQuestion, String APICall, String extraInfoAPI,
			String err, String mandatory, String regex) {
		super(name, constraint, baseQuestion, null, APICall, extraInfoAPI,
				err, mandatory, regex);
		super.setSlotType(slotType);
		// TODO read all these from a mapping file
		// tentative list, an exhaustive version should be added
		// or, better, a date recognition model should be added.
		LocalDate date = LocalDate.now();
		LocalDate tomorrow = LocalDate.now().plusDays(1);

		dateMap.put("tomorrow", tomorrow);
		dateMap.put("today", date);
		dateMap.put("at this moment", date);
		dateMap.put("now", date);
		dateMap.put("yesterday", date.minusDays(1));
		dateMap.put("afternoon", date);
		dateMap.put("next week", date.plusWeeks(1));
		dateMap.put("last week", date.minusWeeks(1));
		dateMap.put("in an hour", date);
		dateMap.put("this evening", date);
		dateMap.put("domani", tomorrow);
		dateMap.put("oggi", date);
		dateMap.put("ieri", date.minusDays(1));
		dateMap.put("adesso", date);
		dateMap.put("subito", date);
		dateMap.put("ora", date);
		dateMap.put("immediatamente", date);

		String[] cs = constraint.split(",");
		for (String c : cs) {

			if (c.contains("<")) {
				c = c.replace("<", "");
				LocalDate val = getLocalDate(c);
				if (val == null) {
					log.warning(
							"Date is not recognized. Please check your Intent file. You "
									+ "can specify '<now'");
				} else {
					earlierThan = val;
				}
			} else if (c.contains(">")) {
				c = c.replace(">", "");
				LocalDate val = getLocalDate(c);
				if (val == null) {
					log.warning(
							"Date is not recognized. Please check your Intent file. You "
									+ "can specify '<now'");
				} else {
					laterThan = val;
				}

			}
		}

	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#getMachineUtterance()
	 */
	@Override
	public String getMachineUtterance() {
		return this.getBaseQuestion();
	}

	/**
	 * Returns the date value given a linguistic date phrase, e.g. yesterday, today
	 * 
	 * @param dateValue
	 * @return date
	 */
	public String getDate(String dateValue) {
		for (String key : dateMap.keySet()) {

			if (dateValue.contains(key)) {
				return dateMap.get(key).toString();
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#matchUtterance(java.lang.String)
	 */
	@Override
	public String matchUtterance(String utterance) {
		String d = getDate(utterance);
		if (d == null) {
			ArrayList<String> dates = matchRealDate(utterance);
			if (dates.isEmpty())
				return null;
			else
				return dates.get(0);
		} else
			return d;
	}

	/**
	 * Extracts real date expressions from the given utterance
	 * 
	 * @param utterance user utterance 
	 * @return dates list of detected dates
	 */
	public ArrayList<String> matchRealDate(String utterance) {

		ArrayList<String> dates = new ArrayList<String>();
		String pattern1 = "(((0[1-9]|[12]\\d|3[01])(\\/|-|.)(0[13578]|1[02])(\\/|-|.)((19|[2-9]\\d)\\d{2}))|((0[1-9]|[12]\\d|30)(\\/|-|.)(0[13456789]|1[012])(\\/|-|.)((19|[2-9]\\d)\\d{2}))|((0[1-9]|1\\d|2[0-8])\\/02\\/((19|[2-9]\\d)\\d{2}))|(29\\/02\\/((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))))";
		List<Pattern> patterns = new ArrayList<Pattern>();
		Pattern patternx = Pattern.compile(pattern1);
		patterns.add(patternx);

		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(utterance);
			while (matcher.find()) {
				dates.add(matcher.group().trim());
			}
		}
		return dates;
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return super.getErrorMessage();
	}

	/**
	 * Matches the first date value in the utterance and return a LocalDate object
	 * 
	 * @param utterance
	 * @return local date
	 */
	public LocalDate getLocalDate(String utterance) {
		for (String key : dateMap.keySet()) {

			if (utterance.contains(key)) {
				return dateMap.get(key);
			}
		}

		ArrayList<String> dates = matchRealDate(utterance);
		if (dates.isEmpty())
			return null;
		else {
			String[] vals = dates.get(0).replace(".", "-").replace("/", "-").split("-");
			LocalDate ld = LocalDate.of(Integer.parseInt(vals[0]),
					Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
			return ld;
		}

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

	/* (non-Javadoc)
	 * @see fastdial.slots.Slot#getValueSample()
	 */
	@Override
	public String getValueSample() {
		return "27-09-2019, 27.09.2019";
}


}
