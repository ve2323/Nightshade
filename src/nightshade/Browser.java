package nightshade;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Browser{
	
	// native class variables
	private boolean stageMaxed = true;
	private double xOffset,yOffset;
	Rectangle2D primaryScreenBounds;
	
	// cross class variables
	private BorderPane contentPane;
	private Stage primarystage;
	
	public Browser(Stage primarystage, BorderPane contentPane){
		this.contentPane = contentPane;
		this.primarystage = primarystage;
	}
	
	// create and add browser view with all required components
	public BorderPane addBrowser(Tab tab){
		
		// create new webEngine
		WebView browser = new WebView();
		WebEngine webEngine = browser.getEngine();
		TextField addressField = new TextField();
		BorderPane browserView = new BorderPane();
		SplitPane browserContainer = new SplitPane();
		
		browse(webEngine, "https://www.google.com/");
		
		// create search bar
		addSearchbar(webEngine, browserView, addressField, tab);
		
		browserContainer.getItems().add(browser);
		
		// set browser content
		browserView.setCenter(browserContainer);
		
		// create console
        Console console = new Console(browserContainer);
        console.addConsole();
		
        // create fire bug
		Firebug bug = new Firebug(webEngine, browserView);
		bug.addFirebug();
		
		return browserView;
		
	}
	
	// create and add search bar
	private void addSearchbar(WebEngine webEngine, BorderPane browserView, TextField addressField,Tab tab){
		
		HBox searchContainer = new HBox();
		searchContainer.setId("search-container");
		
		Button goBack = new Button();
		goBack.getStyleClass().add("browser-button");
		Image goBackImage = new Image(getClass().getResourceAsStream("/assets/nightshade_arrow_left.png"));
		goBack.setGraphic(new ImageView(goBackImage));
		
		Button goForward = new Button();
		goForward.getStyleClass().add("browser-button");
		Image goForwardImage = new Image(getClass().getResourceAsStream("/assets/nightshade_arrow_right.png"));
		goForward.setGraphic(new ImageView(goForwardImage));
		
		Button reload = new Button();
		reload.getStyleClass().add("browser-button");
		Image reloadImage = new Image(getClass().getResourceAsStream("/assets/nightshade_reload.png"));
		reload.setGraphic(new ImageView(reloadImage));
		
		addressField.setId("address-field");
		addressField.setPrefWidth(searchContainer.getWidth()/2);
		searchContainer.widthProperty().addListener((obs, oldVal, newVal) -> {addressField.setPrefWidth((double)newVal/2);});
		
		// append url to address field on trigger
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
    		@SuppressWarnings("rawtypes")
    		@Override public void changed(ObservableValue ov, State oldState, State newState) {
              if (newState == Worker.State.SUCCEEDED) {
            	tab.setText(webEngine.getTitle());
                addressField.clear();
                addressField.appendText(webEngine.getLocation());
              }
              if(newState == Worker.State.RUNNING){
            	  tab.setText("Loading...");
              }
            }
        });
		
		// go back button
		goBack.setOnAction((event) -> {webEngine.executeScript("history.back()");});
		
		// reload button
		reload.setOnAction((event) -> {webEngine.reload();});
		
		// go forward button
		goForward.setOnAction((event) -> {webEngine.executeScript("history.forward()");});
		
		// handle enter press
		EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                	browse(webEngine, addressField.getText());
                }
            }
        };

        addressField.setOnKeyPressed(keyEventHandler);
		
		searchContainer.getChildren().addAll(goBack,reload,goForward,addressField);
		
		browserView.setTop(searchContainer);
	}
	
	// trigger browse
	public void browse(WebEngine webEngine, String address){
        webEngine.load(address);
	}
	
	// trigger browse
	public void initBrowser(){
		
		final TabPane tabPane = new TabPane();
		tabPane.setId("browser-tabs");
		
		makedraggable(tabPane);
		
		HBox hbox = new HBox();
		hbox.getChildren().addAll(minButton("minimize"),maxButton("maximize"), exitButton("exit"));
		AnchorPane anchor = new AnchorPane();
		anchor.getChildren().addAll(tabPane, hbox);
        AnchorPane.setTopAnchor(hbox, 3.0);
        AnchorPane.setRightAnchor(hbox, 5.0);
        AnchorPane.setTopAnchor(tabPane, 1.0);
        AnchorPane.setRightAnchor(tabPane, 1.0);
        AnchorPane.setLeftAnchor(tabPane, 1.0);
        AnchorPane.setBottomAnchor(tabPane, 1.0);
		
	    final Tab newTab = new Tab("+");
	    newTab.setClosable(false);
	    tabPane.getTabs().add(newTab);
	    createAndSelectNewTab(tabPane);

	    tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
	      @Override
	      public void changed(ObservableValue<? extends Tab> observable,
	          Tab oldSelectedTab, Tab newSelectedTab) {
	        if (newSelectedTab == newTab) {
	          createAndSelectNewTab(tabPane);
	        }
	      }
	    });
	    
	    contentPane.setCenter(anchor);
	    
	}
	
	private Tab createAndSelectNewTab(final TabPane tabPane) {
	    Tab tab = new Tab("Loading...");
	    final ObservableList<Tab> tabs = tabPane.getTabs();
	    tab.closableProperty().bind(Bindings.size(tabs).greaterThan(2));
	    tabs.add(tabs.size() - 1, tab);
	    tab.setContent(addBrowser(tab));
	    tabPane.getSelectionModel().select(tab);
	    return tab;
    }
	
	private Button minButton(String iconName) {
        Button button = new Button();
        button.getStyleClass().add("util-buttons-tray-max");
        button.getStyleClass().add("util-buttons");
        Image buttonImage = new Image(getClass().getResourceAsStream("/assets/nightshade_tray.png"));
		button.setGraphic(new ImageView(buttonImage));
        button.setMinWidth(Region.USE_PREF_SIZE);
        
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            	Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
            	stage.setIconified(true);
            }
        });
        
        return button;
    }
	
	private Button maxButton(String iconName) {
        Button button = new Button();
        button.getStyleClass().add("util-buttons-tray-max");
        button.getStyleClass().add("util-buttons");
        Image buttonImage = new Image(getClass().getResourceAsStream("/assets/nightshade_max_min.png"));
		button.setGraphic(new ImageView(buttonImage));
        button.setMinWidth(Region.USE_PREF_SIZE);
        
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            	if(stageMaxed){
            		stageMaxed = false;
            		primarystage.setMaximized(false);
            	} else{
            		stageMaxed = true;
            		primarystage.setMaximized(true);
            	}
            }
        });
        
        return button;
    }
	
	private Button exitButton(String iconName) {
        Button button = new Button();
        button.getStyleClass().add("util-buttons-close");
        button.getStyleClass().add("util-buttons");
        Image buttonImage = new Image(getClass().getResourceAsStream("/assets/nightshade_close.png"));
		button.setGraphic(new ImageView(buttonImage));
        button.setMinWidth(Region.USE_PREF_SIZE);
        
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            	Platform.exit();
            }
        });
        
        return button;
    }
	
	private void makedraggable(TabPane tabpane){
		
		tabpane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                xOffset = primarystage.getX() - event.getScreenX();
                yOffset = primarystage.getY() - event.getScreenY();
            }
        });
		
		tabpane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	if(!stageMaxed){
            		
            		// position x
            		// if mouse position is not less or more than min/max
            		// and does not pass max screen width - 10
            		if(
	    				!(event.getScreenX() <= primaryScreenBounds.getMinX()) && 
	    				!(event.getScreenX() >= primaryScreenBounds.getMaxX()) &&
	    				!(event.getScreenX() > primaryScreenBounds.getWidth()-10)
    				){
            			primarystage.setX(event.getScreenX() + xOffset);
            		}
            		
            		// position y
            		// if mouse position is not less or more than min/max
            		// and does not pass max screen height - 10
            		if(
        				!(event.getScreenY() <= primaryScreenBounds.getMinY()) && 
	    				!(event.getScreenY() >= primaryScreenBounds.getMaxY()) &&
	    				!(event.getScreenY() >= primaryScreenBounds.getHeight()-10)
    				){
            			primarystage.setY(event.getScreenY() + yOffset);
            		}
            	}
            }
        });
		
		tabpane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	
            	// x position
            	// if mouse position+5 is more or equal to max screen bounds
            	// or if mouse position-5 is less or equal to min screen bounds
            	// make stage maximized
            	if((event.getScreenX()+10) >= primaryScreenBounds.getMaxX() || (event.getScreenX()-10) <= primaryScreenBounds.getMinX()){
            		stageMaxed = true;
            		primarystage.setMaximized(true);
            	}
            	
            	// y position
            	// if mouse position+5 is more or equal to max screen bounds
            	// or if mouse position-5 is less or equal to min screen bounds
            	// make stage maximized
            	if((event.getScreenY()+10) >= primaryScreenBounds.getMaxY() || (event.getScreenY()-10) <= primaryScreenBounds.getMinY()){
            		stageMaxed = true;
            		primarystage.setMaximized(true);
            	}
            }
        });
		
	}
	
	
}