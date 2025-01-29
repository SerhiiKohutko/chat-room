package Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClientController {
    @FXML
    private Button sendButton;
    @FXML
    private TextField textField;
    @FXML
    private TextArea textArea;

    private Client client;


    public void initialize() {

        boolean connected = false;

        while (!connected) {
            String[] result = ClientDialogProvider.showInputDialog();
            if (result == null) {
                return;
            }
            client = new Client();
            try {
                client.start(result[0], Integer.parseInt(result[1]), result[2], this);
                connected = true;
            } catch (Exception e) {
                handleConnectionError(e);
            }
        }

        sendButtonConfiguration();

        textField.setOnAction(event -> sendMessage());
    }
    @FXML
    public void sendMessage() {
        String message = textField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        textArea.appendText(message + "\n");
        textField.clear();
        client.sendMessage(message);
    }

    public void writeMessage(String message) {
        Platform.runLater(() -> textArea.appendText(message + "\n"));
    }

    private void sendButtonConfiguration(){
        sendButton.setDisable(true);
        textField.textProperty().addListener((observable, oldValue, newValue) -> sendButton.setDisable(newValue.trim().isEmpty()));

    }
    private void handleConnectionError(Exception e) {
        switch (e.getMessage()) {
            case "USERNAME_NULL" -> ClientDialogProvider.showErrorDialog("Error", "This username is null.");
            case "USERNAME_TAKEN" -> ClientDialogProvider.showErrorDialog("Error", "This username is already taken. Please choose another one.");
            case "Connection refused" -> ClientDialogProvider.showErrorDialog("Error", "Connection refused.");
            case "USERNAME_RESERVED" -> ClientDialogProvider.showErrorDialog("Error", "Server.Server reserved name use.");
            default -> ClientDialogProvider.showErrorDialog("Error", e.getMessage());
        }
    }



}
