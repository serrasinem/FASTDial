package fastdial.interfaces.telegram;

/**
 * Imitation of a simple bank transfer 
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class Transfer {

	String transferId = null;
	String toOrFrom = null;
	String account = null;
	String partner = null;
	String note = null;
	String amount = null;
	String date = null;
	String blockable = null;
	
	public Transfer() {}

	public Transfer(String transferId, String toOrFrom, String account, String partner, String note, String amount,
			String date, String blockable) {
		super();
		this.transferId = transferId;
		this.toOrFrom = toOrFrom;
		this.account = account;
		this.partner = partner;
		this.note = note;
		this.amount = amount;
		this.date = date;
		this.blockable = blockable;
	}
	
	public boolean CheckTransfer(Transfer temp) {
		if(temp.account != null)
		{ 
			if(!this.account.toLowerCase().equals(temp.account.toLowerCase()))
				return false;
		}
		if(temp.toOrFrom != null) 
		{
			if(!this.toOrFrom.toLowerCase().contains(temp.toOrFrom.toLowerCase()))
				return false;
		}
		
		if(temp.partner != null)
		{
			if(!this.partner.toLowerCase().equals(temp.partner.toLowerCase()))
				return false;
		}
		
		if(temp.amount != null)
		{
			if(!this.amount.toLowerCase().equals(temp.amount.toLowerCase()))
				return false;
		}
		
		if(temp.blockable != null)
		{
			if(!this.blockable.toLowerCase().equals(temp.blockable.toLowerCase()))
				return false;
		}
		
		return true;
	}
	
	public void print() {
		System.out.println(toOrFrom+account+partner);
	}

}
