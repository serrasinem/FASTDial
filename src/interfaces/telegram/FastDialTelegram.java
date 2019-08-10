package fastdial.interfaces.telegram;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import fastdial.FastProperties;
import fastdial.clientmodel.APIRequest;
import fastdial.clientmodel.APIResponse;
import fastdial.clientmodel.MessageType;
import fastdial.clientmodel.RequestState;
import fastdial.clientmodel.ResponseMessageType;
import fastdial.dialoguepolicy.PolicyManager;
import fastdial.dialoguepolicy.ResponseReady;
import fastdial.interfaces.service.Session;
import opendial.DialogueSystem;
import opendial.bn.values.SetVal;
import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;

/**
 * Telegram interface of FASTDial
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class FastDialTelegram extends TelegramLongPollingBot {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");
	
	// dialogue session
	private Session session;
	
	// properties
	FastProperties properties = new FastProperties();

	/**
	 * Sets the session
	 * 
	 * @param activelist
	 */
	public FastDialTelegram(Session s) {
		this.session = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.telegram.telegrambots.generics.LongPollingBot#onUpdateReceived(org.
	 * telegram.telegrambots.api.objects.Update)
	 */
	@Override
	public void onUpdateReceived(Update update) {
		UUID uuid = UUID.nameUUIDFromBytes(update.getMessage().getChatId().toString().getBytes());
		Long id = update.getMessage().getChatId();

		if (update.getMessage().getText().toLowerCase().contains("/start")) {
			APIRequest request = convertToInitAPI(update);
			JSONObject output = initSession(uuid, request);
			SendMessage message = new SendMessage() // Create a message object 
					.setChatId(id).setText(output.getString("message"));
			try {
				execute(message); // Sending our message object to user
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}

		// We check if the update has a message and the message has text
		else if (this.session.isActive(uuid)) {
			APIRequest request = convertToMessageAPI(update);

			if (this.session.isActive(uuid)) {
				JSONObject output = new JSONObject();

				if (request.getMessage_type() == MessageType.USER_UTTERANCE)
					output = sendMessage(uuid, request);

				SendMessage message = new SendMessage() // Create a message object
						.setChatId(id).setText(output.getString("message"));
				try {
					execute(message); // Sending our message object to user
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * Converts Telegram update to begin api request
	 * 
	 * @param update
	 * @return api request
	 */
	private APIRequest convertToInitAPI(Update update) {
		// dialogue session id sent by API calls
		UUID uuid = UUID.nameUUIDFromBytes(update.getMessage().getChatId().toString().getBytes());
		String m = update.getMessage().getText().toLowerCase();
		MessageType messageType = MessageType.BEGIN_SESSION;
		String lang = "english";
		if (m.contains("italian"))
			lang = "italian";

		String message = "disable_auth";
		APIRequest a = new APIRequest();
		a.setSessionId(uuid);
		a.setLang(lang);
		a.setMessage(message);
		a.setMessage_type(messageType);

		return a;
	}

	/**
	 * Converts Telegram update to message api request
	 * 
	 * @param update
	 * @return api request
	 */
	private APIRequest convertToMessageAPI(Update update) {
		// dialogue session id sent by API calls
		UUID uuid = UUID.nameUUIDFromBytes(update.getMessage().getChatId().toString().getBytes());
		String m = update.getMessage().getText().toLowerCase();
		MessageType messageType = MessageType.USER_UTTERANCE;
		APIRequest a = new APIRequest();
		a.setSessionId(uuid);
		a.setMessage(m);
		a.setMessage_type(messageType);
		return a;
	}

	/**
	 * Initializes the dialogue session. If message in the update object contains
	 * disable_auth, the dms will directly activates the intent identification,
	 * otherwise, the user should first conclude the authentication intent
	 * 
	 * @param uuid session id
	 * @param update APIRequet
	 * @return response json object
	 */
	public JSONObject initSession(UUID uuid, APIRequest update) {
		String language = update.getLang().toLowerCase();
		String a = update.getMessage().toString().toLowerCase();
		a = "disable_auth";
		String output = "";
		Boolean authentication = true;

		if (a != null && a.contains("disable_auth"))
			authentication = false;

		output = this.session.addUser(uuid, language, authentication);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("session_id", uuid);
		if (authentication)
			jsonObject.put("intent", "Authentication");
		jsonObject.put("message_type", "MESSAGE");
		jsonObject.put("message", output);
		return jsonObject;
	}

	/**
	 * Handles the api call if the type is message
	 * 
	 * @param uuid session id
	 * @param update APIRequest 
	 * @return json object
	 */
	public JSONObject sendMessage(UUID uuid, APIRequest update) {
		String utterance = update.getMessage().toString().toLowerCase();
		DialogueSystem system = this.session.getSystem(uuid);
		ResponseReady RD = new ResponseReady();
		system.attachModule(RD);
		system.addUserInput(utterance);
		PolicyManager p = system.getModule(PolicyManager.class);
		if (system.getModule(ResponseReady.class).getNotified()) {
			return handleResponseMessage(system, uuid, system.getModule(ResponseReady.class).getNotifiedVar(),
					p.getStateVariables(), p.getSlotValues());
		} else {
			// sleep a very short amount of time to re-check the DMS trigger
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
			}
			if (system.getModule(ResponseReady.class).getNotified()) {
				return handleResponseMessage(system, uuid, system.getModule(ResponseReady.class).getNotifiedVar(),
						p.getStateVariables(), p.getSlotValues());
			}
		}
		system.detachModule(ResponseReady.class);
		return new JSONObject();
	}

	/**
	 * Handles the api call if the type is information
	 * 
	 * @param uuid session id
	 * @param update request update
	 * @return json object
	 */
	public JSONObject sendInformation(UUID uuid, APIRequest update) {
		Collection<Value> vals = new HashSet<Value>();

		if (this.session.isActive((uuid))) {
			DialogueSystem system = this.session.getSystem(uuid);

			String info = update.getMessage();
			if (update.getState() == RequestState.EXECUTE_INTENT_SUCCESS) {
				vals.add(ValueFactory.create("response:success"));
				if (info != null) 
					vals.addAll(update.getExecutionVariables());

			} else if (update.getState() == RequestState.EXECUTE_INTENT_FAILED) {
				vals.add(ValueFactory.create("response:error"));
			} else if (update.getState() == RequestState.NOTIFY_INTENT_SUCCESS) {
				vals.add(ValueFactory.create("is_valid:true"));
			} else if (update.getState() == RequestState.NOTIFY_INTENT_FAILED) {
				vals.add(ValueFactory.create("is_valid:false"));
			} else if (update.getState() == RequestState.VALIDATION_SUCCESS) {
				vals.add(ValueFactory.create("is_valid:true"));
				if (update.getMessage() != null && !update.getMessage().equals("")) 
					vals.add(ValueFactory.create("_slot_:" + update.getMessage()));
			} else if (update.getState() == RequestState.VALIDATION_FAILED) {
				vals.add(ValueFactory.create("is_valid:false"));
				if (info.contains("error_code"))
					vals.add(ValueFactory.create(info));
				else
					vals.add(ValueFactory.create("error_code:not_found"));

			} else if (update.getState() == RequestState.QUERY_SUCCESS) {
				vals.add(ValueFactory.create("response:success"));
				String information_type = update.getInformation_type();
				vals.add(ValueFactory.create("{" + information_type + ":" + info + "}"));
			} else if (update.getState() == RequestState.QUERY_FAILED) {
				vals.add(ValueFactory.create("response:error"));
			}

			ResponseReady RD = new ResponseReady();
			system.attachModule(RD);
			SetVal o = ValueFactory.create(vals);
			system.addContent("api_r", o);

			PolicyManager p = system.getModule(PolicyManager.class);
			Map<String, Value> slotValues = p.getSlotValues();
			if (system.getModule(ResponseReady.class).getNotified()) {
				return handleResponseMessage(system, uuid, system.getModule(ResponseReady.class).getNotifiedVar(),
						p.getStateVariables(), slotValues);
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
				}
				if (system.getModule(ResponseReady.class).getNotified()) {
					return handleResponseMessage(system, uuid,
							system.getModule(ResponseReady.class).getNotifiedVar(), p.getStateVariables(),
							slotValues);
				}
			}
			system.detachModule(ResponseReady.class);
		}

		APIResponse a = new APIResponse();
		a.setSession_id(uuid);
		a.setMessage("Error: DMS cannot generate an answer.");
		return a.toJSON();
	}

	/**
	 * Sets the json object of the response
	 * 
	 * @param system 
	 * @param uuid
	 * @param notified
	 * @param stateVariables
	 * @param slotValuesalues
	 * @return json object
	 */
	public JSONObject handleResponseMessage(DialogueSystem system, UUID uuid, String notified,
			HashMap<String, String> stateVariables, Map<String, Value> slotValues) {
		APIResponse a = new APIResponse();
		system.detachModule(ResponseReady.class);

		if (notified.equals("")) {
			a.setMessage("Error: DMS cannot generate an answer.");
			return a.toJSON();
		}
		if (notified.equals("api")) {
			String api = system.getContent("api").getBest().toString();
			if (api != null && !api.equals("None")) {
				String intent = system.getContent("Intent").getBest().toString();
				if (api.contains("ValidateService") || api.contains("InitialValidation")) {
					a.setMessage_type(ResponseMessageType.VALIDATION_QUERY);
					String[] apis = api.split(":");
					a.setInformation_type(apis[1]);
					a.setMessage(apis[2]);
				} else if (api.contains("Execute") || api.contains("Authentication")) {
					a.setMessage_type(ResponseMessageType.EXECUTE_INTENT);
				} else if (api.contains("InitialInfoService") || api.contains("InfoService")) {
					a.setMessage_type(ResponseMessageType.KB_QUERY);
					String[] apis = api.split(":");
					a.setMessage(apis[1]);
				} else if (api.contains("InformIntent")) {
					a.setMessage_type(ResponseMessageType.NOTIFY_INTENT);
					intent = api.split(":")[1];
					a.setMessage("intent_check");
				}

				a.setIntent(intent);
				a.setSession_id(uuid);
				APIRequest r = MidwareSimulation.callAPI(a, stateVariables, slotValues);
				this.session.updateUser(uuid, system);
				return sendInformation(uuid, r);
			}
		} else {
			String output = system.getContent("u_m").getBest().toString();
			a.setSession_id(uuid);
			a.setMessage_type(ResponseMessageType.MACHINE_UTTERANCE);
			a.setMessage(output);
		}

		this.session.updateUser(uuid, system);

		String step = system.getContent("current_step").getBest().toString();
		if (step.equals("Close")) {
			this.session.disactivate(uuid);
			a.setMessage_type(ResponseMessageType.END_SESSION);
			a.setSession_id(uuid);
		} else if (step.contains("CloseWithHelpline")) {
			this.session.disactivate(uuid);
			a.setMessage_type(ResponseMessageType.HELPLINE);
			a.setSession_id(uuid);
		}
		return a.toJSON();
	}

    /* (non-Javadoc)
     * 
     * @see org.telegram.telegrambots.generics.LongPollingBot#getBotUsername()
     */
	@Override
	public String getBotUsername() {
		return properties.getProperty("telegramBotUsername");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.telegram.telegrambots.bots.DefaultAbsSender#getBotToken()
	 */
	@Override
	public String getBotToken() {
		return properties.getProperty("telegramBotToken");
	}
}
