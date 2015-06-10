package org.milo.download;

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
            //Sleak will help diagnose SWT memory leaks
            //if you set this to true you will get an additional window
            //that allows you to track resources that are created and not destroyed correctly

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
    