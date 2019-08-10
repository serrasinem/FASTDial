package fastdial.dialoguepolicy;

import java.util.Collection;
import java.util.logging.Logger;

import opendial.DialogueState;
import opendial.modules.Module;

/**
 * A helper module for the api call vs. machine utterance trigger control
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class ResponseReady implements Module {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	boolean paused = true;
	boolean notified = false;
	public String notifiedVar = "";

	@Override
	public boolean isRunning() {
		return !paused;
	}

	@Override
	public void pause(boolean toPause) {
		paused = toPause;
	}

	@Override
	public void start() {
		paused = false;
	}

	@Override
	public void trigger(DialogueState state, Collection<String> updatedVars) {
		if (updatedVars.contains("api") && state.hasChanceNode("api")) {
			notifiedVar = "api";
			notified = true;

		} else if (updatedVars.contains("u_m") && state.hasChanceNode("u_m")) {
			notifiedVar = "u_m";
			notified = true;
		}

	}

	public boolean getNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public String getNotifiedVar() {
		return notifiedVar;
	}

	public void setNotifiedVar(String notifiedVar) {
		this.notifiedVar = notifiedVar;
	}

}
