package org.milo.download;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class ComonFunctions {
	private SecureRandom mRandom = new SecureRandom();
	private static Logger logger = LogManager.getLogger();
    private XPathFactory factory = XPathFactory.newInstance();
    private XPath xpath = factory.newXPath();
    private static final String version = "0.0.8";

	private static ComonFunctions comonFunctions;

	private ComonFunctions() {
	}
	
	public static synchronized ComonFunctions getComonFunctions() {
		if (comonFunctions == null) {
			comonFunctions = new ComonFunctions();
		}
		return comonFunctions;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

    
    
    //checks to see if the flags match
    public boolean canShow(ArrayList<String> setList, String IifSet, String IifNotSet) {
    	boolean icanShow = false;
    	boolean blnSet = true;
    	boolean blnNotSet = true;

    	try {
    		if (!IifSet.trim().equals("")) {
    			blnSet = MatchesIfSetCondition(IifSet.trim(), setList);
    		}
    		if (!IifNotSet.trim().equals("")) {
    			blnNotSet = MatchesIfNotSetCondition(IifNotSet.trim(), setList);
    		}
    		if (blnSet && blnNotSet) {
    			icanShow = true;
    		} else {
    			icanShow = false;
    		}
    	}
    	catch (Exception ex){
    		logger.error(ex.getLocalizedMessage(), ex);
    	}
    	return icanShow;
    }

    
    // Overloaded function checks pages as well 
    public boolean canShow(ArrayList<String> setList, String IifSet, String IifNotSet, String IPageId) {
    	boolean icanShow = false;
    	try {
    		icanShow = canShow(setList, IifSet, IifNotSet);
    		if (icanShow) {
    			if (IPageId.equals("")) {
    				icanShow = true;
    			} else {
    				icanShow = MatchesIfNotSetCondition(IPageId, setList);
    			}
    		}
    	}
    	catch (Exception ex){
    		logger.error(ex.getLocalizedMessage(), ex);
    	}
    	return icanShow;
	}

    //checks list of flags to see if they match
	private boolean MatchesIfSetCondition(String condition, ArrayList<String> setList) {
		boolean blnReturn = false;
		boolean blnAnd = false;
		boolean blnOr = false;
		String[] conditions;

		try {
			if (condition.indexOf("|") > -1) {
				blnOr = true;
				condition = condition.replace("|", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (setList.contains(conditions[i].trim())) {
						blnReturn = true;
						break;
					}
				}
			}

			if (condition.indexOf("+") > -1) {
				blnAnd = true;
				blnReturn = true;
				condition = condition.replace("+", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (!setList.contains(conditions[i].trim())) {
						blnReturn = false;
						break;
					}
				}
			}

			if (!blnAnd && !blnOr) {
				blnReturn = setList.contains(condition);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		return blnReturn;
	}
	
	//checks a list of flags to make sure they don't match
	private boolean MatchesIfNotSetCondition(String condition, ArrayList<String> setList) {
		boolean blnReturn = false;
		boolean blnAnd = false;
		boolean blnOr = false;
		String[] conditions;

		try {
			if (condition.indexOf("+") > -1) {
				blnAnd = true;
				blnReturn = true;
				condition = condition.replace("+", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (setList.contains(conditions[i].trim())) {
						blnReturn = false;
						break;
					}
				}
			}

			if (condition.indexOf("|") > -1) {
				blnOr = true;
				condition = condition.replace("|", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (!setList.contains(conditions[i].trim())) {
						blnReturn = true;
						break;
					}
				}
			}

			if (!blnAnd && !blnOr) {
				blnReturn = !setList.contains(condition);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		return blnReturn;
	}

	// functions to handle set flags go here
	public void SetFlags(String flagNames, ArrayList<String> setList) {
		String[] flags;
		try {
			flags = flagNames.split(",", -1);
			for (int i = 0; i < flags.length; i++) {
				if (!flags[i].trim().equals("")) {
					if (!setList.contains(flags[i].trim())) {
						setList.add(flags[i].trim());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
		}
	}

	public String GetFlags(ArrayList<String> setList) {
		String strFlags = "";
		try {
			for (int i = 0; i < setList.size(); i++) {
				if (i==0) {
					strFlags = setList.get(i);
				} else {
					strFlags = strFlags + "," + setList.get(i);
				}
			}

		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return strFlags;
	}

	public void UnsetFlags(String flagNames, ArrayList<String> setList) {
		String[] flags;
		try {
			flags = flagNames.split(",", -1);
			for (int i = 0; i < flags.length; i++) {
				if (!flags[i].trim().equals("")) {
					if (setList.contains(flags[i].trim())) {
						setList.remove(flags[i].trim());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
		}
	}
	
	//Get random number between x and y where Random is (x..y)
	public int getRandom(String random) {
		int intRandom = 0;
		int intPos1;
		int intPos2;
		int intPos3;
		int intMin;
		int intMax;
		String strMin;
		String strMax;
		
		try {
			intPos1 = random.indexOf("(");
			if (intPos1 > -1) {
				intPos2 = random.indexOf("..", intPos1);
				if (intPos2 > -1) {
					intPos3 = random.indexOf(")", intPos2);
					if (intPos3 > -1) {
						strMin = random.substring(intPos1 + 1, intPos2);
						intMin = Integer.parseInt(strMin);
						strMax = random.substring(intPos2 + 2, intPos3);
						intMax = Integer.parseInt(strMax);
						int i1 = mRandom.nextInt(intMax - intMin + 1) + intMin;
						intRandom = i1;
					}
				}
			} else {
				intRandom = Integer.parseInt(random);
			}
		} catch (NumberFormatException en) {
			intRandom = 0;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		
		return intRandom;
	}
	
	public int getMilisecFromTime(String iTime) {
		int intPos1;
		int intPos2;
		String strHour;
		String strMinute;
		String strSecond;
		int intTime = 0;
		
		try {
			intPos1 = iTime.indexOf(":");
			if (intPos1 > -1) {
				intPos2 = iTime.indexOf(":", intPos1 + 1);
				if (intPos2 > -1) {
					strHour = iTime.substring(0, intPos1);
					strMinute = iTime.substring(intPos1 + 1, intPos2);
					strSecond = iTime.substring(intPos2 + 1, iTime.length());
					intTime = Integer.parseInt(strSecond) * 1000;
					intTime = intTime + Integer.parseInt(strMinute) * 1000 * 60;
					intTime = intTime + Integer.parseInt(strHour) * 1000 * 60 * 60;
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return intTime;
	}

	public Element getOrAddElement(String xPath, String nodeName, Element parent, Document doc) {
		//xml helper function
		try {
			Element elToSet = getElement(xPath, parent);
			if (elToSet == null) {
				elToSet = addElement(nodeName, parent, doc);
			}
			return elToSet;
		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(),ex);
			return null;
		}
	}
	
	public Element getElement(String xPath, Element parent) {
		//xml helper function
		try {
			XPathExpression expr = xpath.compile(xPath);
			Object Xpathresult = expr.evaluate(parent, XPathConstants.NODESET);
			NodeList nodes = (NodeList) Xpathresult;
			Element elToSet = null;
			if (nodes.getLength() > 0) {
				elToSet = (Element) nodes.item(0);
			}
			return elToSet;
		}
		catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(),ex);
			return null;
		}
	}
	
	public Element addElement(String nodeName, Element parentNode, Document doc) {
		//xml helper function
		try {
			Element elToSet;
			elToSet = doc.createElement(nodeName);
			parentNode.appendChild(elToSet);
			return elToSet;
		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(),ex);
			return null;
		}
	}

	public String readFile(String path, Charset encoding) throws IOException {
		//returns the contents of a file as a String
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public String fixSeparator(String path, String fileSeparator) {
		String retrn = path;
		retrn = retrn.replace("\\", fileSeparator);
		retrn = retrn.replace("/", fileSeparator);
		if (retrn.startsWith(fileSeparator)) {
			retrn = retrn.substring(1, retrn.length());
		}
		if (retrn.endsWith(fileSeparator)) {
			retrn = retrn.substring(0, retrn.length() - 1);
		}
		return retrn;
	}
	
	public Boolean fileExists(String fileName) {
		AppSettings appSettings = AppSettings.getAppSettings();
		String fileSeparator = appSettings.getFileSeparator();
		
		String dataDirectory;
		String prefix = "";
		dataDirectory = appSettings.getDataDirectory();
		if (dataDirectory.startsWith("/")) {
			prefix = "/";
		}
		dataDirectory = prefix + fixSeparator(appSettings.getDataDirectory(), fileSeparator);
		
		
		String media = fixSeparator(fileName, fileSeparator);
		logger.debug("CommonFunctions fileExists getMediaFullPath " + media);
		int intSubDir = media.lastIndexOf(fileSeparator);
		String strSubDir;
		if (intSubDir > -1) {
			strSubDir = fixSeparator(media.substring(0, intSubDir + 1), fileSeparator);
			media = media.substring(intSubDir + 1);
		} else {
			strSubDir = "";
		}
		// String strSubDir
		// no wildcard so just use the file name
		if (strSubDir.equals("")) {
			fileName = dataDirectory + fileSeparator + media;
		} else {
			fileName = dataDirectory + fileSeparator + strSubDir + fileSeparator + media;
		}
		File f = new File(fileName);
		Boolean fileexists = false;
		if (f.exists()) {
			if (f.isFile()) {
				fileexists = true;
			}
		}
		logger.debug("ComonFunctions FileExists check " + fileName + " " + fileexists);
		return fileexists;
	}

	public String imageSignature(byte[] image)
	{
		String returnVal = "";
		try
		{
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(image);
		byte[] digBytes = md.digest();
		StringBuilder sb = new StringBuilder();
		for (byte b : digBytes) {
		  sb.append(String.format("%02X", b & 0xff));
		}
		returnVal = sb.toString();		
		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(),ex);
		}
		return returnVal;
	}
	
	
	public static String getVersion() {
		return version;
	}


	public String innerXml(Node node) {
	    DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
	    LSSerializer lsSerializer = lsImpl.createLSSerializer();
	    lsSerializer.getDomConfig().setParameter("xml-declaration", false);
	    NodeList childNodes = node.getChildNodes();
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < childNodes.getLength(); i++) {
	       sb.append(lsSerializer.writeToString(childNodes.item(i)));
	    }
	    return sb.toString(); 
	}
}
