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

    public ClientHandler(Server server, Socket socket) {
        try {
            //Запоминаем сервер
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            //Поток общения с клиентом
            new Thread(() -> {
                try {
                    while (true) {
                        String inputMessage = in.readUTF();
                        if (inputMessage.startsWith("/auth ")) { //Ждём от клинта ввода имени
                            username = inputMessage.split("\\s+", 2)[1];
                            sendMessage("/authok" + username); //Говорим клиенту, что он авторизовался
                            server.subscribe(this); //Добавляем клиента в чат, чтобы он мог получать рассылку сообщений
                            break;
                        } else {
                            sendMessage("SERVER: Вам необходимо авторизоваться");
                        }
                    }
                    while (true) { //Читаем и отправляем другим клиентам сообщения от клиента
                        String inputMessage = in.readUTF();
                        if (inputMessage.startsWith("/")) {
                            continue;
                        }
                        server.broadcastMessage(username + ": " + inputMessage);
                    }
                } catch (IOException e) { //Ловим исключение, на случай, если клиент выйдет из чата
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.broadcastMessage(username + " покинул чат.");
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) { //Метод отправляет сообщение данному пользователю
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
