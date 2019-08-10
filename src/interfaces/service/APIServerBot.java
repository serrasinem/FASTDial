package fastdial.interfaces.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import fastdial.clientmodel.APIRequest;
import fastdial.clientmodel.APIResponse;
import fastdial.clientmodel.MessageType;
import fastdial.clientmodel.RequestState;
import fastdial.clientmodel.ResponseMessageType;
import fastdial.dialoguepolicy.PolicyManager;
import fastdial.dialoguepolicy.ResponseReady;
import opendial.DialogueSystem;
import opendial.bn.values.SetVal;
import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;

/**
 * DMS Bot implementation for the api server interface
 *
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 *
 */
public class APIServerBot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// list of concurrent user sessions
	private Session chatList;

	/**
	 * Constructor of api server dms bot
	 */
	public APIServerBot() {
		this.chatList = new Session();
	}

	/**
	 * Returns the json object of the api response
	 * 
	 * @param update
	 * @return api response json object
	 */
	public JSONObject onUpdateReceived(APIRequest update) {
		UUID id = update.getSessionId();
		if (update.getMessage_type() == MessageType.BEGIN_SESSION) {
			return initSession(id, update);
		}
		// We check if the update has a message and the message has text
		if (this.chatList.isActive(id))
			if (update.getMessage_type() == MessageType.USER_UTTERANCE) {
				return sendMessage(id, update);
			} else if (update.getMessage_type() == (MessageType.QUERY_RESPONSE)) {
				return sendInformation(id, update);
			}

		JSONObject o = new JSONObject();
		return o;
	}

	/**
	 * Initializes the dialogue session. If message in the update object contains
	 * disable_auth, the dms will directly activates the intent identification, otherwise,
	 * the user should first conclude the authentication intent
	 * 
	 * @param id UUID chat id 
	 * @param update
	 * @return json response
	 */
	public JSONObject initSession(UUID id, APIRequest update) {
		String language = update.getLang().toLowerCase();
		String a = update.getMessage().toString().toLowerCase();
		String output = "";
		Boolean auth = true;

		if (a != null && a.contains("disable_auth"))
			auth = false;

		output = this.chatList.addUser(id, language, auth);

		JSONObject o = new JSONObject();
		o.put("session_id", id);
		if (auth)
			o.put("intent", "Authentication");
		o.put("message_type", "MESSAGE");
		o.put("message", output);
		return o;
	}

	/**
	 * Handles the api call if the type is message
	 * 
	 * @param id UUID chat id
	 * @param update
	 * @return json object
	 */
	public JSONObject sendMessage(UUID id, APIRequest update) {
		String message_text = update.getMessage().toString().toLowerCase();

		if (this.chatList.isActive((id))) {

			DialogueSystem system = this.chatList.getSystem(id);
			ResponseReady RD = new ResponseReady();
			system.attachModule(RD);
			system.addUserInput(message_text);

			if (system.getModule(ResponseReady.class).getNotified()) {
				return handleResponseMessage(system, id,
						system.getModule(ResponseReady.class).getNotifiedVar());
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
				}
				if (system.getModule(ResponseReady.class).getNotified()) {
					return handleResponseMessage(system, id,
							system.getModule(ResponseReady.class).getNotifiedVar());
				}
			}
			system.detachModule(ResponseReady.class);

		}
		log.warning("No new api or u_m is arrived.");
		return new JSONObject();
	}

	/**
	 *  Handles the api call if the type is information
	 * 
	 * @param id UUID chat id
	 * @param update
	 * @return json object
	 */
	public JSONObject sendInformation(UUID id, APIRequest update) {
		Collection<Value> vals = new HashSet<Value>();

		if (this.chatList.isActive((id))) {
			DialogueSystem system = this.chatList.getSystem(id);
			PolicyManager b = system.getModule(PolicyManager.class);
			String info = update.getMessage();

			if (update.getState() == RequestState.EXECUTE_INTENT_SUCCESS) {
				vals.add(ValueFactory.create("response:success"));
				if (info != null) 
					vals.addAll(update.getExecutionVariables());
			} else if (update.getState() == RequestState.EXECUTE_INTENT_FAILED) 
				vals.add(ValueFactory.create("response:error"));
			else if (update.getState() == RequestState.NOTIFY_INTENT_SUCCESS) 
				vals.add(ValueFactory.create("is_valid:true"));
			else if (update.getState() == RequestState.NOTIFY_INTENT_FAILED) 
				vals.add(ValueFactory.create("is_valid:false"));
			else if (update.getState() == RequestState.VALIDATION_SUCCESS) {
				vals.add(ValueFactory.create("is_valid:true"));
				if (update.getMessage() != null && !update.getMessage().equals("")) {
					vals.add(ValueFactory.create("_slot_:" + update.getMessage()));
				}

			} else if (update.getState() == RequestState.VALIDATION_FAILED) {
				vals.add(ValueFactory.create("is_valid:false"));
				vals.add(ValueFactory.create("error_code:not_found"));
			} else if (update.getState() == RequestState.QUERY_SUCCESS) {
				vals.add(ValueFactory.create("response:success"));
				String information_type = update.getInformation_type();
				vals.add(ValueFactory.create("{" + information_type + ":" + info + "}"));
			} else if (update.getState() == RequestState.QUERY_FAILED) 
				vals.add(ValueFactory.create("response:error"));

			ResponseReady RD = new ResponseReady();
			system.attachModule(RD);
			SetVal o = ValueFactory.create(vals);
			system.addContent("api_r", o);

			if (system.getModule(ResponseReady.class).getNotified()) {
				return handleResponseMessage(system, id,
						system.getModule(ResponseReady.class).getNotifiedVar());
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
				}
				if (system.getModule(ResponseReady.class).getNotified()) {
					return handleResponseMessage(system, id,
							system.getModule(ResponseReady.class).getNotifiedVar());
				}
			}

			system.detachModule(ResponseReady.class);
		}

		APIResponse a = new APIResponse();
		a.setSession_id(id);
		a.setMessage("Error: Please check the json request for the specific intent. "
				+ "The session will be destroyed.");
		log.log(Level.SEVERE, "Please check the json request for the specific intent." );
		return a.toJSON();
	}

	/**
	 * Sets the json object of the response
	 * 
	 * @param system
	 * @param id
	 * @param notified
	 * @return json object
	 */
	public JSONObject handleResponseMessage(DialogueSystem system, UUID id,
			String notified) {
		APIResponse a = new APIResponse();
		String api_output = system.getContent("api").getBest().toString();
		if (notified.equals("")) {
			system.detachModule(ResponseReady.class);
			a.setMessage("Error: Please check the json request for the specific intent. "
					+ "The session will be destroyed.");
			log.log(Level.SEVERE, "Please check the json request for the specific intent.");
			return a.toJSON();
		}
		if (notified.equals("api") && api_output != null && !api_output.equals("None")) {
			String intent = system.getContent("Intent").getBest().toString();
			if (api_output.contains("ValidateService")
					|| api_output.contains("InitialValidation")) {
				a.setMessage_type(ResponseMessageType.VALIDATION_QUERY);
				String[] apis = api_output.split(":");
				a.setInformation_type(apis[1]);
				a.setMessage(apis[2]);
			} else if (api_output.contains("Execute")
					|| api_output.contains("Authentication")) {
				a.setMessage_type(ResponseMessageType.EXECUTE_INTENT);
			} else if (api_output.contains("InitialInfoService")
					|| api_output.contains("InfoService")) {
				a.setMessage_type(ResponseMessageType.KB_QUERY);
				String[] apis = api_output.split(":");
				a.setMessage(apis[1]);
			} else if (api_output.contains("InformIntent")) {
				a.setMessage_type(ResponseMessageType.NOTIFY_INTENT);
				intent = api_output.split(":")[1];
				a.setMessage("intent_check");
			}

			a.setIntent(intent);
			a.setSession_id(id);

		} else {
			String output = system.getContent("u_m").getBest().toString();
			a.setSession_id(id);
			a.setMessage_type(ResponseMessageType.MACHINE_UTTERANCE);
			a.setMessage(output);
		}

		this.chatList.updateUser(id, system);

		String step = system.getContent("current_step").getBest().toString();
		if (step.equals("Close")) {
			this.chatList.disactivate(id);
			a.setMessage_type(ResponseMessageType.END_SESSION);
			a.setSession_id(id);
		} else if (step.contains("CloseWithHelpline")) {
			this.chatList.disactivate(id);
			a.setMessage_type(ResponseMessageType.HELPLINE);
			a.setSession_id(id);
		}

		system.detachModule(ResponseReady.class);
		return a.toJSON();
	}
}
