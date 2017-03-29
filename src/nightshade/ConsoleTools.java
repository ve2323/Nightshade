package nightshade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;

public class ConsoleTools {
	
	class SystemSubtabs {
		// native variables
		Commons commons;
		TabPane subTabs = new TabPane();
		
		// cross class variables
		private TabPane tabMaster;
		private Tab system;
		
		public SystemSubtabs(TabPane tabmaster, Tab system, Commons commons){
			this.tabMaster = tabmaster;
			this.system = system;
			this.commons = commons;
		}
		
		public void addTools(){
			
			subTabs.setId("system-subtabs");
			
			// add info tab
			subTabs.getTabs().add(info());
			subTabs.getTabs().add(ip());
			
			// set subset
			system.setContent(subTabs);
			system.setClosable(false);
			tabMaster.getTabs().add(system);
			
		}
		
		private Tab info(){
			
			Tab info = new Tab();
			info.setText("Information");
			info.setClosable(false);
			
			TextArea infoArea = new TextArea();
			infoArea.setId("info-area");
			infoArea.setEditable(false);
			infoArea.setWrapText(true);
			
			Thread memoryThread = new Thread(){
				long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				long spike = memory,fall = memory;
				
				public void run(){
					while(commons.consoleOpen){
						try {
							memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
							if(fall > memory){fall = memory;}
							if(spike < memory){spike = memory;}
							infoArea.clear();
							infoArea.appendText(
								"☆ Memory usage: " + System.lineSeparator() +
								"  • Bytes: " + String.valueOf(memory) + System.lineSeparator() +
								"  • KB: " + String.valueOf(memory/1024) + System.lineSeparator() +
								"  • MB: " + String.valueOf(((memory/1024.0)/1024.0)) + System.lineSeparator() +
								"  • GB: " + String.valueOf((((memory/1024.0)/1024.0)/1024.0)) + System.lineSeparator() +
								"   ■ Highest: " + System.lineSeparator() +
								"     • Bytes: " + String.valueOf(spike) + System.lineSeparator() +
								"     • KB: " + String.valueOf(spike/1024) + System.lineSeparator() +
								"     • MB: " + String.valueOf(((spike/1024.0)/1024.0)) + System.lineSeparator() +
								"     • GB: " + String.valueOf((((spike/1024.0)/1024.0)/1024.0)) + System.lineSeparator() +
								"   ■ Lowest: " + System.lineSeparator() +
								"     • Bytes: " + String.valueOf(fall) + System.lineSeparator() +
								"     • KB: " + String.valueOf(fall/1024) + System.lineSeparator() +
								"     • MB: " + String.valueOf(((fall/1024.0)/1024.0)) + System.lineSeparator() +
								"     • GB: " + String.valueOf((((fall/1024.0)/1024.0)/1024.0)) + System.lineSeparator()
								
							);
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							StringWriter error = new StringWriter();
							e.printStackTrace(new PrintWriter(error));
							
							infoArea.appendText(
								System.lineSeparator() +
								"-- Error occured --" + System.lineSeparator() +
								error.toString() + 
								"Breaking function..."
							);
							break;
						}
					}
					
					infoArea.appendText(
						System.lineSeparator() +
						"-- Function terminated --" + 
						System.lineSeparator() +
						"Attempting to interrupt thread.."
					);
					
					Thread.currentThread().interrupt();
					infoArea.appendText(
						System.lineSeparator() + 
						"Checking if thread is interrupted: " + 
						System.lineSeparator() +
						"Interrupted: " +
						Thread.currentThread().isInterrupted()
					);
					
				}
			};
			memoryThread.start();
			
			info.setContent(infoArea);
			return info;
		}
		
		private Tab ip(){
					
			Tab ip = new Tab();
			ip.setText("Ip");
			ip.setClosable(false);
			
			TextArea ipArea = new TextArea();
			ipArea.setId("ip-area");
			ipArea.setEditable(false);
			ipArea.setWrapText(true);
			ipArea.setPromptText(
				"Press enter to show ip data," +
				System.lineSeparator() +
				"this will run first display your external ip then run 'ipconfig /all' or 'ifconfig -a' depending on OS."
			);
			
			
			ExecutorService executor = Executors.newFixedThreadPool(1);
			
			// get ip address (public and ipconfig / ifconfig depending on OS)
			Runnable ipThread = new Runnable() {
        		
				BufferedReader stdInput;
    			ProcessBuilder pb;
    			String line;
    			boolean ipRunning = false;
				
    			@Override
        		public void run(){
    				EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
			            public void handle(final KeyEvent keyEvent) {
			                if (keyEvent.getCode() == KeyCode.ENTER && !ipRunning) {
			                	ipRunning = true;
			    				try{
			    					ipArea.appendText(
										"-- Public ip --" + 
				    					System.lineSeparator() + 
				    					System.lineSeparator() +
										"IP: " + getPublicIpAddress() + 
										System.lineSeparator() + System.lineSeparator() +
										"-- System > IP --" + 
										System.lineSeparator()
									);
			    					
			    					if(System.getProperty("os.name").toLowerCase().contains("win")){
			    						pb = new ProcessBuilder("ipconfig","/all");
			    					} else if(System.getProperty("os.name").toLowerCase().contains("nux")){
			    						pb = new ProcessBuilder("ifconfig -a");
			    					}
									
									stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
				                    
				                    while (!stdInput.ready()){ /* wait until ready */ }
				            		
									while ((line = stdInput.readLine()) != null){
										if(line != null){
											ipArea.appendText(line + "\n");
										}
									}
			    					
									Thread.currentThread().interrupt();
					            	ipArea.appendText(
										System.lineSeparator() + 
										"Checking if thread is interrupted.." + 
										System.lineSeparator() +
										"Interrupted: " +
										Thread.currentThread().isInterrupted()
									);
					            	stdInput.close();
					            	ipRunning = false;
									return;
									
			    				} catch(Exception e){
			    					StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									
									ipArea.appendText(System.lineSeparator() + System.lineSeparator() + error.toString());
									
									Thread.currentThread().interrupt();
					            	ipArea.appendText(
										System.lineSeparator() + 
										"Checking if thread is interrupted.." + 
										System.lineSeparator() +
										"Interrupted: " +
										Thread.currentThread().isInterrupted()
									);
					            	if(stdInput != null){try {stdInput.close();} catch (IOException e1) {e1.printStackTrace();}}
					            	ipRunning = false;
									return;
			    				}
			                }
			            }
    				};
    				ipArea.setOnKeyPressed(executeCommand);
    			}
    			
			};
			
			executor.execute(ipThread);
			
			ip.setContent(ipArea);
			return ip;
			
		} // fetch public ip address
		public String getPublicIpAddress() throws MalformedURLException,IOException {

		    URL connection = new URL("http://checkip.amazonaws.com/");
		    URLConnection con = connection.openConnection();
		    String str = null;
		    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    str = reader.readLine();
		    
		    reader.close();

		    return str;
		}
		
		
	}
	
	
	class ConsoleSubtabs {
		
		// native variables
		TabPane subTabs = new TabPane();
		boolean freestyleRunning = false;
		
		// cross class variables
		private TabPane tabMaster;
		private Tab Console;
		
		public ConsoleSubtabs(TabPane tabmaster, Tab console){
			this.tabMaster = tabmaster;
			this.Console = console;
		}
		
		public void addTools(){
			
			subTabs.setId("console-subtabs");
			
			// add freestyle tab
			subTabs.getTabs().add(freestyle());
			
			// set subset
			Console.setContent(subTabs);
			Console.setClosable(false);
			tabMaster.getTabs().add(Console);
			
		}
		
		// create and return freestyle tab
		private Tab freestyle(){
				
			Tab free = new Tab();
			free.setText("Freestyle");
			free.setClosable(false);
			
			TextArea command = new TextArea();
			command.setId("freestyle-command");
			command.setPromptText(
				"Enter a commandline argument to execute," + System.lineSeparator() +
				"this is directly linked to java's exec function." + System.lineSeparator() + System.lineSeparator() +
				"This command will origin at current directory which currently is: " + System.lineSeparator() +
				System.getProperty("user.dir") + System.lineSeparator() + System.lineSeparator() +
				"Exec() requires additional arguments to execute: " +
				System.lineSeparator() + 
				" • Windows origin: cmd /c <Insert command here>" + System.lineSeparator() +
				" • Linux origin: /bin/sh <Insert command here>"
			);
			command.setWrapText(true);
			TextArea output = new TextArea();
			output.setId("freestyle-output");
			output.setPromptText(
				"This is where any and all output will be displayed."
			);
			output.setWrapText(true);
			
			SplitPane freestyleContainer = new SplitPane(command,output);
			freestyleContainer.setId("freestyle-container");
			freestyleContainer.setOrientation(Orientation.VERTICAL);
			
			// run actions in separate thread
			Thread freestyleThread = new Thread(){
				
				String commandString,errorOutput;
				Thread freestyleOutput;
				
				public void run(){
					
					// execute on enter click
					EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
			            public void handle(final KeyEvent keyEvent) {
			                if (keyEvent.getCode() == KeyCode.ENTER && !freestyleRunning) {
			                	freestyleRunning = true;
			                	commandString = command.getText();
			                	command.clear();
			                	
			                	freestyleOutput = new Thread(){
			                		
			                		public void run(){
										try {
											output.clear();
											Process process = Runtime.getRuntime().exec(commandString);
											process.waitFor();
											
											output.appendText("-- Executing command --" + System.lineSeparator());
											output.appendText("Command: " + commandString + System.lineSeparator() + System.lineSeparator());
											
											String line;

											BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
											while((line = error.readLine()) != null){
												output.appendText(line + System.lineSeparator());
											}
											error.close();

											BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
											while((line=input.readLine()) != null){
												output.appendText(line + System.lineSeparator());
											}
											input.close();

											OutputStream outputStream = process.getOutputStream();
											PrintStream printStream = new PrintStream(outputStream);
											
											// various data
											output.appendText(System.lineSeparator() + "-- System check --");
											output.appendText(System.lineSeparator() + "stream I/O: " + printStream.toString());
											output.appendText(System.lineSeparator() + "Thread: " + Thread.currentThread().getName());
											output.appendText(System.lineSeparator() + "Action complete, attempting to interrupt thread...");
											
											printStream.flush();
											printStream.close();
											
											Thread.currentThread().interrupt();
											output.appendText(
												System.lineSeparator() + 
												"Checking if thread is interrupted.." + 
												System.lineSeparator() +
												"Interrupted: " +
												Thread.currentThread().isInterrupted()
											);
											freestyleRunning = false;
											return;
											
										} catch (IOException | InterruptedException e) {
											output.clear();
											// write stack trace to string and append to text area
											StringWriter error = new StringWriter();
											e.printStackTrace(new PrintWriter(error));
											errorOutput = error.toString();
											
											output.appendText(errorOutput);
											
											output.appendText(System.lineSeparator() + "Action complete, attempting to interrupt thread..");
											
											Thread.currentThread().interrupt();
											output.appendText(
												System.lineSeparator() + 
												"Checking if thread is interrupted: " + 
												System.lineSeparator() +
												"Interrupted: " +
												Thread.currentThread().isInterrupted()
											);
											freestyleRunning = false;
											return;
										}
			                		}
			                		
			                	};
			                	freestyleOutput.start();
			                	
			                }
			            }
			        };
			        command.setOnKeyPressed(executeCommand);
					
				}
			};
			freestyleThread.start();
			
			free.setContent(freestyleContainer);
			return free;
			
		}
		
	}
	
	class WebSubtabs {
		// native variables
		private TabPane subTabs = new TabPane();
		boolean lookupNotRunning = true;
		boolean scrapegoatNotRunning = true;
		
		// cross class variables
		private TabPane tabMaster;
		private Tab web;
		private WebEngine webEngine;
		
		public WebSubtabs(TabPane tabmaster, Tab web, WebEngine webEngine){
			this.tabMaster = tabmaster;
			this.web = web;
			this.webEngine = webEngine;
		}
		
		public void addTools(){
			
			subTabs.setId("web-subtabs");
			
			// add info tab
			subTabs.getTabs().add(freestyle());
			
			// add statistics tab
			subTabs.getTabs().add(lookup());
			
			// add scrapegoat
			subTabs.getTabs().add(scrapeGoat());
			
			// set subset
			web.setContent(subTabs);
			web.setClosable(false);
			tabMaster.getTabs().add(web);
			
		}
		
		// allow domain lookup
		private Tab lookup(){
			
			Tab lookup = new Tab();
			lookup.setText("Lookup");
			lookup.setClosable(false);
			
			BorderPane lookupContainer = new BorderPane();
			
			TextField inputField = new TextField();
			inputField.setPromptText("Enter a domain name or ip.");
			inputField.setId("lookup-input");
			
			TextArea ipOutput = new TextArea();
			ipOutput.setPromptText(
				"this area will show any and all output regarding input domain ip" + 
				System.lineSeparator() + 
				"This is directly linked to java's inetAddress."
			);
			ipOutput.setId("ip-output");
			ipOutput.setWrapText(true);
			ipOutput.setEditable(false);
			
			TextArea pingOutput = new TextArea();
			pingOutput.setPromptText(
				"this area will show any and all output from input ping" + 
				System.lineSeparator()
			);
			pingOutput.setId("ping-output");
			pingOutput.setWrapText(true);
			pingOutput.setEditable(false);
			
			SplitPane dividerOne = new SplitPane(ipOutput,pingOutput);
			dividerOne.setOrientation(Orientation.VERTICAL);
			
			lookupContainer.setTop(inputField);
			lookupContainer.setCenter(dividerOne);
			
			ExecutorService executor = Executors.newFixedThreadPool(1);
			
			EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
	            public void handle(final KeyEvent keyEvent) {
	                if (keyEvent.getCode() == KeyCode.ENTER && lookupNotRunning) {
	                	lookupNotRunning = false;
	                	Thread lookupThread = new Thread(){
	                		
	                		InetAddress[] allInetAddress;
	            			String input = null;
	            			String errorOutput = null;
	                		
	                		public void run(){
	                			ipOutput.clear();
	    						try {
	    							input = inputField.getText().trim();
	    				            
	    				            ipOutput.appendText("Looking up ip for: " + input + System.lineSeparator());
	    				            
	    				            if(isHostname(input)){
	    				            	allInetAddress = java.net.InetAddress.getAllByName(input);
	    				            	for(int i=0; i<allInetAddress.length; i++){
	    					                ipOutput.appendText(" • " + allInetAddress[i].toString().split("/")[1] + System.lineSeparator());
	    					            }
	    				            } else{
	    				            	ipOutput.appendText(java.net.InetAddress.getByName(input).toString() + System.lineSeparator());
	    				            }
	    				            
	    			            	Thread.currentThread().interrupt();
	    			            	ipOutput.appendText(
	    								System.lineSeparator() + 
	    								"Checking if thread is interrupted.." + 
	    								System.lineSeparator() +
	    								"Interrupted: " +
	    								Thread.currentThread().isInterrupted()
	    							);
	    							return;
	    				            
	    				        } catch (Exception e) {
	    							// write stack trace to string and append to text area
	    							StringWriter error = new StringWriter();
	    							e.printStackTrace(new PrintWriter(error));
	    							errorOutput = error.toString();
	    							
	    							ipOutput.appendText(errorOutput);
	    							Thread.currentThread().interrupt();
	    							ipOutput.appendText(
	    								System.lineSeparator() + 
	    								"Checking if thread is interrupted.." + 
	    								System.lineSeparator() +
	    								"Interrupted: " +
	    								Thread.currentThread().isInterrupted()
	    							);
	    							return;
	    				        }
	                		}
	                	};
	                	
	                	Runnable pingThread = new Runnable() {
	                		
	                		InetAddress[] allInetAddress;
	            			String input = null;
	            			String line,currentIp;
	            			BufferedReader stdInput;
	            			ProcessBuilder pb;
	                		
	            			@Override
	                		public void run(){
	                			pingOutput.clear();
								try{
			            			input = inputField.getText().trim();
			            			
				            		if(isHostname(input)){
				            			allInetAddress = java.net.InetAddress.getAllByName(input);
				            			for(int i=0; i<allInetAddress.length; i++){
				            				
				            				currentIp = allInetAddress[i].toString().split("/")[1];
				            				
				            				pingOutput.appendText(
					                    		System.lineSeparator() + System.lineSeparator() +
					                    		"-- Ping " + currentIp + " --" +
					                    		System.lineSeparator()
				                    		);
				                    		
				            				pb = new ProcessBuilder("ping", currentIp);
						                    stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
						                    
						                    while (!stdInput.ready()){ /* wait until ready */ }
				                    		
											while ((line = stdInput.readLine()) != null){
												if(line != null){
													pingOutput.appendText(line + "\n");
												}
											}
						                    
				            			}
					                    
					                    Thread.currentThread().interrupt();
										pingOutput.appendText(
											System.lineSeparator() + System.lineSeparator() +
											"Checking if thread is interrupted.." + 
											System.lineSeparator() +
											"Interrupted: " +
											Thread.currentThread().isInterrupted()
										);
										lookupNotRunning = true;
										stdInput.close();
										return;
						            } else{
						            	
						            	pingOutput.appendText(
				                    		System.lineSeparator() +
				                    		"-- Ping " + input + " --" +
				                    		System.lineSeparator()
			                    		);
						            	
						            	pb = new ProcessBuilder("ping", input);
					                    stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));        
			
					                    while (!stdInput.ready()){ /* wait until ready */ }
			
					                    while ((line = stdInput.readLine()) != null){
					                    	if(line != null){
					                    		pingOutput.appendText(line);
					                    	}
					                    }
					                    
					                    Thread.currentThread().interrupt();
										pingOutput.appendText(
											System.lineSeparator() + System.lineSeparator() + 
											"Checking if thread is interrupted.." + 
											System.lineSeparator() +
											"Interrupted: " +
											Thread.currentThread().isInterrupted()
										);
										lookupNotRunning = true;
										stdInput.close();
										return;
					                    
						            }
				            		
			                	} catch(Exception e){
			                		// write stack trace to string and append to text area
									StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									
									pingOutput.appendText(error.toString());
									
									// make absolutely sure the stream closes with the thread on exception
									if(stdInput != null){
										try {stdInput.close();} catch (IOException ex) {/* do nothing */}
									}
									
									Thread.currentThread().interrupt();
									pingOutput.appendText(
										System.lineSeparator() + System.lineSeparator() + 
										"Checking if thread is interrupted.." + 
										System.lineSeparator() +
										"Interrupted: " +
										Thread.currentThread().isInterrupted()
									);
									lookupNotRunning = true;
									return;
			                	}
	                		}
	                	};
	                	
	                	lookupThread.start();
						executor.execute(pingThread);
	                }
	            }
	        };
	        inputField.setOnKeyPressed(executeCommand);
			
			lookup.setContent(lookupContainer);
			return lookup;
			
		}
		
		private Tab freestyle(){
			
			Tab free = new Tab();
			free.setText("Freestyle");
			free.setClosable(false);
			
			TextArea command = new TextArea();
			command.setId("freestyle-web-command");
			command.setPromptText(
				"Enter javascript code to execute," + System.lineSeparator() +
				"this is directly linked to java's executeScript function."
			);
			command.setWrapText(true);
			TextArea output = new TextArea();
			output.setId("freestyle-web-output");
			output.setPromptText(
				"Any and all output will be show here."
			);
			output.setWrapText(true);
			
			SplitPane freestyleContainer = new SplitPane(command,output);
			freestyleContainer.setId("freestyle-web-container");
			freestyleContainer.setOrientation(Orientation.VERTICAL);
			
			try{
				// run actions in separate thread
				Platform.runLater(new Runnable(){
					
					String commandString,errorOutput;
					
					public void run(){
						
						// execute on enter click
						EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
				            public void handle(final KeyEvent keyEvent) {
				                if (keyEvent.getCode() == KeyCode.ENTER) {
				                	commandString = command.getText();
				                	command.clear();
				                	
				                	Platform.runLater(new Runnable(){
				                		
				                		public void run(){
											try {
												output.clear();
												
												output.appendText("-- Executing javascript --" + System.lineSeparator());
												output.appendText("Script: " + commandString + System.lineSeparator() + System.lineSeparator());
												
												webEngine.executeScript(commandString);
												
												// various data
												output.appendText(System.lineSeparator() + "-- System check --");
												output.appendText(System.lineSeparator() + "Thread: " + Thread.currentThread().getName());
												output.appendText(System.lineSeparator() + "Action complete, attempting to interrupt thread...");
												
												Thread.currentThread().interrupt();
												output.appendText(
													System.lineSeparator() + 
													"Checking if thread is interrupted.." + 
													System.lineSeparator() +
													"Interrupted: " +
													Thread.currentThread().isInterrupted()
												);
												return;
												
											} catch (Exception e) {
												output.clear();
												// write stack trace to string and append to text area
												e.printStackTrace();
												StringWriter error = new StringWriter();
												e.printStackTrace(new PrintWriter(error));
												errorOutput = error.toString();
												
												output.appendText(errorOutput);
												
												output.appendText(System.lineSeparator() + "Action complete, attempting to interrupt thread...");
												
												Thread.currentThread().interrupt();
												output.appendText(
													System.lineSeparator() + 
													"Checking if thread is interrupted.." + 
													System.lineSeparator() +
													"Interrupted: " +
													Thread.currentThread().isInterrupted()
												);
												return;
											}
				                		}
				                		
				                	});
				                	
				                }
				            }
				        };
				        command.setOnKeyPressed(executeCommand);
						
					}
				});
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			free.setContent(freestyleContainer);
			return free;
		}
		
		// TODO:
		// jsoup tool
		private Tab scrapeGoat(){
			
			Tab jsoup = new Tab();
			jsoup.setText("scrapegoat");
			jsoup.setClosable(false);
			
			BorderPane container = new BorderPane();
			
			// grab from url
			TextField specifiedUrl = new TextField();
			specifiedUrl.setId("scrapegoat-url-input");
			specifiedUrl.setPromptText("Enter url to fetch data from.");
			
			// output
			TextArea output = new TextArea();
			output.setId("scrapegoat-output");
			output.setPromptText(
				"This is where any and all output will be displayed." + System.lineSeparator() +
				"Scrapegoat makes use of Jsoup to parse html."
			);
			output.setWrapText(true);
			
			// element to obtain
			//TextField specifiedItem = new TextField();
			//specifiedItem.setPromptText("Selector, Directly linked to Jsoup select functionality.");
			
			container.setTop(specifiedUrl);
			container.setCenter(output);
			
			ExecutorService executor = Executors.newFixedThreadPool(1);
			
			Runnable jsoupThread = new Runnable() {
				String url,item;
				
				@Override
				public void run(){
					// execute on enter click
					EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
			            public void handle(final KeyEvent keyEvent) {
			                if (keyEvent.getCode() == KeyCode.ENTER && scrapegoatNotRunning) {
			                	output.clear();
			                	scrapegoatNotRunning = false;
			                    url = specifiedUrl.getText().trim();
	     					    //item = specifiedItem.getText().trim();
			                	
			                	try{
			                		
			                		if(url != null && !url.equals("")){
			                			if(item != null && !item.equals("")){
			                				
			                			} else{
			                				Document doc = Jsoup.connect(url).get();
			                				output.appendText(doc.toString());
			                				
			                				Thread.currentThread().interrupt();
											output.appendText(
												System.lineSeparator() + System.lineSeparator() +
												"Checking if thread is interrupted.." + 
												System.lineSeparator() +
												"Interrupted: " +
												Thread.currentThread().isInterrupted()
											);
											scrapegoatNotRunning = true;
											return;
			                			}
			                		} else{
			                			output.appendText("No url found");
			                			
			                			Thread.currentThread().interrupt();
										output.appendText(
											System.lineSeparator() + System.lineSeparator() +
											"Checking if thread is interrupted.." + 
											System.lineSeparator() +
											"Interrupted: " +
											Thread.currentThread().isInterrupted()
										);
										scrapegoatNotRunning = true;
										return;
			                		}
			                		
			                	}catch(Exception e){
			                		output.clear();
									// write stack trace to string and append to text area
									e.printStackTrace();
									StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									output.appendText(error.toString());
			                		
			                		Thread.currentThread().interrupt();
									output.appendText(
										System.lineSeparator() + 
										"Checking if thread is interrupted.." + 
										System.lineSeparator() +
										"Interrupted: " +
										Thread.currentThread().isInterrupted()
									);
									scrapegoatNotRunning = true;
									return;
			                		
			                	}
			                	
			                }
			            }
					};
					specifiedUrl.setOnKeyPressed(executeCommand);
					//specifiedItem.setOnKeyPressed(executeCommand);
					
				}
			};
			executor.execute(jsoupThread);
			
			jsoup.setContent(container);
			return jsoup;
			
		}

		
		private boolean isHostname(String s) {

		    char[] ca = s.toCharArray();
		    // if we see a character that is neither a digit nor a period
		    // then s is probably a hostname
		    for (int i = 0; i < ca.length; i++) {
		      if (!Character.isDigit(ca[i])) {
		        if (ca[i] != '.') {
		          return true;
		        }
		      }
		    }

		    // Everything was either a digit or a period
		    // so s looks like an IP address in dotted quad format
		    return false;

		  } // end isHostName
		
		
	}
	
}