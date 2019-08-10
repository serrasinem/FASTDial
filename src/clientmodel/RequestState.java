package fastdial.clientmodel;
/**
 * Request states sent by the backend to the FASTDial
 * 
 * VALIDATION_FAILED: when slot value is not valid
 * VALIDATION_SUCCESS: when slot value is valid
 * NOTIFY_INTENT_SUCCESS: the detected intent can be handled by the backend
 * NOTIFY_INTENT_FAILED: the detected intent is unknown or cannot be handled at the moment
 * QUERY_SUCCESS: when the KB query is successful
 * QUERY_FAILED: when the KB query is failed
 * EXECUTE_INTENT_SUCCESS: when the intent is executed successfully
 * EXECUTE_INTENT_FAILED: when the intent execution is failed
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public enum RequestState {
    VALIDATION_FAILED ,
    VALIDATION_SUCCESS ,
    NOTIFY_INTENT_FAILED ,
    NOTIFY_INTENT_SUCCESS ,
    QUERY_SUCCESS,
    QUERY_FAILED,
    EXECUTE_INTENT_SUCCESS,
    EXECUTE_INTENT_FAILED;
}
