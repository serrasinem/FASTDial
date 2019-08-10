package fastdial.nlu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fastdial.clientmodel.MidwareResponse;
import fastdial.slots.ConfirmationSlot;
import fastdial.slots.Slot;
import fastdial.slots.SlotState;
import fastdial.slots.StringListSlot;
import fastdial.slots.StringSlot;
import opendial.bn.values.StringVal;
import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;
import opendial.datastructs.Assignment;

/**
 * A SlotTracker object is created and activated when the user intent is identified.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class SlotTracker {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// slot map with the slot orders as keys
	private  HashMap<Integer, Slot> slots = new HashMap<Integer, Slot> ();

	// slot state map with the slot orders as keys
	private  HashMap<Integer, SlotState> states = new HashMap<Integer, SlotState> ();

	// intent specific variables (dynamic)
	private HashMap<String, String> variables = new HashMap<String, String> ();

	// api calls should be done before the slot filling conversation
	private ArrayList<Integer> initialAPIs = new ArrayList<Integer>();

	// intent name
	private String name;

	// user confirmation needed for intent execution call
	private Boolean confirmNeeded = false;

	// api call for intent execution
	private String apiCall;

	/**
	 * Default intent constructor
	 */
	public SlotTracker()
	{
		this.name = "";
	}

	// ===================================
	// GETTERS AND SETTERS
	// ===================================
	/** 
	 * Returns the intent name
	 * 
	 * @return name
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
	 * Returns a boolean value determining if the intent needs a user confirmation step
	 * in order to be executed
	 * 
	 * @return user confirmation needed
	 */
	public Boolean getConfirmNeeded() {
		return confirmNeeded;
	}

	/**
	 * Sets whether the intent has to be confirmed by the user
	 * 
	 * @param confirmNeeded
	 */
	public void setConfirmNeeded(Boolean confirmNeeded) {
		this.confirmNeeded = confirmNeeded;
	}

	/**
	 * Sets the slot list of the intent 
	 * 
	 * @param slotlist
	 */
	public void setSlots(HashMap<Integer, Slot> slotlist) {	

		this.slots = slotlist;
	}

	/**
	 * Returns a map representation of current variables of the intent
	 * 
	 * @return the current intent variables
	 */
	public HashMap<String, String> getVariables() {
		return variables;
	}

	/**
	 * Sets the intent variables
	 * 
	 * @param vals
	 */
	public void setVariables(HashMap<String, String> vals) {
		this.variables = vals;
	}

	/**
	 * Returns the intent execution api call
	 * 
	 * @return apiCall
	 */
	public String getApiCall() {
		return apiCall;
	}

	/**
	 * Sets the api call for the intent execution
	 * 
	 * @param apiCall
	 */
	public void setApiCall(String apiCall) {
		this.apiCall = apiCall;
	}

	// ===================================
	// SLOT FILLING METHODS
	// ===================================

	/**
	 * Extracts the slot value from the utterance. The method should be preconditioned that
	 * the given utterance definitely contains a slot value.
	 * 
	 * @param utterance
	 * @param slotID
	 * @return the slot value
	 */
	public String rematchIntentUtterance(String utterance, Integer slotID) {
		Slot sl = this.getSlot(slotID);
		String val = sl.matchUtterance(utterance);
		return val;
	}

	/**
	 * Attempts to fill the slot in the given index by matching the utterance.
	 * 
	 * @param utterance
	 * @param slotID
	 * @return api request for matched slot value or null
	 */
	public String fillSlot(String utterance, Integer slotID) {
		Slot sl = this.getSlot(slotID);
		String val = sl.matchUtterance(utterance);
		Boolean mandatory = Boolean.parseBoolean(sl.getSlotMandatory());
		if(val!=null) {
			return sl.getCheckAPICall()+":"+val;
		}
		else if(mandatory){
			variables.put("is_valid", "false");
			variables.put("error_code", "not_matched");
		}
		else {
			SlotState s = new SlotState();
			s.setValue("");
			s.setValidated(true);
			this.states.put(slotID, s);
			variables.put("is_valid", "true");

		}
		return null;
	}

	/**
	 * Attempts to fill all the slots in the order using only one utterance. Particularly
	 * useful for intent sentences. 
	 * 
	 * TO-DO for multiple slot at a time scenario, this method could be useful
	 * 
	 * @param utterance
	 * @return
	 */
	public HashMap<Integer,String> fillAllPossibleSlots(String utterance) {
		HashMap<Integer,String> validationNeeds = new HashMap<Integer, String>();
		String marked = utterance;
		for(Integer s:slots.keySet()) {
			Slot sl = slots.get(s);
			SlotState st = new SlotState();

			String val = sl.matchUtterance(marked);

			if(val != null && sl.getCheckAPICall().equals("NoValidate")){
				st.setValidated(true);
				st.setValue(val);
				this.states.put(s, st);
				variables.put("is_valid", "true");	
			}
			else if(val != null && !(sl instanceof StringSlot) && !(sl instanceof ConfirmationSlot)) 
				validationNeeds.put(s, val);

			if(sl instanceof ConfirmationSlot)
			{
				//val = sl.matchRegex(utterance);
				if(val!=null && !sl.getCheckAPICall().equals("NoValidate"))
					validationNeeds.put(s, val);
				else if(val!=null) {
					st = new SlotState();
					st.setValidated(true);
					st.setValue(val);
					this.states.put(s, st);
					variables.put("is_valid", "true");	

				}

			}
		}

		return validationNeeds;
	}

	/**
	 * Sets the state of the slot in the given index
	 * 
	 * @param slotID
	 * @param val
	 * @param validation
	 */
	public void setSlotState(Integer slotID, String value, HashMap<String, String> validation, boolean skipped) {

		Slot sl = slots.get(slotID);
		SlotState s = new SlotState();

		variables = validation;

		/** If a slot value arrives through the validation string, then fill that slot
		 *  It is helpful while creating multiple paths in a single intent:
		 *  - bot asks: path1, path2, path3?
		 *  + user selects: path2
		 *  > api fills: unnecessary path slots (pat1, path3 slots) with dummy values
		 *  - bot asks: for path2 slots
		 */
		setSlotValues(validation);

		if(this.states.containsKey(slotID))
			s = this.states.get(slotID);

		s.setErrorCode(null);

		Boolean valid = Boolean.valueOf(variables.get("is_valid"));
		s.setValidated(valid);

		Boolean mandatory = Boolean.parseBoolean(sl.getSlotMandatory());
		//if validation is successful
		if(valid) {
			s.setValue(value);
		} 
		//if cannot be filled but slot is not mandatory
		else if(!mandatory && variables.get("error_code").contains("not_found")) {
			s.setErrorCode(variables.get("error_code"));
			s.setValue("");
			s.setValidated(true);
			variables.put("is_valid", "true");	
		}
		//if validation is not successful and slot is not mandatory
		else if(!mandatory) {
			variables.put("is_valid", "false");
		}

		if(skipped)
			s.setSkipped(true);

		this.states.put(slotID, s);
	}

	/**
	 * Sets the state of the slot in the given index
	 * 
	 * @param slotID
	 * @param response
	 */
	public void setSlotState(Integer slotID, MidwareResponse response) {
		String value = response.validatedVal;
		Boolean valid = response.success;
		HashMap<String,String> v = response.variables;
		setSlotValues(v);
		SlotState s = new SlotState();

		if(states.containsKey(slotID))
			s = states.get(slotID);

		s.setErrorCode(null);

		/** In confirmation validation cases, if the value is not returned from the 
		 *  middleware, so just keep it as it is.*/
		if(value!=null)
			s.setValue(value);

		s.setValidated(valid);

		if(valid)
			variables.putAll(v);
		else 
			s.setErrorCode(v.get("error_code"));

		this.states.put(slotID, s);
	}
	
	/**
	 * Sets the state of the slot in the given index
	 * 
	 * @param slotID
	 * @param slotState
	 */
	public void setSlotState(Integer id, SlotState s) {
		states.put(id, s);
	}

	/**
	 * Sets validated and final values of the given slots as <slotName, value> pairs
	 * 
	 * @param params
	 */
	public void setSlotValues(HashMap<String,String> params){		
		if(params == null || params.isEmpty())
			return;

		HashMap<String,Integer> names = getSlotNames();

		for(Map.Entry<String,String> v : params.entrySet())
		{			
			if(names.containsKey(v.getKey())) {
				SlotState s = new SlotState();
				if(this.states.containsKey(names.get(v.getKey())))
					s = this.states.get(names.get(v.getKey()));
				s.setValue(v.getValue());
				s.setValidated(true);
				this.states.put(names.get(v.getKey()), s);
			}
		}


	}

	/**
	 * Returns True if all mandatory slots are filled
	 * 
	 * @return filled
	 */
	public boolean isIntentFilled() {
		if(this.slots.size() == getNumberOfFilledSlots())
			return true;

		return false;
	}

	/**
	 * Returns the number of the filled and validated slots
	 * 
	 * @return number of filled slots
	 */
	public int getNumberOfFilledSlots() {
		int l = 0;
		for(Integer k:states.keySet())
		{
			Boolean a = states.get(k).getValidated();
			if(states.get(k).getValue() != null && a!=null && a)
				l++;
		}
		return l;
	}


	/**
	 * Returns a map representation of filled slot names and values
	 * 
	 * @return map of filled slot names, values
	 */
	public Map<String, Value> getSlotValues() {
		Map<String, Value> pairs = new HashMap<String, Value>();
		for(Integer key:this.slots.keySet()) {

			if(states.containsKey(key)) {
				if(states.get(key).getValue()!= null)
				{pairs.put(slots.get(key).getSlotName(), 
						ValueFactory.create(states.get(key).getValue()));
				}
			}
		}
		return pairs;

	}


	/**
	 * Returns the list of filled slot names
	 * 
	 * @return filled slot name list
	 */
	public ArrayList<String> getFilledSlotNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(Integer key:this.slots.keySet()) {
			if(states.containsKey(key))
				if(states.get(key).getValue()!= null)
					names.add(slots.get(key).getSlotName());
		}
		return names;
	}


	/**
	 * Returns a map representation of slot name, index tuples of the intent
	 * 
	 * @return < slot name, index > tuples of the intent
	 */
	public HashMap<String, Integer> getSlotNames() {
		HashMap<String, Integer> names = new HashMap<String, Integer>();
		for(Integer key:this.slots.keySet()) {

			names.put(slots.get(key).getSlotName(), key);
		}
		return names;
	}

	/**
	 * The list of slots that are filled but invalid
	 * 
	 * @return invalid valued slot list
	 */
	public ArrayList<Integer> getInvalidSlots() {
		ArrayList<Integer> slots = new ArrayList<Integer>();
		for(Integer key:this.slots.keySet()) {
			
			if(states.containsKey(key)) {
				Boolean a  = states.get(key).getValidated();
				if(a!=null && !a) 
					slots.add(key);
					
			}
		}
		return slots;

	}

	/**
	 * Returns the list of API calls that need to be done before starting the slot filling
	 * questions, e.g. user specific information
	 * 
	 * @return
	 */
	public ArrayList<Integer> getInitialAPIs() {
		return initialAPIs;
	}

	/**
	 * Sets the initial API list that need to be done before starting the slot filling
	 * questions, e.g. user specific information
	 * 
	 * @param initialAPIs
	 */
	public void setInitialAPIs(ArrayList<Integer> initialAPIs) {
		this.initialAPIs = initialAPIs;
	}

	/**
	 * Returns the next slot id of which the API call to be done; if list is empty 
	 * returns null
	 * 
	 * @return slotID
	 */
	public Integer getNextInitialAPI() {
		if(!initialAPIs.isEmpty())
		{
			return initialAPIs.remove(0);
		}

		return null;
	}

	/**
	 * Sets the slot values to be searched in the user utterances
	 * 
	 * @param slotID
	 * @param slotKeys
	 * @param prefill
	 */
	public void setSlotKeys(Integer slotID, ArrayList<String> slotKeys, Boolean prefill) {
		if(slots.get(slotID).getSlotType().equals("StringList")) {
			StringListSlot sls = (StringListSlot) slots.get(slotID);

			if(slotKeys.isEmpty())
				return;

			sls.setKeylist(slotKeys);

			/** If there is only one option, e.g., there is only one user account, 
			 * the value is assumed to be given. 
			 */
			if(prefill && slotKeys.size() == 1) {
				SlotState s = new SlotState();
				if(states.containsKey(slotID))
					s = states.get(slotID);
				s.setValue(slotKeys.get(0));
				s.setValidated(true);
				states.put(slotID, s);
			}

			slots.put(slotID, sls);
		}
	}

	/**
	 * Adds a new user information API call to init calls which are called before starting 
	 * slot filling
	 * 
	 * @param slotID
	 * @param neededInfo
	 */
	public void addInitAPIs(Integer slotID, String neededInfo) {
		this.initialAPIs.add(slotID);
	}

	/**
	 * Removes an API call from the init calls
	 * 
	 * @param infoSlotID
	 */
	public void removeInitAPI(Integer infoSlotID) {
		this.initialAPIs.remove(infoSlotID);
	}

	/**
	 * Clears all slots, states and init calls
	 */
	public void cancelAll() {
		states.clear();
		variables.clear();
		initialAPIs.clear();
		slots.clear();
	}

	// ===================================
	// SLOT LEVEL METHODS
	// ===================================

	/**
	 * Gets the slot in the given index 
	 * 
	 * @param slotID
	 * @return slot object
	 */
	public Slot getSlot(Integer slotID) {
		return this.slots.get(slotID);
	}

	/**
	 * Returns the state of the given slot id
	 * 
	 * @param slotID
	 * @return state
	 */
	public SlotState getSlotState(Integer slotID) {
		return this.states.get(slotID);
	}

	/**
	 * Gets the slot type in the given index
	 * 
	 * @param slotID
	 * @return slot type
	 */
	public String getSlotType(Integer slotID) {
		return this.slots.get(slotID).getSlotType();
	}

	/**
	 * Returns whether the slot must be filled for the execution of the intent.
	 * 
	 * @param slotID
	 * @return mandatory field of the slot
	 */
	public Boolean getSlotMandatory(Integer slotID) {
		return Boolean.parseBoolean(
				this.slots.get(slotID).getSlotMandatory());
	}

	/**
	 * Returns True if given slot ID is within the index range of the slots
	 * 
	 * @param slotID
	 * @return True if slot index is smaller than slots size
	 */
	public boolean isIndexValid(Integer slotID) {
		if(this.slots.size() > slotID)
			return true;

		return false;
	}

	/**
	 * Returns True if the slot value is not yet filled or filled but not valid
	 * 
	 * @param slotID
	 * @return if slot is filled
	 */
	public boolean isEmpty(Integer slotID) {
		if(isIndexValid(slotID) && 
				(!states.containsKey(slotID) || 
						states.get(slotID).getValue() == null))//||
						//!states.get(slotID).getValidated()))
			return true;

		if(states.containsKey(slotID))
		{
			Boolean a = states.get(slotID).getValidated();
			if(isIndexValid(slotID) && (a==null || !a))
				return true;
		}
		return false;
	}

	/**
	 * Returns the string representation of the value of the given slot ID
	 * 
	 * @param slotID
	 * @return String value of the given slot id, null if it is not filled
	 */
	public String getSlotValue(Integer slotID) {
		if(states.containsKey(slotID))
			return states.get(slotID).getValue();

		return null;
	}

	/**
	 * Returns True if the slot is validated
	 * 
	 * @param slotID
	 * @return
	 */
	public Boolean isSlotValidated(Integer slotID) {
		if(states.containsKey(slotID))
			return states.get(slotID).getValidated();

		return null;		
	}

	/**
	 * Sets the validation response to the slot state
	 * 
	 * @param slotID
	 * @param validated
	 */
	public void setSlotValidated(Integer slotID, Boolean validated) {
		SlotState s = new SlotState();
		if(states.containsKey(slotID))
			s = states.get(slotID);
		s.setValidated(validated);
		this.states.put(slotID, s);		
	}

	/**
	 * If a slot value does not need to be validated, its API call is set as NoValidate
	 * in intent description 
	 * 
	 * @param slotID
	 * @return True if slot value must be validated. 
	 */
	public boolean isValidationRequired(Integer slotID) {
		if(slots.get(slotID).getCheckAPICall().equals("NoValidate"))
			return false;

		return true;
	}


	/**
	 * Returns the error code returned by validation API calls 
	 * 
	 * @param slotID
	 * @return validation error code, NULL if it is valid or not validated
	 */
	public String getErrorCode(Integer slotID) {
		if(states.containsKey(slotID))
			return states.get(slotID).getErrorCode();
		return null;
	}

	/**
	 * Returns True if there is an error code returned by validation API calls
	 * 
	 * @param slotID
	 * @return True if there is an error code bounded to the given slot
	 */
	public boolean errorCodeExists(Integer slotID) {
		if(states.containsKey(slotID))
			if(states.get(slotID).getErrorCode() != null)
				return true;
		return false;
	}

	public void removeVariable(String key) {
		variables.remove(key);
	}

	public Integer findSlot(String name) {
		for(Integer k:slots.keySet())
			if(slots.get(k).getSlotName().equals(name))
				return k;
		return null;
	}


}
