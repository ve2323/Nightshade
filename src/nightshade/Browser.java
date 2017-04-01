package nightshade;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	String location;
	Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
	
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
		TextField statusField = new TextField();
		BorderPane browserView = new BorderPane();
		SplitPane browserContainer = new SplitPane();
		browserContainer.setId("browser-container");
		
		browse(webEngine, "https://www.google.com/");
		
		// create search bar
		addSearchbar(webEngine, browserView, addressField, statusField, tab);
		
		browserContainer.getItems().add(browser);
		
		// set browser content
		browserView.setCenter(browserContainer);
		
		// create console
        Console console = new Console(browserContainer, webEngine);
        console.addConsole();
		
        // create fire bug
		Firebug bug = new Firebug(webEngine, browserView);
		bug.addFirebug();
		
		return browserView;
		
	}
	
	// create and add search bar
	private void addSearchbar(WebEngine webEngine, BorderPane browserView, TextField addressField, TextField statusField, Tab tab){
		
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
		
		statusField.setId("status-field");
		statusField.setEditable(false);
		
		// view load progress
		webEngine.getLoadWorker().progressProperty().addListener((observable, oldValue, newValue) -> {
		    if(newValue.doubleValue() > 0.0){
		    	String progress = String.format("%.2f", newValue.doubleValue()).replace(",", "");
		    	if(progress.indexOf("0")==0){
		    		tab.setText( progress.substring(1) + " %");
		    	} else{
		    		tab.setText( progress + " %");
		    	}
		    }
		});
		
		// append url to address field on trigger
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
    		@SuppressWarnings("rawtypes")
    		@Override public void changed(ObservableValue ov, State oldState, State newState) {
              if (newState == Worker.State.SUCCEEDED) {
            		  tab.setText(webEngine.getTitle());
                      addressField.clear();
                      statusField.clear();
                      location = webEngine.getLocation();
                      addressField.appendText(location);
                      statusField.appendText(getIp(location));
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
		
		searchContainer.getChildren().addAll(goBack,reload,goForward,addressField,statusField);
		
		browserView.setTop(searchContainer);
	}
	
	// trigger browse
	public void browse(WebEngine webEngine, String address){
		if(address.contains(".")){
			webEngine.load(address);
		} else{
			webEngine.load("https://www.google.se/search?q=" + address.replace(" ", "%20") + "&*");
		}
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
	    Tab tab = new Tab("0 %");
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
            		primarystage.setWidth(700);
            		primarystage.setHeight(600);
            		primarystage.setX((primaryScreenBounds.getWidth()/2)-(primarystage.getWidth()/2));
            		primarystage.setY((primaryScreenBounds.getHeight()/2)-(primarystage.getHeight()/2));
            	} else{
            		stageMaxed = true;
            		primarystage.setWidth(primaryScreenBounds.getWidth());
            		primarystage.setHeight(primaryScreenBounds.getHeight());
            		primarystage.setX(0);
            		primarystage.setY(0);
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
            	System.exit(0);
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
            	if((event.getScreenX()-10) <= primaryScreenBounds.getMinX()){
            		stageMaxed = true;
            		primarystage.setWidth(primaryScreenBounds.getWidth()/2);
            		primarystage.setHeight(primaryScreenBounds.getHeight());
            		primarystage.setX(0);
            		primarystage.setY(0);
            	} if((event.getScreenX()+10) >= primaryScreenBounds.getMaxX()){
            		stageMaxed = true;
            		primarystage.setWidth(primaryScreenBounds.getWidth()/2);
            		primarystage.setHeight(primaryScreenBounds.getHeight());
            		primarystage.setX(primaryScreenBounds.getMaxX()/2);
            		primarystage.setY(0);
            	}
            	
            	// y position
            	// if mouse position+5 is more or equal to max screen bounds
            	// or if mouse position-5 is less or equal to min screen bounds
            	// make stage maximized
            	if((event.getScreenY()+10) >= primaryScreenBounds.getMaxY() || (event.getScreenY()-10) <= primaryScreenBounds.getMinY()){
            		stageMaxed = true;
            		primarystage.setWidth(primaryScreenBounds.getWidth());
            		primarystage.setHeight(primaryScreenBounds.getHeight());
            		primarystage.setX(0);
            		primarystage.setY(0);
            	}
            }
        });
		
	}
	
	private String getIp(String radegast){
		
		String id1,id2,id3;
		int index1,index2,index3;
		boolean proceed = false;
		String host = null;
		String returnString = null;
		InetAddress inetAddress;
		
		if(radegast.startsWith("https://")){radegast = radegast.substring(8); proceed = true;}
		else if(radegast.startsWith("http://")){radegast = radegast.substring(7); proceed = true;}
		
		if(proceed){
			index1 = radegast.indexOf(".");
			id1 = radegast.substring(0, index1);
			radegast = radegast.replace(id1+".", "");
			index2 = radegast.indexOf(".");
			id2 = radegast.substring(0, index2);
			radegast = radegast.replace(id2+".", "");
			index3 = radegast.indexOf("/");
			id3 = radegast.substring(0, index3);
			
			host = id1+"."+id2+"."+id3;
			
			try {
				inetAddress = InetAddress.getByName(host);
				returnString = "IP: " + inetAddress.toString().split("/")[1];
			} catch (UnknownHostException e) {
				// do nothing
			}
			
		}
		
		if(returnString == null){returnString = "IP: unavailable";}
		return returnString;
	}
	
	
}