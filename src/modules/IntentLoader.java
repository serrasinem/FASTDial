package fastdial.modules;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import fastdial.nlu.SlotTracker;
import fastdial.nlu.IntentClassifier;
import fastdial.slots.Slot;
import fastdial.slots.SlotFactory;
import fastdial.slots.StringListSlot;
/**
 * After the identification of the current user intent, the intent description
 * is loaded through IntentLoader.
 * 
 * For more information of how to define intents please follow the tutorials.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class IntentLoader {

    // logger
    final static Logger log = Logger.getLogger("FastLogger");
    	
    /**
    * Create a UserIntent using the json intent description.
    * 
    * @param intentFile the absolute path of intent file
    * @param intentName the unique name of the intent identified by the NLU model
    * @param user user information
    * 
    * @return the intent object to be activated in the system
    */
    public static SlotTracker loadJSONIntent(String intentFile, String intentName) throws IOException {
	String jsonData = IntentClassifier.readFile(intentFile);		 
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray slotList  = new JSONArray();
        if(jsonObject.has("slots"))
        	slotList = jsonObject.getJSONArray("slots");
        HashMap<Integer,Slot> slots = new HashMap<Integer,Slot>(); 
        Integer id = 0;
        
        SlotTracker intent = new SlotTracker();
        for(int n = 0; n < slotList.length(); n++)
        {
            JSONObject jo = slotList.getJSONObject(n);
            String type = jo.getString("slot_type");
            String name = jo.getString("slot_name");
            String constraint = jo.getString("constraint");
            String question = jo.getString("question");
            String api = jo.getString("validation_api");
            //String error_message = jo.getString("error_message");
            JSONObject errors = jo.getJSONObject("error_message");
            @SuppressWarnings("unchecked")
	    HashMap<String, String> e = new Gson().fromJson(errors.toString(), HashMap.class);
            Boolean mandatory = jo.getBoolean("mandatory");
            
            String dependingInfo = null;
            if(jo.has("extra_info_api"))
        	dependingInfo = jo.getString("extra_info_api");
            
            String regex = "";
            if(jo.has("regex"))
            	regex = jo.getString("regex");
            
            String dependency = "";
            if(jo.has("dependency"))
            	dependency = jo.getString("dependency");

            Slot newSlot = SlotFactory.create(type, name, constraint, dependency, question, 
        	    api, dependingInfo, e.get("not_matched"), mandatory.toString(),regex);
         
            
            if(jo.getString("slot_type").equals("StringList") && 
        	    ((StringListSlot)newSlot).getInfoAPICall() != null)
                intent.addInitAPIs(id, ((StringListSlot)newSlot).getInfoAPICall());
            
            slots.put(id, newSlot);
            id++;
        }
            
        intent.setName(intentName);
        if(!slots.isEmpty()) 
    		intent.setSlots(slots);

        return intent;
    }
	
}
