package fastdial.interfaces.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.inject.Singleton;

import opendial.DialogueSystem;

/**
 * DMS Bot Sessions
 *
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 *
 */
@Singleton
public class SessionResource {

    // logger
    final static Logger log = Logger.getLogger("FastLogger");

    // static variable single_instance of type Singleton
    private static SessionResource single_instance = null;
    private ConcurrentHashMap<UUID, DialogueSystem> userlist;

    /**
     * Constructor of a session list 
     */
    public SessionResource() {
	this.userlist = new ConcurrentHashMap<UUID, DialogueSystem>();
    }

    /**
     * Returns the dialogue system of the given id
     * 
     * @param id
     * @return dialogue system
     */
    public DialogueSystem getID(UUID id) {
	return this.userlist.get(id);
    }

    /**
     * Static method to create instance of Singleton session resource
     * 
     * @return session resource
     */
    public static SessionResource getInstance() {
	if (single_instance == null)
	    single_instance = new SessionResource();

	return single_instance;
    }

    /**
     * Updates the dialogue system for the given id
     * 
     * @param id
     * @param system
     */
    public void putSession(UUID id, DialogueSystem system) {
	this.userlist.put(id, system);

    }

    /**
     * Returns true if the session for the given id is active
     * 
     * @param id
     * @return True if session exists
     */
    public boolean containsKey(UUID id) {
	return this.userlist.containsKey(id);
    }

    /**
     * Destroys the session (removes it from the session list object) with the given id
     * 
     * @param id
     */
    public void removeSession(UUID id) {
	this.userlist.remove(id);
    }

}
