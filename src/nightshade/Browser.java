package nightshade;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
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
	private WebView browser = new WebView();
	private WebEngine webEngine = browser.getEngine();
	private TextField addressField = new TextField();
	
	// cross class variables
	private BorderPane browserView;
	private BorderPane contentPane;
	
	public Browser(BorderPane browserView, BorderPane contentPane){
		this.browserView = browserView;
		this.contentPane = contentPane;
	}
	
	// create and add browser view with all required components
	public void addBrowser(){
		
		addSearchbar();
		browserView.setCenter(browser);
		contentPane.setCenter(browserView);
		
		Firebug bug = new Firebug(webEngine);
		bug.addFirebug();
		
		browse("https://www.google.com/");
	}
	
	// create and add searchbar
	private void addSearchbar(){
		
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
		
		// append url to addressfield on trigger
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
    		@SuppressWarnings("rawtypes")
    		@Override public void changed(ObservableValue ov, State oldState, State newState) {
              if (newState == Worker.State.SUCCEEDED) {
                addressField.clear();
                addressField.appendText(webEngine.getLocation());
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
                	browse(addressField.getText());
                }
            }
        };

        addressField.setOnKeyPressed(keyEventHandler);
		
		searchContainer.getChildren().addAll(goBack,reload,goForward,addressField);
		
		browserView.setTop(searchContainer);
	}
	
	// trigger browse
	public void browse(String address){
        webEngine.load(address);
	}
	
}