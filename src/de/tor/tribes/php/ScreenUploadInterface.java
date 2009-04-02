/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.php;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Charon
 */
public class ScreenUploadInterface {

    public final static int ID_FILE_TOO_LARGE = -30;
    public final static int ID_NO_FILE_SET = -31;
    public final static int ID_FILE_ALREADY_EXIST = -32;
    public final static int ID_COPY_FAILED = -33;
    public final static int ID_WRONG_TYPE = -34;

    public static void upload(String pLocalFile) {
        try {
            String hostname = "dsworkbench.de";
            int port = 80;
            InetAddress addr = InetAddress.getByName(hostname);
            Socket socket = new Socket(addr, port);

            //@TODO (1.3) Get owner from globaloptions
            String owner = "Torridity";
            // Send header
            String path = "/upload_interface.php";

            // File To Upload
            File theFile = new File(pLocalFile);
            String extension = theFile.getName().substring(theFile.getName().lastIndexOf("."));
            DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(theFile)));
            byte[] theData = new byte[(int) theFile.length()];

            fis.readFully(theData);
            fis.close();

            DataOutputStream raw = new DataOutputStream(socket.getOutputStream());
            Writer wr = new OutputStreamWriter(raw);
            String command = "--sep\r\n" + "Content-Disposition: form-data; name=\"user\";\r\n\r\n" + owner + "\r\n";
            command += "--sep\r\n" + "Content-Disposition: form-data; name=\"type\";\r\n\r\n" + extension + "\r\n";
            command += "--sep\r\n" + "Content-Disposition: form-data; name=\"userfile\"; filename=\"" + theFile.getName() + "\"\r\n" + "Content-Type: image/jpg\r\n" + "\r\n";

            String trail = "\r\n--sep--\r\n";
            String header = "POST " + path + " HTTP/1.0\r\n" + "Accept: */*\r\n" + "Referer: http://localhost\r\n" + "Accept-Language: de\r\n" + "Content-Type: multipart/form-data; boundary=sep\r\n" + "User_Agent: DSWorkbench\r\n" + "Host: dsworkbench.de\r\n" + "Content-Length: " + ((int) theFile.length() + command.length() + trail.length()) + "\r\n" + "Connection: Close\r\n" + "Pragma: no-cache\r\n" + "\r\n";

            wr.write(header);
            wr.write(command);

            wr.flush();
            raw.write(theData);
            raw.flush();
            wr.write("\r\n--sep--\r\n");
            wr.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            String lastLine = "";
            boolean error = false;
            while ((line = rd.readLine()) != null) {
                try {
                    int returnCode = Integer.parseInt(line);
                    System.out.println("CODE!? " + returnCode);
                    error = true;
                } catch (Exception e) {
                    //no int
                    if (line != null) {
                        lastLine = line;
                    }
                }
            }
            if (!error) {
                System.out.println("PATH: " + lastLine);
            }
            wr.close();
            raw.close();


            socket.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        ScreenUploadInterface.upload("c:/Banner.jpg");
    }
}
