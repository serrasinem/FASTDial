package fastdial.slots;

import java.util.logging.Logger;

import opendial.datastructs.Assignment;

/**
 * Abstract definition of Slot Class
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public abstract class Slot {

    // logger
    final static Logger log = Logger.getLogger("FastLogger");

    String constraint;
    String baseQuestion;
    String checkAPICall;
    String infoAPICall;
    String extraAPICall;
    String errorMessage;
    String slotType;
    String slotName;
    String slotMandatory;
    String regex;
    String extraInfo;

    /**
     * Creates a new Slot with no information
     * 
     */
    public Slot() {
    }

    /**
     * Creates a new Slot
     * 
     * @param unique slot name
     * @param contsraint definition which can be specific to the extended slot types
     * @param question to ask while filling it
     * @param api_call for the constraints of the slot
     * @param api_call for validating the slot value
     * @param error message to be shown in case of the invalid slot value
     * @param a true or false flag to identify if the slot is mandatory for the intent
     * @param regex definitions to be used by NLU in order to identify the slot value
     */
    public Slot(String name, String constraint, String baseQuestion, String infoAPICall,
	    String checkAPICall, String extraAPICall, String errorMessage,
	    String mandatory, String regex) {
	this.slotName = name;
	this.constraint = constraint;
	this.baseQuestion = baseQuestion;
	this.infoAPICall = infoAPICall;
	this.checkAPICall = checkAPICall;
	this.extraAPICall = extraAPICall;
	this.errorMessage = errorMessage;
	this.slotMandatory = mandatory;
	this.regex = regex;
    }

    /**
     * Returns the slot type
     * 
     * @return slot_type
     */
    public String getSlotType() {
	return slotType;
    }

    /**
     * Sets the slot type
     * 
     * @param slot_type
     *            the slot_type to set
     */
    public void setSlotType(String slotType) {
	this.slotType = slotType;
    }

    /**
     * Returns the slot name
     * 
     * @return slot_name
     */
    public String getSlotName() {
	return slotName;
    }

    /**
     * Sets the slot name
     * 
     * @param slot_name
     *            the unique for an intent slot name to set
     */
    public void setSlotName(String slotName) {
	this.slotName = slotName;
    }

    /**
     * Returns if the slot is mandatory to execute the intent
     * 
     * @return slot_mandatory
     */
    public String getSlotMandatory() {
	return slotMandatory;
    }

    /**
     * Sets the obligation of the slot for the execution of the intent
     * 
     * @param slot_mandatory
     *            string representation of a boolean value, coming from FSDS
     */
    public void setSlotMandatory(String slotMandatory) {
	this.slotMandatory = slotMandatory;
    }

    /**
     * Returns the constraint value of the slot
     * 
     * @return raw constraint definition
     */
    public String getConstraint() {
	return constraint;
    }

    /**
     * Sets the constraint value of the slot
     * 
     * @param raw constraint string
     */
    public void setConstraint(String constraint) {
	this.constraint = constraint;
    }

    /**
     * Returns the machine question to be asked to request the slot value
     * 
     * @return slot question asked by DMS
     */
    public String getBaseQuestion() {
	return baseQuestion;
    }

    /**
     * Sets the question to be asked by DMS to request the slot value from the user
     * 
     * @param question slot question asked by DMS
     */
    public void setBaseQuestion(String baseQuestion) {
	this.baseQuestion = baseQuestion;
    }

    /**
     * Returns the validation api call for the slot value
     * 
     * @return api call to validate the slot value
     */
    public String getCheckAPICall() {
	return checkAPICall;
    }

    /**
     * Sets the validation api call for the slot value
     * 
     * @param api call to validate the slot value
     */
    public void setCheckAPI_Call(String APICall) {
	checkAPICall = APICall;
    }

    /**
     * Returns the api call to retrieve predefined list of possible slot values
     * 
     * @return api call 
     */
    public String getInfoAPICall() {
	return infoAPICall;
    }

    /**
     * Sets the api call to retrieve predefined list of possible slot values
     * 
     * @param api call
     */
    public void setInfoAPICall(String infoAPICall) {
	this.infoAPICall = infoAPICall;
    }

    /**
     * Returns the regex templates for slot filling
     * 
     * @return list of regex's separated by comma to identify the slot value
     */
    public String getRegex() {
	return regex;
    }

    /**
     * Sets the regex templates for slot filling
     * 
     * @param list of regex's separated by comma to identify the slot value
     */
    public void setRegex(String regex) {
	this.regex = regex;
    }

    /**
     * Returns the error message to be shown to user in case the filled value is invalid
     * 
     * @return invalid value error text
     */
    public String getErrorMessage() {
	return errorMessage;
    }

    /**
     * Sets the error message to be shown to user in case the filled value is invalid
     * 
     * @param invalid value error text
     */
    public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
    }

    /**
     * Returns the api call to retrieve extra information after validating the slot value
     * 
     * @return api call to retrieve additional info 
     */
    public String getExtraAPICall() {
	return extraAPICall;
    }

    /**
     * Sets the api call to retrieve extra information after validating the slot value
     * 
     * @param api call to retrieve additional info after validating the slot value
     */
    public void setExtraAPICall(String extraAPICall) {
	this.extraAPICall = extraAPICall;
    }

    /**
     * Returns the slot filling question to be asked to the user
     * 
     * @return machine utterance
     */
    public abstract String getMachineUtterance();

    /**
     * Extracts the slot value from the given user utterance. Returns the slot value if 
     * found, otherwise null
     * 
     * @param utterance
     * @return slot value
     */
    public abstract String matchUtterance(String utterance);

    /**
     * Matches the predefined regex templates with the user utterance to extract the slot
     * value. Returns the slot value if found, otherwise null
     * 
     * @param utterance
     * @return slot value
     */
    public abstract String matchRegex(String utterance);

    /**
     * Returns the prerequisite slot name
     * 
     * @return prerequisite slot
     */
    public abstract String getDependency();

    /**
     * Returns the list of possible values of a slot
     * @return the value list
     */
    public abstract String getValueSample();
    
}
