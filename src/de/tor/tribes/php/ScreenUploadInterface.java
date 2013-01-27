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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class ScreenUploadInterface {

    private static Logger logger = Logger.getLogger("PSUAccess");
    public final static int ID_FILE_TOO_LARGE = -30;
    public final static int ID_NO_FILE_SET = -31;
    public final static int ID_FILE_ALREADY_EXIST = -32;
    public final static int ID_COPY_FAILED = -33;
    public final static int ID_WRONG_TYPE = -34;
    public final static int ID_SERVICE_NOT_AVAILABLE = -66;

    public static void main(String[] args) {
        System.out.println("Result: " + ScreenUploadInterface.upload("C:/Users/Torridity/AppData/Local/DSWorkbench/tmp.png"));
    }

    public static String upload(String pLocalFile) {
        String result = null;
        try {

            URL url = new URL("http://dsworkbench.de/upload_interface.php");
            //URLConnection con = url.openConnection(webProxy);
            URLConnection con = url.openConnection();//DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setDefaultUseCaches(false);

            String owner = "Torridity";//GlobalOptions.getProperty("account.name");
            if (owner == null) {
                return "Benutzername konnte nicht bestimmt werden. Bitte überprüfe deine Logineinstellungen.";
            }
            // Send header
            String path = "/upload_interface.php";

            // File To Upload
            File theFile = new File(pLocalFile);
            if (theFile.length() > 200000) {
                return "Die Datei ist zu groß.";
            }
            String extension = theFile.getName().substring(theFile.getName().lastIndexOf("."));
            DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(theFile)));
            //read the file into the array
            byte[] theData = new byte[(int) theFile.length()];
            fis.readFully(theData);
            fis.close();

            //build the form content
            String separator = Long.toString(System.currentTimeMillis());

            String command = "--" + separator + "\r\n" + "Content-Disposition: form-data; name=\"user\";\r\n\r\n" + owner + "\r\n";
            command += "--" + separator + "\r\n" + "Content-Disposition: form-data; name=\"type\";\r\n\r\n" + extension + "\r\n";
            command += "--" + separator + "\r\n" + "Content-Disposition: form-data; name=\"userfile\"; filename=\"" + theFile.getName()
                    + "\"\r\n" + "Content-Type: image/jpg\r\n" + "\r\n";
           // con.setRequestMethod("POST");
            con.setRequestProperty("Content-Length", String.valueOf(command.length()));
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + separator);
            DataOutputStream printout = new DataOutputStream(con.getOutputStream());

            System.out.println(command);
            printout.writeBytes(command);

            printout.flush();
            con.getOutputStream().write(theData);
            con.getOutputStream().flush();
            printout.writeBytes("\r\n--" + separator + "--\r\n");
            printout.flush();
            printout.close();

            //read the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                try {
                    int returnCode = Integer.parseInt(line);
                    switch (returnCode) {
                        case ID_FILE_TOO_LARGE: {
                            result = "Die Datei ist zu groß.";
                            break;
                        }
                        case ID_NO_FILE_SET: {
                            result = "Die Datei konnte nicht hochgeladen werden.\nBitte versuch es später noch einmal.";
                            break;
                        }
                        case ID_FILE_ALREADY_EXIST: {
                            result = "Interner Fehler.\nBitte versuch es in wenigen Sekunden noch einmal.";
                            break;
                        }
                        case ID_COPY_FAILED: {
                            result = "Die Datei konnte nicht kopiert werden.\nBitte kontaktiere den DS Workbench Support.";
                            break;
                        }
                        case DatabaseInterface.ID_DATABASE_CONNECTION_FAILED: {
                            result = "Datenbankverbindung fehlgeschlagen.";
                            break;
                        }
                        case DatabaseInterface.ID_USER_NOT_EXIST: {
                            result = "Dein Benutzername konnte nicht gefunden werden.";
                            break;
                        }
                        case DatabaseInterface.ID_QUERY_RETURNED_UNEXPECTED_RESULT: {
                            result = "Fehler beim Einfügen in die Datenbank.";
                            break;
                        }
                        case ID_SERVICE_NOT_AVAILABLE: {
                            result = "Der Dienst steht im Moment leider nicht zur Verfügung.";
                            break;
                        }
                        default: {
                            //unknown code
                            result = "Unbekannter Fehlercode.";
                            break;
                        }
                    }
                    break;
                } catch (Exception e) {
                    //no int
                    if (line != null) {
                        result = line;
                    }
                }
            }
            rd.close();
        } catch (Exception e) {
            logger.error("Internal error in PSU module", e);
            e.printStackTrace();
        }
        return result;
    }
}
