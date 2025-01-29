package Server;

import Client.ClientDialogProvider;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class ServerDialogProvider {

    public static String[] showServerInputDialog(){

        while(true) {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle("Connecting to server");
            dialog.setHeaderText("Input data for connection");

            Label portLabel = new Label("Port:");
            TextField portTextField = new TextField();
            portTextField.setPromptText("8080");

            VBox vBox = new VBox(10, portLabel, portTextField);
            dialog.getDialogPane().setContent(vBox);

            ButtonType startButtonType = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);

            dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == startButtonType) {
                    return new String[]{portTextField.getText()};
                }
                return null;
            });

            Optional<String[]> result = dialog.showAndWait();

            if (result.isEmpty()) {
                Platform.exit();
                return null;
            }

            String[] data = result.get();
            try {
                Integer.parseInt(data[0]);
            } catch (NumberFormatException e) {
                showErrorDialog("Error", "Port should be an integer");
                continue;
            }

            return data;
        }

    }

    public static void showErrorDialog(String title, String message) {
        ClientDialogProvider.showErrorDialog(title, message);
    }
}
