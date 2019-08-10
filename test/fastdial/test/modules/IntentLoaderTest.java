package fastdial.test.modules;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.logging.Logger;

import org.junit.Test;

import fastdial.modules.IntentLoader;
import fastdial.nlu.SlotTracker;

/**
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class IntentLoaderTest {

	final static Logger log = Logger.getLogger("FastLogger");
	public static final String intentFile = "WebContent/WEB-INF/intents_json/IntentTest_EN.json";

	@Test
	public void testLoadingIntent() {

		SlotTracker i1;
		try {
			i1 = IntentLoader.loadJSONIntent(intentFile, "Test");
			assertTrue(i1.getName().equals("Test"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
