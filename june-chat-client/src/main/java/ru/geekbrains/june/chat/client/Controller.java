package ru.geekbrains.june.chat.client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    TextField messageField, usernameField;

    @FXML
    HBox authPanel, msgPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void sendMessage() {
        try {
            out.writeUTF(messageField.getText());
            messageField.clear();
            messageField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {
        connect();
        try {
            out.writeUTF("/auth " + usernameField.getText());
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
            socket = new Socket("localhost", 8289);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String inputMessage = in.readUTF();
                        if (inputMessage.equals("/authok")) {
                            msgPanel.setVisible(true);
                            msgPanel.setManaged(true);
                            authPanel.setVisible(false);
                            authPanel.setManaged(false);
                            break;
                        }
                        chatArea.appendText(inputMessage + "\n");
                    }
                    while (true) {
                        String inputMessage = in.readUTF();
                        chatArea.appendText(inputMessage + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();
        } catch (IOException e) {
            showError("Невозможно подключиться к серверу");
        }
    }

    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
