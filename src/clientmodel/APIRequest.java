package fastdial.clientmodel;

import org.apache.commons.lang3.EnumUtils;
import org.json.JSONObject;

import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handling the API Request from the middleware
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class APIRequest {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// dialogue session id sent by API calls
	private UUID session_id;

	// message type
	private MessageType message_type;

	// request state
	private RequestState state;

	// information type
	private String information_type;

	// request message
	private String message;

	// session language
	private String lang;

	/**
	 * Constructs an API request object with the received json from middleware
	 * 
	 * @param jsonObject
	 */
	public APIRequest(JSONObject jsonObject) {
		this.session_id = UUID.fromString(jsonObject.get("session_id").toString());

		if (EnumUtils.isValidEnum(MessageType.class,
				jsonObject.get("message_type").toString())) {
			this.message_type = MessageType
					.valueOf(jsonObject.get("message_type").toString());

			if (this.message_type == MessageType.QUERY_RESPONSE) {
				if (EnumUtils.isValidEnum(RequestState.class,
						jsonObject.get("state").toString())) {
					this.state = RequestState.valueOf(jsonObject.get("state").toString());
					this.message = "";

					if (jsonObject.has("information_type")
							&& !jsonObject.isNull("information_type"))
						this.information_type = jsonObject.getString("information_type")
						.toString();

					// message can be sent as a json object, not as a strict string here
					if (jsonObject.has("message") && !jsonObject.isNull("message"))
						this.message = jsonObject.get("message").toString();
				}
			} else if (this.message_type == MessageType.USER_UTTERANCE) {
				this.message = jsonObject.getString("message").toString();
			} else if (this.message_type == MessageType.BEGIN_SESSION) {
				this.lang = "English";
				this.message = "";

				if (!jsonObject.isNull("lang"))
					this.lang = jsonObject.getString("lang").toString();

				if (!jsonObject.isNull("message"))
					this.message = jsonObject.getString("message").toString();
			}
		}
	}

	/**
	 * Constructs an empty API request object 
	 * 
	 * @param jsonObject
	 */
	public APIRequest() {
		this.lang = "English";
		this.message = "";
	}

	/**
	 * Returns the session UUID
	 * 
	 * @return session_id
	 */
	public UUID getSessionId() {
		return session_id;
	}

	/**
	 * Sets the session UUID
	 * 
	 * @param sessionId
	 */
	public void setSessionId(UUID sessionId) {
		this.session_id = sessionId;
	}

	/**
	 * Returns the message type
	 * 
	 * @return message type
	 */
	public MessageType getMessage_type() {
		return message_type;
	}

	/**
	 * Sets the message type
	 * 
	 * @param message_type
	 */
	public void setMessage_type(MessageType message_type) {
		this.message_type = message_type;
	}

	/**
	 * Returns the request state
	 * 
	 * @return
	 */
	public RequestState getState() {
		return state;
	}

	/**
	 * Sets the request state
	 * 
	 * @param state
	 */
	public void setState(RequestState state) {
		this.state = state;
	}

	/**
	 * Returns the information type
	 * 
	 * @return information type
	 */
	public String getInformation_type() {
		return information_type;
	}

	/**
	 * Sets the information type
	 * 
	 * @param information_type
	 */
	public void setInformation_type(String information_type) {
		this.information_type = information_type;
	}

	/**
	 * Returns the request message
	 * 
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the request message
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the collection of variables of the execution response
	 * 
	 * @return value collection
	 */
	public Collection<Value> getExecutionVariables() {
		String m = message.replace("{", "").replace("}", "").replace("\\\"", "")
				.replace("\"", "");
		Collection<Value> vars = new HashSet<Value>();
		for (String v : m.split(","))
			vars.add(ValueFactory.create(v));
		return vars;
	}

	/**
	 * Returns the language value of the request
	 * 
	 * @return language
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Sets the language of the request
	 * 
	 * @param language
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

}
