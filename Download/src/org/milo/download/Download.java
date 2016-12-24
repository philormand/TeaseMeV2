package org.milo.download;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class Download implements Runnable {
	private static Logger logger = LogManager.getLogger();
	private String strUrl;
	private String strGuidePath;
	private Boolean isFlash;
	MainShell mainShell;
	private AppSettings appSettings;
	private HashMap<String, byte[]> images = new HashMap<String, byte[]>();

	public Download(String url, MainShell shell) {
		strUrl = url;
		mainShell = shell;
	}

	@Override
	public void run() {
		appSettings = AppSettings.getAppSettings();

		// path to the xml files
		strGuidePath = appSettings.getDataDirectory();

		int idpos = strUrl.indexOf("?id=");
		String StrNyxId = strUrl.substring(idpos + 4);
		String strNyxUrl = "https://www.milovana.com/webteases/getscript.php?id=" + StrNyxId;
		String rawGuide = parseGuide(strNyxUrl);
		String htmlstr = parseGuide(strUrl);
		if (rawGuide.equals("")) {
			isFlash = false;
		} else {
			isFlash = true;
		}
		
		String strMediaDir = "";
		
		if (isFlash) {
			//flash tease
			int intStrt;
			int intEnd;
			SecureRandom rndgen = new SecureRandom();
			HashMap<String, Integer> rndImages = new HashMap<String, Integer>();
			HashMap<String, String> mustNotMap = new HashMap<String, String>();
			HashMap<String, String> mustMap = new HashMap<String, String>();
			String AuthorID = "";
			try {
				intStrt = htmlstr.indexOf("href=\"webteases/#author=");
				if (intStrt != -1) {
					intStrt = intStrt + 24;
					intEnd = htmlstr.indexOf("\"", intStrt);
					AuthorID = htmlstr.substring(intStrt, intEnd);
				}
			}
			catch (Exception ex) {
				logger.error(" AuthorID " + ex.getLocalizedMessage());
			}
			String strAuthorName = "";
			try {
				intStrt = htmlstr.indexOf("teaseAuthor=");
				if (intStrt != -1) {
					intStrt = intStrt + 12;
					intEnd = htmlstr.indexOf("&", intStrt);
					strAuthorName = htmlstr.substring(intStrt, intEnd);
				}
			}
			catch (Exception ex) {
				logger.error(" strAuthorName " + ex.getLocalizedMessage());
			}
			String strTitle = "";
			try {
				intStrt = htmlstr.indexOf("teaseName=");
				if (intStrt != -1) {
					intStrt = intStrt + 10;
					intEnd = htmlstr.indexOf("&", intStrt);
					strTitle = htmlstr.substring(intStrt, intEnd);
					strTitle = strTitle.replace("+", " ");
					strTitle = strTitle.replace("%26", " and ");
					strTitle = strTitle.replace("%3A", ":");
					strTitle = strTitle.replace("%21", "!");
					strTitle = strTitle.replace("%23", "#");
					strTitle = strTitle.replace("%27", "'");
					strTitle = strTitle.replace("%3F", "?");
					strTitle = strTitle.replace("%28", "(");
					strTitle = strTitle.replace("%29", ")");
					strTitle = strTitle.replace("%2C", ",");
				}
				strMediaDir = strTitle.replace(" ", "-");
				strMediaDir = strMediaDir.replace(":", "");
				strMediaDir = strMediaDir.replace("!", "");
				strMediaDir = strMediaDir.replace("'", "");
				strMediaDir = strMediaDir.replace("#", "");
				strMediaDir = strMediaDir.replace("?", "");
				strMediaDir = strMediaDir.replace(".", "-");
				strMediaDir = strMediaDir.replace("(", "-");
				strMediaDir = strMediaDir.replace(")", "-");
				strMediaDir = strMediaDir.replace(",", "-");
				strMediaDir = strMediaDir.replace("--", "-");
				strMediaDir = strMediaDir.replace("--", "-");
				doUpdateTitle(mainShell, strTitle);
			}
			catch (Exception ex) {
				logger.error(" strTitle " + ex.getLocalizedMessage());
			}

			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				// root elements
				Document doc = docBuilder.newDocument();
				Document docText;
				Element rootElement = doc.createElement("Tease");
				try {
					rootElement.setAttribute("id", StrNyxId);
					rootElement.setAttribute("scriptVersion", "v0.1");
				}
				catch (Exception ex) {
					logger.error(" StrNyxId " + ex.getLocalizedMessage());
				}
				doc.appendChild(rootElement);
				// Title elements
				try {
					Element Title = doc.createElement("Title");
					Title.appendChild(doc.createTextNode(strTitle));
					rootElement.appendChild(Title);
				}
				catch (Exception ex) {
					logger.error(" Title " + ex.getLocalizedMessage());
				}
				// Url elements
				try {
					Element Url = doc.createElement("Url");
					Url.appendChild(doc.createTextNode(strUrl));
					rootElement.appendChild(Url);
				}
				catch (Exception ex) {
					logger.error(" Url " + ex.getLocalizedMessage());
				}
				// Author elements
				try {
					Element Author = doc.createElement("Author");
					rootElement.appendChild(Author);
					Author.setAttribute("id", AuthorID);
					// AuthorName elements
					Element AuthorName = doc.createElement("Name");
					AuthorName.appendChild(doc.createTextNode(strAuthorName));
					Author.appendChild(AuthorName);
					// AuthorUrl elements
					Element AuthorUrl = doc.createElement("Url");
					Author.appendChild(AuthorUrl);
				}
				catch (Exception ex) {
					logger.error(" Author " + ex.getLocalizedMessage());
				}
				// MediaDirectory elements
				try {
					Element MediaDirectory = doc.createElement("MediaDirectory");
					MediaDirectory.appendChild(doc.createTextNode(strMediaDir));
					rootElement.appendChild(MediaDirectory);
				}
				catch (Exception ex) {
					logger.error(" MediaDirectory " + ex.getLocalizedMessage());
				}

				// Settings elements
				try {
					Element Settings = doc.createElement("Settings");
					rootElement.appendChild(Settings);
					// AutoSetPageWhenSeen elements
					Element AutoSetPageWhenSeen = doc.createElement("AutoSetPageWhenSeen");
					AutoSetPageWhenSeen.appendChild(doc.createTextNode("true"));
					Settings.appendChild(AutoSetPageWhenSeen);
				}
				catch (Exception ex) {
					logger.error(" Settings " + ex.getLocalizedMessage());
				}
				// Pages elements
				try {
					Element Pages = doc.createElement("Pages");
					rootElement.appendChild(Pages);

					File directory = new File(strGuidePath + "\\" + strMediaDir);
					if (!directory.exists()) {
						directory.mkdir();
					}

					String strPage;
					String strPageName;
					int currposn = 0;
					int posnMax = rawGuide.length();
					int posn;
					int posnPage;
					int posnGoto;
					int posnVert;
					int posnMult;
					int posn2;
					int posn2Page;
					int posn2Goto;
					int posn2Vert;
					int posn2Mult;
					int posn3;
					int posnTextStart;
					int posnTextEnd;
					int posnPicStart;
					int posnPicEnd;
					String endText = "',";
					String startImage = "media:pic(id:\"";
					String startButtons = "action:buttons(target";
					String strText;
					String strImage;
					String strImagePath;
					Boolean blnLoop = true;
					Element Page;
					String pageType;

					
					//process mustnot
					while (blnLoop) {
						posn = rawGuide.indexOf("mustnot(self:");
						if (posn == -1) {
							blnLoop = false;
						}
						else {
							posn2 = rawGuide.indexOf("#", posn + 1);
							strPageName = rawGuide.substring(posn + 13, posn2);
							String strPageList = "";
							boolean getpage = false;
							int posnEnd = rawGuide.indexOf(")", posn2);
							for (int i = posn2; i < posnEnd; i++)
							{
								if (getpage) {
									if (rawGuide.substring(i, i+1).equals("#")) {
										getpage = false;
										strPageList = strPageList + ",";
									}
									else {
										strPageList = strPageList + rawGuide.substring(i, i+1);
									}
								}
								else {
									if (rawGuide.substring(i, i+1).equals(":")) {
										getpage = true;
									}
								}
							}
							strPageList = strPageList.substring(0, strPageList.length() - 1);
							mustNotMap.put(strPageName, strPageList);
							rawGuide = rawGuide.substring(0, posn) + rawGuide.substring(posnEnd + 1);
						}
						
					}
					
					
					//process must
					blnLoop = true;
					while (blnLoop) {
						posn = rawGuide.indexOf("must(self:");
						if (posn == -1) {
							blnLoop = false;
						}
						else {
							posn2 = rawGuide.indexOf("#", posn + 1);
							strPageName = rawGuide.substring(posn + 10, posn2);
							String strPageList = "";
							boolean getpage = false;
							int posnEnd = rawGuide.indexOf(")", posn2);
							for (int i = posn2; i < posnEnd; i++)
							{
								if (getpage) {
									if (rawGuide.substring(i, i+1).equals("#")) {
										getpage = false;
										strPageList = strPageList + ",";
									}
									else {
										strPageList = strPageList + rawGuide.substring(i, i+1);
									}
								}
								else {
									if (rawGuide.substring(i, i+1).equals(":")) {
										getpage = true;
									}
								}
							}
							strPageList = strPageList.substring(0, strPageList.length() - 1);
							mustMap.put(strPageName, strPageList);
							rawGuide = rawGuide.substring(0, posn) + rawGuide.substring(posnEnd + 1);
						}
						
					}

					//loop round the nyx script
					blnLoop = true;
					while (blnLoop) {
						try {
							strText = "";
							posnMult = rawGuide.indexOf("#mult", currposn);
							if (posnMult == -1) posnMult = posnMax;
							posnVert = rawGuide.indexOf("#vert", currposn);
							if (posnVert == -1) posnVert = posnMax;
							posnGoto = rawGuide.indexOf("#goto", currposn);
							if (posnGoto == -1) posnGoto = posnMax;
							posnPage = rawGuide.indexOf("#page", currposn);
							if (posnPage == -1) posnPage = posnMax;
							if (posnPage == posnMax  && posnGoto == posnMax  && posnVert == posnMax && posnVert == posnMult) {
								blnLoop = false;
							} else {
								//found a page
								posn = posnMax;
								pageType = "";
								if (posnMult < posn)
								{
									pageType = "#mult";
									endText = "'),";
									startImage = ":pic(id:\"";
									startButtons = "buttons(target";
									posn = posnMult;
								}
								if (posnVert < posn)
								{
									pageType = "#vert";
									endText = "'),";
									startImage = ":pic(id:\"";
									startButtons = "buttons(target";
									posn = posnVert;
								}
								if (posnGoto < posn)
								{
									pageType = "#goto";
									endText = "',";
									startImage = "media:pic(id:\"";
									startButtons = "action:buttons(target";
									posn = posnGoto;
								}
								if (posnPage < posn)
								{
									pageType = "#page";
									endText = "',";
									startImage = "media:pic(id:\"";
									startButtons = "action:buttons(target";
									posn = posnPage;
								}
								strPageName = rawGuide.substring(currposn, posn);
								Page = doc.createElement("Page");
								Page.setAttribute("id", strPageName);
								if (mustMap.containsKey(strPageName)) {
									Page.setAttribute("if-set", mustMap.get(strPageName));
								}
								if (mustNotMap.containsKey(strPageName)) {
									Page.setAttribute("if-not-set", mustNotMap.get(strPageName));
								}
								Pages.appendChild(Page);
								doUpdate(mainShell, strPageName);
								

								posn2Mult = rawGuide.indexOf("#mult", posn + 1);
								if (posn2Mult == -1) posn2Mult = posnMax;
								posn2Vert = rawGuide.indexOf("#vert", posn + 1);
								if (posn2Vert == -1) posn2Vert = posnMax;
								posn2Goto = rawGuide.indexOf("#goto", posn + 1);
								if (posn2Goto == -1) posn2Goto = posnMax;
								posn2Page = rawGuide.indexOf("#page", posn + 1);
								if (posn2Page == -1) posn2Page = posnMax;
								posn2 = posnMax;
								if (posn2Mult < posn2)
								{
									posn2 = posn2Mult;
								}
								if (posn2Vert < posn2)
								{
									posn2 = posn2Vert;
								}
								if (posn2Goto < posn2)
								{
									posn2 = posn2Goto;
								}
								if (posn2Page < posn2)
								{
									posn2 = posn2Page;
								}
								
								if (posn2 != posnMax) {
									posn3 = rawGuide.lastIndexOf(")", posn2);
									strPage = rawGuide.substring(posn, posn3 + 1);
									currposn = posn3 + 1;
								} else {
									strPage = rawGuide.substring(posn);
									blnLoop = false;
								}
								
								// Page name is now in strPageName
								//content is now in strPage
								//add the nyx code for the page as a comment
								try {
									Comment pageascomment = doc.createComment(strPage);
									Page.appendChild(pageascomment);
								}
								catch (Exception ex) {
									logger.error(" Nyx Comment " + strPageName + " " + ex.getLocalizedMessage());
								}
								//Get the text
								try {
									posnTextStart = strPage.indexOf("text:'");
									if (posnTextStart != -1) {
										posnTextStart = posnTextStart + 6;
										posnTextEnd = strPage.indexOf(endText, posnTextStart);
										if (posnTextEnd == -1)
										{
											posnTextEnd = strPage.indexOf("')", posnTextStart);
										}
										strText = strPage.substring(posnTextStart, posnTextEnd);
										strText = strText.replace("SIZE=\"", "style=\"font-size:");
										strText = strText.replace("Â","");

										//strip the text
										strPage = strPage.substring(0, posnTextStart) + strPage.substring(posnTextEnd);
										//strip the unneeded page and text stuff
										strPage = strPage.replace(pageType + "(text:'',", "");
										// Text elements
										Element Text = doc.createElement("Text");
										StringReader tempStr = new StringReader( "<TEXTFORMAT>" + strText + "</TEXTFORMAT>");
										InputSource tempInpSrc = new InputSource(tempStr );
										docText = docBuilder.parse(tempInpSrc);
										Node tmpText = doc.importNode(docText.getFirstChild(), true);
										Text.appendChild(tmpText);
										Page.appendChild(Text);
										tempStr.close();
									}
								}
								catch (Exception ex) {
									logger.error(" text node " + strPageName + " " + ex.getLocalizedMessage());
								}
								//get the media
								//images
								try {
									posnPicStart = strPage.indexOf(startImage);
									if (posnPicStart != -1) {
										posnPicEnd =  strPage.indexOf("\")", posnPicStart);
										if (posnPicEnd != -1) {
											strImage = strPage.substring(posnPicStart + 14, posnPicEnd);
											strPage = strPage.substring(0, posnPicStart) + strPage.substring(posnPicEnd + 2);
											// Image elements
											Element Image = doc.createElement("Image");
											Image.setAttribute("id", strImage);
											Page.appendChild(Image);

											if (strImage.contains("*")) {
												String tmpImage = strImage;
												if (tmpImage.endsWith("*"))
												{
													tmpImage = tmpImage + ".jpg";
												}
												if (!rndImages.containsKey(strImage)) {
													for (int i = 0; i < 25; i++) {
													//for (int i = 0; i < 1; i++) {
														int intName = rndgen.nextInt();
														if (intName < 0) {
															intName = 0 - intName;
														}
														String strRndName = tmpImage.replace("*", Integer.toString(intName));
														strImagePath = strGuidePath + "\\" + strMediaDir + "\\" + strRndName;
														saveFile(strImagePath, strImage, AuthorID, StrNyxId);
													}
													rndImages.put(strImage, 5);
												}
											} else {
												strImagePath = strGuidePath + "\\" + strMediaDir + "\\" + strImage;
												saveFile(strImagePath, strImage, AuthorID, StrNyxId);
											}
										}
									}
								}
								catch (Exception ex) {
									logger.error(" images node " + strPageName + " " + ex.getLocalizedMessage());
								}

								//sound
								try {
									String delim = "'";
									int posnSndStart = strPage.indexOf("hidden:sound(id:" + delim);
									if (posnSndStart == -1)
									{
										delim = "\"";
										posnSndStart = strPage.indexOf("hidden:sound(id:" + delim);
									}
									if (posnSndStart != -1) {
										int posnSndEnd =  strPage.indexOf(delim + ")", posnSndStart);
										if (posnSndEnd != -1) {
											String strAudio = strPage.substring(posnSndStart + 17, posnSndEnd);
											strPage = strPage.substring(0, posnSndStart) + strPage.substring(posnSndEnd + 2);
											// Audio elements
											Element Audio = doc.createElement("Audio");
											Audio.setAttribute("id", strAudio);
											Page.appendChild(Audio);
											String strAudioPath = strGuidePath + "\\" + strMediaDir + "\\" + strAudio;

											File f = new File(strAudioPath);
											if(!f.exists()){

												URL Audiourl = new URL("https://www.milovana.com/media/get.php?folder=" + AuthorID + "/" + StrNyxId + "&name=" + strAudio);
												InputStream in = new BufferedInputStream(Audiourl.openStream());
												ByteArrayOutputStream out = new ByteArrayOutputStream();
												byte[] buf = new byte[1024];
												int n = 0;
												while (-1!=(n=in.read(buf)))
												{
													out.write(buf, 0, n);
												}
												out.close();
												in.close();
												byte[] response = out.toByteArray();
												FileOutputStream fos = new FileOutputStream(strAudioPath);
												fos.write(response);
												fos.close();
												try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
													logger.error(" Thread sleep " + e.getLocalizedMessage());
												}				                	
											}
										}
									}
								}
								catch (Exception ex) {
									logger.error(" sound node " + strPageName + " " + ex.getLocalizedMessage());
								}

								//Vert   ))
								int posnvertStart = strPage.indexOf("action:vert(");
								if (posnvertStart != -1) {
									strPage = strPage.replace("action:vert(e0:", "action:");
									strPage = strPage.replace(",e1:", ",action:");
									strPage = strPage.replace(",e2:", ",action:");
								}
								
								//Delay
								//action:delay(time:20sec,target:warmup8#,style:hidden
								//<Delay seconds="10" target="Page4" start-with="" style="hidden" onTriggered="delayP4()" />
								//instruc:delay
								try {
									strPage = strPage.replace("instruc:delay", "action:delay");
									int posndelayStart = strPage.indexOf("action:delay(time:");
									if (posndelayStart != -1) {
										String strDTarget;
										String strDStyle = "normal";
										String strDSeconds;
										String strDSecondsType;
										int intDSeconds = 0;
										int posndelayEnd = strPage.indexOf(")", posndelayStart);
										int tmpStart;
										int tmpEnd;
										Element delay = doc.createElement("Delay");
										tmpStart = strPage.indexOf("target:", posndelayStart);
										int tmpRange = strPage.indexOf("range(from:", tmpStart);
										if (tmpRange != -1) {
											int tmpMiddle = strPage.indexOf(",to:", tmpRange);
											tmpEnd = strPage.indexOf(",:'page')", tmpMiddle);
											if (tmpEnd == -1)
											{
												tmpEnd = strPage.indexOf(")", tmpMiddle);
											}
											strDTarget = "(" + strPage.substring(tmpRange + 11, tmpMiddle) + ".." + strPage.substring(tmpMiddle + 4, tmpEnd) + ")";
											posndelayEnd = strPage.indexOf(")", tmpEnd + 9);
										} else {
											tmpEnd = strPage.indexOf("#", tmpStart);
											strDTarget = strPage.substring(tmpStart + 7, tmpEnd);
										}
										delay.setAttribute("target", strDTarget);

										tmpStart = strPage.indexOf("time:", posndelayStart);
										tmpEnd = strPage.indexOf(",", tmpStart);
										strDSeconds = strPage.substring(tmpStart + 5, tmpEnd - 3);
										strDSecondsType  = strPage.substring(tmpEnd - 3, tmpEnd);
										if (strDSecondsType.equals("min")) {
											intDSeconds = Integer.parseInt(strDSeconds);
											intDSeconds = intDSeconds * 60;
											strDSeconds = Integer.toString(intDSeconds);
										}
										delay.setAttribute("seconds", strDSeconds);

										tmpStart = strPage.indexOf("style:", posndelayStart);
										if (tmpStart != -1) {
											tmpEnd = strPage.indexOf(")", tmpStart);
											if (tmpEnd != -1) {
												strDStyle = strPage.substring(tmpStart + 6, tmpEnd);
											} else {
												strDStyle = strPage.substring(tmpStart + 6);
											}
										}
										delay.setAttribute("style", strDStyle);
										Page.appendChild(delay);
										strPage = strPage.substring(0, posndelayStart) + strPage.substring(posndelayEnd + 1);
									}
								}
								catch (Exception ex) {
									logger.error(" delay node " + strPageName + " " + ex.getLocalizedMessage());
								}
								
								//goto
								try {
									int posndelayStart = strPage.indexOf("goto(target:");
									if (posndelayStart != -1) {
										String strDTarget;
										String strDStyle = "hidden";
										String strDSeconds = "0";
										int posndelayEnd = strPage.indexOf(")", posndelayStart);
										int tmpStart;
										int tmpEnd;
										Element delay = doc.createElement("Delay");
										tmpStart = strPage.indexOf("target:", posndelayStart);
										tmpEnd = strPage.indexOf("#", tmpStart);
										if (tmpEnd == -1)
										{
											//range
											int fromPos = strPage.indexOf("from:", tmpStart);
											int fromEndPos = strPage.indexOf(",to:", fromPos);
											int toEndPos = strPage.indexOf(")", fromEndPos);
											String startPage = strPage.substring(fromPos + 5, fromEndPos);
											String endPage = strPage.substring(fromEndPos + 4, toEndPos);
											strDTarget = "(" + startPage + ".." + endPage + ")";
											posndelayEnd++;
										}
										else
										{
											strDTarget = strPage.substring(tmpStart + 7, tmpEnd);
										}
										delay.setAttribute("target", strDTarget);
										delay.setAttribute("seconds", strDSeconds);
										delay.setAttribute("style", strDStyle);
										Page.appendChild(delay);
										strPage = strPage.substring(0, posndelayStart) + strPage.substring(posndelayEnd + 1);
									}
								}
								catch (Exception ex) {
									logger.error(" delay node " + strPageName + " " + ex.getLocalizedMessage());
								}
								

								//Continue buttons
								try {
									int posnGoStart = strPage.indexOf("action:go(target:");
									String strGoTarget;
									if (posnGoStart != -1) {
										int tmpStart;
										int tmpEnd;
										tmpStart = strPage.indexOf("target:", posnGoStart);
										int tmpRange = strPage.indexOf("range(from:", tmpStart);
										if (tmpRange != -1) {
											int tmpMiddle = strPage.indexOf(",to:", tmpRange);
											int intOffset = 9;
											tmpEnd = strPage.indexOf(",:'page')", tmpMiddle);
											if (tmpEnd == -1)
											{
												tmpEnd = strPage.indexOf(")", tmpMiddle);
												intOffset = 1;
											}
											strGoTarget = "(" + strPage.substring(tmpRange + 11, tmpMiddle) + ".." + strPage.substring(tmpMiddle + 4, tmpEnd) + ")";
											tmpEnd = strPage.indexOf(")", tmpEnd + intOffset);
										} else {
											tmpEnd = strPage.indexOf("#", tmpStart);
											strGoTarget = strPage.substring(tmpStart + 7, tmpEnd);
										}
										Element go = doc.createElement("Button");
										go.appendChild(doc.createTextNode("Continue"));
										go.setAttribute("target", strGoTarget);
										Page.appendChild(go);
										strPage = strPage.substring(0, posnGoStart) + strPage.substring(tmpEnd + 2);
									}
								}
								catch (Exception ex) {
									logger.error(" continue node " + strPageName + " " + ex.getLocalizedMessage());
								}

								//Yes No Buttons
								try {
									int posnYnStart = strPage.indexOf("action:yn(yes:");
									if (posnYnStart != -1) {
										int posnYnEnd = strPage.indexOf("#,no:", posnYnStart);
										String strYnTarget = strPage.substring(posnYnStart + 14, posnYnEnd);
										Element button = doc.createElement("Button");
										button.appendChild(doc.createTextNode("Yes"));
										button.setAttribute("target", strYnTarget);
										Page.appendChild(button);
										strPage = strPage.substring(0, posnYnStart) + strPage.substring(posnYnEnd + 5);
										posnYnEnd = strPage.indexOf("#)", posnYnStart);
										strYnTarget = strPage.substring(posnYnStart, posnYnEnd);
										button = doc.createElement("Button");
										button.appendChild(doc.createTextNode("No"));
										button.setAttribute("target", strYnTarget);
										Page.appendChild(button);
										strPage = strPage.substring(0, posnYnStart) + strPage.substring(posnYnEnd + 2);
									}
								}
								catch (Exception ex) {
									logger.error(" yes no node " + strPageName + " " + ex.getLocalizedMessage());
								}

								//Buttons
								try {
									int posnBtnStart = strPage.indexOf(startButtons);
									if (posnBtnStart != -1) {
										int posnBtnEnd = strPage.indexOf("\")");
										String strButtons = strPage.substring(posnBtnStart + startButtons.length(), posnBtnEnd);
										strPage = strPage.substring(0, posnBtnStart) + strPage.substring(posnBtnEnd + 2);
										Boolean btnloop = true;
										int btnstart;
										int btnend;
										int capstart;
										int capend;
										String strBtnTarget;
										String strBtnCap;
										Element button;
										int killloop = 0;
										while (btnloop) {
											killloop++;
											if (killloop < 40) {
												try {
													btnstart = strButtons.indexOf(":");
													if (btnstart != -1) {
														btnend = strButtons.indexOf("#,", btnstart);
														strBtnTarget = strButtons.substring(btnstart + 1, btnend);
														capstart = strButtons.indexOf(":\"", btnend);
														capend = strButtons.indexOf("\",", capstart);
														if (capend != -1) {
															strBtnCap = strButtons.substring(capstart + 2, capend);
														} else {
															strBtnCap = strButtons.substring(capstart + 2);
														}
														strBtnCap = HtmlUnEncode(strBtnCap);
														button = doc.createElement("Button");
														button.appendChild(doc.createTextNode(strBtnCap));
														button.setAttribute("target", strBtnTarget);
														Page.appendChild(button);
														if (capend != -1) {
															strButtons = strButtons.substring(capend + 2);
														} else {
															strButtons = "";
														}
													} else {
														btnloop = false;
													}
												}
												catch (Exception ex) {
													logger.error(" button node " + strPageName + " " + ex.getLocalizedMessage());
												}

											} else {
												btnloop = false;
											}
										}
									}
								}
								catch (Exception ex) {
									logger.error(" buttons node " + strPageName + " " + ex.getLocalizedMessage());
								}

								// set / unset
								//instruc:mult(
								//action:mult(
								//hidden:unset
								try {
									if (pageType == "#mult" || pageType == "#vert")
									{
										strPage = strPage.replace(":unset(", "action:unset(");
										strPage = strPage.replace(":set(", "action:set(");
									}
									strPage = strPage.replace("action:mult(", "instruc:mult(");
									strPage = strPage.replace("hidden:unset(", "instruc:unset(");
									int posnInstructStart = strPage.indexOf("instruc:mult(");
									if (posnInstructStart == -1) {
										posnInstructStart = strPage.indexOf("instruc:unset(");
										if (posnInstructStart == -1) {
											posnInstructStart = strPage.indexOf("instruc:set(");
										}
										if (posnInstructStart == -1) {
											posnInstructStart = strPage.indexOf("action:unset(");
											if (posnInstructStart == -1) {
												posnInstructStart = strPage.indexOf("action:set(");
											}
											else
											{
												if (strPage.indexOf("action:set(") < posnInstructStart)
												{
													posnInstructStart = strPage.indexOf("action:set(");
												}
											}
										}
									}
									if (posnInstructStart != -1) {
										int posnInstructEnd = -1;

										int posnSetEnd = 0;
										int posnUnSetEnd = 0;
										int posnLastSet = 0;
										int posnLastUnSet = 0;
										int posnSetStart = posnInstructStart;
										int posnUnSetStart = posnInstructStart;
										boolean keepGoingSet = true; 
										boolean keepGoingUnSet = true; 
										String strSet = "";
										String strUnSet = "";
										while (keepGoingSet || keepGoingUnSet)
										{
											//Set
											posnSetStart = strPage.indexOf(":set(", posnSetStart);
											posnSetEnd = -1;
											if  (posnSetStart != -1 && keepGoingSet) {
												posnSetEnd = strPage.indexOf(")", posnSetStart);
												if (posnSetEnd > posnLastSet){
													posnLastSet = posnSetEnd;
												}
												int intActStart = strPage.indexOf("action", posnSetStart);
												intActStart = strPage.indexOf(":", intActStart);
												int intActEnd = strPage.indexOf("#", intActStart);
												if (strSet.length() > 0)
												{
													strSet = strSet + "," + strPage.substring(intActStart + 1, intActEnd);
												}
												else
												{
													strSet = strSet + strPage.substring(intActStart + 1, intActEnd);
												}
												intActStart = strPage.indexOf("action", intActEnd);
												while (intActStart != -1 && intActStart < posnSetEnd) {
													intActStart = strPage.indexOf(":", intActStart);
													intActEnd = strPage.indexOf("#", intActStart);
													strSet = strSet + "," + strPage.substring(intActStart + 1, intActEnd);
													intActStart = strPage.indexOf("action", intActEnd);
												}
												posnSetStart = intActEnd;
											}
											else
											{
												if (keepGoingSet)
												{
													Page.setAttribute("set", strSet);
													keepGoingSet = false;
												}
											}
	
											//Unset
											posnUnSetStart = strPage.indexOf(":unset(", posnUnSetStart);
											posnUnSetEnd = -1;
											if  (posnUnSetStart != -1  && keepGoingUnSet) {
												posnUnSetEnd = strPage.indexOf(")", posnUnSetStart);
												if (posnUnSetEnd > posnLastUnSet){
													posnLastUnSet = posnUnSetEnd;
												}												int intActStart = strPage.indexOf("action", posnUnSetStart);
												intActStart = strPage.indexOf(":", intActStart);
												int intActEnd = strPage.indexOf("#", intActStart);
												if (strUnSet.length() > 0)
												{
													strUnSet = strUnSet + "," + strPage.substring(intActStart + 1, intActEnd);
												}
												else
												{
													strUnSet = strUnSet + strPage.substring(intActStart + 1, intActEnd);
												}
												intActStart = strPage.indexOf("action", intActEnd);
												while (intActStart != -1 && intActStart < posnUnSetEnd) {
													intActStart = strPage.indexOf(":", intActStart);
													intActEnd = strPage.indexOf("#", intActStart);
													strUnSet = strUnSet + "," + strPage.substring(intActStart + 1, intActEnd);
													intActStart = strPage.indexOf("action", intActEnd);
												}
												posnUnSetStart = intActEnd;
											}
											else
											{
												if (keepGoingUnSet)
												{
													Page.setAttribute("unset", strUnSet);
													keepGoingUnSet = false;
												}
											}
										}
										if (posnLastSet > posnLastUnSet) {
											posnInstructEnd = strPage.indexOf(")", posnLastSet);
										} else {
											posnInstructEnd = strPage.indexOf(")", posnLastUnSet);
										}
										strPage = strPage.substring(0, posnInstructStart) + strPage.substring(posnInstructEnd + 1);
									}
								}
								catch (Exception ex) {
									logger.error(" set Unset node " + strPageName + " " + ex.getLocalizedMessage());
								}

								//whats left
								//,,),)
								if (!strPage.equals(",") && !strPage.equals(",)") && !strPage.equals(")") && !strPage.equals(",,))") && !strPage.equals(",,)") && !strPage.equals(",,,)") && !strPage.equals(",,),)")) {
									Page.appendChild(doc.createComment("Error:  " + strPage));
								}

							}
						}
						catch (Exception ex) {
							logger.error(" Page " + ex.getLocalizedMessage());
						}
					}
				}
				catch (Exception ex) {
					logger.error(" Pages " + ex.getLocalizedMessage());
				}
				doUpdate(mainShell, "Finished");
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(strGuidePath + "\\" + strMediaDir + ".xml"));
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.transform(source, result);
			}
			catch (Exception ex) {
				logger.error(" XML Gen " + ex.getLocalizedMessage());
			}
			logger.trace("Exit SaveButtonListner");
		} else {
			//Html tease
			int intPageStart;
			int intPageEnd;
			int intPageNo;
			int intPageNoEnd;
			int intImgSearchStart;
			int intImageStart;
			int intImageEnd;
			//int intRate;
			String strNextPage;
			String strPageText;
			String strImage;
			String strImagePath;
			String strPageName;
			Element Page;

			int intStrt;
			int intEnd;
			int intAuthId = -1;
			
			String AuthorID = "";
			try {
				intAuthId = htmlstr.indexOf("href=\"webteases/#author=");
				if (intAuthId != -1) {
					intAuthId = intAuthId + 24;
					intEnd = htmlstr.indexOf("\"", intAuthId);
					AuthorID = htmlstr.substring(intAuthId, intEnd);
				}
			}
			catch (Exception ex) {
				logger.error(" AuthorID " + ex.getLocalizedMessage());
			}
			String strAuthorName = "";
			try {
				intStrt = htmlstr.indexOf(";\">", intAuthId);
				if (intStrt != -1) {
					intStrt = intStrt + 3;
					intEnd = htmlstr.indexOf("</a>", intStrt);
					strAuthorName = htmlstr.substring(intStrt, intEnd);
				}
			}
			catch (Exception ex) {
				logger.error(" strAuthorName " + ex.getLocalizedMessage());
			}
			String strTitle = "";
			try {
				intStrt = htmlstr.indexOf("<h1 id=\"tease_title\">");
				if (intStrt != -1) {
					intStrt = intStrt + 21;
					intEnd = htmlstr.indexOf("<span", intStrt);
					strTitle = htmlstr.substring(intStrt, intEnd);
					strTitle = strTitle.trim(); 
					strTitle = strTitle.replace("+", " ");
					strTitle = strTitle.replace("%26", " and ");
					strTitle = strTitle.replace("%3A", ":");
					strTitle = strTitle.replace("%21", "!");
					strTitle = strTitle.replace("%23", "#");
					strTitle = strTitle.replace("%27", "'");
					strTitle = strTitle.replace("%3F", "?");
					strTitle = strTitle.replace("%28", "(");
					strTitle = strTitle.replace("%29", ")");
					strTitle = strTitle.replace("%2C", ",");
				}
				strMediaDir = strTitle.replace(" ", "-");
				strMediaDir = strMediaDir.replace(":", "");
				strMediaDir = strMediaDir.replace("!", "");
				strMediaDir = strMediaDir.replace("'", "");
				strMediaDir = strMediaDir.replace("#", "");
				strMediaDir = strMediaDir.replace("?", "");
				strMediaDir = strMediaDir.replace(".", "-");
				strMediaDir = strMediaDir.replace("(", "-");
				strMediaDir = strMediaDir.replace(")", "-");
				strMediaDir = strMediaDir.replace(",", "-");
				strMediaDir = strMediaDir.replace("--", "-");
				strMediaDir = strMediaDir.replace("--", "-");
				doUpdateTitle(mainShell, strTitle);
			}
			catch (Exception ex) {
				logger.error(" strTitle " + ex.getLocalizedMessage());
			}

			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				// root elements
				Document doc = docBuilder.newDocument();
				Document docText;
				Element rootElement = doc.createElement("Tease");
				try {
					rootElement.setAttribute("id", StrNyxId);
					rootElement.setAttribute("scriptVersion", "v0.1");
				}
				catch (Exception ex) {
					logger.error(" StrNyxId " + ex.getLocalizedMessage());
				}
				doc.appendChild(rootElement);
				// Title elements
				try {
					Element Title = doc.createElement("Title");
					Title.appendChild(doc.createTextNode(strTitle));
					rootElement.appendChild(Title);
				}
				catch (Exception ex) {
					logger.error(" Title " + ex.getLocalizedMessage());
				}
				// Url elements
				try {
					Element Url = doc.createElement("Url");
					Url.appendChild(doc.createTextNode(strUrl));
					rootElement.appendChild(Url);
				}
				catch (Exception ex) {
					logger.error(" Url " + ex.getLocalizedMessage());
				}
				// Author elements
				try {
					Element Author = doc.createElement("Author");
					rootElement.appendChild(Author);
					Author.setAttribute("id", AuthorID);
					// AuthorName elements
					Element AuthorName = doc.createElement("Name");
					AuthorName.appendChild(doc.createTextNode(strAuthorName));
					Author.appendChild(AuthorName);
					// AuthorUrl elements
					Element AuthorUrl = doc.createElement("Url");
					Author.appendChild(AuthorUrl);
				}
				catch (Exception ex) {
					logger.error(" Author " + ex.getLocalizedMessage());
				}
				// MediaDirectory elements
				try {
					Element MediaDirectory = doc.createElement("MediaDirectory");
					MediaDirectory.appendChild(doc.createTextNode(strMediaDir));
					rootElement.appendChild(MediaDirectory);
				}
				catch (Exception ex) {
					logger.error(" MediaDirectory " + ex.getLocalizedMessage());
				}

				// Settings elements
				try {
					Element Settings = doc.createElement("Settings");
					rootElement.appendChild(Settings);
					// AutoSetPageWhenSeen elements
					Element AutoSetPageWhenSeen = doc.createElement("AutoSetPageWhenSeen");
					AutoSetPageWhenSeen.appendChild(doc.createTextNode("false"));
					Settings.appendChild(AutoSetPageWhenSeen);
				}
				catch (Exception ex) {
					logger.error(" Settings " + ex.getLocalizedMessage());
				}
				// Pages elements
				try {
					Element Pages = doc.createElement("Pages");
					rootElement.appendChild(Pages);

					File directory = new File(strGuidePath + "\\" + strMediaDir);
					if (!directory.exists()) {
						directory.mkdir();
					}
					
					Boolean contloop = true;

					strPageName = "start";
					while (contloop) {
						Page = doc.createElement("Page");
						Page.setAttribute("id", strPageName);
						Pages.appendChild(Page);
						doUpdate(mainShell, strPageName);
						intPageStart = htmlstr.indexOf("<div id=\"tease_content\">");
						if (intPageStart != -1) {
							intPageEnd = htmlstr.indexOf("<p class=\"link\"><a href=\"webteases/showtease.php?id=", intPageStart);
							if (intPageEnd == -1) {
								intPageEnd = htmlstr.indexOf("<a name=\"rate\"></a>");
								contloop = false;
							}
							if (intPageEnd != -1) {
								intPageNo = htmlstr.indexOf("&p=",intPageEnd);
								intPageNoEnd = htmlstr.indexOf("#",intPageNo);
								strNextPage = htmlstr.substring(intPageNo + 3, intPageNoEnd);
								//Text
								strPageText = htmlstr.substring(intPageStart, intPageEnd);
								if (strPageText.trim().endsWith("<div class=\"item\">")) {
									int intTmpEnd = strPageText.lastIndexOf("<div class=\"item\">");
									strPageText = "<div>" + strPageText.substring(0, intTmpEnd);
								}
								strPageText = strPageText + "</div>";
								Element Text = doc.createElement("Text");
								docText = docBuilder.parse(new InputSource( new StringReader( strPageText ) ) );
								Node tmpText = doc.importNode(docText.getFirstChild(), true);
								Text.appendChild(tmpText);
								Page.appendChild(Text);
								
								//image
								intImgSearchStart = htmlstr.indexOf("<a name=\"t\"></a>");
								intImageStart = htmlstr.indexOf("<img src=\"", intImgSearchStart);
								intImageStart = intImageStart + 10;
								intImageEnd = htmlstr.indexOf("\"", intImageStart);
								strImage = htmlstr.substring(intImageStart, intImageEnd);
								strImagePath = strGuidePath + "\\" + strMediaDir + "\\" + strPageName + strImage.substring(strImage.length() - 4);
								saveFile(strImagePath, strImage, strImage);
								Element Image = doc.createElement("Image");
								Image.setAttribute("id", strPageName + strImage.substring(strImage.length() - 4));
								Page.appendChild(Image);

								//continue button
								if (contloop) {
									Element go = doc.createElement("Button");
									go.appendChild(doc.createTextNode("Continue"));
									go.setAttribute("target", strNextPage);
									Page.appendChild(go);
								}

								//last page
								htmlstr.indexOf("<a name=\"rate\"></a>");
								if (contloop) {
									htmlstr = parseGuide(strUrl + "&p=" + strNextPage + "#t");
									strPageName = strNextPage;
								}
							}
						}
					}
				}
				catch (Exception ex) {
					logger.error(" Pages " + ex.getLocalizedMessage());
				}
				doUpdate(mainShell, "Finished");
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(strGuidePath + "\\" + strMediaDir + ".xml"));
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.transform(source, result);
			}
			catch (Exception ex) {
				logger.error(" XML Gen " + ex.getLocalizedMessage());
			}
			logger.trace("Exit SaveButtonListner");
		}
	}
	private void saveFile(String strImagePath, String strImage, String AuthorID, String StrNyxId) {
		String strUrl = "https://www.milovana.com/media/get.php?folder=" + AuthorID + "/" + StrNyxId + "&name=" + strImage;
		saveFile(strImagePath, strUrl, strImage);
	}

	
	
	
	private void saveFile(String strImagePath, String strUrl, String strImage) {
        File f = new File(strImagePath);
        if (strImagePath.endsWith("mp3")) {
        	logger.debug("audio file");
        }
        try {
			if(!f.exists()){

				URL Imageurl = new URL(strUrl);
				InputStream in = new BufferedInputStream(Imageurl.openStream());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int n = 0;
				while (-1!=(n=in.read(buf)))
				{
					out.write(buf, 0, n);
				}
				out.close();
				in.close();
				byte[] response = out.toByteArray();
				boolean found = false;
				Iterator<Entry<String, byte[]>> it = images.entrySet().iterator();
				while (it.hasNext())
				{
					HashMap.Entry<String, byte[]> pair = (HashMap.Entry<String, byte[]>)it.next();
					
					if (Arrays.equals(pair.getValue(), response)  && pair.getKey().equals(strImage))
					{
						
						found = true;
					}	
				}
				if (!found)
				{
					images.put(strImage, response);
					FileOutputStream fos = new FileOutputStream(strImagePath);
					fos.write(response);
					fos.close();
				}
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					logger.error(" Thread sleep " + e.getLocalizedMessage());
				}				                	
			}
		} catch (MalformedURLException e) {
			logger.error(" MalformedURLException " + e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			logger.error(" FileNotFoundException " + e.getLocalizedMessage());
		} catch (IOException e) {
			logger.error(" IOException " + e.getLocalizedMessage());
		}
		
	}
	private String parseGuide(String strURL) {
		String USER_AGENT = "Mozilla/5.0";
		String strReturn = "";
		//int responseCode = 0;
		HttpsURLConnection con;
		//strURL = "https://www.milovana.com/webteases/showflash.php?id=21630";
		//strURL = "https://www.milovana.com/webteases/getscript.php?id=21630";
		//https://www.milovana.com/media/get.php?folder=27708/21630&name={2}

		URL obj;
		try {
			obj = new URL(strURL);
			con = (HttpsURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.getResponseCode();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//print result
			strReturn =  response.toString();
		} catch (IOException e) {
			logger.error(" IOException " + e.getLocalizedMessage());
		}
		return strReturn;
	}

	private String HtmlUnEncode(String text)
	{
		text = text.replace("&apos;","'");
		text = text.replace("&amp;","&");
		text = text.replace("&lt;","<");
		text = text.replace("&gt;",">");
		text = text.replace("&quot;","\"");
		return text;
	}
	
	  private static void doUpdate(final MainShell mainShell,
		      final String value) {
		  Display.getDefault().asyncExec(new Runnable() {
		      @Override
		      public void run() {
		    	  mainShell.setLblmessage(value);
		      }
		    });
		  }
	  private static void doUpdateTitle(final MainShell mainShell,
		      final String value) {
		  Display.getDefault().asyncExec(new Runnable() {
		      @Override
		      public void run() {
		    	  mainShell.setLbltitle(value);
		      }
		    });
		  }
	  }
