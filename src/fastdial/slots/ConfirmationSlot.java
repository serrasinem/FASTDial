package fastdial.slots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Collections;


/**
 * Confirmation slot type. The slot requires 2 set of keys, i.e. negative and positive 
 * keys, which are searched in the user utterances to confirm or disconfirm.
 * Confirmation is treated as a slot value to make the components of a full conversational 
 * path easily be plugged in or out.
 * 
 * A negative answer to a confirmation may lead to 3 effects:
 * 1- directing the dialogue to a real human agent
 * 2- skipping the next slot, which depends on the confirmation slot
 * 3- cancelling the whole intent
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class ConfirmationSlot extends Slot{

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot type
	final static String slotType = "Confirmation";

	/** positive and negative key sets defined either as a constraint or retrieved 
	 * through api call
	 */
	ArrayList<String> pKeys, nKeys;

	// positive and negative regular expressions
	ArrayList<String> pTemplates, nTemplates;

	// action to direct the dialogue to a helpline
	Boolean directHelpline = false;
	// action to skip the slot the index of which is current_index+1
	Boolean skipNext = false;
	// action to cancel the current intent
	Boolean cancelIntent = false;

	// prerequisite slot name
	String dependency;

	/**
	 * Constructor of an empty confirmation slot
	 */
	public ConfirmationSlot() {
		super();
	}

	/**
	 * Constructor with all parameters
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
	public ConfirmationSlot(String name, String constraint, String dependency, 
			String baseQuestion, String APICall, String extraInfoAPI, 
			String errorMessage, String mandatory, String regex) {
		super(name, constraint, baseQuestion, null, APICall, extraInfoAPI, 
				errorMessage, mandatory, regex);
		super.setSlotType(slotType);

		pKeys = new ArrayList<String>();
		nKeys = new ArrayList<String>();

		this.dependency = dependency;
		if(!constraint.equals("")) {
			String[] additionals = constraint.split(";");
			String[] positives = additionals[0].split(",");
			String[] negatives = additionals[1].split(",");
			Collections.addAll(pKeys, positives); 
			Collections.addAll(nKeys, negatives);
		}
		if(constraint.contains("action:directHelpLine")){
			directHelpline = true;
		}
		if(constraint.contains("action:skipNext")){
			skipNext = true;
		}
		if(constraint.contains("action:cancelIntent")){
			cancelIntent = true;
		}

		if(!regex.equals(""))
		{
			String[] posNeg = regex.split(";");

			pTemplates = new ArrayList<String>(
					Arrays.asList(posNeg[0].split(",")));
			nTemplates = new ArrayList<String>(
					Arrays.asList(posNeg[1].split(",")));
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
	 * Returns the confirmation key if any, otherwise null
	 * It uses the keys defined in the slot constraint or returned as an API response
	 * 
	 * @param utterance
	 * @return confirmation
	 */
	@Override
	public String matchUtterance(String utterance) {
		String prepared = " "+utterance+" ";

		for(String key:pKeys)
		{
			if(prepared.contains(" "+key.toLowerCase()+" "))
				return "yes";

		}
		for(String key:nKeys)
		{
			if(prepared.contains(" "+key.toLowerCase()+" "))
			{	
				if(directHelpline) 
					return "directHelpLine";
				else if(skipNext)
					return "skipNext";
				else if(cancelIntent)
					return "cancelIntent";

				return "no";
			}

		}

		return matchRegex(utterance);
	}

	/**
	 * Returns the confirmation key if any, otherwise null
	 * It uses the defined regular expressions to match the slot value 
	 * 
	 * @param utterance
	 * @return confirmation
	 */
	@Override
	public String matchRegex(String utterance) {
		if(nTemplates != null) {
			for(String temp: nTemplates)
			{	
				Pattern keypattern = Pattern.compile(".*" + temp + ".*");
				Matcher matcher = keypattern.matcher(utterance);
				if(matcher.find()) {
					if(directHelpline)
						return "directHelpLine";
					else if(skipNext)
						return "skipNext";
					else if(cancelIntent)
						return "cancelIntent";

					return "no";
				}	
			}
		}

		if(pTemplates != null) {
			for(String temp: pTemplates)
			{	
				Pattern keypattern = Pattern.compile(".*" + temp + ".*");
				Matcher matcher = keypattern.matcher(utterance);
				if(matcher.find()) {
					return "yes";
				}

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
	@Override
	public String getDependency() {
		return this.dependency;
	}

	/* (non-Javadoc)
	 * @see fastdial.slots.Slot#getValueSample()
	 */
	@Override
	public String getValueSample() {
		return "For confirming the question:"+String.format(",",pKeys)+ " and for "
				+ "rejecting: "+String.format(",",nKeys);
	}

}
