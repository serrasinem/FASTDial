package fastdial.utils;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {
	
	/**
	 * Cleans the user utterance from the unknown/unexpected characters
	 * Supports Italian, Turkish, and Hungarian characters. To deal with other languages
	 * we can ignore this method. Or one should modify it to handle other
	 * character sets such as Chinese, Russion, etc.
	 * 
	 * @param utterance user utterance
	 * @return clean user utterance
	 */
	public static String preformatUtterance(String utterance) {
		String cleanUtterance = utterance.replaceAll("[^a-zA-Zìòèàùáéúőóüöçğış0-9€$£]+", " ");
		return cleanUtterance;
	}
	
	/**
	 * Formats a hashmap into a String as key:value; pairs
	 * 
	 * @param variables A hashmap with String keys and String values
	 * 
	 * @return pairs
	 */
	public static String formatHashMap(HashMap<String, String> variables) {
		String o = "";
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			o += entry.getKey() + ":" + entry.getValue() + ";";
		}
		return o;
	}
}
