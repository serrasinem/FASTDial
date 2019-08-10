package fastdial.interfaces.service;

import java.util.UUID;
import java.util.logging.Logger;

import fastdial.FastProperties;
import fastdial.dialoguepolicy.PolicyManager;
import opendial.DialogueSystem;
import opendial.domains.Domain;
import opendial.readers.XMLDomainReader;

/**
 * The representation of a dialogue session
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class Session {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	/**
	 * Returns the dialogue system given the id
	 * 
	 * @param id UUID id
	 * @return dialogue system
	 */
	public DialogueSystem getSystem(UUID id) {
		return SessionResource.getInstance().getID(id);
	}

	/**
	 * Creates and returns a new dialogue system 
	 * 
	 * @param lang
	 * @param authentication
	 * @return dialogue system
	 */
	public DialogueSystem runAgent(String lang, Boolean authentication) {
		FastProperties properties = new FastProperties();

		Domain domain;

		if (lang.equals("hungarian")) {
			domain = XMLDomainReader
					.extractDomain(properties.getProperty("domainPathHungarian"));
		} else if (lang.equals("italian")) {
			domain = XMLDomainReader
					.extractDomain(properties.getProperty("domainPathItalian"));
		} else {
			domain = XMLDomainReader
					.extractDomain(properties.getProperty("domainPathEnglish"));

		}
		DialogueSystem system = new DialogueSystem(domain);
		PolicyManager b = new PolicyManager(system, authentication);
		// Adding a new policy manager module
		system.attachModule(b);
		// Switching off the OpenDial GUI
		system.getSettings().showGUI = false;
		system.startSystem();

		return system;
	}

	/**
	 * Adds a new session to the session list and returns the greeting machine utterance
	 * 
	 * @param id UUID id
	 * @param lang 
	 * @param authentication
	 * @return machine utterance
	 */
	public String addUser(UUID id, String lang, Boolean authentication) {
		DialogueSystem system = this.runAgent(lang, authentication);
		SessionResource.getInstance().putSession(id, system);
		return system.getContent("u_m").getBest().toString();
	}

	/**
	 * Updates the dialogue system of the session
	 * 
	 * @param id UUID id
	 * @param system
	 */
	public void updateUser(UUID id, DialogueSystem system) {
		SessionResource.getInstance().putSession(id, system);

	}

	/**
	 * Returns true if the session exists
	 * 
	 * @param id UUID id
	 * @return true if active
	 */
	public boolean isActive(UUID id) {
		if (SessionResource.getInstance().containsKey(id))
			return true;
		else
			return false;
	}

	/**
	 * Removes the session with the given id
	 * 
	 * @param id UUID id
	 */
	public void disactivate(UUID id) {
		if (SessionResource.getInstance().containsKey(id))
			SessionResource.getInstance().removeSession(id);
	}

}
