package fastdial.clientmodel;

import java.util.UUID;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * API response to be sent back to the midware. There can be 3 main types of response:
 * 1- An API call ( validation, information requests, or intent execution )
 * 2- A machine utterance
 * 3- Closing the session ( simple closing or transfering to helpline )
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class APIResponse {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// session id
	UUID session_id; 

	// message type 
	ResponseMessageType message_type;

	// message as a machine utterance or request values
	String message;

	// intent type
	String intent;

	// information type
	String information_type;

	/**
	 * Returns the session ID 
	 * @return session_id
	 */
	public UUID getSession_id() {
		return session_id;
	}

	/**
	 * Sets the session ID
	 * 
	 * @param session_id
	 */
	public void setSession_id(UUID session_id) {
		this.session_id = session_id;
	}

	/**
	 * Returns the response message type
	 * 
	 * @return message_type
	 */
	public ResponseMessageType getMessage_type() {
		return message_type;
	}

	/**
	 * Sets the response message type
	 * 
	 * @param message_type
	 */
	public void setMessage_type(ResponseMessageType message_type) {
		this.message_type = message_type;
	}

	/**
	 * Returns the response message
	 * 
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the response message
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the intent type
	 * 
	 * @return intent tyoe
	 */
	public String getIntent() {
		return intent;
	}

	/**
	 * Sets the intent type
	 * 
	 * @param intent
	 */
	public void setIntent(String intent) {
		this.intent = intent;
	}

	/**
	 * Returns the information type
	 * 
	 * @return information_type
	 */
	public String getInformation_type() {
		return information_type;
	}

	/**
	 * Sets the response information type
	 * 
	 * @param information_type
	 */
	public void setInformation_type(String information_type) {
		this.information_type = information_type;
	}

	/**
	 * Converts API response object to JSON object
	 * 
	 * @return response
	 */
	public JSONObject toJSON() {
		JSONObject jsonObject = new JSONObject();

		if(message_type != null)
			jsonObject.put("message_type", message_type);
		if(information_type != null)
			jsonObject.put("information_type", information_type);
		if(message != null)
			jsonObject.put("message", message);
		if(intent != null)
			jsonObject.put("intent", intent);

		jsonObject.put("session_id", session_id);

		return jsonObject;
	}


}
