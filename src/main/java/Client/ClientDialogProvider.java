package Client;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class ClientDialogProvider {


    public static String[] showInputDialog() {
        while (true) {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle("Connecting to server");
            dialog.setHeaderText("Input data for connection");

            Label hostLabel = new Label("Host:");
            TextField hostTextField = new TextField();
            hostTextField.setPromptText("localhost");

            Label portLabel = new Label("Port:");
            TextField portTextField = new TextField();
            portTextField.setPromptText("8080");

            Label usernameLabel = new Label("Username:");
            TextField usernameTextField = new TextField();
            usernameTextField.setPromptText("Your name:");

            VBox content = new VBox(10, hostLabel, hostTextField, portLabel, portTextField, usernameLabel, usernameTextField);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    return new String[]{hostTextField.getText(), portTextField.getText(), usernameTextField.getText()};
                }
                return null;
            });

            Optional<String[]> result = dialog.showAndWait();
            if (result.isEmpty()) {
                Platform.exit();
                return null;
            }

            String[] data = result.get();
            if (data[0].isEmpty() || data[1].isEmpty() || data[2].isEmpty()) {
                showErrorDialog("Error", "All fields should not be empty");
                continue;
            }

            try {
                Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
                showErrorDialog("Error", "Port should be an integer");
                continue;
            }

            return data;
        }
    }
    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
