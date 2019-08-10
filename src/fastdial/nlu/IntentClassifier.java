package fastdial.nlu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import fastdial.FastProperties;
import fastdial.modules.IntentLoader;

import org.json.JSONArray;
import org.json.JSONObject;


public class IntentClassifier {
	// logger
	final Logger log = Logger.getLogger("FastLogger");

	// intent folder
	private static String intents;

	// language extensions
	public enum Language {
		english("_EN"), italian("_IT");

		public final String l;

		private Language(String l) {
			this.l = l;
		}

	}

	// name-model path
	HashMap<String, IntentModel> model = new HashMap<String, IntentModel>();

	// error keys and nl texts map
	HashMap<String, String> error_dict = new HashMap<String, String>();

	// intent identification patterns, sorted by length as a simple model
	String[] patterns = null;

	/**
	 * Constructor of an intent identification model 
	 * 
	 * @param lang
	 */
	public IntentClassifier(String lang) {
		FastProperties properties = new FastProperties();
		intents = properties.getProperty("jsonIntentsPath");
		prepareModel(lang);
	}


	/**
	 * @param filename
	 * @return
	 */
	public static String readFile(String filename) {
		String result = "";
		final Logger log = Logger.getLogger("FastLogger");
		log.info("The intent file " + filename);
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			StringBuilder sb = new StringBuilder();
			try {
				String line = br.readLine();
				while (line != null) {
					sb.append(line);
					line = br.readLine();
				}
				result = sb.toString();
			} finally {
				br.close();
			}
		} catch (Exception e) {
			log.severe(
					"The intent file cannot be found in the given path:" + filename);
			throw new RuntimeException("Failed to read JSON from stream", e);
		}
		return result;
	}

	/**
	 * Reads the intent files and creates the list of patterns and error dictionary
	 * 
	 * @param lang
	 */
	public void prepareModel(String lang) {
		File folder = new File(intents);
		Language l = Language.valueOf(lang.toLowerCase());
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (!listOfFiles[i].getPath()
						.contains(l.l))
					continue;
				try {
					String jsonData = readFile(listOfFiles[i].getPath());
					JSONObject jsonObject = new JSONObject(jsonData);
					String keystring = (String) jsonObject.get("keys");
					String[] keys = keystring.toLowerCase().trim().split(",");
					for (String key : keys) {
						model.put(key,
								new IntentModel((String) jsonObject.get("name"),
										listOfFiles[i].getPath(),
										(Boolean) jsonObject.get("confirmation"),
										(String) jsonObject.get("execution_call")));
					}
					JSONArray slotList = jsonObject.getJSONArray("slots");
					for (int n = 0; n < slotList.length(); n++) {
						JSONObject jo = slotList.getJSONObject(n);
						JSONObject errors = jo.getJSONObject("error_message");
						HashMap<String, String> e = new Gson().fromJson(errors.toString(),
								HashMap.class);
						for (String error_id : e.keySet()) {
							if (!error_id.equals("not_matched"))
								error_dict.put(error_id + lang, e.get(error_id));
						}

					}
				} catch (Exception e) {
					log.warning("The intent file " + listOfFiles[i].getPath()
							+ " content is not a valid JSON or the necessary information "
							+ "cannot be found in the JSON object. The object should "
							+ "contain name, file_path, confirmation and execution_call "
							+ "parameters.");
				}

			}
		}

		sortForEarlierThanJava8();
	}

	/**
	 * Sorts the intent identification patterns with respect to token and char lengths
	 */
	public void sortForEarlierThanJava8() {
		patterns = model.keySet().toArray(new String[model.size()]);

		// Create an object of Comparator
		Comparator<String> lengthComparator = new Comparator<String>() {
			@Override
			public int compare(String x, String y) {
				int c1 = x.split(" ").length;
				int c2 = y.split(" ").length;
				if (c1 == c2) {
					c1 = x.length();
					c2 = y.length();
				}
				return Integer.compare(c2, c1);
			}
		};

		Arrays.sort(patterns, lengthComparator);
	}

	/**
	 * Identifies the intent using the user utterance 
	 * 
	 * @param utterance user utterance
	 * @return intent description 
	 */
	public IntentModel classifyIntentWitModel(String utterance) {
		for (String key : patterns) {
			Pattern keypattern = Pattern.compile(".*" + key + ".*");
			Matcher matcher = keypattern.matcher(utterance.toLowerCase());
			if (matcher.find()) 
				return this.model.get(key);
			else if (utterance.contains(key)) 
				return this.model.get(key);

		}
		return null;
	}

	/**
	 * Loads the intent object given the intent description
	 * 
	 * @param intentType 
	 * @return intent object
	 */
	public SlotTracker loadIntent(IntentModel intentType) {
		final Logger log = Logger.getLogger("FastLogger");
		SlotTracker intent = null;
		if (intentType != null) {
			try {
				intent = IntentLoader.loadJSONIntent(intentType.filePath,
						intentType.name);
				intent.setConfirmNeeded(intentType.confirmation);
				intent.setApiCall(intentType.executionCall);
			} catch (IOException e) {
				log.severe("The intent file for the intent " + intentType
						+ " cannot be loaded from the given path:" + intentType.filePath);
				throw new RuntimeException("cannot initialise the intent: " + e);

			}

		}
		return intent;
	}

	/**
	 * Returns the error dictionary 
	 * 
	 * @return
	 */
	public HashMap<String, String> getErrorDict() {
		return this.error_dict;
	}

	/**
	 * Prints the sorted intent identification patterns
	 */
	public void printPatterns() {
		for (int k = 0; k < patterns.length; k++) {
			System.out.println(
					String.format("%s : %s", patterns[k], model.get(patterns[k])));
		}
	}
}
