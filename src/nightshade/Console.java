package nightshade;

import javafx.event.EventHandler;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;

public class Console{
	
	// class native variables
	private boolean consoleOpen = false;
	private TabPane tabmaster = new TabPane();
	private BorderPane tabContainer = new BorderPane();
	
	// cross class variables
	private SplitPane browserContainer;
	private WebEngine webEngine;
	
	// constructor
	public Console(SplitPane browserContainer, WebEngine webEngine){
		this.browserContainer = browserContainer;
		this.webEngine = webEngine;
	}
	
	// open console on key combination
	public void addConsole(){
		
		// id
		tabmaster.setId("console-tabs");
		
		// open/close console  with ctrl+D
		KeyCombination consoleCombo = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
		browserContainer.setOnKeyPressed(new EventHandler<KeyEvent>(){
	        @Override
	        public void handle(KeyEvent event){
		        if (consoleCombo.match(event)){
		            console();
		        }
	        }
	    });
	}
	
	// console object
	public void console(){
		
		// check if console is open
	    if(!consoleOpen){
	    	consoleOpen = true;
	    	
	    	// console tabs
	    	Tab console = new Tab();
	    	console.setText("Console");
	    	
	    	// system tabs
	    	Tab system = new Tab();
	    	system.setText("System");
	    	
	    	// web tabs
	    	Tab web = new Tab();
	    	web.setText("Web");
	    	
	    	// console content
	    	ConsoleTools.ConsoleSubtabs consoleTools = new ConsoleTools().new ConsoleSubtabs(tabmaster, console);
	    	consoleTools.addTools();
	    	
	    	// system content
	    	ConsoleTools.SystemSubtabs systemTools = new ConsoleTools().new SystemSubtabs(tabmaster, system);
	    	systemTools.addTools();
	    	
	    	// web content
	    	ConsoleTools.WebSubtabs webTools = new ConsoleTools().new WebSubtabs(tabmaster, web, webEngine);
	    	webTools.addTools();
	    	
	    	// console settings
	    	tabContainer.setCenter(tabmaster);
	    	
	    	// add tabcontainer to browserContainer
	    	browserContainer.getItems().add(tabContainer);
	    	
	    } 
	    // close console
	    else{
	    	consoleOpen = false;
	    	browserContainer.getItems().remove(tabContainer);
	    	
	    	// remove tabs one by one
	    	tabmaster.getTabs().remove(0);
	    	tabmaster.getTabs().remove(0);
	    	tabmaster.getTabs().remove(0);
	    }
	    
	}
	
}