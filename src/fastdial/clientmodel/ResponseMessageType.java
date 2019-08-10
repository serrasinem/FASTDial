package fastdial.clientmodel;

/**
 * Response message types sent by FASTDial to the backend.
 * 
 * MACHINE_UTTERANCE: to send a machine utterance to the user
 * VALIDATION_QUERY: to request a validation of a detected slot value
 * END_SESSION: to finalize the session
 * HELPLINE: to redirect the user to a human operator
 * KB_QUERY: to request a knowledge base query
 * EXECUTE_INTENT: to execute an intent 
 * NOTIFY_INTENT: to inform the backend about the new user intent
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public enum ResponseMessageType {
	MACHINE_UTTERANCE ,
	VALIDATION_QUERY ,
	END_SESSION ,
	HELPLINE ,
	KB_QUERY ,
	EXECUTE_INTENT ,
	NOTIFY_INTENT;
}


