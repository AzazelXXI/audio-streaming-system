package serverside;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import clienthandler.ClientHandler;

public class Server {
    public static void main(String[] args) {
        int serverPort = 1124;

        try (ServerSocket myServerSocket = new ServerSocket(serverPort);) {
            System.out.println("Server is starting on port " + serverPort);
            while (true) {
                // try {
                Socket mySocket = myServerSocket.accept(); // Installed the connection from the client

                new ClientHandler(mySocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
