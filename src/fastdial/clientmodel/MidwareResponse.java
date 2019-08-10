package fastdial.clientmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import opendial.bn.values.StringVal;
import opendial.bn.values.Value;

/**
 * Response object created with a JSON object returned by the middleware.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class MidwareResponse {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// api call response flag
	public boolean success = false; 

	// required variables from middleware
	public HashMap<String, String> variables;

	// re-passed validated slot value
	public String validatedVal = "";

	/**
	 * Constructor with a JSON string
	 * 
	 * @param response
	 */
	public MidwareResponse(String response) {
		variables = new HashMap<String, String>();

		// Fill the response variables
		if (!response.equals("")) {

			String cleanResponse = response.replace("}", "").replace("{", "")
					.replace("\"", "").replace("\'", "");
			// Saving,response:success
			for (String var : cleanResponse.split(",")) {

				// When more than 1 info type is found in the response:
				// account_info:AccountTransferLimit:1010,AccountBalance:101010
				String[] val = var.split(":");
				if (val.length > 2)
					variables.put(val[1].trim(), val[2].trim());
				else if (val.length > 1)
					variables.put(val[0].trim(), val[1].trim());
			}

			if (variables.containsKey("is_valid"))
				success = Boolean.parseBoolean(variables.get("is_valid"));

			if (variables.containsKey("response:success"))
				success = true;

			if (variables.containsKey("_slot_"))
				validatedVal = variables.get("_slot_");
		}

	}

	/**
	 * Constructor with a collection of OpenDial Value objects 
	 * 
	 * @param response
	 */
	public MidwareResponse(Collection<Value> response) {
		variables = new HashMap<String, String>();
		if (response.contains(new StringVal("response:success"))
				|| response.contains(new StringVal("is_valid:true")))
			success = true;
		// type1= i:{var1:x,var2:y,var3:[z1,z2,z3]}
		// type2= i:{x}
		// type3= i:{var1:x}
		for (Value var : response) {
			// type 1
			if (isJSONValid(var.toString())) {
				JSONObject j = new JSONObject(var.toString());
				Iterator<String> it = j.keys();
				while (it.hasNext()) {
					// When more than 1 info type is found in the response:
					// account_info:{AccountTransferLimit:1000\u20ac,AccountBalance:200\u20ac}
					String key = it.next(); // parsed i
					String val = j.get(key).toString();
					if (isJSONValid(val)) {
						JSONObject v = new JSONObject(val);
						Iterator<String> i = v.keys();
						while (i.hasNext()) {
							String k = i.next();
							variables.put(k, v.getString(k));
						}
					} else {
						String[] vs = val.split(":");
						if (vs.length > 1)
							variables.put(vs[0], vs[1]);
						else
							variables.put(key, val);
					}

				}
			} // type 2 and 3
			else {
				String cleanResponse = var.toString().replace("}", "").replace("{", "")
						.replace("\"", "").replace("\'", "").replace("]", "")
						.replace("[", "");
				String[] vals = cleanResponse.toString().split(",");
				for (String v : vals) {
					String[] vs = v.split(":");
					if (vs.length > 2)
						variables.put(vs[1].trim(), vs[2].trim());
					else if (vs.length > 1)
						variables.put(vs[0].trim(), vs[1].trim());
				}
			}

		}

		if (variables.containsKey("_slot_"))
			validatedVal = variables.get("_slot_");
	}

	/**
	 * Returns True if the given string is a valid JSON string
	 * 
	 * @param test
	 * @return isValid
	 */
	public boolean isJSONValid(String s) {
		try {
			new JSONObject(s);
		} catch (JSONException e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the list of slot keys by parsing the given variable value
	 * 
	 * @param infoType
	 * @return keyList
	 */
	public ArrayList<String> parseKeyList(String infoType) {
		String slotKeys = variables.get(infoType);
		String[] typeVal = slotKeys.split(":");
		String info = typeVal[0];
		if (typeVal.length > 1)
			info = typeVal[1];

		info = info.replace("[", "").replace("]", "").replace("'", "").replace("\"", "")
				.trim();

		ArrayList<String> savedKeys = new ArrayList<String>();

		for (String k : info.split(","))
			savedKeys.add(k.trim());

		return savedKeys;
	}

}
