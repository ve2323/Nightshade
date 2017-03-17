package nightshade;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
 
public class Shade extends Application {

    @Override
    public void start(Stage primarystage) {
    	
        primarystage.setTitle("Shade");
        primarystage.setMaximized(true);
        Scene scene = new Scene(new Group());
        
        // main container
        BorderPane contentPane = new BorderPane();
        
        // browser container
        BorderPane browserView = new BorderPane();
        
        scene.setRoot(contentPane);
        
        // create browser
        Browser browser = new Browser(browserView,contentPane);
        browser.addBrowser();
        
        // create console
        Console console = new Console(scene,contentPane);
        console.addConsole();
        
        // set scene and show
        primarystage.setScene(scene);
        primarystage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}