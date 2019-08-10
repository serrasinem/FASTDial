package fastdial.modules;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.JSONObject;

import fastdial.FastProperties;
import fastdial.nlu.IntentClassifier;

/**
 * The creator of the NLG domain files for each language automatically by retrieving the 
 * information from all the intent files.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class NLGLoader {
	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	/**
	 * creates the intent confirmation machine utterance with the condition that the 
	 * grounding is done
	 * 
	 * @param intentName name of the intent
	 * @param confirmationQuestion the question to get the confirmation from the user
	 * 
	 * @return confirmed grounding rule for the intent
	 */
	public static String createConfirmation(String intentName, 
			String confirmationQuestion) {
		String confirmation = "<rule>\n" + 
				"			<case>\n" + 
				"				<condition>\n" + 
				"					<if var=\"a_m\" value=\"Ground("+
				intentName.trim()+",Done)\" />\n" + 
				"				</condition>\n" + 
				"				<effect util=\"1\">\n" + 
				"					<set var=\"u_m\" value=\""+
				confirmationQuestion+"\" />\n" + 
				"				</effect>\n" + 
				"			</case>\n" + 
				"		</rule>";

		return confirmation;
	}


	/**
	 * creates the intent execution rules; success or error
	 * 
	 * @param intentName intent name
	 * @param executionSuccess machine utterance if the intent is executed successfully
	 * @param executionError  machine utterance if the execution is unsuccessful
	 * @param redirect flag for redirecting the state to helpline in case of an execution 
	 * 	      error
	 * 
	 * @return intent execution rules
	 */
	public static String createIntentText(String intentName, String executionSuccess, 
			String executionError, Boolean redirect) {
		String execution = "		<rule>\n" + 
				"			<case>\n" + 
				"				<condition>\n" + 
				"					<if var=\"a_m\" value=\""+
				intentName.trim()+"\" />\n" + 
				"					<if var=\"response\" value=\"success\"/>\n" + 
				"				</condition>\n" + 
				"				<effect util=\"1\">\n" + 
				"					<set var=\"u_m\"\n" + 
				"						value=\""+
				executionSuccess+"\"  />\n" + 
				"				</effect>\n" + 
				"			</case>\n" + 
				"			<case>\n" + 
				"				<condition>\n" + 
				"					<if var=\"a_m\" value=\""+
				intentName.trim()+"\" />\n" + 
				"					<if var=\"response\" value=\"error\"/>\n" + 
				"				</condition>\n" + 
				"				<effect util=\"1\">\n" + 
				"					<set var=\"u_m\"\n" + 
				"						value=\""+executionError+"\"  />\n";
		if(redirect)
			execution +="		    <set var=\"a_m\" value=\"DirectHelpLine\" />";


		execution += "</effect>\n" + 
				"			</case>\n" + 
				"		</rule>";

		return execution;

	}

	/**
	 * Generates the NLG domain files for a given language key
	 * 
	 * @param languageKey in the format of a file name extension such as "_en" or "_it"
	 */
	public static void prepareNLGModel(String languageKey) {
		//log
		final Logger log = Logger.getLogger("FastLogger");

		FastProperties properties = new FastProperties();
		String intents = properties.getProperty("jsonIntentsPath");
		String domains = properties.getProperty("domainPath");

		File folder = new File(intents);
		File[] listOfFiles = folder.listFiles();

		String confirmations = "";
		String executions = "";
		ArrayList<String> actions = new ArrayList<String>();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if(!listOfFiles[i].getName().toLowerCase().contains(languageKey))
					continue;

				try {
					String jsonData = IntentClassifier.readFile(
							listOfFiles[i].getPath());		 
					JSONObject jsonObject = new JSONObject(jsonData);

					String intentName = (String) jsonObject.get("name");
					String description = (String) jsonObject.get("description");
					Boolean confirmed = jsonObject.getBoolean("confirmation");
					if(confirmed)
						confirmations += createConfirmation(intentName, 
								jsonObject.getString("confirmation_question"));
					if(!description.trim().equals(""))
						actions.add(description);

					executions += createIntentText(intentName, 
							jsonObject.getString("success_message"), 
							jsonObject.getString("error_message"), 
							jsonObject.getBoolean("direct_helpline"));
				}
				catch (Exception e) {
					log.warning("Could not load the file: " + listOfFiles[i].getPath());
				}

			}
		}

		//save them into the nlg template 
		String template = domains+"/FASTDomain_nlg_temp"+languageKey+".xml";
		String text = "";
		try {
			text = new String(Files.readAllBytes(Paths.get(template)), 
					StandardCharsets.UTF_8);
			text = text.replace( "{action_string}",  String.join(", ", actions));
			text = text.replace("{confirmation_string}", confirmations);
			text = text.replace("{execution_string}", executions);
		} catch (IOException e) {
			log.severe("NLG Template File Error: NLG Template File cannot be read. "
					+ "Please check the NLG template (" + template + ") in the domains folder.");
		}

		Path file = Paths.get(template.replace("_temp" + languageKey, languageKey));
		try {
			Files.write(file, text.getBytes());
		} catch (IOException e) {
			log.severe("NLG File Error: NLG File cannot be created. Please check the "
					+ "NLG template (" + template + ") and domains folder.");
		}

	}

	/**
	 * Registers all language models by creating the NLG files from intents
	 */
	public static void registerNLGFiles() {
		prepareNLGModel("_en");
		prepareNLGModel("_it");
	}

}
