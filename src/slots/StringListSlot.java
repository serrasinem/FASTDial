package fastdial.slots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import opendial.templates.Template;
import opendial.templates.Template.MatchResult;
/**
 * StringList slot type implementation. It can use both a keylist and
 * regular expressions for OpenDial template matching to detect the
 * slot value.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class StringListSlot extends Slot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot type
	final static String slotType = "StringList";

	// predefined list of possible slot values
	ArrayList<String> keylist;

	// regular expressions to fill the slot
	ArrayList<String> templates;

	// prerequisite slot name
	String dependency = "";

	/**
	 *  Constructor of an empty StringList slot
	 */
	public StringListSlot() {
		super();
	}

	/**
	 * Constructor of StringList slot with all parameters
	 * 
	 * @param name
	 * @param constraint
	 * @param dependency
	 * @param base_question
	 * @param check_API_call
	 * @param extra_info_API
	 * @param error_message
	 * @param mandatory
	 * @param regex
	 */
	public StringListSlot(String name, String constraint, String dependency,
			String base_question, String check_API_call, String extra_info_API,
			String error_message, String mandatory, String regex) {
		super(name, constraint, base_question, null, check_API_call, extra_info_API,
				error_message, mandatory, regex);
		super.setSlotType(slotType);

		String[] consts = constraint.split(";");
		String infoApiCall = null;
		if (constraint.contains("_keys_"))
			infoApiCall = consts[0].split(":")[1];
		this.dependency = dependency;
		constraint = consts[0];
		super.setConstraint(constraint);
		super.setInfoAPICall(infoApiCall);

		if (!constraint.equals("") && !constraint.contains("_keys_")) 
			keylist = new ArrayList<String>(Arrays.asList(constraint.split(",")));

		if (!regex.equals(""))
			templates = new ArrayList<String>(Arrays.asList(regex.split(",")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fbk.slots.type.Slot#getMachineUtterance()
	 */
	@Override
	public String getMachineUtterance() {
		return this.getBaseQuestion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fbk.slots.type.Slot#matchUtterance(java.lang.String)
	 */
	@Override
	public String matchUtterance(String utterance) {
		if (keylist != null)
			for (String key : keylist) {
				String lk = key.toLowerCase();
				if (utterance.contains(lk)) {
					return key;
				}
			}

		return matchRegex(utterance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fbk.slots.type.Slot#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return super.getErrorMessage() + " " + formatKeylist() + ".";
	}

	/**
	 * Returns the stringified version of keylist. Useful to embed it through placeholders
	 * into the machine utterances
	 * 
	 * @return keys
	 */
	public String formatKeylist() {
		String keys = "";
		for (int k = 0; k < keylist.size() - 1; k++) {
			keys += keylist.get(k) + ", ";
		}
		keys += keylist.get(keylist.size() - 1);
		return keys;
	}

	/**
	 * Returns the key list
	 * 
	 * @return keylist
	 */
	public ArrayList<String> getKeylist() {
		return keylist;
	}

	/**
	 * Sets the keylist and modifies the base (slot filling) question by replacing the
	 * {_keys_} placeholder with the formatted list
	 * 
	 * @param keylist
	 */
	public void setKeylist(ArrayList<String> keylist) {
		this.keylist = keylist;
		String base_question = super.getBaseQuestion();
		base_question = base_question.replace("{_keys_}",
				StringUtils.join(keylist, ", "));
		super.setBaseQuestion(base_question);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fbk.slots.type.Slot#getDependency()
	 */
	@Override
	public String getDependency() {
		return dependency;
	}

	/**
	 * Returns true if the keylist is not empty and is not null.
	 * 
	 * @return true if the keylist is set
	 */
	public boolean isKeylistSet() {
		if (this.keylist != null && !this.keylist.isEmpty())
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		return StringUtils.join(keylist, ", ");
	}

}
