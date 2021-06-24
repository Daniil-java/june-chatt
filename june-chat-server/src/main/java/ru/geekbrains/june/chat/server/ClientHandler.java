package ru.geekbrains.june.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private String username;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            //Запоминаем сервер
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            //Поток общения с клиентом
            new Thread(() -> logic()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) { //Отправка сообщения
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logic() { //Отключение пользователя от сервера
        try {
            while (!consumeAuthorizeMessage(in.readUTF()));
            while (consumeRegularMessage(in.readUTF()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Клиент " + username + " отключился");
            server.unsubscribe(this);
            closeConnection();
        }
    }

    private boolean consumeRegularMessage(String inputMessage) {
        if (inputMessage.startsWith("/")) {
            if (inputMessage.equals("/exit")) {
                sendMessage("/exit");
                return false;
            }
            if (inputMessage.startsWith("/w ")) {
                String[] tokens = inputMessage.split("\\s+", 3);
                server.sendPersonalMessage(this, tokens[1], tokens[2]);
            }
            return true;
        }
        server.broadcastMessage(username + ": " + inputMessage);
        return true;
    }

    private boolean consumeAuthorizeMessage(String message) { //Узнаём имя пользователя
        if (message.startsWith("/auth ")) { // /auth bob
            String[] tokens = message.split("\\s+");
            if (tokens.length == 1) {
                sendMessage("SERVER: Вы не указали имя пользователя");
                return false;
            }
            if (tokens.length > 2) {
                sendMessage("SERVER: Имя пользователя не может состоять из нескольких слов");
                return false;
            }
            String selectedUsername = tokens[1];
            if (server.isUsernameUsed(selectedUsername)) {
                sendMessage("SERVER: Данное имя пользователя уже занято");
                return false;
            }
            username = selectedUsername;
            sendMessage("/authok");
            server.subscribe(this);
            return true;
        } else {
            sendMessage("SERVER: Вам необходимо авторизоваться");
            return false;
        }
    }

    private void closeConnection() { //Закрываем соединение с сервером
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
}
