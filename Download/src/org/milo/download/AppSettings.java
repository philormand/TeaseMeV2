package org.milo.download;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppSettings {
	private Logger logger = LogManager.getLogger();

	private String DataDirectory;
	private Properties appSettingsProperties = new Properties();
	private String settingsLocation;
	private String userDir;
	private String userHome;
	private String userName;
	private String fileSeparator;
	private static AppSettings appSettings;

	public static synchronized AppSettings getAppSettings() {
		if (appSettings == null) {
			appSettings = new AppSettings(false);
		}
		return appSettings;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	protected  AppSettings(Boolean overrideconstructor) {
		super();
		if (!overrideconstructor) {
			Properties properties = java.lang.System.getProperties();
			userDir = String.valueOf(properties.get("user.dir"));
			userHome = String.valueOf(properties.get("user.home"));
			userName = String.valueOf(properties.get("user.name"));
			fileSeparator = String.valueOf(properties.get("file.separator"));
			settingsLocation = "data" + fileSeparator + "settings.properties";
			logger.debug("AppSettings userDir: " + userDir);
			logger.debug("AppSettings userHome: " + userHome);
			logger.debug("AppSettings userName: " + userName);
			logger.debug("AppSettings fileSeparator: " + fileSeparator);
			logger.debug("AppSettings settingsLocation: " + settingsLocation);
			try {
				try {
					appSettingsProperties.loadFromXML(new FileInputStream(settingsLocation));
				}
				catch (IOException ex) {
					//failed to load file so just carry on
					logger.error(ex.getLocalizedMessage(), ex);
				}
				DataDirectory = appSettingsProperties.getProperty("DataDirectory", userDir);
			}
			catch (Exception ex) {
				logger.error(ex.getLocalizedMessage(), ex);
			}
			saveSettings();
		}
	}

	public String getDataDirectory() {
		return DataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		DataDirectory = dataDirectory;
	}

	public void saveSettings() {
		try {
			appSettingsProperties.setProperty("DataDirectory", DataDirectory);
			appSettingsProperties.storeToXML(new FileOutputStream(settingsLocation), null);
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public String getUserDir() {
		return userDir;
	}

	public String getUserHome() {
		return userHome;
	}

	public String getUserName() {
		return userName;
	}

	public String getFileSeparator() {
		return fileSeparator;
	}


}
