package org.milo.download;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.snapps.swt.SquareButton;

public class MainShell {
	/*
	 Main screen and UI thread.
	 exposes methods that allow other components to update the screen components
	 and play video and music
	 */
	private static Logger logger = LogManager.getLogger();
	private AppSettings appSettings;
	private MainShell mainShell;
	private Display myDisplay;
	private Shell shell;
	private Text txtURL;
	private Label lblURL;
	private Label lbltitle;
	private Label lblmessage;
	private Browser brwsSearch;
	

	public void setLblmessage(String message) {
		this.lblmessage.setText(message);
	}

	public void setLbltitle(String title) {
		this.lbltitle.setText(title);
	}
	
	public Shell createShell(final Display display) {
		logger.trace("Enter createShell");
		// Initialise variable
		try {
			logger.trace("Get appSettings");

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
			shell = new Shell(myDisplay);
			logger.trace("shell add listener");
			shell.addShellListener(new shellCloseListen());

			//get primary monitor and its size
			Monitor primary = display.getPrimaryMonitor();
			Rectangle clientArea = primary.getClientArea();
			shell.setText("Milo Download (" + ComonFunctions.getVersion() + ")");
			FormLayout layout = new FormLayout();
			shell.setLayout(layout);

			brwsSearch = new Browser(shell, 0);
			FormData brwsSearchFormData = new FormData();
			brwsSearchFormData.top = new FormAttachment(shell,5);
			brwsSearchFormData.left = new FormAttachment(0,5);
			brwsSearchFormData.right = new FormAttachment(100,0);
			brwsSearchFormData.bottom = new FormAttachment(75, 0);
			brwsSearch.setLayoutData(brwsSearchFormData);

			lblURL = new Label(shell, SWT.LEFT);
			lblURL.setText("Milo URL:");
			FormData lblURLFormData = new FormData();
			lblURLFormData.top = new FormAttachment(brwsSearch,5);
			lblURLFormData.left = new FormAttachment(0,5);
			//lblTmpFormData.right = new FormAttachment(25,0);
			lblURL.setLayoutData(lblURLFormData);
			
			txtURL = new Text(shell, SWT.SINGLE);
			txtURL.setText("");
			FormData txtURLFormData = new FormData();
			txtURLFormData.top = new FormAttachment(brwsSearch,5);
			//txtTmpFormData.left = new FormAttachment(lblTmp,20);
			txtURLFormData.left = new FormAttachment(25,20);
			txtURLFormData.right = new FormAttachment(100,-5);
			txtURL.setLayoutData(txtURLFormData);			

			SquareButton btnSaveGuide = new SquareButton(shell, SWT.PUSH);
			btnSaveGuide.setText("Save");
			FormData btnSaveFormData = new FormData();
			btnSaveFormData.top = new FormAttachment(lblURL, 5);
			btnSaveFormData.left = new FormAttachment(0, 5);
			btnSaveGuide.setLayoutData(btnSaveFormData);
			btnSaveGuide.addSelectionListener(new SaveButtonListener());

			lbltitle = new Label(shell, SWT.LEFT);
			FormData lbltitleFormData = new FormData();
			lbltitleFormData.top = new FormAttachment(btnSaveGuide,5);
			lbltitleFormData.left = new FormAttachment(0,5);
			lbltitleFormData.right = new FormAttachment(100, -5);
			lbltitle.setLayoutData(lbltitleFormData);
			lbltitle.setText("title");
			
			lblmessage = new Label(shell, SWT.LEFT);
			FormData lblmessageFormData = new FormData();
			lblmessageFormData.top = new FormAttachment(lbltitle,5);
			lblmessageFormData.left = new FormAttachment(0,5);
			lblmessageFormData.right = new FormAttachment(100, -5);
			lblmessage.setLayoutData(lblmessageFormData);
			lblmessage.setText("message");
			
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
				if (appSettings != null) {
					appSettings.saveSettings();
				}
			}
			catch (Exception ex) {
				logger.error("shellCloseListen ", ex);
			}
			super.shellClosed(e);
		}

		public void handleEvent(Event event) {
		}
	}
	
	// Click event code for the dynamic buttons
	class SaveButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			try {
				logger.trace("Enter SaveButtonListner");

				String strUrl = txtURL.getText();
				Download download = new Download(strUrl, mainShell);
				Thread t = new Thread(download);
				t.start();
			}
			catch (Exception ex) {
				logger.error(" SaveButtonListner " + ex.getLocalizedMessage());
			}
		}
	}


	class FileLoadListener  extends SelectionAdapter {
		//File Load from the menu
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				logger.trace("Enter Menu Load");
				brwsSearch.setUrl("https://www.milovana.com/webteases/#pp=20&type=2");
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
				Shell prefShell = new PreferenceShell().createShell(myDisplay);
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