package fastdial.slots;

import java.util.logging.Logger;

/**
 * Keeps the state of a slot: 1) String value, 2) if slot value is validated,
 * 3) error_code if not validated, and 4) if the slot is skipped and won't be 
 * filled
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class SlotState {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	// Slot value
	private String value;
	// If the slot value is valid/invalid
	private Boolean validated = null;
	// Error code if the value is invalid
	private String errorCode;
	// If the slot is skipped due to a confirmation
	private boolean skipped = false;

	/**
	 * Returns the slot value 
	 * 
	 * @return slot value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the slot value
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns True if the slot value is valid 
	 * 
	 * @return validated
	 */
	public Boolean getValidated() {
		return validated;
	}

	/** 
	 * Sets the validated flag
	 * 
	 * @param validated
	 */
	public void setValidated(Boolean validated) {
		this.validated = validated;
	}

	/**
	 * Returns the slot validation error code that is agreed through middleware
	 * 
	 * @return error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets the validation error code 
	 * 
	 * @param error_code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Returns true if the slot is skipped 
	 * 
	 * @return skipped true if skipped
	 */
	public boolean getSkipped() {
		return skipped;
	}

	/**
	 * Sets the skipped flag
	 * 
	 * @param skipped true if the slot is skipped
	 */
	public void setSkipped(boolean skipped) {
		this.skipped = skipped;
	}

}
