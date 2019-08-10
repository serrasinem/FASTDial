package fastdial;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The Logger for the FASTDial
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class FastLogger {
    // formatter for the log text
    static private SimpleFormatter formatterTxt;
    
    // log file handler
    static private FileHandler fh;  
    
    /**
     * Sets up the format of FASTDial Logger. The log files can be found in 
     * the folder defined in logPath 
     * @throws IOException
     */
    static public void setup() throws IOException {
        Logger logger = Logger.getLogger("FastLogger");
        logger.setUseParentHandlers(false);
        LocalDateTime ldt = LocalDateTime.now();
        FastProperties properties = new FastProperties();
    	String logFolder = properties.getProperty("logPath");
        fh = new FileHandler(logFolder+ldt.toLocalDate().toString()+".log", true);  
        																												
        formatterTxt = new SimpleFormatter();
        fh.setFormatter(formatterTxt);
        logger.addHandler(fh);
    }

}

