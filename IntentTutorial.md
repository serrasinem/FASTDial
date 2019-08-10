#FASTDial Intent Design and Registration

Below are the basic instructions on how to write an intent in FASTDial.

- Intents are designed and created as JSON files.
- All intents must be kept in the folder/FASTDial/WebContent/WEB-INF/intents_json
- The system registers all possible available intents to the system when the server context initializes. 
- The intent file should be named by adding the language extension: for English "IntentFileName_EN.json",
and for Italian "IntentFileName_IT.json".

##*************************The Content of an Intent File******************************

An intent json file should contain the below variables:

- name: defines the name of the intent. Any string can be an intent name. It should be unique among all the registered intents in the same language.

- definition: a simple intent definition. This text is also added to the machine utterance while providing a list
of possible tasks to the user.

- keys: defines the keywords are the basic NLU for understanding the intent. It is a comma separated list of regular expressions.

- confirmation: a boolean value determines if a the execution of the intent requires a confirmation from the user.

- confirmation_question : if a confirmation is required for the execution of the intent, the confirmation question must be saved in this parameter. The string may include any slot name or variables (must be already assigned) in the format of "{VariableName}.

- execution_call: the final execution APICall. API formalism should be agreed upon with the middleware.

- success_message: the message to be shown to the user when the execution of the intent is successful. The string may include any slot name or variables (must be already assigned) in the format of "{VariableName}.

- error_message: the message to be shown to the user when the execution of the intent is failed. The string may include any slot name or variables (must be already assigned) in the format of "{VariableName}.

- direct_helpline: a boolean value determines if the failure of the intent execution redirects the user to the helpline.

- slots: the 'ordered' list of slot objects that are needed to be filled to execute the intent.

-- slot object:

--- slot_name: should be unique for the intent. Multiple intents can have the same slot name.
--- slot_type: implemented Slot types are; Confirmation, Currency, Date, String, Numeric, StringList
--- constraint: depends on the slot type. The author should be very careful about the constraint definition, 
	since it determines the conditions necessary for the natural language understanding module.
	
	* Confirmation_constraint: comma separated positive keys + semicolon + comma separated negative keys + semicolon + action:ActionName
		e.g. "now,yes,ok,today,positive,correct,right,sure;no,not now,not today,negative,tomorrow;action:directHelpLine"
		in case of a negative user utterance, action can take the values "action:skipNext" or "action:directHelpLine".
		- skipNext will jump over the next slot filling. It is useful when the following slot is optional for the user. e.g, "Would you like to add a note?"
		- directHelpLine redirects the user to the operator.
		
	* Currency_constraint: The amounts and monetary values are handled through Currency slot. The default currency constraint type is "€".
	 
	* Date_constraint : Dates can be constraint into an earlier or later than a date. The constraint value should be registered with "<" for
		earlier and ">" for a later date value that is following the character. 
		- verbal date constraints: tomorrow, today, now, yesterday, next week, last week . e.g., "<now"
		- value date constraints: "year-month-day". e.g.,">2018-05-27"
		
	* String_constraint : There is only a max_len constraint for the String slot type. Basically, it will record any given user message as the slot value. 
	
	* StringList_constraint : 
		- _keys_ : the constraint should include the "_keys_" keyword.
			1. to define predefined list of slot values should be registered with "_keys_" keyword. e.g., "_keys_:daily,monthly"
			2. to define an api call to the middleware to retrieve the user specific keys. e.g.,"_keys_:account_list" to get the accounts of the current user. API call list should be agreed upon with the middleware. "_keys_" keyword can also be used within the machine utterance to be filled during the dialogue. e.g., "You have {_keys_} accounts. Which of your accounts would you like to query?"
	
	* Numeric_constraints: Numeric slot type can have 3 constraints.
		- digits: determines the number of the digits that the slot value must contain. E.g., "digits:10"
		- < : determines the numeric value that the slot value should be smaller than. E.g., "<100"
		- > : determines the numeric value that the slot value should be bigger than. E.g., ">100"		

--- question : the question to be shown to the user to fill the slot value. The string may include any slot name or variables (must be already assigned) in the format of "{VariableName}

--- validation_api : the validation api name to call after filling the slot value. Each slot should be verified after the receiving the value from the user in order to continue to the next slot, unless specified. For the slots that do not have to be verified, this variable should be "NoValidate".

--- extra_info_api: an info request api call name if extra information from the middleware is required after the validation of the slot value.

--- error_message: the error messages to be shown when the slot value cannot be detected from the user utterance or cannot be validated. The error messages should be defined as a json object of error_key:text pairs. Every slot must have a "not_matched" key. An example error message list:
	{"not_matched":"The amount cannot be processed. Please provide the amount of money with the currency such as '€500 or 500 dollars'.",
				"limit_exceeded":"You have exceeded the amount that you can send."}
For the StringList slot, the predefined or api-retrieved keywords are attached at the end of the not_matched text automatically. Therefore, it is better to keep an error_message similar to "We cannot detect a 'slot value', you may choose among the options: ".
	
--- mandatory: a boolean value determining if the slot must be filled in order to execute the user intent.

--- regex: the list of a modified regular expressions that are used for extracting the slot value. Each regex should contain "{slot}" string, which is the filler for the expected slot value. E.g., "from {slot}, from my {slot} account". This parameter is significant, especially for the slot filling from the intent utterance*. For the Confirmation slot type, this parameter can also be defined without {slot} variable, as "comma_separated_positive_expressions;comma_separated_negative_expressions" to extract the slot value.
	
--- dependency: a slot name which is defined prior to the current slot. For instance, the bank card names are dependent to the account of the user, therefore, the card list slot should have a dependency to the account slot. E.g., "dependency":"AccountType"

*Intent utterance is defined as the first utterance that the bot retrieves from the user after the greeting. At this step, it will try to identify the user intent.

Authentication is designed as a separate intent.

The content of an example intent file:
--------------------------------------
	{
	"name":"AccountBalance",
	"description":"account balance query",
	"keys":"balance,how much money do i have,how much money .* there,how much,amount,money .*enough",
	"confirmation":false,
	"execution_call":"execute_balance",
	"slots": [{
		"slot_name":"Account",
		"slot_type": "StringList",
		"constraint":"_keys_:account_list",
		"question":"You have {_keys_} accounts. Which of your accounts would you like to query?",
		"validation_api": "check_account",
		"error_message": {"not_matched":"I couldn't find such an account. Your account list is: ",
						  "no_account":"There is no account with the given name."},
		"mandatory": true,
		"regex":"from {slot}, in my {slot}, in {slot} account"
	}],
	"success_message":"The current balance on your {Account} account is {Amount} {CurrencyType}. Can I help you with anything else?",
	"error_message": "There was an error while we are checking your balance in the account {Account}. Can I help you with anything else?",
	"direct_helpline": false
	}

In the Account Balance intent, {Amount} and {CurrencyType} variables must be returned by the middleware as the response of the intent execution.


#FASTDial-Middleware Interaction Protocol

##Dialogue Flow

Simple Pseudo-flow of a bot (Server) and a client (Middleware) interaction

Starting a session without authentication phase:
Client: Init session
	*{
    	"session_id": "A_VALID_UUID, e.g. aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "BEGIN_SESSION",
    "message": "disable_auth",
    "information_type": null,
    "state": null,
    "lang": ""
	}
Server: User Greeting
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "MESSAGE",
    "message": "Welcome to FASTDial. How can I help you today?"
}

Client: Intent Utterance
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "USER_UTTERANCE",
    "message": "can I check my account balance?"
}
Server: Intent identification and intent notification
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "NOTIFY_INTENT",
    "message": "intent_check",
    "intent": "AccountBalance"
}
Client: Intent notification success/fail
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "QUERY_RESPONSE",
    "state": "NOTIFY_INTENT_SUCCESS",
    "message":"AccountBalance",
}
Server: Initial queries if necessary
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "KB_QUERY",
    "message": "account_list",
    "intent": "AccountBalance"
}
Client: Query responses
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "QUERY_RESPONSE",
    "state": "QUERY_SUCCESS",
    "information_type":"account_list",
    "message":"['saving','checking']"
}
Server: Starts filling the slots by asking single question at a time
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "MACHINE_UTTERANCE",
    "message": "You have saving, checking accounts. Which of your accounts would you like to query?"
}
Client: Slot filling answer
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "USER_UTTERANCE",
    "message": "checking please"
}
Server: Slot Value Validation API call
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "VALIDATION_QUERY",
    "information_type": "check_account",
    "message": "checking",
    "intent": "AccountBalance"
}

Client: Validation success/fail
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "QUERY_RESPONSE",
    "state": "VALIDATION_SUCCESS",
    "information_type": "check_account",
    "message": "checking"
}
Server: If all slots are filled, send execution call
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "EXECUTE_INTENT",
    "intent": "AccountBalance"
}

Client: Execution success/fail
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "QUERY_RESPONSE",
    "state": "EXECUTE_INTENT_SUCCESS",
    "message": {"Amount":100,"CurrencyType":"€"}
}

Server: Execution response utterance and restart question
{
    "session_id": "aaa111a1-1aaa-11aa-1a1a-1a1aa11a11a1",
    "message_type": "MACHINE_UTTERANCE",
    "message": "The current balance on your checking account is 100 \\u20ac. Can I help you with anything else?"
}


