package fastdial.dialoguepolicy;

import java.util.logging.*;

import fastdial.FastProperties;
import fastdial.clientmodel.MidwareResponse;
import fastdial.nlu.IntentModel;
import fastdial.nlu.SlotTracker;
import fastdial.nlu.IntentClassifier;
import fastdial.slots.SlotState;
import fastdial.slots.StringListSlot;
import fastdial.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import opendial.DialogueState;
import opendial.DialogueSystem;
import opendial.bn.values.StringVal;
import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;
import opendial.datastructs.Assignment;
import opendial.modules.Module;
import opendial.templates.Template;


/**
 * The policy manager of FASTDial. Main listener for the triggers such as u_m, a_m, u_u,
 * api, api_r, etc. FASTDial creates either u_m or api to be handled by any user interface
 * and should be received u_u or api_r to continue to the next step
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class PolicyManager implements Module {

	// logger
	public final static Logger log = Logger.getLogger("FastLogger");

	// the dialogue system
	DialogueSystem system;

	// whether the module is paused or active
	boolean paused = true;

	// whether the user authentication is active 
	boolean authenticationActive = true;

	// whether the user is authenticated
	boolean userAuthenticated = false;

	// current intent variables 
	private Set<String> stateVariables = new HashSet<String>();

	// list of slots and values to be validated
	private HashMap<Integer, String> validationNeeds = new HashMap<Integer, String>();

	// session language
	String language = "";

	// nlu instance
	IntentClassifier uu;

	// active intent in the session
	SlotTracker activeIntent;

	// active slot id in the active intent during slot value filling
	private Integer activeSlotID = 0;

	// active slot id during not slot value filling but slot info, constraint filling
	private Integer infoSlotID = 0;

	FastProperties properties;

	public static int MAX_REPEAT = 3;

	/**
	 * Creates a new instance of the FASTDial module
	 * 
	 * @param system the dialogue system to which the module should be attached
	 * @param auth true if an authentication is required
	 */
	public PolicyManager(DialogueSystem system, Boolean auth) {
		this.authenticationActive = auth;
		this.system = system;
		this.language = system.getContent("lang").getBest().toString();
		uu = new IntentClassifier(this.language);
		this.properties = new FastProperties();
	}

	/**
	 * Starts the module.
	 */
	@Override
	public void start() {
		// If you do not want to keep track of any previous dialogue, uncomment 
		//the following line system.detachModule(DialogueRecorder.class);
		paused = false;
	}

	/**
	 * Handles the state update
	 * 
	 * @param state the current dialogue state
	 * @param u the updated variables in the state
	 */
	@Override
	public void trigger(DialogueState state, Collection<String> u) {

		String current = state.queryProb("current_step").getBest().toString();
		if (authenticationActive && current.equals("StartAuthentication")
				&& !userAuthenticated) {
			handleAuthentication(state);
		}

		if (!authenticationActive && current.equals("StartAuthentication")
				&& !userAuthenticated) {
			system.addContent("current_step", new StringVal("Init"));
		}
		// If a new Utterance from the user arrives
		else if (u.contains("u_u") && state.hasChanceNode("u_u")) {
			String utterance = state.queryProb("u_u").getBest().toString().trim()
					.toLowerCase();
			log.info("Utterance:" + utterance);
			handleUserUtterance(utterance, current, state);
		}
		// If a new API Response from the middleware arrives
		else if (u.contains("api_r") && state.hasChanceNode("api_r")) {
			handleAPIResponse(current, state);
			
		} else if (u.contains("a_m") && state.hasChanceNode("a_m")) {
			String action = state.queryProb("a_m").getBest().toString();
			handleA_M(action, state, current);
		}

		if (u.contains("AskRepeat") && state.hasChanceNode("AskRepeat")) {
			String action = state.queryProb("AskRepeat").getBest().toString();
			if (action.toLowerCase().equals(String.valueOf(MAX_REPEAT + 1)))
				directHelpLine();
		}
	}

	/**
	 * Handles the state transition with respect to the new user utterance
	 * 
	 * @param utterance  user utterance
	 * @param currrent current dialogue step
	 * @param state dialogue state
	 */
	private void handleUserUtterance(String utterance, String current,
			DialogueState state) {
		if (utterance.equals("help")) {
			directHelpLine();
		}
		// Finding a new user intent from the user utterance
		else if (current.equals("Init")) {
			IntentModel intentType = uu.classifyIntentWitModel(utterance);
			informIntent(intentType);
		}
		// Depending on the state, creating a machine utterance or api call
		else if (!current.equals("ConfirmIntent") && !current.equals("Final")
				&& !current.equals("ConfirmSlotEdit")) {
			utteranceOrAPI(utterance, current, state);
		}
		// Final step and if there is an intent already, let's restart.
		else if (current.equals("Final")) {
			IntentModel intentType = uu.classifyIntentWitModel(utterance);
			if (intentType != null) {
				system.addContent("a_m", new StringVal("Ground(Restart)"));
			}
		}
	}

	/**
	 * Validates the intent extracted from the first user utterance. Either creates an
	 * api call informing the found intent or a repeat request to the user
	 * 
	 * @param intentInfo intent information
	 */
	private void informIntent(IntentModel intent) {
		activeSlotID = 0;

		if (intent != null) 
			system.addContent("api", "InformIntent:" + intent.getName());
		else {
			// When we did not understand the intent
			if (system.getContent("AskRepeat").getBest().toString()
					.equals(String.valueOf(MAX_REPEAT)))
				directHelpLine();
			system.addContent("a_u", new StringVal("Other"));
		}
	}

	/**
	 * Handles the state either through the user utterance path or the api call path
	 * 
	 * @param utterance latest user utterance
	 * @param current step
	 * @param state dialogue state
	 */
	private void utteranceOrAPI(String utterance, String current, DialogueState state) {
		String filler = activeIntent.fillSlot(utterance, activeSlotID);
		// filler is apiName:detectedValue;
		
		if (filler == null || filler.equals("null")) 
			handleSlotPolicy(current, state);
		else if(filler.contains("directHelpLine"))
			directHelpLine();
		else if(filler.contains("cancelIntent"))
			system.addContent("a_m", new StringVal("Ground(Cancel)"));
		else {
			if (activeIntent.isValidationRequired(activeSlotID)) 
				system.addContent("api", "ValidateService:" + filler);
			else if(activeIntent.getSlot(activeSlotID).getExtraAPICall() != null) {
				setValidSlot(filler);
				system.addContent("api", "InfoService:"
						+ activeIntent.getSlot(activeSlotID).getExtraAPICall());
			}
			else {
				setValidSlot(filler);
				handleSlotPolicy(current, state);
			}
		}
	}

	/**
	 * Sets the slot state as valid with the slot filling value
	 * 
	 * @param value
	 */
	private void setValidSlot(String value) {
		HashMap<String, String> vals = new HashMap<String, String>();
		vals.put("is_valid", "true");
		activeIntent.setSlotState(activeSlotID, value.split(":")[1], vals, false);
		system.addContent(activeIntent.getSlot(activeSlotID).getSlotName(),
				ValueFactory.create(value.split(":")[1]));
	}

	/**
	 * Handles the state transition with respect to the API response and the current step
	 * 
	 * @param current current step
	 * @param state dialogue state
	 */
	private void handleAPIResponse(String current, DialogueState state) {
		Collection<Value> api_r = state.queryProb("api_r").getBest().getSubValues();
		MidwareResponse response = new MidwareResponse(api_r);

		// Get and remove the api request from the DS (Dialogue System)
		String api = system.getContent("api").getBest().toString();
		system.removeContent("api");

		String[] apiParsed = api.split(":");
		String currentapi = apiParsed[0];

		// When the intent execution or authentication response arrives
		if (currentapi.equals("Execute") || currentapi.equals("Authentication")) {
			response.variables.put("ErrorMessage", "");
			setVariables(response.variables);
			changeToFinalState(state);
		}
		// When a response for a dms query arrives
		else if (currentapi.equals("InfoService")) {
			setVariables(response.variables);
			handleSlotPolicy(current, state);
		}
		// When a response for an initial dms query arrives before slot filling steps
		else if (currentapi.equals("InitialInfoService")) {
			if (response.success) {
				if(response.variables.get(apiParsed[1]) != null) {
					activeIntent.setSlotKeys(infoSlotID, response.parseKeyList(apiParsed[1]),
							Boolean.parseBoolean(
									properties.getProperty("prefillSingleOption")));
					String rematch = activeIntent.rematchIntentUtterance(
							state.queryProb("u_u").getBest().toString().trim().toLowerCase(),
							infoSlotID);
					if (rematch != null) 
						validationNeeds.put(infoSlotID, rematch);
				}
				setVariables(response.variables);
				// If there is need for another info, call API, otherwise start the dialogue
				Integer nextInfo = getNextInitialAPI();
				if (nextInfo != null) {
					infoSlotID = nextInfo;
					system.addContent("api", "InitialInfoService:"
							+ activeIntent.getSlot(nextInfo).getInfoAPICall());
				} else {
					infoSlotID = 0;
					startInitialSlotFilling(state);
				}
			} else
				// There was a mistake in the call, retry the call by api
				system.addContent("api", "InitialInfoService:"
						+ activeIntent.getSlot(activeSlotID).getInfoAPICall());

		}

		// When the slot keys for a dependent slot arrive during slot filling
		else if (currentapi.equals("callDependentInfoService")) {
			if (response.success)
				activeIntent.setSlotKeys(infoSlotID, response.parseKeyList(apiParsed[1]), 
						Boolean.parseBoolean(
								properties.getProperty("prefillSingleOption")));
			else
				// Calling the same API again. We may change this behavior to cancel the
				// intent.
				system.addContent("api", "callDependentInfoService:"
						+ activeIntent.getSlot(activeSlotID).getInfoAPICall());

			handleSlotPolicy(current, state);
		}
		// When a validation response arrives after slot filling
		else if (currentapi.equals("ValidateService")) {
			activeIntent.setSlotState(activeSlotID, response);
			// Set the value to the system
			if (response.success)
				system.addContent(activeIntent.getSlot(activeSlotID).getSlotName(),
						response.validatedVal);
			// if a post slot filling info call is necessary
			if (response.success
					&& activeIntent.getSlot(activeSlotID).getExtraAPICall() != null)
				system.addContent("api", "InfoService:"
						+ activeIntent.getSlot(activeSlotID).getExtraAPICall());
			else 
				handleSlotPolicy(current, state);
		}

		// When slot validation responses arrive for the user initiated slot values at
		// the intent state
		else if (currentapi.equals("InitialValidation")) {
			activeIntent.setSlotState(activeSlotID, response);
			if (response.success
					&& activeIntent.getSlot(activeSlotID).getExtraAPICall() != null)
				system.addContent("api", "InitialInfoService:"
						+ activeIntent.getSlot(activeSlotID).getExtraAPICall());
			else
				handleInitialValidation(state);

		}
		// When intent validation response arrives
		else if (currentapi.equals("InformIntent")) {
			String utterance = state.queryProb("u_u").getBest().toString().toLowerCase();
			IntentModel intentType = uu.classifyIntentWitModel(utterance);
			activeSlotID = 0;
			if (response.success)
				handleDiscoveredIntent(intentType, state);
			else {
				if (system.getContent("AskRepeat").getBest().toString()
						.equals(String.valueOf(MAX_REPEAT)))
					directHelpLine();
				system.addContent("a_u", new StringVal("Other"));
			}
		}
	}

	/**
	 * Returns the slot index of the next information api call
	 * 
	 * @return slot index
	 */
	private Integer getNextInitialAPI() {
		Integer nextInfo = activeIntent.getNextInitialAPI();
		while (nextInfo != null) {
			if (!isDependencyFulfilled(nextInfo)) {
				nextInfo = activeIntent.getNextInitialAPI();
				continue;
			}
			break;
		}

		return nextInfo;
	}

	/**
	 * Checks if the prerequisite slot of the given slot id has already been filled.
	 * 
	 * @param nextInfo slot id that has been checked for its dependency
	 * @return true if dependency is fulfilled
	 */
	private boolean isDependencyFulfilled(Integer nextInfo) {
		String dependency = activeIntent.getSlot(nextInfo).getDependency();
		if (dependency == null|| dependency.equals(""))
			return true;

		if (system.getState().hasChanceNode(dependency))
			return true;

		return false;
	}

	/**
	 * Checks if the prerequisite slot of the given slot id has been skipped.
	 * 
	 * @param id
	 * @return true if the prerequisite slot is skipped
	 */
	private boolean isDependencySkipped(Integer id) {
		String dependency = activeIntent.getSlot(id).getDependency();
		if (dependency == null|| dependency.equals(""))
			return false;
		Integer d = activeIntent.findSlot(dependency);
		if (activeIntent.getSlotState(d).getSkipped())
			return true;

		return false;
	}

	/**
	 * Attempts to fill all intent slots using the intent utterance
	 * 
	 * @param state dialogue state
	 */
	private void startInitialSlotFilling(DialogueState state) {
		String utterance = system.getContent("u_u").getBest().toString().toLowerCase();
		int filled = activeIntent.getNumberOfFilledSlots();
		if (filled == 0)
			validationNeeds = activeIntent.fillAllPossibleSlots(utterance);
		
		handleInitialValidation(state);

	}

	/**
	 * Takes the action w.r.t. the required validations before the dialogue starts
	 * 
	 * @param state dialogue state
	 */
	private void handleInitialValidation(DialogueState state) {
		if (validationNeeds.isEmpty()) {
			HashMap<String, String> values = activeIntent.getVariables();
			removeIntentVariables();
			setVariables(values);
			activeSlotID = 0;
			setStateToActiveIntent(state);
		} else {
			activeSlotID = validationNeeds.keySet().iterator().next();
			String detected = validationNeeds.get(activeSlotID);
			validationNeeds.remove(activeSlotID);
			SlotState st = new SlotState();
			if(isDependencyFulfilled(activeSlotID)) {
				st.setValidated(false);
				}
			st.setValue(detected);
			activeIntent.setSlotState(activeSlotID, st);
	
			if (detected.equals("directHelpLine")) 
				directHelpLine();
			else if (detected.equals("cancelIntent")) 
				system.addContent("a_m", new StringVal("Ground(Cancel)"));
			else if (activeIntent.isValidationRequired(activeSlotID) && isDependencyFulfilled(activeSlotID)) {
				String check = activeIntent.getSlot(activeSlotID)
						.getCheckAPICall();
				system.addContent("api", "InitialValidation:" + check + ":"
						+ detected);
			}
			else 
				handleInitialValidation(state);
		}
	}

	/**
	 * Handles the state after detecting the user intent
	 * 
	 * @param intentInfo intent information
	 * @param state dialogue state
	 */
	private void handleDiscoveredIntent(IntentModel intent, DialogueState state) {
		system.addContent("Intent", ValueFactory.create(intent.getName()));
		activeIntent = uu.loadIntent(intent);
		Integer nextInfo = activeIntent.getNextInitialAPI();

		if (nextInfo != null && isDependencyFulfilled(nextInfo)) {
			Map<String, Value> pairs = new HashMap<String, Value>();
			infoSlotID = nextInfo;

			pairs.put("api", new StringVal("InitialInfoService:"
					+ activeIntent.getSlot(nextInfo).getInfoAPICall()));
			system.addContent(new Assignment(pairs));
		} else {
			infoSlotID = 0;
			startInitialSlotFilling(state);
		}
	}

	private void removeIntentVariables() {
		//activeIntent.removeVariable("is_valid");
		activeIntent.removeVariable("error_code");
	}
	/**
	 * Handles the policy among the slot states in the system
	 * 
	 * @param current current step name
	 * @param state dialogue state
	 */
	private void handleSlotPolicy(String current, DialogueState state) {
		HashMap<String, String> values = activeIntent.getVariables();
		// Error State Policy
		handleErrorState(values, current, state);



	}

	/**
	 * Handles the error states if found. Possible error cases are slot value cannot be 
	 * matched, the matched value is not correct, directing to help line, canceling an 
	 * intent
	 * 
	 * @param values
	 * @param current current step
	 * @param state
	 */
	private void handleErrorState(HashMap<String, String> values, String current, DialogueState state) {
		Boolean isValid = Boolean.valueOf(values.remove("is_valid"));
		// null: if api is not yet called 
		
		Boolean validated = activeIntent.isSlotValidated(activeSlotID);
		String translatedError = "";
		if (values.containsKey("error_code") )
			translatedError += translateErrorCode(values.get("error_code"), true);
		else if (stateVariables.contains("error_code"))
			translatedError += translateErrorCode(system.getContent("error_code").getBest().toString(), true);
		SlotState e = activeIntent.getSlotState(activeSlotID);
		if(e !=null && e.getErrorCode()!=null)
			translatedError += translateErrorCode(e.getErrorCode(), true);
		if (isSlotValueError(isValid, validated, values, current)) {
			setInvalidErrorState(translatedError, current);
		}
		else if ((!activeIntent.isIntentFilled() && !isValid && validated != null)
				|| (validated != null && !validated)) { // Slot Fixing Error Handling
			if (system.getContent("AskRepeat").getBest().toString()
					.equals(String.valueOf(MAX_REPEAT + 1)))
				directHelpLine();
			Map<String, Value> pairs = new HashMap<String, Value>();
			pairs.put("current_step", ValueFactory
					.create(activeIntent.getSlotType(activeSlotID)));
			pairs.put("ErrorMessage",
					ValueFactory.create(
							activeIntent.getSlot(activeSlotID).getErrorMessage()
							+ translatedError));
			pairs.put("ConstraintRequest", ValueFactory.create(translatedError));
			pairs.put("a_u", new StringVal("Other"));
			system.addContent(new Assignment(pairs));
		} else if (validated != null && validated
				&& activeIntent.getSlotValue(activeSlotID).equals("directHelpLine"))
			directHelpLine();
		else if (validated != null && validated
				&& activeIntent.getSlotValue(activeSlotID).equals("cancelIntent"))
			system.addContent("a_m", new StringVal("Ground(Cancel)"));
		else {
			// Valid State Policy
			setVariables(values);
			updateSlotID();
			removeIntentVariables();
			activeIntent.removeVariable("is_valid");
			handleValidState(current, state);
		}
	}

	/**
	 * If an error is not found, a valid state change is applied. Possible paths are
	 * intent grounding, handling the filled slot value in the intent level, calling an 
	 * api, and asking a new slot
	 * 
	 * @param values
	 * @param current current step
	 * @param state
	 */
	private void handleValidState(String current, DialogueState state) {
	
		if (!activeIntent.isIndexValid(activeSlotID) || activeIntent.isIntentFilled())
			groundIntent();
		 else if (activeIntent.getSlot(activeSlotID).getSlotType().equals("StringList")
					&& !((StringListSlot) activeIntent.getSlot(activeSlotID))
					.isKeylistSet()) { 
				// when the keylist is not set because of a prerequisite
				infoSlotID = activeSlotID;
				system.addContent("api", "callDependentInfoService:"
						+ activeIntent.getSlot(activeSlotID).getInfoAPICall());
			} 
		else if (activeIntent.isSlotValidated(activeSlotID) != null
				&& !activeIntent.isSlotValidated(activeSlotID)
				&& !current.equals("ConfirmSlotEdit")) {
			// user provided a slot value on the intent level
			System.out.println("user provided a slot");
			Map<String, Value> pairs = setMachineUtterance(state);
			system.addContent(new Assignment(pairs));
			utteranceOrAPI(activeIntent.getSlotValue(activeSlotID),
					activeIntent.getSlot(activeSlotID).getSlotType(), state);
		}
		else if (activeIntent.isSlotValidated(activeSlotID) == null
				&& activeIntent.getSlotValue(activeSlotID) !=null
				&& !current.equals("ConfirmSlotEdit")) {
			// user provided a slot value on the intent level
			Map<String, Value> pairs = setMachineUtterance(state);
			system.addContent(new Assignment(pairs));
			utteranceOrAPI(activeIntent.getSlotValue(activeSlotID),
					activeIntent.getSlot(activeSlotID).getSlotType(), state);
		}
		else {
			// ask new slot
			Map<String, Value> pairs = setMachineUtterance(state);
			pairs.put("current_step", ValueFactory
					.create(activeIntent.getSlot(activeSlotID).getSlotType()));
			system.addContent(new Assignment(pairs));
		}
	}

	/**
	 * Prepares the next slot requesting machine utterance with the variables in the
	 * state
	 * 
	 * @param state dialogue state
	 */
	private Map<String, Value> setMachineUtterance(DialogueState state) {
		String filledQuestion = fillVariables(
				activeIntent.getSlot(activeSlotID).getBaseQuestion(), state);
		Map<String, Value> pairs = new HashMap<String, Value>();
		pairs.put("AskRepeat", ValueFactory.create(0));
		pairs.put("MachineUtterance", ValueFactory.create(filledQuestion));
		return pairs;
	}

	/**
	 * Updates the slot id to be processed in the current state. If there is an
	 * invalid slot to be asked again, it will update the slot id as the invalid
	 * slot. If not, the active slot will be updated as 0.
	 */
	private void updateSlotID() {
		ArrayList<Integer> invalids = activeIntent.getInvalidSlots();
		activeSlotID = 0;
		if (!invalids.isEmpty()) {
			if (activeIntent.getSlot(invalids.get(0)).getDependency() == null)
				activeSlotID = invalids.get(0);
		}

		while (!activeIntent.isEmpty(activeSlotID)
				&& activeIntent.isIndexValid(activeSlotID))
			activeSlotID++;

		if (activeSlotID > 0
				&& activeIntent.getSlotType(activeSlotID - 1).equals("Confirmation")
				&& activeIntent.getSlotValue(activeSlotID - 1) != null
				&& activeIntent.getSlotValue(activeSlotID - 1).equals("skipNext")) {
			//&& !activeIntent.getSlotMandatory(activeSlotID)) {
			HashMap<String, String> v = new HashMap<String,String>();
			v.put("is_valid", "true");
			activeIntent.setSlotState(activeSlotID, "", v, true);
			activeSlotID++;
		}

		if(activeIntent.isIndexValid(activeSlotID) && isDependencySkipped(activeSlotID))
			activeSlotID++;
	}

	/**
	 * Handles the authentication procedure
	 * 
	 * @param state dialogue state
	 */
	private void handleAuthentication(DialogueState state) {
		IntentModel intent = null;
		language = system.getContent("lang").getBest().toString();
		if (language.toLowerCase().equals("italian"))
			intent = uu.classifyIntentWitModel("_authentication_it_");
		else
			intent = uu.classifyIntentWitModel("_authentication_");

		activeSlotID = 0;

		if (intent != null) {
			activeIntent = uu.loadIntent(intent);
			Map<String, Value> pairs = new HashMap<String, Value>();
			pairs.put("Intent", ValueFactory.create(intent.getName()));
			pairs.put("current_step", ValueFactory
					.create(activeIntent.getSlot(activeSlotID).getSlotType()));
			pairs.putAll(setMachineUtterance(state));
			system.addContent(new Assignment(pairs));
		}

	}

	/**
	 * Handles the machine action updates
	 * 
	 * @param action action name
	 * @param state dialogue state
	 * @param current current step name
	 */
	private void handleA_M(String action, DialogueState state, String current) {
		language = system.getContent("lang").getBest().toString();
		if (action.equals("DirectHelpLine")) {
			directHelpLine();
		} else if (action.contains("Ground(Restart)")) {
			cleanState();
			String utt = state.queryProb("u_u").getBest().toString().toLowerCase();
			utt = StringUtils.preformatUtterance(utt);
			IntentModel intent = uu.classifyIntentWitModel(utt);
			if (intent != null) 
				informIntent(intent);
			else {
				system.addContent("u_m",
						system.getContent("RestartUtterance").getBest().toString());
			}
		} 
		else if (action.contains("Ground(SlotEdit)")) {
			Map<String, Value> pairs = setMachineUtterance(state);
			pairs.put("current_step", ValueFactory
					.create(activeIntent.getSlot(activeSlotID).getSlotType()));
			system.addContent(new Assignment(pairs));
		} 
		else if (activeIntent != null && action.equals("Ground(Authentication)")) 
			system.addContent("api", "Authentication");
		else if (activeIntent != null
				&& action.equals("Ground(" + activeIntent.getName() + ")")) 
			system.addContent("api", "Execute");
	}

	/**
	 * Fills the variables in a given text, i.e. template
	 * 
	 * @param question the template to be filled
	 * @param state already assigned variables, especially by querying the user DB
	 * @return the filled template
	 */
	public String fillVariables(String question, DialogueState state) {

		Template temp = Template.create(question);
		Set<String> fillers = temp.getSlots();
		String preError = "";
		if (activeIntent.errorCodeExists(activeSlotID - 1)) {
			if (activeIntent.isSlotValidated(activeSlotID - 1) != null
					&& activeIntent.isSlotValidated(activeSlotID - 1))
				preError = translateErrorCode(
						activeIntent.getErrorCode(activeSlotID - 1), false);
		}

		if (fillers == null || fillers.isEmpty()) {
			return preError + question;
		}
		String filledQuestion = "";
		if(fillers.contains("_keys_")) {
			fillers.remove("_keys_");	   
			filledQuestion = question.replace("{_keys_}", activeIntent.getSlot(activeSlotID).getValueSample());
			temp = Template.create(filledQuestion);
		}

		if(!fillers.isEmpty())
			filledQuestion = temp.fillSlots(state.queryProb(fillers).sample());
		return preError + filledQuestion;

	}
	
	/**
	 * Returns the slot-value pairs of the active intent
	 * 
	 * @return slotName-value map
	 */
	public Map<String, Value> getSlotValues(){
		if(activeIntent == null) 
			return null;

		return activeIntent.getSlotValues();
	}
	
	/**
	 * Returns the extra variable-value pairs in the current state
	 * 
	 * @return a hashmap with variable keys and values
	 */
	public HashMap<String, String> getStateVariables() {
		HashMap<String, String> hm = new HashMap<String, String>();
		for (String key : stateVariables)
			hm.put(key.trim(), system.getContent(key).getBest().toString().trim());
		return hm;
	}

	/**
	 * Sets extra variables into the system state
	 * 
	 * @param variables to be set
	 */
	private void setVariables(HashMap<String, String> variables) {
		Map<String, Value> vars = new HashMap<String, Value>();
		vars.putAll(activeIntent.getSlotValues());
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			vars.put(entry.getKey(), new StringVal(entry.getValue()));
			stateVariables.add(entry.getKey());
		}
		system.addContent(new Assignment(vars));
	}

	/**
	 * Returns True when a slot value is not matched or invalid
	 * 
	 * @param isValid 
	 * @param validated
	 * @param values
	 * @param current current step
	 * @return true if a value is not matched or invalid
	 */
	private boolean isSlotValueError(Boolean isValid, Boolean validated,
			HashMap<String, String> params, String current) {
		return !isValid // && !current.equals("Confirmation")
				&& !current.equals("ConfirmSlotEdit") 
				&& ( (params.containsKey("error_code")
						&& (params.get("error_code").equals("not_matched") || params.get("error_code").equals("not_found")))	
						//|| 
						//(!activeIntent.isIntentFilled() && validated != null)
						);
	}

	// ===================================
	// STATE CHANGES
	// ===================================

	/**
	 * Grounds the intent either with a confirmation path or finalizing path
	 */
	private void groundIntent() {
		Map<String, Value> pairs = activeIntent.getSlotValues();

		if (activeIntent.getConfirmNeeded()) {
			pairs.put("current_step", ValueFactory.create("ConfirmIntent"));
			pairs.put("a_m", ValueFactory
					.create("Ground(" + activeIntent.getName() + ",Done)"));
		} else {
			pairs.put("a_m",
					ValueFactory.create("Ground(" + activeIntent.getName() + ")"));
		}

		system.addContent(new Assignment(pairs));
	}

	/**
	 * Activates the new intent
	 */
	private void setStateToActiveIntent(DialogueState state) {
		system.addContent("Intent", activeIntent.getName());
		Boolean isValid = activeIntent.isSlotValidated(activeSlotID);
		if (!activeIntent.isIndexValid(activeSlotID)
				|| activeIntent.isIntentFilled()) {
			groundIntent();
		} else {
			if (isValid != null && !isValid) {
				String translated = translateErrorCode(
						activeIntent.getErrorCode(activeSlotID), false);
				Map<String, Value> pairs = new HashMap<String, Value>();
				pairs.put("SlotNotValidated", ValueFactory.create(translated));
				pairs.put("current_step", ValueFactory.create("ConfirmSlotEdit"));
				pairs.put("a_m", ValueFactory.create("ValidateRepeat"));
				system.addContent(new Assignment(pairs));
			}
			else {
				handleSlotPolicy(state.queryProb("current_step").getBest().toString(),
						state);
			}
		}
	}

	/**
	 * Changes the state to the final step 
	 * 
	 * @param state dialogue state
	 */
	private void changeToFinalState(DialogueState state) {
		Map<String, Value> pairs = new HashMap<String, Value>();
		pairs.put("a_m-prev", state.queryProb("a_m").getBest());
		pairs.put("a_m", ValueFactory.create(activeIntent.getName()));
		pairs.put("current_step", new StringVal("Final"));
		system.addContent(new Assignment(pairs));
	}

	/**
	 * Changing current step to invalid value error state.
	 * 
	 * @param errorText text that fills the machine utterance
	 */
	private void setInvalidErrorState(String errorText, String current) {
		Map<String, Value> pairs = new HashMap<String, Value>();
		if(current.equals("Confirmation"))
		{
			pairs.put("current_step", ValueFactory.create(this.activeIntent.getSlot(this.activeSlotID).getSlotType()));
			pairs.put("ErrorMessage",ValueFactory.create(activeIntent.getSlot(this.activeSlotID).getErrorMessage()+errorText));
			pairs.put("ConstraintRequest",ValueFactory.create(errorText));
			pairs.put("a_u",new StringVal("Other"));
		}
		else {
			pairs.put("SlotNotValidated", ValueFactory.create(errorText));
			pairs.put("current_step", ValueFactory.create("ConfirmSlotEdit"));
			pairs.put("a_m", ValueFactory.create("ValidateRepeat"));
		}
		system.addContent(new Assignment(pairs));

	}

	/**
	 * Removes all session variables from the system and cleans the user variables
	 * from the policy object. It is necessary to run this function while starting
	 * a new intent in the same session.
	 */
	private void cleanState() {

		for (String key : stateVariables) {
			system.removeContent(key);
		}
		stateVariables.clear();
		for (String name : activeIntent.getFilledSlotNames()) {
			system.removeContent(name);
		}

		validationNeeds.clear();
		activeIntent.cancelAll();
		activeSlotID = 0;
		infoSlotID = 0;
		// Remove session and intent related state variables
		system.removeContent("Intent");
		system.removeContent("ErrorMessage");
		system.removeContent("ConstraintRequest");
		system.removeContent("MachineUtterance");
		system.removeContent("SlotNotValidated");
		system.removeContent("Operation");
		system.removeContent("api_r");
		system.removeContent("api");
		system.removeContent(activeIntent.getName());

		system.addContent("AskRepeat", ValueFactory.create(0));
		activeIntent = null;

	}

	/**
	 * Redirects the user to the helpdesk, customer service
	 */
	private void directHelpLine() {
		Map<String, Value> pairs = new HashMap<String, Value>();
		pairs.put("current_step", ValueFactory.create("CloseWithHelpline"));
		system.addContent(new Assignment(pairs));
	}

	/**
	 * Pauses the module.
	 * 
	 * @param toPause whether to pause the module or not
	 */
	@Override
	public void pause(boolean toPause) {
		paused = toPause;
	}

	/**
	 * Returns whether the module is currently running or not.
	 * 
	 * @return whether the module is running or not.
	 */
	@Override
	public boolean isRunning() {
		return !paused;
	}

	/**
	 * Translates the error code retrieved from the middleware into the NL machine
	 * utterance text corresponding.
	 * 
	 * @param errorCode  middleware error code
	 * @param askAgain if the machine utterance should include a repeating the question
	 *        sub-sentence.
	 * @return machine utterance corresponding to the api error code
	 */

	private String translateErrorCode(String errorCode, boolean askAgain) {
		if (errorCode == null)
			return "";
		String translation = "";
		String[] errorMessage = activeIntent.getSlot(activeSlotID).getErrorMessage()
				.split(".");
		String askAgainText = "";
		if (errorMessage.length > 1)
			askAgainText = errorMessage[1] + ".";

		String error_key = errorCode + language;
		if (uu.getErrorDict().containsKey(error_key)) 
			translation = uu.getErrorDict().get(error_key);

		if (errorCode.equals("not_matched") || errorCode.trim().equals("")|| errorCode.trim().equals("not_found")) {
			if (!askAgain)
				return activeIntent.getSlot(activeSlotID).getErrorMessage()
						.split(".")[0] + ".";
			return translation = activeIntent.getSlot(activeSlotID)
					.getErrorMessage();
		} else if (askAgain)
		 	return translation + askAgainText;
		else
			return translation;
	}

}
