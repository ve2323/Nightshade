package nightshade;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
 
public class Shade extends Application {

    @Override
    public void start(Stage primarystage) {
    	
        primarystage.setTitle("Nightshade");
        primarystage.getIcons().add(new Image("/assets/nightshade_stage.png"));
        primarystage.setMaximized(true);
        primarystage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(new Group());
        scene.getStylesheets().add("/assets/shade.css");
        
        // main container
        BorderPane contentPane = new BorderPane();
        scene.setRoot(contentPane);
        
        // create browser
        Browser browser = new Browser(primarystage, contentPane);
        browser.initBrowser();
        
        // set scene and show
        primarystage.setScene(scene);
        primarystage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}