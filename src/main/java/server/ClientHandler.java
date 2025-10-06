package server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            File dir = new File("server_storage");

            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String upload = in.readUTF();
            
            if ("UPLOAD".equals(upload)) {
                String fileName = in.readUTF();
                long fileSize = in.readLong();

                try (FileOutputStream out = new FileOutputStream("server_storage/" + fileName);) {
                    byte[] buffer = new byte[4096];
                    long remaining = fileSize;
                    int read;

                    while (remaining > 0 && (read = in.read(buffer, 0 , (int)Math.min(buffer.length, remaining))) > 0) {
                        out.write(buffer, 0, read);
                        remaining -= read;
                    }

                    out.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }
}
