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

public class Console extends Thread{
	
	// class native variables
	private boolean consoleOpen = false;
	private TabPane tabmaster = new TabPane();
	private BorderPane tabContainer = new BorderPane();
	
	// cross class variables
	private SplitPane browserContainer;
	
	public Console(SplitPane browserContainer){
		this.browserContainer = browserContainer;
	}
	
	// open console on key combination
	public void addConsole(){
		
		tabmaster.setId("console-tabs");
		
		// ctrl+d
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
	    	
	    	Tab system = new Tab();
	    	system.setText("System");
	    	
	    	// console content
	    	ConsoleTools.ConsoleSubtabs consoleTools = new ConsoleTools().new ConsoleSubtabs(tabmaster, console);
	    	consoleTools.addTools();
	    	
	    	// system content
	    	ConsoleTools.SystemSubtabs systemTools = new ConsoleTools().new SystemSubtabs(tabmaster, system);
	    	systemTools.addTools();
	    	
	    	// console settings
	    	tabContainer.setCenter(tabmaster);
	    	
	    	browserContainer.getItems().add(tabContainer);
	    	
	    } 
	    // close console
	    else{
	    	consoleOpen = false;
	    	browserContainer.getItems().remove(tabContainer);
	    	
	    	// remove tabs
	    	tabmaster.getTabs().remove(0);
	    	tabmaster.getTabs().remove(0);
	    }
	    
	}
	
}