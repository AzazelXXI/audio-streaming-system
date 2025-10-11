package clienthandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private String dirPath = "server_storage";

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
            // client send commands string and server need to read it for trigger
            String command = dis.readUTF(); // command will be assign from the first read from client

            if ("UPLOAD".equals(command)) {
                uploadTrigger(dis, dos);
            }

            if ("LIST".equals(command)) {
                listTrigger(dis, dos);
            }

            if ("STREAMING".equals(command)) {
                streamingTrigger(dis, dos);
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * When client send download function it will trigger this function
     */
    private void streamingTrigger(DataInputStream dis, DataOutputStream dos) {
        try {
            String fileName = dis.readUTF();
            File file = new File(dirPath, fileName);

            if (!file.exists() || !file.isFile()) {
                dos.writeLong(-1L); // signal: not found
                dos.flush();
                return;
            }

            long fileSize = file.length();

            // Send file size first
            dos.writeLong(fileSize);
            dos.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, read); // optional: dos.flush(); not necessary every chunk; flush at the end
                }
                dos.flush();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * This method use to call the list song
     */
    private void listTrigger(DataInputStream dis, DataOutputStream dos) {
        try {
            // String dirPath = "server_storage";

            // Check folder if it existing
            if (!isDirExist(dirPath)) {
                return;
            }

            File dir = new File(dirPath);
            String[] names = (dir.isDirectory() && dir.exists()) ? dir.list() : new String[0];

            if (names == null) {
                names = new String[0];
            }

            // send cound then names
            dos.writeInt(names.length);
            for (String name : names) {
                dos.writeUTF(name);
            }
            dos.flush();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * This method use to create server_storage directory
     */
    private boolean isDirExist(String path) {
        File dir = new File(path);

        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }

    /**
     * When client call upload it will trigger this method
     */
    private void uploadTrigger(DataInputStream dis, DataOutputStream dos) {
        try {
            String fileName = dis.readUTF(); // Because the second read is the fileName so call this again for file
            // name
            long fileSize = dis.readLong(); // read file size
            // String dirPath = "server_storage";

            // Check folder if it existing
            if (!isDirExist(dirPath)) {
                return;
            }

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
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
