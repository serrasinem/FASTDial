package fastdial.clientmodel;

/**
 * Request message types sent by the backend to the FASTDial
 * BEGIN_SESSION: to initialize a dialogue session
 * USER_UTTERANCE: to push a new user utterance to the dms
 * QUERY_RESPONSE: to send the response of a KB_QUERY or VALIDATION_QUERY
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public enum MessageType {
    BEGIN_SESSION, USER_UTTERANCE, QUERY_RESPONSE;
}