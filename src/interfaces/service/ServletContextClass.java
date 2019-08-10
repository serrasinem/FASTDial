package fastdial.interfaces.service;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import fastdial.FastLogger;
import fastdial.modules.NLGLoader;

/**
 * DMS Server Servlet Context
 *
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 *
 */
public class ServletContextClass implements ServletContextListener {

    // logger
    final static Logger log = Logger.getLogger("FastLogger");

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
	NLGLoader.registerNLGFiles();
	try {
	    FastLogger.setup();
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException("Problems with creating the log files");
	}

	System.out.println("I am registered and initialized");
	log.info("The DMS server is up and running.");
    }
}
