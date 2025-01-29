package Server;

import DBTesting.HibernateUtility;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class ServerController {
    @FXML
    private TextArea textAreaServer;
    @FXML
    private Button stopButtonServer;
    @FXML
    private TextField serverTextInput;
    @FXML
    private Button showConnectedClientsButton;
    @FXML
    private Button bannedNamesButton;
    @FXML
    private  Button bannedIpsButton;

    private final ObservableList<String> clientList = FXCollections.observableArrayList();
    private final ObservableList<String> bannedNamesList = FXCollections.observableArrayList();
    private final ObservableList<String> bannedIpsList = FXCollections.observableArrayList();
    private ServerStarter serverStarter;
    private Stage connectedClientsStage;
    private Stage bannedNamesStage;
    private Stage bannedIpsStage;
    private Stage primaryStage;

    public void initialize() {

        String[] result = ServerDialogProvider.showServerInputDialog();
        if (result == null) {
            return;
        }
        int port = Integer.parseInt(result[0]);

        try {
            serverStarter = new ServerStarter(port, this);
            serverStarter.startServer();
        }catch (Exception e) {
            ServerDialogProvider.showErrorDialog("Error occurred: ", e.getMessage());
            HibernateUtility.shutdown();
            Platform.exit();
        }


        serverTextInput.setOnAction(event -> sendServerMessage());
        showConnectedClientsButton.setOnAction(event -> showConnectedClients());
        bannedNamesButton.setOnAction(event -> showBannedUsernames());
        bannedIpsButton.setOnAction(event -> showBannedIps());
    }


    private void showBannedUsernames(){

        if (bannedNamesStage != null && bannedNamesStage.isShowing()) {
            bannedNamesStage.toFront();
            return;
        }

        bannedNamesStage = new Stage();
        bannedNamesStage.setTitle("Banned Usernames");

        ListView<String> bannedNamesListView = new ListView<>(bannedNamesList);
        bannedNamesListView.setCellFactory(param -> new ListCell<>() {
            private final Button unbanButton = new Button("Unban");
            private final HBox content = new HBox();
            private final Label nameLabel = new Label();

            {
                content.setSpacing(10);
                content.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(nameLabel, unbanButton);

                unbanButton.setOnAction(event -> {
                    String clientInfo = getItem();
                    if (clientInfo != null) {
                        unbanClient(clientInfo, false);
                    }
                });

            }
            @Override
            protected void updateItem(String clientInfo, boolean empty) {
                super.updateItem(clientInfo, empty);
                if (empty || clientInfo == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLabel.setText(clientInfo);
                    setGraphic(content);
                }
            }
        });

        VBox vbox = new VBox(bannedNamesListView);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 300, 400);
        bannedNamesStage.setScene(scene);
        bannedNamesStage.show();


    }
    private void showBannedIps(){
        if (bannedIpsStage != null && bannedIpsStage.isShowing()) {
            bannedIpsStage.toFront();
            return;
        }

        bannedIpsStage = new Stage();
        bannedIpsStage.setTitle("Banned Ips");

        ListView<String> bannedIpsListView = new ListView<>(bannedIpsList);
        bannedIpsListView.setCellFactory(param -> new ListCell<>() {
            private final Button unbanButton = new Button("Unban");
            private final HBox content = new HBox();
            private final Label nameLabel = new Label();

            {
                content.setSpacing(10);
                content.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(nameLabel, unbanButton);

                unbanButton.setOnAction(event -> {
                    String clientInfo = getItem();
                    if (clientInfo != null) {
                        unbanClient(clientInfo, true);
                    }
                });

            }
            @Override
            protected void updateItem(String clientInfo, boolean empty) {
                super.updateItem(clientInfo, empty);
                if (empty || clientInfo == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLabel.setText(clientInfo);
                    setGraphic(content);
                }
            }
        });

        VBox vbox = new VBox(bannedIpsListView);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 300, 400);
        bannedIpsStage.setScene(scene);
        bannedIpsStage.show();

    }
    private void showConnectedClients() {
        if (connectedClientsStage != null && connectedClientsStage.isShowing()) {
            connectedClientsStage.toFront();
            return;
        }

        connectedClientsStage = new Stage();
        connectedClientsStage.setTitle("Connected Clients");

        ListView<String> clientsListView = new ListView<>(clientList);
        clientsListView.setCellFactory(param -> new ListCell<>() {
            private final Button kickButton = new Button("Kick");
            private final Button banNameButton = new Button("Ban by name");
            private final Button banIpButton = new Button("Ban by ip");
            private final HBox content = new HBox();
            private final Label nameLabel = new Label();

            {
                // Настройка интерфейса элемента
                content.setSpacing(10);
                content.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(nameLabel, kickButton, banNameButton, banIpButton);

                // Обработчик для кнопки "Kick"
                kickButton.setOnAction(event -> {
                    String clientInfo = getItem();
                    if (clientInfo != null) {
                        kickClient(clientInfo.split(":")[0].trim());
                    }
                });

                // Обработчик для кнопки "Ban"
                banNameButton.setOnAction(event -> {
                    String clientInfo = getItem();
                    if (clientInfo != null) {
                        banClientByName(clientInfo.split(":")[0].trim());
                    }
                });

                // Обработчик для кнопки "Ban"
                banIpButton.setOnAction(event -> {
                    String clientInfo = getItem();
                    if (clientInfo != null) {
                        banClientByIp(clientInfo.split(":")[0].trim());
                    }
                });
            }

            @Override
            protected void updateItem(String clientInfo, boolean empty) {
                super.updateItem(clientInfo, empty);
                if (empty || clientInfo == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLabel.setText(clientInfo);
                    setGraphic(content);
                }
            }
        });

        VBox vBox = new VBox(clientsListView);
        vBox.setPadding(new Insets(10));

        Scene scene = new Scene(vBox, 300, 400);
        connectedClientsStage.setScene(scene);
        connectedClientsStage.show();
    }
    public void writeMessage(String message) {
        Platform.runLater(() -> textAreaServer.appendText(message+"\n\n"));
    }
    private void sendServerMessage() {
        String message = serverTextInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        serverStarter.sendServerMessage(message);
        serverTextInput.clear();
    }

    public void stopServer(){
        serverStarter.stopServer();
        primaryStage.hide();
        initialize();
        primaryStage.show();
    }

    private void kickClient(String client) {
        serverStarter.kickClientFromServer(client);
    }
    private void banClientByName(String client) {
        serverStarter.banClientFromServer(client, false);
    }
    private void banClientByIp(String username) {
        serverStarter.banClientFromServer(username, true);
    }

    private void unbanClient(String client, boolean byIp) {
        serverStarter.unbanClientFromServer(client, byIp);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void updateClientsList(Map<String, Server.ClientHandler> clients) {
        Platform.runLater(() -> {
            clientList.clear();
            for (String key : clients.keySet()) {
                clientList.add(key + " : " + clients.get(key).getConnection().getInetAddress().getHostAddress());
            }
        });
    }

    public void updateBannedList(List<String> bannedClients, boolean byIp){
        if (byIp) {
            updateBannedIps(bannedClients);
        }else{
            updateBannedNames(bannedClients);
        }
    }

    private void updateBannedIps(List<String> ips){
        Platform.runLater(() -> {
            bannedIpsList.clear();
            bannedIpsList.addAll(ips);
        });
    }
    private void updateBannedNames(List<String> names){
        Platform.runLater(() -> {
            bannedNamesList.clear();
            bannedNamesList.addAll(names);
        });
    }
}
