package ru.geekbrains.june.chat.client;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {
    @FXML
    TextArea chatArea;

    @FXML
    TextField messageField, usernameField, nicknameField;

    @FXML
    HBox authPanel, msgPanel, nicknamePanel;

    @FXML
    ListView<String> clientsListView;



    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public void setAuthorized(boolean authorized) {
        msgPanel.setVisible(authorized);
        msgPanel.setManaged(authorized);
        authPanel.setVisible(!authorized);
        authPanel.setManaged(!authorized);
        nicknamePanel.setVisible(authorized);
        nicknamePanel.setManaged(authorized);
        clientsListView.setVisible(authorized);
        clientsListView.setManaged(authorized);
    }

    public void sendMessage() {
        try {
            out.writeUTF(messageField.getText());
            messageField.clear();
            messageField.setPromptText("Вы используете имя: " + name);
            messageField.requestFocus();
        } catch (IOException e) {
            showError("Невозможно отправить сообщение на сервер");
        }
    }

    public void sendCloseRequest() {
        try {
            if (out != null) {
                out.writeUTF("/exit");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {
        connect();
        try {
            out.writeUTF("/auth " + usernameField.getText());
            name = usernameField.getText();
            messageField.setPromptText("Вы используете имя: " + name);
            nicknameField.setText("Вы используете имя: " + name);
            usernameField.clear();
        } catch (IOException e) {
            showError("Невозможно отправить запрос авторизации на сервер");
        }
    }

    public void connect() {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> logic()).start();
        } catch (IOException e) {
            showError("Невозможно подключиться к серверу");
        }
    }

    private void logic() {
        try {
            while (true) {
                String inputMessage = in.readUTF();
                if (inputMessage.equals("/exit")) { //Команда для выхода из чата
                    closeConnection();
                }
                if (inputMessage.equals("/authok")) {
                    setAuthorized(true);
                    break;
                }
                chatArea.appendText(inputMessage + "\n");
            }
            while (true) {
                String inputMessage = in.readUTF();
                if (inputMessage.startsWith("/")) {
                    if (inputMessage.equals("/exit")) {
                        break;
                    }
                    // /clients_list bob john
                    if (inputMessage.startsWith("/clients_list ")) {
                        Platform.runLater(() -> {
                            String[] tokens = inputMessage.split("\\s+");
                            clientsListView.getItems().clear();
                            for (int i = 1; i < tokens.length; i++) {
                                clientsListView.getItems().add(tokens[i]);
                            }
                        });
                    }
                    continue;
                }
                chatArea.appendText(inputMessage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        setAuthorized(false);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }


    public void clientsListDoubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String selectedUser = clientsListView.getSelectionModel().getSelectedItem();
            messageField.setText("/w " + selectedUser + " ");
            messageField.requestFocus();
            messageField.selectEnd();
        }
    }
}
