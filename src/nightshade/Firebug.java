package nightshade;

import org.w3c.dom.Document;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;

public class Firebug {
	
	private WebEngine engine;
	private BorderPane browserView;
	private boolean enabled = false;
	
	public Firebug(WebEngine engine, BorderPane browserView){
		this.engine = engine;
		this.browserView = browserView;
	}
	
	public void addFirebug(){
		engine.documentProperty().addListener(new ChangeListener<Document>() {
			@Override
			public void changed(ObservableValue<? extends Document> prop,Document oldDoc, Document newDoc) {
				if(!enabled){
					// ctrl+shift+F
					KeyCombination consoleCombo = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
					browserView.setOnKeyPressed(new EventHandler<KeyEvent>(){
				        @Override
				        public void handle(KeyEvent event){
					        if (consoleCombo.match(event)){
					        	enabled = true;
					        	enableFirebug(engine);
					        }
				        }
				    });
				}
			}
		});
		
	}
	
	private static void enableFirebug(final WebEngine engine) {
		engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 
	}
	
}