package clienthandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
            // client send commands string and server need to read it for trigger
            String command = dis.readUTF();
            if ("UPLOAD".equals(command)) { // command will be assign from the first read from client
                String fileName = dis.readUTF(); // Because the second read is the fileName so call this again for file
                                                 // name
                long fileSize = dis.readLong(); // read file size
                String dirPath = "server_storage";

                // Check folder if it existing
                isDirExist(dirPath);

                try (FileOutputStream fos = new FileOutputStream(dirPath + "/" + fileName)) {

                    byte[] buffer = new byte[4096];
                    long remaining = fileSize;

                    while (remaining > 0) {
                        int toRead = (int) Math.min(buffer.length, remaining);
                        int read = dis.read(buffer, 0, toRead);

                        if (read == -1) { // read until EOF
                            break;
                        }
                        fos.write(buffer, 0, read);
                        remaining -= read;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                dos.writeUTF("TRUE");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /*
     * This method use to create server_storage directory
     */
    private void isDirExist(String path) {
        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
