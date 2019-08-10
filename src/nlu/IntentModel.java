package fastdial.nlu;

import java.util.logging.Logger;

/**
 * An intent object contains the intent name, intent file path, confirmation
 * flag and the call to be executed through api at the end of slot filling
 * phase.
 *
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 *
 */
public class IntentModel {

    // logger
    final static Logger log = Logger.getLogger("FastLogger");

    String name;
    String filePath;
    Boolean confirmation;
    String executionCall;

    /**
     * IntentModel constructor
     * 
     * @param name
     * @param filePath
     * @param confirmation
     * @param executionCall
     */
    public IntentModel(String name, String filePath, Boolean confirmation,
	    String executionCall) {
	super();
	this.name = name;
	this.filePath = filePath;
	this.confirmation = confirmation;
	this.executionCall = executionCall;
    }

    /**
     * Returns intent name
     * 
     * @return intent name
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the intent name
     * 
     * @param name
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Returns intent file path
     * 
     * @return file path
     */
    public String getFilePath() {
	return filePath;
    }

    /**
     * Sets the intent file path
     * 
     * @param filePath
     */
    public void setFilePath(String filePath) {
	this.filePath = filePath;
    }

    /**
     * Returns true if the intent requires a confirmation step to be executed, else 
     * returns false
     * 
     * @return confirmation step
     */
    public Boolean getConfirmation() {
	return confirmation;
    }

    /**
     * Sets the confirmation step necessity
     * 
     * @param confirmation
     */
    public void setConfirmation(Boolean confirmation) {
	this.confirmation = confirmation;
    }

    /**
     * Returns the execution api call
     * 
     * @return api call
     */
    public String getExecutionCall() {
	return executionCall;
    }

    /**
     * Sets the api call for intent execution
     * 
     * @param executionCall
     */
    public void setExecutionCall(String executionCall) {
	this.executionCall = executionCall;
    }
}
