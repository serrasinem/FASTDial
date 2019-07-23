package fastdial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * The DMS property reader such as filePaths and other general settings.
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */ 
public class FastProperties {

    // logger
    final static Logger log = Logger.getLogger("FastLogger");

    // dms properties
    public Properties props = null;

    /**
     * Reads the properties in /wtpwebapps/cbf.properties
     */
    public FastProperties() {
	String base = System.getProperty( "catalina.base" );
	if(base != null) {
	 
	//change to webapps only wtpwebapps
	String propFileName = base+"/wtpwebapps/cbf.properties";
	InputStream inputStream;
	try {
	    props = new Properties();
	    inputStream =  new FileInputStream(propFileName);
	    if (inputStream != null) 
		props.load(inputStream);
	   
	    try {
		inputStream.close();
	    } catch (IOException e) {
		log.warning("Property file cannot be closed properly. Please make sure "
			+ "its content is intact and valid.");
	    }
	} catch (Exception e) {
	    log.severe("Property file can not be found in the classpath:"+propFileName);
	}
	}
	else {
		
		String propFileName = "resources/telegram.properties";
		InputStream inputStream;
		try {
		    props = new Properties();
		    inputStream =  new FileInputStream(propFileName);
		    if (inputStream != null) 
			props.load(inputStream);
		   
		    try {
			inputStream.close();
		    } catch (IOException e) {
			log.warning("Property file cannot be closed properly. Please make sure "
				+ "its content is intact and valid.");
		    }
		} catch (Exception e) {
		    log.severe("Property file can not be found in the classpath:"+propFileName);
		}
	}
    }

    /**
     * Returns the property value given its key
     * 
     * @param propKey property key
     * @return the property value
     */
    public String getProperty(String propKey) {
	String base = System.getProperty( "catalina.base");
	if(base == null)
	    return props.getProperty(propKey);
	return props.getProperty(propKey).replace("{catalina_base}",base);
    }
}