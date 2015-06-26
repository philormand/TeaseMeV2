package org.milo.download;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class App 
{
	/*
	 This is where it all starts from
	 main will create and display the first shell (window) 
	 */
	private static Logger logger = LogManager.getLogger();
	
    
	public static void main(String[] args)
    {
        try {
            logger.trace("Enter main");

			Properties properties = java.lang.System.getProperties();
			Iterator<Object> it = properties.keySet().iterator();
			//display all the jvm properties in the log file
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				String value = String.valueOf(properties.get(key));
				//write out at error level even though it is a debug message
				//so we can turn it on, on a users machine
				logger.error(key + " - " + value);
			}
			String version;
			version = ComonFunctions.getVersion();
			logger.error("Download Version - " + version);
			logger.error("Charset " + Charset.defaultCharset().displayName());
			
            System.setProperty("jsse.enableSNIExtension", "false");
            
      		Display display;

   			display = new Display();
      		
      		logger.trace("create main shell");
            MainShell mainShell = new MainShell();
            logger.trace("create shell");
            Shell shell = mainShell.createShell(display);
            logger.trace("open shell");
            shell.open();
            //loop round until the window is closed
            while (!shell.isDisposed())
              if (!display.readAndDispatch())
              {
                display.sleep();
              }
          }
          catch (Exception ex) {
            logger.error("Main error " + ex.getLocalizedMessage(), ex);
          }
          logger.trace("Exit main");
    }
}
    