package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ServerGui extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server_start.fxml"));

        AnchorPane root = loader.load();

        ServerController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        Scene scene =new Scene(root);
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            controller.stopServer();
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
