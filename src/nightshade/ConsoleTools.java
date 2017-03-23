package nightshade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;

public class ConsoleTools {
	
	class SystemSubtabs {
		// native variables
		TabPane subTabs = new TabPane();
		
		// cross class variables
		private TabPane tabMaster;
		private Tab system;
		
		public SystemSubtabs(TabPane tabmaster, Tab system){
			this.tabMaster = tabmaster;
			this.system = system;
		}
		
		public void addTools(){
			
			subTabs.setId("system-subtabs");
			
			// add info tab
			subTabs.getTabs().add(info());
			
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
					while(true){
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
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			memoryThread.start();
			
			info.setContent(infoArea);
			return info;
		}
		
		
	}
	
	
	class ConsoleSubtabs {
		
		// native variables
		TabPane subTabs = new TabPane();
		
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
			command.setWrapText(true);
			TextArea output = new TextArea();
			output.setId("freestyle-output");
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
			                if (keyEvent.getCode() == KeyCode.ENTER) {
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
			subTabs.getTabs().add(statistics());
			
			// set subset
			web.setContent(subTabs);
			web.setClosable(false);
			tabMaster.getTabs().add(web);
			
		}
		
		private Tab statistics(){
			
			Tab stat = new Tab();
			stat.setText("Statistics");
			stat.setClosable(false);
			
			
			
			
			return stat;
			
		}
		
		private Tab freestyle(){
			
			Tab free = new Tab();
			free.setText("Freestyle");
			free.setClosable(false);
			
			TextArea command = new TextArea();
			command.setId("freestyle-web-command");
			command.setWrapText(true);
			TextArea output = new TextArea();
			output.setId("freestyle-web-output");
			output.setWrapText(true);
			
			SplitPane freestyleContainer = new SplitPane(command,output);
			freestyleContainer.setId("freestyle-web-container");
			freestyleContainer.setOrientation(Orientation.VERTICAL);
			
			//TODO: check fx application thread exceptions
			// encapsule with try/catch
			
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
			
			free.setContent(freestyleContainer);
			return free;
		}
		
		
	}
	
}