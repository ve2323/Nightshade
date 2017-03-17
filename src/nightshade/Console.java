package nightshade;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class Console {
	
	// class native variables
	private boolean consoleOpen = false;
	private TabPane tabmaster = new TabPane();
	private StackPane tabArea = new StackPane();
	private BorderPane tabContainer = new BorderPane();
	private double consoleWidth;
	
	// cross class variables
	private Scene scene;
	private BorderPane contentPane;
	
	public Console(Scene scene,BorderPane contentPane){
		this.scene = scene;
		this.contentPane = contentPane;
	}
	
	// open console on key combination
	public void addConsole(){
		
		// ctrl+d
		KeyCombination consoleCombo = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>(){
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
	    	
	    	Tab tabOne = new Tab();
	    	tabOne.setText("new tab");
	    	tabOne.setContent(tabArea);
	    	tabmaster.getTabs().add(tabOne);
	    	
	    	tabContainer.setCenter(tabmaster);
	    	tabContainer.prefHeightProperty().bind(scene.heightProperty());
	    	consoleWidth = scene.getWidth()/3;
	    	tabContainer.setPrefWidth(consoleWidth);
	    	scene.widthProperty().addListener((obs, oldVal, newVal) -> {tabContainer.setPrefWidth((double)newVal/3);});
	    	
	    	contentPane.setRight(tabContainer);
	    	
	    } 
	    // close console
	    else{
	    	consoleOpen = false;
	    	contentPane.setRight(null);
	    	tabmaster.getTabs().remove(0);
	    }
	    
	}
	
}