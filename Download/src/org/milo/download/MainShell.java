package org.milo.download;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class MainShell {
	/*
	 Main screen and UI thread.
	 exposes methods that allow other components to update the screen components
	 and play video and music
	 */
	private static Logger logger = LogManager.getLogger();
	private static org.eclipse.swt.graphics.Color colourBlack;
	private static org.eclipse.swt.graphics.Color colourWhite;
	private AppSettings appSettings;
	private MainShell mainShell;
	private Display myDisplay;
	private Shell shell;
	private ComonFunctions comonFunctions = ComonFunctions.getComonFunctions();
	private String strGuidePath;

	public Shell createShell(final Display display) {
		logger.trace("Enter createShell");
		// Initialise variable
		colourBlack = display.getSystemColor(SWT.COLOR_BLACK);
		colourWhite = display.getSystemColor(SWT.COLOR_WHITE);
		try {
			logger.trace("Get appSettings");
			appSettings = AppSettings.getAppSettings();

			// path to the xml files
			String strGuidePath = appSettings.getDataDirectory();

		} catch (NumberFormatException e) {
			logger.error("OnCreate NumberFormatException ", e);
		} catch (Exception e) {
			logger.error("OnCreate Exception ", e);
		}


		try {
			//Create the main UI elements
			logger.trace("display");
			myDisplay = display;
			logger.trace("shell");
			if (appSettings.isFullScreen()) {
				shell = new Shell(myDisplay, SWT.NO_TRIM);
			} else {
				shell = new Shell(myDisplay);
			}
			logger.trace("shell add listener");
			shell.addShellListener(new shellCloseListen());

			//get primary monitor and its size
			Monitor primary = display.getPrimaryMonitor();
			Rectangle clientArea = primary.getClientArea();
			shell.setText("Milo Download (" + ComonFunctions.getVersion() + ")");
			FormLayout layout = new FormLayout();
			shell.setLayout(layout);
			
			//Menu Bar
			Menu MenuBar = new Menu (shell, SWT.BAR);

			//Top Level File drop down
			MenuItem fileItem = new MenuItem (MenuBar, SWT.CASCADE);
			fileItem.setText ("&File");

			//Sub Menu for File
			Menu fileSubMenu = new Menu (shell, SWT.DROP_DOWN);
			//Associate it with the top level File menu
			fileItem.setMenu (fileSubMenu);

			//File Load menu item
			MenuItem fileLoadItem = new MenuItem (fileSubMenu, SWT.PUSH);
			fileLoadItem.setText ("&Load");
			fileLoadItem.addSelectionListener(new FileLoadListener());

			//File Preferences menu item
			MenuItem filePreferencesItem = new MenuItem (fileSubMenu, SWT.PUSH);
			filePreferencesItem.setText ("&Application Preferences");
			filePreferencesItem.addSelectionListener(new FilePreferences());

			//File Exit menu item
			MenuItem fileExitItem = new MenuItem (fileSubMenu, SWT.PUSH);
			fileExitItem.setText ("&Exit");
			fileExitItem.addListener (SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					logger.trace("Enter Menu Exit");
					shell.close();
					logger.trace("Exit Menu Exit");
				}
			});

			// Add the menu bar to the shell
			shell.setMenuBar (MenuBar);

			// tell SWT to display the correct screen info
			shell.pack();
			shell.setMaximized(true);
			shell.setBounds(clientArea);
		}
		catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
		logger.trace("Exit createShell");
		mainShell = this;
		return shell;
	}

	class shellCloseListen  extends ShellAdapter {
		// Clean up stuff when the application closes
		@Override
		public void shellClosed(ShellEvent e) {
			try {
				appSettings.saveSettings();
			}
			catch (Exception ex) {
				logger.error("shellCloseListen ", ex);
			}
			super.shellClosed(e);
		}

		public void handleEvent(Event event) {
		}
	}
	
	

	class FileLoadListener  extends SelectionAdapter {
		//File Load from the menu
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				logger.trace("Enter Menu Load");
				//display a dialog to ask for a guide file to play
				FileDialog dialog = new FileDialog (shell, SWT.OPEN);
				//TODO Need to change this here to implement the new html format
				String [] filterNames = new String [] {"XML Files"};
				String [] filterExtensions = new String [] {"*.xml"};
				dialog.setFilterNames (filterNames);
				dialog.setFilterExtensions (filterExtensions);
				dialog.setFilterPath (strGuidePath);
				String strFileToLoad;
				try {
					strFileToLoad = dialog.open();
					try {
						if (strFileToLoad != null) {
							//if a guide file has been chosen load it 
							//default the initial directory for future loads to the current one
							strGuidePath = dialog.getFilterPath() + appSettings.getFileSeparator();
							appSettings.setDataDirectory(strGuidePath);
							//load the file it will return the start page and populate the guide object
							//TODO Need to change this here to implement the new html format
							//String strPage = xmlGuideReader.loadXML(strFileToLoad, guide, appSettings);
						}
					}
					catch (Exception ex5) {
						logger.error("Process Image error " + ex5.getLocalizedMessage(), ex5);
					}
				}
				catch (Exception ex4) {
					logger.error("Load Image Dialogue error " + ex4.getLocalizedMessage(), ex4);
				}
			}
			catch (Exception ex3) {
				logger.error("Load Image error " + ex3.getLocalizedMessage(), ex3);
			}
			logger.trace("Exit Menu Load");
			super.widgetSelected(e);
		}

	}

	class FilePreferences  extends SelectionAdapter {
		//File Preferences from the menu
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				logger.trace("Enter FilePreferences");
				//display a modal shell to change the preferences
				Shell prefShell = new PreferenceShell().createShell(myDisplay, appSettings);
				prefShell.open();
				while (!prefShell.isDisposed()) {
					if (!myDisplay.readAndDispatch())
						myDisplay.sleep();
				}
			}
			catch (Exception ex) {
				logger.error(" FilePreferences " + ex.getLocalizedMessage());
			}
			super.widgetSelected(e);
		}

	}

}