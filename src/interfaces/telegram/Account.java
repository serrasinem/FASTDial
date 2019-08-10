package fastdial.interfaces.telegram;

import java.util.HashMap;
import java.util.Map;

/**
 * Imitation of a simple bank account 
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */

public class Account {

    String name;
    Map<String, Card> cards;
    String balance;
    String currency;
    String transferLimit;

    public Account(String name, HashMap<String, Card> cards, String balance,
	    String currency, String transferLimit) {
	this.name = name;
	this.cards = cards;
	if (cards == null)
	    this.cards = new HashMap<String, Card>();
	this.balance = balance;
	this.currency = currency;
	this.transferLimit = transferLimit;
    }

    public String getName() {
	return name;
    }

    public String getBalance() {
	return balance;
    }

    public String getCurrency() {
	return currency;
    }

    public Map<String, Card> getCards() {
	return cards;
    }

    public void addcard(Card c) {
	cards.put(c.cardNumber, c);
    }
    
    public String getInfo() {
	return "{'AccountTransferLimit':'"+transferLimit+currency+"','AccountBalance':'"+balance+currency+"'}";
    }
    
    public String getCardNames() {
	String[] names = new String[cards.size()];
	int i = 0;
	for(String k:cards.keySet()) {
	    names[i] = cards.get(k).cardName;
	    i++;
	}
	return String.join(",",names);
    }
}
