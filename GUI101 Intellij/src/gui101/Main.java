package gui101;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // assign the fxml file path into an FXMLLoader variable
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));

        // after loading the fxml file into memory, assign it into a Parent variable
        Parent root = loader.load();

        // set the stage title
        primaryStage.setTitle("Hello World");

        // set the stage scene with a root loaded by the Parent variable
        primaryStage.setScene(new Scene(root));

        // initialize the style of the stage window with an undecorated one (one without a toolbar)
        primaryStage.initStyle(StageStyle.UNDECORATED);

        // show the stage window
        primaryStage.show();

        // set the stage for the FXML controller
        loader.<Controller>getController().setStage(primaryStage);
    }


    public static void main(String[] args) {
        launch(args); // start the program
    }
}
