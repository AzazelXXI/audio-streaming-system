package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());) {
            File dir = new File("server_storage");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String command = in.readUTF();

            if ("UPLOAD".equals(command)) {
                String fileName = in.readUTF();
                long fileSize = in.readLong();

                try (FileOutputStream fileOut = new FileOutputStream("server_storage/" + fileName);) {
                    byte[] buffer = new byte[4096];
                    long remaining = fileSize;
                    int read;

                    while (remaining > 0 && (read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                        fileOut.write(buffer, 0, read);
                        remaining -= read;
                    }

                    fileOut.flush();
                    // System.out.println("");
                }
            } else if ("LIST".equals(command)) {
                String[] files = dir.list();
                if (files == null)
                    files = new String[0];
                out.writeInt(files.length);
                for (String file : files) {
                    out.writeUTF(file);
                }
                
            }
        } catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }
}
