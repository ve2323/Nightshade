package nightshade;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Browser{
	
	// native class variables
	
	// cross class variables
	private BorderPane contentPane;
	private Scene scene;
	
	public Browser(Scene scene, BorderPane contentPane){
		this.contentPane = contentPane;
		this.scene = scene;
	}
	
	// create and add browser view with all required components
	public BorderPane addBrowser(Tab tab){
		
		// create new webEngine
		WebView browser = new WebView();
		WebEngine webEngine = browser.getEngine();
		TextField addressField = new TextField();
		BorderPane browserView = new BorderPane();
		
		browse(webEngine, "https://www.google.com/");
		
		// create search bar
		addSearchbar(webEngine, browserView, addressField, tab);
		
		// set browser content
		browserView.setCenter(browser);
		
		// create console
        Console console = new Console(scene,browserView);
        console.addConsole();
		
        // create firebug
		Firebug bug = new Firebug(webEngine, browserView);
		bug.addFirebug();
		
		return browserView;
		
	}
	
	// create and add searchbar
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
	    
	    contentPane.setCenter(tabPane);
	    
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
	
}