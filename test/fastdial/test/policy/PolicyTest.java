package fastdial.test.policy;

import java.util.logging.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import opendial.DialogueSystem;
import opendial.bn.values.SetVal;
import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;
import opendial.domains.Domain;
import opendial.modules.DialogueRecorder;
import opendial.readers.XMLDomainReader;

import org.junit.Test;

import fastdial.dialoguepolicy.PolicyManager;

public class PolicyTest {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	public static final String domainFile = "test/resources/FASTDomain.xml";

	Domain domain = XMLDomainReader.extractDomain(domainFile);

	@Test
	public void dialogue() throws InterruptedException {
		DialogueSystem system = new DialogueSystem(domain);
		system.getSettings().showGUI = false;

		PolicyManager b = new PolicyManager(system, false);
		system.attachModule(b);
		system.startSystem();

		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<interaction><systemTurn><variable id=\"u_m\">"
						+ "<value>Welcome to FASTDial. How can I help you today?</value></variable></systemTurn>"));
		Map<String, Double> u_u = new HashMap<String, Double>();
		u_u.put("do some testing", 1.0);
		system.addUserInput(u_u);

		assertEquals("InformIntent:Test", system.getContent("api").getBest().toString());
		u_u.clear();
		system.addContent("api_r", "is_valid:true");
		assertEquals("InitialInfoService:key_list_call",
				system.getContent("api").getBest().toString());
		Collection<Value> vals = new HashSet<Value>();
		vals.add(ValueFactory.create("response:success"));
		vals.add(ValueFactory.create("{key_list_call:['a1','b1','c1']}"));
		SetVal o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertEquals("InitialInfoService:key_list_call2",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("response:success"));
		vals.add(ValueFactory.create("{key_list_call2:['d1','e1','f1']}"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		System.out.println(system.getModule(DialogueRecorder.class).getRecord());
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>Do you want to make an instant test?</value></variable>"));
		u_u.clear(); 
		u_u.put("yes exactly", 1.0);
		system.addUserInput(u_u);
		assertEquals(1.0, system.getContent("a_u").getProb("[Confirm]"), 0.01);
		assertEquals("ValidateService:check_test_allowed:yes",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("is_valid:true"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>Question1?</value></variable>"));
		u_u.clear(); 
		u_u.put("a1", 1.0);
		system.addUserInput(u_u);
		assertEquals("ValidateService:api_call_1:a1",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("is_valid:true"));
		vals.add(ValueFactory.create("_slot_:a1"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertEquals(system.getContent("List1").getBest().toString(),"a1");
		assertEquals("InfoService:api_call_2",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("response:success"));
		vals.add(ValueFactory.create("InfoVariableNeeded:Requested Info"));
		vals.add(ValueFactory.create("Variable1:var1"));
		vals.add(ValueFactory.create("Variable2:var2"));

		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertEquals("Requested Info",
				system.getContent("InfoVariableNeeded").getBest().toString());
		//NEW SLOT
		assertEquals("StringList",system.getContent("current_step").getBest().toString());
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>Question2?</value></variable>"));
		u_u.clear(); 
		u_u.put("d1", 1.0);
		system.addUserInput(u_u);
		assertEquals("ValidateService:api_call_3:d1",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("is_valid:true"));
		vals.add(ValueFactory.create("_slot_:d1"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>The variable Requested Info is needed. Currency Question?</value></variable>"));
		u_u.clear(); 
		u_u.put("blabla bla 100 euro bla bla", 1.0);
		system.addUserInput(u_u);
		assertEquals("ValidateService:api_call_4:100",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("is_valid:true"));
		vals.add(ValueFactory.create("_slot_:100"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>Question:add a free string?</value></variable>"));
		u_u.clear(); 
		u_u.put("yeah sure", 1.0);
		system.addUserInput(u_u);
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>Keep all string:</value></variable>"));
		u_u.clear(); 
		u_u.put("some text some more text, even more text, I cannot believe it!", 1.0);
		system.addUserInput(u_u);
		assertEquals("ValidateService:api_call_5:some text some more text, even more text, i cannot believe it!",
				system.getContent("api").getBest().toString());
		vals.clear();
		vals.add(ValueFactory.create("is_valid:true"));
		vals.add(ValueFactory.create("_slot_:some text some more text, even more text, i cannot believe it!"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		System.out.println(system.getContent("a_u").getBest().toString());
		assertEquals("Ground(Test,Done)", system.getContent("a_m").getBest().toString());
		assertEquals("ConfirmIntent", system.getContent("current_step").getBest().toString());
		u_u.clear(); 
		u_u.put("ok, I confirm", 1.0);
		system.addUserInput(u_u);
		assertEquals("Ground(Test)", system.getContent("a_m").getBest().toString());
		assertEquals("ConfirmIntent",system.getContent("current_step").getBest().toString());
		assertEquals("Execute",
				system.getContent("api").getBest().toString());

		vals.clear();
		vals.add(ValueFactory.create("response:success"));
		o = ValueFactory.create(vals);
		system.addContent("api_r", o);
		assertTrue(system.getModule(DialogueRecorder.class).getRecord()
				.contains("<variable id=\"u_m\"><value>OK, your test is done. Can I help you with anything else?</value></variable>"));

	}
}
