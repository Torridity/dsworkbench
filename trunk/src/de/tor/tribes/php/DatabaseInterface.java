/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.php;

import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.sec.SecurityAdapter;
import de.tor.tribes.ui.DSWorkbenchSettingsDialog;
import de.tor.tribes.util.Constants;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author Jejkal
 */
public class DatabaseInterface {

    private static Logger logger = Logger.getLogger(DatabaseInterface.class);
    //server specific codes
    public static final int ID_UNKNOWN_ERROR = -4711;
    public static final int ID_SUCCESS = 0;
    public static final int ID_WEB_CONNECTION_FAILED = -1;
    public static final int ID_DATABASE_CONNECTION_FAILED = -2;
    public static final int ID_ARGUMENT_ERROR = -4;
    public static final int ID_QUERY_RETURNED_UNEXPECTED_RESULT = -5;
    public static final int ID_UNKNOWN_FUNTION = -6;
    //function specific codes
    public static final int ID_UNKNOWN_SERVER = -11;
    public static final int ID_USER_ALREADY_EXIST = -13;
    public static final int ID_USER_NOT_EXIST = -14;
    public static final int ID_VERSION_NOT_ALLOWED = -26;
    public final static String INTERFACE_URL = "http://www.support.dsworkbench.de/interface.php";
    private static boolean bAccountValidated = false;
    private static String sValidatedUser = null;
    private static String sValidatedPassword = null;

    private static Object callWebInterface(String pFunction, Hashtable<String, String> pArguments) {
        List<String> lines = new LinkedList<String>();
        URL url;
        URLConnection urlConn = null;
        DataOutputStream printout = null;
        BufferedReader input = null;
        try {
            if (pArguments == null) {
                pArguments = new Hashtable<String, String>();
            }

            pArguments.put("function", pFunction);

            // URL of CGI-Bin script.
            url = new URL(INTERFACE_URL);
            // URL connection channel.
            urlConn = url.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
            // urlConn = url.openConnection();
            // Let the run-time system (RTS) know that we want input.
            urlConn.setDoInput(true);

            // Let the RTS know that we want to do output.
            urlConn.setDoOutput(true);
            // No caching, we want the real thing.
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            printout = new DataOutputStream(urlConn.getOutputStream());
            Enumeration<String> keys = pArguments.keys();
            String content = "";

            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String value = pArguments.get(key);
                content += key + "=" + URLEncoder.encode(value, "UTF-8");
                if (keys.hasMoreElements()) {
                    content += "&";
                }
            }
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            // Get response data.
            input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String str;
            while ((str = input.readLine()) != null) {
                lines.add(str);
            }
            input.close();
        } catch (Throwable t) {
            if (urlConn != null) {
                logger.error("Failed calling interface. HTTP Status " + urlConn.getHeaderField(0));
            } else {
                logger.error("Failed calling interface before connect", t);
            }
            return ID_WEB_CONNECTION_FAILED;
        } finally {
            if (printout != null) {
                try {
                    printout.close();
                } catch (Exception e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
        }
        //returning read lined
        return lines.toArray(new String[]{});
    }

    private static void processStatus(String pFunction, int pStatus) {
        switch (pStatus) {
            case ID_SUCCESS: {
                //do nothing
                break;
            }
            case ID_WEB_CONNECTION_FAILED: {
                logger.error("Failed to connect to the web interface");
                break;

            }
            case ID_DATABASE_CONNECTION_FAILED: {
                logger.error("Failed to select database");
                break;

            }
            case ID_ARGUMENT_ERROR: {
                logger.error("Argument error");
                break;

            }
            case ID_UNKNOWN_SERVER: {
                logger.error("Unknown server");
                break;

            }
            case ID_QUERY_RETURNED_UNEXPECTED_RESULT: {
                logger.error("Server-sided failure while '" + pFunction + "'. Query returned an unexpected/no result or an update affected no single line");
                break;

            }
            case ID_USER_NOT_EXIST: {
                logger.error("Error in '" + pFunction + "'. User does not exist or password is wrong");
                break;

            }
            case ID_USER_ALREADY_EXIST: {
                logger.error("Error in '" + pFunction + "'. User already exists");
                break;

            }
            default: {
                logger.error("Unknown status code in '" + pFunction + "': " + pStatus);
            }
        }
    }

    public static long getServerDataVersion(String pServer) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("sid", pServer);
        Object result = callWebInterface("getServerDataVersion", arguments);
        try {
            String[] lines = (String[]) result;
            long lResult = Long.parseLong(lines[0]);
            if (lResult < 0) {
                //status code
                processStatus("get server data version", (int) lResult);
            }

            return lResult;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed getting server data version. Result is " + result);
        }

        return ID_UNKNOWN_ERROR;
    }

    public static int isVersionAllowed() {
        double min = Double.parseDouble(DatabaseInterface.getProperty("min_version"));
        if (min < 0) {
            return (int) min;
        } else if (Constants.VERSION < min) {
            return ID_VERSION_NOT_ALLOWED;
        }
        return ID_SUCCESS;
    }

    public static List<DatabaseServerEntry> getServerInfo() {
        Object result = null;
        try {
            result = callWebInterface("getServerInfo", null);
            String[] lines = (String[]) result;
            List<DatabaseServerEntry> entries = new LinkedList<DatabaseServerEntry>();
            try {
                for (String line : lines) {
                    StringTokenizer t = new StringTokenizer(line, "[,]");

                    if (t.countTokens() < 3) {
                        //invalid entry, probably a status code!?
                        try {
                            int status = Integer.parseInt(line);
                            processStatus("get server info ", status);
                            return null;
                        } catch (Exception noStatus) {
                            logger.error("Server entry in invalid format. Dropping '" + line + "'");
                        }

                    } else {
                        DatabaseServerEntry entry = new DatabaseServerEntry();
                        entry.setServerID(t.nextToken());
                        entry.setServerURL(t.nextToken());
                        entry.setAcceptanceRiseSpeed(Double.parseDouble(t.nextToken()));
                        entry.setNightBonus(Byte.parseByte(t.nextToken()));
                        try {
                            entry.setDecoration(Integer.parseInt(t.nextToken()));
                        } catch (Exception ignore) {
                            //no decoration entry found
                        }
                        entries.add(entry);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to proceed reading server data", e);
            }

            return entries;
        } catch (Exception outer) {
            //typecast or connection failed
            logger.error("Failed getting list of servers. Result is " + result);
        }

        return null;
    }

    /*   public static List<DatabaseServerEntry> listServers() {
    Object result = null;
    try {
    result = callWebInterface("listServers", null);
    String[] lines = (String[]) result;
    List<DatabaseServerEntry> entries = new LinkedList<DatabaseServerEntry>();
    try {
    for (String line : lines) {
    StringTokenizer t = new StringTokenizer(line, "[,]");
    if (t.countTokens() != 2) {
    //invalid entry, probably a status code!?
    try {
    int status = Integer.parseInt(line);
    processStatus("list servers", status);
    return null;
    } catch (Exception noStatus) {
    throw new Exception("Invalid entry '" + line + "'");
    }

    }
    DatabaseServerEntry entry = new DatabaseServerEntry();
    entry.setServerID(t.nextToken());
    entry.setServerURL(t.nextToken());
    entries.add(entry);
    }
    logger.debug("Merging acceptance increments");
    Hashtable<String, Double> riseSpeeds = getAcceptanceRiseSpeed();
    if (riseSpeeds != null) {
    for (DatabaseServerEntry e : entries) {
    Double speed = riseSpeeds.get(e.getServerID());
    if (speed != null) {
    e.setAcceptanceRiseSpeed(speed);
    }
    }
    }

    } catch (Exception e) {
    logger.error("Server entry in invalid format? Dropping.", e);
    }

    return entries;
    } catch (Exception outer) {
    //typecast or connection failed
    logger.error("Failed getting list of servers. Result is " + result);
    }

    return null;
    }

    public static Hashtable<String, Double> getAcceptanceRiseSpeed() {
    Object result = null;
    try {
    result = callWebInterface("getAcceptanceRiseSpeed", null);
    String[] lines = (String[]) result;
    Hashtable<String, Double> entries = new Hashtable<String, Double>();
    try {
    for (String line : lines) {
    StringTokenizer t = new StringTokenizer(line, "[,]");
    if (t.countTokens() != 2) {
    //invalid entry, probably a status code!?
    try {
    int status = Integer.parseInt(line);
    processStatus("get acceptance rise speed", status);
    return null;
    } catch (Exception noStatus) {
    throw new Exception("Invalid entry '" + line + "'");
    }

    }

    entries.put(t.nextToken(), Double.parseDouble(t.nextToken()));
    }

    } catch (Exception e) {
    logger.error("Server entry in invalid format? Dropping.", e);
    }

    return entries;
    } catch (Exception outer) {
    //typecast or connection failed
    logger.error("Failed getting list of servers. Result is " + result);
    }
    return null;
    }
     */
    public static int checkUser(String pUser, String pPassword) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        String password = SecurityAdapter.hashStringMD5(pPassword);

        if (pUser != null && sValidatedUser != null && pPassword != null && sValidatedPassword != null) {
            if (!pUser.equals(sValidatedUser) || !password.equals(sValidatedPassword)) {
                //user or password has changed
                bAccountValidated = false;
            }
        } else {
            //some value is null or was not set yet
            bAccountValidated = false;
        }
        if (bAccountValidated) {
            //account is valid
            return ID_SUCCESS;
        }

        //validate account
        arguments.put("user", pUser);
        arguments.put("pass", password);
        Object result = callWebInterface("checkUser", arguments);
        try {
            String[] lines = (String[]) result;
            int status = Integer.parseInt(lines[0]);
            processStatus("check user", status);
            if (status == 0) {
                sValidatedUser = pUser;
                sValidatedPassword = password;
                bAccountValidated = true;
            }
            return status;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed checking user. Result is " + result);
            try {
                if ((Integer) result == -1) {
                    return ID_WEB_CONNECTION_FAILED;
                }

            } catch (Exception cc) {
                //result is no integer
            }
        }
        return ID_UNKNOWN_ERROR;
    }

    public static int changePassword(String pUser, String pOldPassword, String pNewPassword) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("user", pUser);

        arguments.put("oldpass", SecurityAdapter.hashStringMD5(pOldPassword));
        arguments.put("newpass", SecurityAdapter.hashStringMD5(pNewPassword));
        Object result = callWebInterface("changePassword", arguments);
        try {
            String[] lines = (String[]) result;
            int status = Integer.parseInt(lines[0]);
            processStatus("change password", status);
            return status;
        } catch (Exception e) {
            //typecast or connection failed
            logger.error("Failed changing password. Result is " + result);
            try {
                if ((Integer) result == -1) {
                    return ID_WEB_CONNECTION_FAILED;
                }

            } catch (Exception cc) {
                //result is no integer
            }
        }
        return ID_UNKNOWN_ERROR;
    }

    public static int addUser(String pUser, String pPassword) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("user", pUser);
        arguments.put("pass", SecurityAdapter.hashStringMD5(pPassword));
        Object result = callWebInterface("addUser", arguments);
        try {
            String[] lines = (String[]) result;
            int status = Integer.parseInt(lines[0]);
            processStatus("add user", status);
            return status;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed adding user. Result is " + result);
        }

        return ID_UNKNOWN_ERROR;
    }

    public static String getProperty(String pKey) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("key", pKey);

        Object result = callWebInterface("getProperty", arguments);
        try {
            String[] lines = (String[]) result;
            try {
                //check if a status was returned
                int status = Integer.parseInt(lines[0]);
                processStatus("get property", status);
            } catch (Exception noStatus) {
                return lines[0];
            }

        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed getting property. Result is " + result);
        }

        return null;
    }

    public static String getDownloadURL(String pServer) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("sid", pServer);
        Object result = callWebInterface("getDownloadURL", arguments);
        try {
            String[] lines = (String[]) result;
            try {
                //check if a status was returned
                int status = Integer.parseInt(lines[0]);
                processStatus("get download url", status);
            } catch (Exception noStatus) {
                return lines[0];
            }

        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed getting download URL. Result is " + result);
        }

        return null;
    }

    public static long getUserDataVersion(String pUser, String pServer) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("user", pUser);
        arguments.put("sid", pServer);
        Object result = callWebInterface("getUserDataVersion", arguments);
        try {
            String[] lines = (String[]) result;
            long lResult = Long.parseLong(lines[0]);
            if (lResult < 0) {
                //status code
                processStatus("get user data version", (int) lResult);
            }

            return lResult;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed getting user data version. Result is " + result);
        }

        return ID_UNKNOWN_ERROR;
    }

    public static double getCurrentVersion() {
        try {
            return Double.parseDouble(DatabaseInterface.getProperty("current_version"));
        } catch (Exception e) {
            return ID_UNKNOWN_ERROR;
        }
    }

    public static int registerUserForServer(String pUser, String pServer) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("user", pUser);
        arguments.put("sid", pServer);
        Object result = callWebInterface("registerUser", arguments);
        try {
            String[] lines = (String[]) result;
            int status = Integer.parseInt(lines[0]);
            processStatus("register user", status);
            return status;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed registering user for server. Result is " + result);
        }

        return ID_UNKNOWN_ERROR;
    }

    public static int updateDataVersion(String pUser, String pServer) {
        Hashtable<String, String> arguments = new Hashtable<String, String>();
        arguments.put("user", pUser);
        arguments.put("sid", pServer);
        Object result = callWebInterface("updateDataVersion", arguments);
        try {
            String[] lines = (String[]) result;
            int status = Integer.parseInt(lines[0]);
            processStatus("update data version", status);
            return status;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed to update data version. Result is " + result);
        }

        return ID_UNKNOWN_ERROR;
    }

    public static long getServerTime() {
        Object result = callWebInterface("getServerTime", null);
        try {
            String[] lines = (String[]) result;
            long lResult = Long.parseLong(lines[0]);
            if (lResult < 0) {
                //status code
                processStatus("get server time", (int) lResult);
            }

            return lResult;
        } catch (Exception e) {
            //typecast or connection failed 
            logger.error("Failed getting server time. Result is " + result);
        }

        return ID_UNKNOWN_ERROR;
    }

    public static void main(String[] args) throws Exception {

        DOMConfigurator.configure("log4j.xml");
        /* System.setProperty("proxyUse", "true");

        ConsoleAppender a = new ConsoleAppender();
        a.setLayout(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c - %m%n"));
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(a);
        Logger.getRootLogger().setLevel(Level.DEBUG);


        /* System.setProperty("proxyUse", "true");

        System.setProperty("proxyHost", "proxy.fzk.de");
        System.setProperty("proxyPort", "8000");*/

        /* URL u = new URL("http://de37.die-staemme.de/interface.php?func=get_conquer&since=1245912202");
        URLConnection con = u.openConnection();
        BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line = "";
        while ((line = r.readLine()) != null) {
        System.out.println(line);
        }*/



        /* List<DatabaseServerEntry> info = DatabaseInterface.getServerInfo();

        /* List<DatabaseServerEntry> info = DatabaseInterface.getServerInfo();

        for (DatabaseServerEntry in : info) {

        System.out.println("ID: " + in.getServerID());
        System.out.println("URL: " + in.getServerURL());
        System.out.println("Rise: " + in.getAcceptanceRiseSpeed());
        System.out.println("Night: " + in.getNightBonus());
        System.out.println("--------");
        }*/

        /*   System.out.println("ID: " + in.getServerID());
        System.out.println("URL: " + in.getServerURL());
        System.out.println("Rise: " + in.getAcceptanceRiseSpeed());
        System.out.println("Night: " + in.getNightBonus());
        System.out.println("--------");
        }*/
        //System.out.println(DatabaseInterface.listServers());
        //  System.out.println(DatabaseInterface.checkUser("Torridity", "cfcaef487fc66a6d8295e8e3f68b4db9"));
        //System.out.println(DatabaseInterface.addUser("Torridity", "cfcaef487fc66a6d8295e8e3f68b4db9"));

        // System.out.println(DatabaseInterface.getProperty("min_version"));
        System.out.println(DatabaseInterface.getUserDataVersion("ulkminister", "de63"));
        //System.out.println(DatabaseInterface.getDownloadURL("de26"));

        System.out.println(DatabaseInterface.getUserDataVersion("Torridity", "de43"));
        //System.out.println(DatabaseInterface.getDownloadURL("de26"));

        /*long versionU = DatabaseInterface.getUserDataVersion("Torridity", "de8");
        if (versionU > 0) {
        System.out.println("User: " + new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(new Date(versionU)));
        }
        long versionS = DatabaseInterface.getServerDataVersion("de8");
        if (versionS > 0) {
        System.out.println("Server: " + new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(new Date(versionS)));
        }
         */
        //  System.out.println("Result " + DatabaseInterface.updateDataVersion("Torridity", "de8"));

        /* versionU = DatabaseInterface.getUserDataVersion("Torridity", "de8");
        if (versionU > 0) {
        System.out.println("User: " + new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(new Date(versionU)));
        }
        versionS = DatabaseInterface.getServerDataVersion("de8");
        if (versionS > 0) {
        System.out.println("Server: " + new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(new Date(versionS)));
        }*/

        //System.out.println(DatabaseInterface.addUser("Töter&12<>", SecurityAdapter.hashStringMD5("1234")));
    }
}
