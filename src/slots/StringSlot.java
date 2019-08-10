package fastdial.slots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import opendial.templates.Template;
import opendial.templates.Template.MatchResult;
/**
 * String slot type implementation. It holds the whole utterance as the slot value.
 *  An integer max_len constraint can be defined in order to hold only the first 
 *  max_len characters of the utterance.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class StringSlot extends Slot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot type
	final static String slotType = "StringSlot";

	// regular expressions to fill the slot
	ArrayList<String> templates;   

	// prerequisite slot name
	String dependency = "";

	/**
	 * Constructor of an empty string slot
	 */
	public StringSlot() {
		super();
	}

	/**
	 * Constructor of String slot with all parameters
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
	public StringSlot(String name, String constraint, String dependency,
			String base_question, String API_call, String extra_info_API,
			String error_message, String mandatory, String regex) {
		super(name, constraint, base_question, null, API_call, extra_info_API,
				error_message, mandatory, regex);
		super.setSlotType(slotType);
		this.dependency = dependency;
		if (!regex.equals(""))
			templates = new ArrayList<String>(Arrays.asList(regex.split(",")));

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
		String v = utterance;
		if(regex!=null && !regex.equals(""))
			v = matchRegex(utterance);
		if(v != null) {
			Integer maxLen = 0;
			if(constraint.contains("max_len:"))
				maxLen = Integer.valueOf(constraint.replace("max_len:", "").trim());

			if(maxLen > 0 && v.length() > maxLen)
				return v.substring(0, maxLen);
		}
		return v;
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
		return dependency;
	}

	/* (non-Javadoc)
	 * @see fbk.slots.type.Slot#matchRegex(java.lang.String)
	 */
	@Override
	public String matchRegex(String utterance) {
		if (templates != null) {
			for (String t : templates) {
				Template template = Template.create(t);
				MatchResult mr = template.partialmatch(utterance + " ");
				if (mr.isMatching()) {
					if (mr.containsVar("slot"))
						return mr.getValue("slot").toString().trim();
					else
						return t;
				}
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see fastdial.slots.Slot#getValueSample()
	 */
	@Override
	public String getValueSample() {
		return "";
	}

}
