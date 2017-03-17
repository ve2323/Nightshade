package nightshade;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Browser{
	
	// native class variables
	private WebView browser = new WebView();
	private WebEngine webEngine = browser.getEngine();
	
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
		
		browse("http://www.google.com/");
	}
	
	// create and add searchbar
	private void addSearchbar(){
		
		HBox searchContainer = new HBox();
		
		Button goBack = new Button("Back");
		Button goForward = new Button("Forward");
		Button reload = new Button("reload");
		TextField addressField = new TextField();
		
		searchContainer.getChildren().addAll(goBack,reload,goForward,addressField);
		
		browserView.setTop(searchContainer);
	}
	
	// trigger browse
	public void browse(String address){
        webEngine.load(address);
	}
	
}