package fastdial.interfaces.telegram;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fastdial.clientmodel.APIRequest;
import fastdial.clientmodel.APIResponse;
import fastdial.clientmodel.RequestState;
import fastdial.clientmodel.ResponseMessageType;
import opendial.bn.values.Value;

/**
 * Simulation of a midware, sending requests to DMS, handling 
 * the DMS responses
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class MidwareSimulation {

	/**
	 * Searches a card with defined properties
	 * 
	 * @param temp
	 * @return card if found, else null
	 */
	private static Card findCard(Card temp) {
		for (Card c : getCardList().values()) {
			if (c.checkCard(temp))
				return c;
		}
		return null;
	}

	/**
	 * Returns account name list
	 * @return accounts list of account names
	 */
	public static ArrayList<String> getAccountList() {
		ArrayList<String> accounts = new ArrayList<String>();
		accounts.add("saving");
		accounts.add("checking");
		return accounts;
	}

	/**
	 * Returns accounts
	 * 
	 * @return accounts
	 */
	public static HashMap<String, Account> getAccounts() {
		HashMap<String, Account> accounts = new HashMap<String, Account>();

		Account n = new Account("saving", null, "3000", "€", "1000");
		Card c = new Card("Credit", "saving", "23456", "1500", "4000", "5");
		n.addcard(c);
		c = new Card("Debit", "saving", "34567", "1000", "5000", "5");
		n.addcard(c);
		c = new Card("Prepaid", "saving", "78900", "1500", "3000", "4");
		n.addcard(c);
		accounts.put("saving", n);

		n = new Account("checking", null, "5000", "€", "2000");
		c = new Card("Credit", "checking", "12345", "1000", "3000", "3");
		n.addcard(c);
		accounts.put("checking", n);

		return accounts;
	}

	/**
	 * Returns the recipients
	 * 
	 * @return recipients list of recipient names
	 */
	public static ArrayList<String> getSavedRecipients() {
		ArrayList<String> saved_keys = new ArrayList<String>();
		saved_keys.add("Mom");
		saved_keys.add("Dad");
		saved_keys.add("Tiziano Ferro");
		saved_keys.add("Roberto Benigni");
		saved_keys.add("Monica Bellucci");
		return saved_keys;
	}

	/**
	 * Returns card list
	 * 
	 * @return cards list of bank cards
	 */
	public static HashMap<String, Card> getCardList() {
		HashMap<String, Card> cards = new HashMap<String, Card>();
		Card card1 = new Card("Credit", "checking", "12345", "1000", "3000", "3");
		Card card2 = new Card("Credit", "saving", "23456", "1500", "4000", "5");
		Card card3 = new Card("Debit", "saving", "34567", "1000", "5000", "5");
		Card card4 = new Card("Prepaid", "saving", "78900", "1500", "3000", "4");
		cards.put("12345", card1);
		cards.put("23456", card2);
		cards.put("34567", card3);
		cards.put("78900", card4);
		return cards;
	}

	/**
	 * Returns card names
	 * 
	 * @return cards
	 */
	public static ArrayList<String> getCardNames() {
		ArrayList<String> cards = new ArrayList<String>();
		cards.add("Credit");
		cards.add("Debit");
		cards.add("Prepaid");
		return cards;
	}

	/**
	 * Handles the response of the DMS and returns a new API request
	 * to DMSs
	 * 
	 * @param APIResponse
	 * @param stateVariables
	 * @param slotValues
	 * @return APIRequest to be sent back to DMS
	 */
	public static APIRequest callAPI(APIResponse a, HashMap<String, String> stateVariables,
			Map<String, Value> slotValues) {
		APIRequest r = new APIRequest();
		Boolean simulateError = false;
		if (a.getMessage_type() == ResponseMessageType.VALIDATION_QUERY)
			r = handleInformationRequest(a, simulateError, stateVariables, slotValues);
		else if (a.getMessage_type() == ResponseMessageType.EXECUTE_INTENT)
			r = handleExecution(a, simulateError, stateVariables, slotValues);
		else if (a.getMessage_type() == ResponseMessageType.KB_QUERY)
			r = handleDMSQuery(a, slotValues);
		else if (a.getMessage_type() == ResponseMessageType.NOTIFY_INTENT)
			r.setState(RequestState.NOTIFY_INTENT_SUCCESS);

		return r;
	}

	/**
	 * Handles the intent execution requests of the DMS
	 * 
	 * @param a API response of the DMS
	 * @param simulateError if an error is simulated
	 * @param stateVariables current state variables
	 * @param slotValues currently filled slot values
	 * @return APIRequest to be sent back to DMS
	 */
	private static APIRequest handleExecution(APIResponse a, Boolean simulateError,
			HashMap<String, String> stateVariables, Map<String, Value> slotValues) {
		APIRequest r = new APIRequest();
		String i = a.getIntent();
		String response = "";
		r.setState(RequestState.EXECUTE_INTENT_SUCCESS);
		r.setMessage("{}");
		if (i.equals("AccountBalance")) {
			String account = slotValues.get("Account").toString();
			Account b = getAccounts().get(account);
			response = "{'Amount':" + b.getBalance() + ",'CurrencyType':" + b.getCurrency() + "}";

		} else if (i.equals("Transfer")) {

		} else if (i.equals("Authentication")) {

		} else if (i.equals("OrderPizza")) {

		} else if (i.equals("InfoCardLimit")) {
			Card temp = new Card();
			temp.account = slotValues.get("Account").toString();
			temp.cardName = slotValues.get("CardName").toString();
			String period = slotValues.get("TimePeriod").toString();
			Card found = findCard(temp);

			if (found == null)
				simulateError = true;
			else {
				if (period.toLowerCase().equals("daily") || period.toLowerCase().equals("giornaliero")
						|| period.toLowerCase().equals("napi"))
					response = "{\"Amount\":" + found.dailyLimit + ",\"CurrencyType\":€}";
				if (period.toLowerCase().equals("monthly") || period.toLowerCase().equals("mensile")
						|| period.toLowerCase().equals("havi"))
					response = "{\"Amount\":" + found.monthlyLimit + ",\"CurrencyType\":€}";
			}

		}

		r.setMessage(response);
		if (simulateError)
			r.setState(RequestState.EXECUTE_INTENT_FAILED);
		return r;
	}

	/**
	 * Handles the information requests of DMS
	 * 
	 * @param a APIResponse of DMS
	 * @param simulateError if an error is simulated
	 * @param stateVariables variables of the current intent state
	 * @param slotValues currently filled slot values
	 * @return APIRequest new API request to be sent back to DMS
	 */
	private static APIRequest handleInformationRequest(APIResponse a, Boolean simulateError,
			HashMap<String, String> stateVariables, Map<String, Value> slotValues) {
		APIRequest r = new APIRequest();
		String response = "";
		r.setState(RequestState.VALIDATION_SUCCESS);
		String APIName = a.getInformation_type();
		r.setInformation_type(APIName);

		if (APIName.equals("check_transfer_allowed")) {
			response = "yes";
			if (simulateError)
				response = "error_code:not_allowed";

		} else if (APIName.equals("drink_addable")) {
			String d = a.getMessage();
			if (!d.equals("yes"))
				response = d + ",DrinkText:'without any drink '";
			else
				response = d;
		}

		else if (APIName.equals("check_account")) {
			String accountName = a.getMessage();
			response = accountName;
			if (!getAccountList().contains(accountName))
				simulateError = true;
			if (simulateError)
				response = "error_code:no_account";
		} else if (APIName.equals("check_card_name")) {
			System.out.println("called card name" + response);
			response = a.getMessage();
			Card temp = new Card();
			temp.cardName = response;
			if (slotValues.containsKey("Account"))
				temp.account = slotValues.get("Account").toString();
			if (findCard(temp) == null)
				simulateError = true;
			if (simulateError)
				response = "error_code:no_cardname;";
			else {
				response = temp.cardName + ",CardIdentifier:" + findCard(temp).cardNumber;
			}

		} else if (APIName.equals("check_enough_funds_and_limit")) {
			response = a.getMessage();
			String amount = response;
			amount = amount.replaceAll("[^0-9]", "");
			BigInteger moneyVal = new BigInteger(amount);
			if (stateVariables.containsKey("AccountTransferLimit")) {
				String transferLimit = stateVariables.get("AccountTransferLimit").replaceAll("[^0-9]", "");
				String balance = stateVariables.get("AccountBalance").replaceAll("[^0-9]", "");
				if (moneyVal.compareTo(new BigInteger(transferLimit)) == 1
						|| moneyVal.compareTo(new BigInteger(balance)) == 1) {
					simulateError = true;
					response = "error_code:limit_exceeded";
				}
			} else {
				simulateError = true;
				response = "error_code:dependency_needed";
			}

		} else if (APIName.equals("check_card_number_last10")) {
			response = a.getMessage();
			String cardNumber = response;
			if (!getCardList().containsKey(cardNumber)) {
				simulateError = true;
				response = "error_code:not_matched";
			} else {
				Card found = getCardList().get(cardNumber);
				response += ",Account:" + found.account + ",CardName:" + found.cardName;
			}
		} else if (APIName.equals("check_telecode")) {
			if (simulateError)
				response = "error_code:no_telecode";
		} else if (APIName.equals("check_secretcode")) {
			if (simulateError)
				response = "error_code:no_secretcode";
		} else if (APIName.equals("can_order")) {
			response = a.getMessage();
			response += ",Price:'100 Euro',ReturnText:'R',TotalCost:" + Integer.parseInt(a.getMessage()) * 100;
			if (simulateError)
				response = "error_code:order_not_possible";
		} else if (APIName.equals("check_period")) {
			response = a.getMessage();
			if (simulateError)
				response = "error_code:period";
		} else if (APIName.equals("check_partner")) {
			response = a.getMessage();
			String recipientName = response;
			if (!getSavedRecipients().contains(recipientName))
				simulateError = true;
			if (simulateError)
				response = "error_code:no_recipient;";
		} else if (APIName.equals("savePost")) {
			response = a.getMessage();
			if (simulateError)
				response = "error_code:not_saved";
		} else if (APIName.equals("canOrder")) {
			response = a.getMessage();
		} else
			r.setMessage(a.getMessage());
		// System.out.println("Response:"+response);
		r.setMessage(response);
		if (simulateError)
			r.setState(RequestState.VALIDATION_FAILED);
		return r;
	}

	/**
	 * Handles the query calls of DMS 
	 *  
	 * @param a APIResponse of DMS
	 * @param slotValues currently filled slot values of the intent
	 * @return APIRequest to send back to DMS
	 */
	private static APIRequest handleDMSQuery(APIResponse a, Map<String, Value> slotValues) {
		APIRequest r = new APIRequest();
		r.setState(RequestState.QUERY_SUCCESS);
		String call = a.getMessage();
		r.setInformation_type(call);
		r.setMessage("");

		if (a.getMessage().equals("account_info")) {
			String name = slotValues.get("Account").toString();
			String i = getAccounts().get(name).getInfo();
			r.setMessage(i);
		} else if (a.getMessage().equals("drink_text")) {
			String d = slotValues.get("Drink").toString();
			String num = slotValues.get("NbDrink").toString();
			r.setMessage("{'DrinkText':'" + num + " " + d + "'}");
		} else if (a.getMessage().equals("account_list"))
			r.setMessage("[" + String.join(",", getAccountList()) + "]");
		else if (call.equals("saved_recipient_list"))
			r.setMessage("[" + String.join(",", getSavedRecipients()) + "]");
		else if (call.equals("card_list")) {
			String name = slotValues.get("Account").toString();
			String i = getAccounts().get(name).getCardNames();
			r.setMessage("[" + i + "]");
		}
		else if (call.equals("key_list_call")) {
			r.setMessage("['a1','b1','c1']");
		}
		else if (call.equals("key_list_call2")) {
			r.setMessage("['a2','b2','c2']");
		}

		return r;
	}

}
