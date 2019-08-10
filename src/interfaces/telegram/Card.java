package fastdial.interfaces.telegram;

/**
 * Imitation of a simple bank card
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class Card {

	String cardName = null;
	String account = null;
	String cardNumber = null;
	String dailyLimit = null;
	String monthlyLimit = null;
	String fee = null;

	public Card() {
	}

	public Card(String cardName, String account, String cardNumber, String dailyLimit, String monthlyLimit,
			String fee) {
		this.cardName = cardName;
		this.account = account;
		this.cardNumber = cardNumber;
		this.dailyLimit = dailyLimit;
		this.monthlyLimit = monthlyLimit;
		this.fee = fee;
	}

	public boolean checkCard(Card temp) {
		if (temp.cardName != null) {
			if (!temp.cardName.toLowerCase().equals(this.cardName.toLowerCase()))
				return false;
		}
		if (temp.cardNumber != null) {
			if (!temp.cardNumber.equals(this.cardNumber))
				return false;
		}
		if (temp.account != null) {
			if (!temp.account.toLowerCase().equals(this.account.toLowerCase()))
				return false;
		}

		return true;
	}

}
