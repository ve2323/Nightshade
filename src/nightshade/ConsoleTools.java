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
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
		boolean ipRunning = false;
		
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
			
			TextArea systemArea = new TextArea();
			systemArea.setId("system-area");
			systemArea.setEditable(false);
			systemArea.setWrapText(true);
			
			TextArea memoryArea = new TextArea();
			memoryArea.setId("info-area");
			memoryArea.setEditable(false);
			memoryArea.setWrapText(true);
			
			SplitPane content = new SplitPane(systemArea,memoryArea);
			content.setOrientation(Orientation.VERTICAL);
			
			// display system properties
			Thread systemThread = new Thread(){
				Properties pro = System.getProperties();
				
				public void run(){
					
					systemArea.appendText(
			    			"Obtaining system properties..." + 
	    					System.lineSeparator() + System.lineSeparator()
    					);
					
					pro = System.getProperties();
				    for(Object obj : pro.keySet()){
				    	systemArea.appendText(
	    					(String)obj + ": " + System.lineSeparator() +
							System.getProperty((String)obj) + 
	    					System.lineSeparator() + System.lineSeparator()
    					);
				    }
				    
				    Thread.currentThread().interrupt();
					systemArea.appendText(
						System.lineSeparator() + 
						"Checking if thread is interrupted: " + 
						System.lineSeparator() +
						"Interrupted: " +
						Thread.currentThread().isInterrupted()
					);
					return;
					
				}
				
			};
			systemThread.start();
			
			// actively display system memory
			Thread memoryThread = new Thread(){
				long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				long spike = memory,fall = memory;
				
				public void run(){
					while(commons.consoleOpen){
						try {
							memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
							if(fall > memory){fall = memory;}
							if(spike < memory){spike = memory;}
							memoryArea.clear();
							memoryArea.appendText(
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
							
							memoryArea.appendText(
								System.lineSeparator() +
								"-- Error occured --" + System.lineSeparator() +
								error.toString() + 
								"Breaking function..."
							);
							try {error.close();} catch (IOException e1) {e1.printStackTrace();}
							break;
						}
					}
					
					memoryArea.appendText(
						System.lineSeparator() +
						"-- Function terminated --" + 
						System.lineSeparator() +
						"Attempting to interrupt thread.."
					);
					
					Thread.currentThread().interrupt();
					memoryArea.appendText(
						System.lineSeparator() + 
						"Checking if thread is interrupted: " + 
						System.lineSeparator() +
						"Interrupted: " +
						Thread.currentThread().isInterrupted()
					);
					return;
				}
			};
			memoryThread.start();
			
			info.setContent(content);
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
				"this will first display your external ip then run 'ipconfig /all' or 'ifconfig -a' depending on OS."
			);
			
			// get ip address (public and ipconfig / ifconfig depending on OS)
			EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
	            public void handle(final KeyEvent keyEvent) {
	                if (keyEvent.getCode() == KeyCode.ENTER && !ipRunning) {
	                	ExecutorService executor = Executors.newSingleThreadExecutor();
	                	Runnable ipThread = new Runnable() {
	        				
	            			@Override
	                		public void run(){
			                	ipRunning = true;
			    				try{
			    					ipArea.clear();
			    					ipArea.selectHome();
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
			    						ProcessBuilder pb = new ProcessBuilder("ipconfig","/all");
			    						
			    						BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
					                    
					                    while (!stdInput.ready()){ /* wait until ready */ }
					            		
					                    String line = "Null";
					                    
										while ((line = stdInput.readLine()) != null){ipArea.appendText(line + System.lineSeparator());}
				    					
						            	if(stdInput != null){try {stdInput.close();} catch (IOException e1) { /* do nothing */ }}
						            	ipRunning = false;
			    						
			    					} else if(System.getProperty("os.name").toLowerCase().contains("nux")){
			    						ProcessBuilder pb = new ProcessBuilder("ifconfig -a");
			    						
			    						BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
					                    
					                    while (!stdInput.ready()){ /* wait until ready */ }
					            		
					                    String line = "Null";
					                    
										while ((line = stdInput.readLine()) != null){ipArea.appendText(line + System.lineSeparator());}
				    					
						            	if(stdInput != null){try {stdInput.close();} catch (IOException e1) { /* do nothing */ }}
						            	ipRunning = false;
			    					}
									
			    				} catch(Exception e){
			    					StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									
									ipArea.appendText(System.lineSeparator() + System.lineSeparator() + error.toString());
									try {error.close();} catch (IOException e1) {e1.printStackTrace();}
									
					            	ipRunning = false;
			    				}
			                }
	                	};
	                	executor.execute(ipThread);
	                	executor.shutdown();
	                	try {
							executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	                }
	            }
			};
			ipArea.setOnKeyPressed(executeCommand);
			
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
				
				public void run(){
					
					// execute on enter click
					EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
			            public void handle(final KeyEvent keyEvent) {
			                if (keyEvent.getCode() == KeyCode.ENTER && !freestyleRunning) {
			                	freestyleRunning = true;
			                	String commandString = command.getText();
			                	command.clear();
			                	command.selectHome();
			                	Thread freestyleOutput = new Thread(){
			                		
			                		public void run(){
										try {
											output.clear();
											output.selectHome();
											
											output.appendText("-- Executing system command --" + System.lineSeparator());
											output.appendText("Command: " + commandString + System.lineSeparator() + System.lineSeparator());
											
											if(!commandString.equals("") && !commandString.equals(null)){
												Process process = Runtime.getRuntime().exec(commandString);
												process.waitFor();
												
												String line = "Null";

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
											}
											
											
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
											output.selectHome();
											// write stack trace to string and append to text area
											StringWriter error = new StringWriter();
											e.printStackTrace(new PrintWriter(error));
											String errorOutput = error.toString();
											
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
		Runnable jsoupThread;
		
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
			
			TextArea traceOutput = new TextArea();
			traceOutput.setPromptText(
				"this area will show any and all output from input route trace" + 
				System.lineSeparator()
			);
			traceOutput.setId("trace-output");
			traceOutput.setWrapText(true);
			traceOutput.setEditable(false);
			
			SplitPane dividerOne = new SplitPane(pingOutput,traceOutput);
			SplitPane dividerTwo = new SplitPane(ipOutput,dividerOne);
			dividerTwo.setOrientation(Orientation.VERTICAL);
			
			lookupContainer.setTop(inputField);
			lookupContainer.setCenter(dividerTwo);
			
			EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
	            public void handle(final KeyEvent keyEvent) {
	                if (keyEvent.getCode() == KeyCode.ENTER && lookupNotRunning) {
	                	lookupNotRunning = false;
	                	
	                	ExecutorService executor = Executors.newFixedThreadPool(2);
	                	
	                	// look up address
	                	Thread lookupThread = new Thread(){
	                		
	                		public void run(){
	                			ipOutput.clear();
	                			ipOutput.selectHome();
	    						try {
	    							String input = inputField.getText().trim();
	    				            
	    				            ipOutput.appendText("Looking up ip for: " + input + System.lineSeparator());
	    				            
	    				            if(isHostname(input)){
	    				            	InetAddress[] allInetAddress = java.net.InetAddress.getAllByName(input);
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
	    							String errorOutput = error.toString();
	    							
	    							ipOutput.appendText(errorOutput);
	    							try {error.close();} catch (IOException e1) {e1.printStackTrace();}
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
	                	
	                	//ping address
	                	Runnable pingThread = new Runnable() {
	                		
	            			@Override
	                		public void run(){
	                			pingOutput.clear();
	                			pingOutput.selectHome();
								try{
			            			String input = inputField.getText().trim();
			            			
				            		if(isHostname(input)){
				            			InetAddress[] allInetAddress = java.net.InetAddress.getAllByName(input);
				            			for(int i=0; i<allInetAddress.length; i++){
				            				
				            				String currentIp = allInetAddress[i].toString().split("/")[1];
				            				
				            				pingOutput.appendText(
					                    		System.lineSeparator() + System.lineSeparator() +
					                    		"-- Ping " + currentIp + " --" +
					                    		System.lineSeparator()
				                    		);
				                    		
				            				ProcessBuilder pb = new ProcessBuilder("ping", currentIp);
				            				BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
						                    
						                    while (!stdInput.ready()){ /* wait until ready */ }
						                    
						                    String line = "Null";
				                    		
											while ((line = stdInput.readLine()) != null){
												pingOutput.appendText(line + "\n");
											}
											
											stdInput.close();
						                    
				            			}
										
						            } else{
						            	
						            	pingOutput.appendText(
				                    		System.lineSeparator() +
				                    		"-- Ping " + input + " --" +
				                    		System.lineSeparator()
			                    		);
						            	
						            	ProcessBuilder pb = new ProcessBuilder("ping", input);
						            	BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));        
			
					                    while (!stdInput.ready()){ /* wait until ready */ }
					                    
					                    String line = "Null";
			
					                    while ((line = stdInput.readLine()) != null){
				                    		pingOutput.appendText(line);
					                    }
					                    
										stdInput.close();
					                    
						            }
				            		
			                	} catch(Exception e){
			                		// write stack trace to string and append to text area
									StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									
									pingOutput.appendText(error.toString());
									try {error.close();} catch (IOException e1) {e1.printStackTrace();}
									
			                	}
	                		}
	                	};
	                	
	                	// traceroute address
                		Runnable traceThread = new Runnable() {
	                		
	            			@Override
	                		public void run(){
	                			traceOutput.clear();
	                			traceOutput.selectHome();
								try{
			            			String input = inputField.getText().trim();
			            			
			            			String commandValue = "Null";
					            	if(System.getProperty("os.name").toLowerCase().contains("win")){
					            		commandValue = "tracert";
					            	} else if(System.getProperty("os.name").toLowerCase().contains("nux")){
					            		commandValue = "traceroute";
					            	}
			            			
				            		if(isHostname(input)){
				            			InetAddress[] allInetAddress = java.net.InetAddress.getAllByName(input);
				            			for(int i=0; i<allInetAddress.length; i++){
				            				
				            				String currentIp = allInetAddress[i].toString().split("/")[1];
				            				
				            				traceOutput.appendText(
					                    		System.lineSeparator() + System.lineSeparator() +
					                    		"-- Traceroute " + currentIp + " --" +
					                    		System.lineSeparator()
				                    		);
				            				
			            					ProcessBuilder pb = new ProcessBuilder(commandValue, currentIp);
				    						
				    						BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
						                    
						                    while (!stdInput.ready()){ /* wait until ready */ }
						                    
						                    String line = "Null";
				                    		
											while ((line = stdInput.readLine()) != null){
												traceOutput.appendText(line + "\n");
											}
											
											stdInput.close();
				            				
						                    
				            			}
										
						            } else{
						            	
						            	String currentIp = input;
						            	
						            	traceOutput.appendText(
				                    		System.lineSeparator() +
				                    		"-- Traceroute " + input + " --" +
				                    		System.lineSeparator()
			                    		);
						            	
			    						ProcessBuilder pb = new ProcessBuilder(commandValue, currentIp);
			    						
			    						BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));        
			    						
					                    while (!stdInput.ready()){ /* wait until ready */ }
					                    
					                    String line = "Null";
			
					                    while ((line = stdInput.readLine()) != null){
				                    		traceOutput.appendText(line);
					                    }
					                    
										stdInput.close();
					                    
						            }
				            		
			                	} catch(Exception e){
			                		// write stack trace to string and append to text area
									StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									
									traceOutput.appendText(error.toString());
									try {error.close();} catch (IOException e1) {e1.printStackTrace();}
									
			                	}
	                		}
	                	};
	                	
	                	lookupThread.start();
	                	executor.submit(pingThread);
	                	executor.submit(traceThread);
	                	executor.shutdown();
	                	
	                	// await termination
	                	Thread runChecker = new Thread(){
	                		public void run(){
	                			try {
	    							executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	    							Thread.currentThread().interrupt();
									lookupNotRunning = true;
									return;
	    						} catch (InterruptedException e) {
									e.printStackTrace();
									Thread.currentThread().interrupt();
									lookupNotRunning = true;
									return;
	    						}
	                		}
	                	};
	                	runChecker.start();
	                	
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
					
					public void run(){
						
						// execute on enter click
						EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
				            public void handle(final KeyEvent keyEvent) {
				                if (keyEvent.getCode() == KeyCode.ENTER) {
				                	String commandString = command.getText();
				                	command.clear();
				                	command.selectHome();
				                	Platform.runLater(new Runnable(){
				                		
				                		public void run(){
											try {
												output.clear();
												command.selectHome();
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
												command.selectHome();
												// write stack trace to string and append to text area
												e.printStackTrace();
												StringWriter error = new StringWriter();
												e.printStackTrace(new PrintWriter(error));
												String errorOutput = error.toString();
												
												output.appendText(errorOutput);
												try {error.close();} catch (IOException e1) {e1.printStackTrace();}
												
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
			TextField specifiedItem = new TextField();
			specifiedItem.setId("scrapegoat-selector-input");
			specifiedItem.setPromptText("Selector, Directly linked to Jsoup select functionality.");
			
			container.setTop(new SplitPane(specifiedUrl,specifiedItem));
			container.setCenter(output);
			
			// execute on enter click
			EventHandler<KeyEvent> executeCommand = new EventHandler<KeyEvent>() {
	            public void handle(final KeyEvent keyEvent) {
	                if (keyEvent.getCode() == KeyCode.ENTER && scrapegoatNotRunning) {
	                	ExecutorService executor = Executors.newSingleThreadExecutor();
	                	jsoupThread = new Runnable() {
	        				
	        				@Override
	        				public void run(){
			                	output.clear();
			                	output.selectHome();
			                	scrapegoatNotRunning = false;
			                    String url = specifiedUrl.getText().trim();
	     					    String item = specifiedItem.getText().trim();
	     					    specifiedUrl.clear();
	     					    specifiedUrl.selectHome();
			                	specifiedItem.clear();
			                	specifiedItem.selectHome();
			                	
			                	try{
			                		
			                		if(url != null && !url.equals("")){
			                			if(item != null && !item.equals("")){
			                				try{
				                				Document doc = Jsoup.connect(url).get();
				                				
				                				Elements items = doc.select(item);
				                				
				                				if(items != null){
				                					output.appendText(items.toString() + System.lineSeparator());
				                				}
				                				
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
				                				
			                				}catch(Exception e){
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
			                		output.selectHome();
									// write stack trace to string and append to text area
									e.printStackTrace();
									StringWriter error = new StringWriter();
									e.printStackTrace(new PrintWriter(error));
									output.appendText(error.toString());
									try {error.close();} catch (IOException e1) {e1.printStackTrace();}
			                		
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
						};
						executor.execute(jsoupThread);
						executor.shutdown();
						try {
							executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	                	
	                }
	            }
			};
			specifiedUrl.setOnKeyPressed(executeCommand);
			specifiedItem.setOnKeyPressed(executeCommand);
			
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