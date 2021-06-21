package ru.geekbrains.june.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<ClientHandler> clients;

    public Server() {
        try {
            this.clients = new ArrayList<>(); //Создаём список клиентов, которые находятся в чате
            ServerSocket serverSocket = new ServerSocket(8289);
            System.out.println("Сервер запущен. Ожидаем подключение клиентов..");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Подключился новый клиент");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler c) { //Добавляем клиента в чат
        clients.add(c);
    }

    public synchronized void unsubscribe(ClientHandler c) { //Удаляем клиента из чата
        clients.remove(c);


    }

    public synchronized void broadcastMessage(String message) { //Метод, для рассылки сообщения всем клиентам в чате
        for (ClientHandler c : clients) {
        }
    }
}
