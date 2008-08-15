/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io.server;

import de.tor.tribes.db.DatabaseAdapter;
import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author Jejkal
 */
public class DataBuilderDaemon {

    private static Logger logger = Logger.getLogger(DataBuilderDaemon.class);
    private final Properties SERVER_LIST = new Properties();
    private final Properties DAEMON_SETTINGS = new Properties();
    private static final String BASE_DIR_PROPERTY = "base.dir";
    private static final String SERVER_LIST_PROPERTY = "server.list.path";

    public void downloadData() throws Exception {
        logger.info("Starting update");
        //load settings for daemon
        logger.info("Setting database adapter to local mode");
        DatabaseAdapter.setToLocalMode();

        String baseDir = DatabaseAdapter.getPropertyValue("update_base_dir");
        if (baseDir == null) {
            //no server base dir found
            throw new Exception("Could not obtain 'update_base_dir' property from database.");
        }

        List<DatabaseServerEntry> servers = DatabaseAdapter.getServerList();

        //update all servers in server list
        for (DatabaseServerEntry server : servers) {
            String sId = server.getServerID();
            String url = server.getServerURL();
            int version = server.getDataVersion();
            logger.info("Updating server '" + sId + "'");
            String serverDir = baseDir + "/servers/" + sId;
            boolean newServer = false;
            //check if dir for current server exists
            if (!new File(serverDir).exists()) {
                newServer = true;
                logger.info("Creating server dir for server '" + sId + "'");
                if (!new File(serverDir).mkdirs()) {
                    throw new Exception("Failed to create server dir at '" + serverDir + "'");
                } else {
                    logger.info("Server dir created successfully");
                }
            }

            //increment updateId
            version++;

            downloadServerData(url, serverDir);

            //Resulting FileList: 
            //village_tmp.txt.gz
            //ally_tmp.txt.gz
            //tribe_tmp.txt.gz
            //<SERVER_DIR>/kill_off.txt.gz
            //<SERVER_DIR>/kill_def.txt.gz
            if ((!new File(serverDir + "/village.txt.gz").exists()) ||
                    (!new File(serverDir + "/tribe.txt.gz").exists()) ||
                    (!new File(serverDir + "/ally.txt.gz").exists()) ||
                    (!new File(serverDir + "/kill_att.txt.gz").exists()) ||
                    (!new File(serverDir + "/kill_def.txt.gz").exists())) {
                //one of the full data files does not exist
                newServer = true;
            }


            if (!newServer) {
                buildDailyData(serverDir, version);
                //rename current data
                new File("village_tmp.txt.gz").renameTo(new File(serverDir + "/village.txt.gz"));
                new File("ally_tmp.txt.gz").renameTo(new File(serverDir + "/ally.txt.gz"));
                new File("tribe_tmp.txt.gz").renameTo(new File(serverDir + "/tribe.txt.gz"));
            } else {
                //if we have a new server only rename the data files
                new File("village_tmp.txt.gz").renameTo(new File(serverDir + "/village.txt.gz"));
                new File("ally_tmp.txt.gz").renameTo(new File(serverDir + "/ally.txt.gz"));
                new File("tribe_tmp.txt.gz").renameTo(new File(serverDir + "/tribe.txt.gz"));
            }

            //delete last diff which is older than 10 days
            int idToDelete = version - 10;
            if (idToDelete > 0) {
                logger.info("Deleting diffs older than 10 days");
                new File(serverDir + "/village" + idToDelete + ".txt.gz").delete();
                new File(serverDir + "/tribe" + idToDelete + ".txt.gz").delete();
                new File(serverDir + "/ally" + idToDelete + ".txt.gz").delete();
            }

            if (!DatabaseAdapter.setDataVersion(sId, version)) {
                logger.warn("Failed to update data for server with ID '" + sId + "'");
            }
        }

        logger.info("Update finished");
    }

    protected void buildDailyData(String pServerDir, int pDayOfMonth) throws Exception {
        // <editor-fold defaultstate="collapsed" desc="Diff'ing villages">
        logger.info("Reading old village data");
        String currentFile = pServerDir + "/village.txt.gz";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(currentFile))));
        Hashtable<Integer, Village> old_villages = new Hashtable<Integer, Village>();
        String line = "";
        while ((line = reader.readLine()) != null) {
            Village v = Village.parseFromPlainData(line);
            if (v != null) {
                old_villages.put(v.getId(), v);
            }
        }
        logger.info("Reading new village data and creating diff");
        currentFile = "village_tmp.txt.gz";

        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(currentFile))));
        line = "";

        GZIPOutputStream diffS = new GZIPOutputStream(new FileOutputStream(pServerDir + "/village" + pDayOfMonth + ".txt.gz"));
        //create diff for all new villages
        while ((line = reader.readLine()) != null) {
            Village v = Village.parseFromPlainData(line);
            if (v != null) {
                String diff = v.createDiff(old_villages.get(v.getId()));
                if (diff != null) {
                    diffS.write(diff.getBytes());
                }
            }
        }
        diffS.finish();
        diffS.close();
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Diff'ing tribes">
        logger.info("Reading old tribe data");
        currentFile = pServerDir + "/tribe.txt.gz";
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(currentFile))));
        line = "";
        Hashtable<Integer, Tribe> old_tribes = new Hashtable<Integer, Tribe>();
        while ((line = reader.readLine()) != null) {
            Tribe t = Tribe.parseFromPlainData(line);
            if (t != null) {
                old_tribes.put(t.getId(), t);
            }
        }

        logger.info("Reading new tribe data and creating diff");
        currentFile = "tribe_tmp.txt.gz";

        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(currentFile))));
        line = "";
        diffS = new GZIPOutputStream(new FileOutputStream(pServerDir + "/tribe" + pDayOfMonth + ".txt.gz"));
        while ((line = reader.readLine()) != null) {
            Tribe t = Tribe.parseFromPlainData(line);
            if (t != null) {
                String diff = t.createDiff(old_tribes.get(t.getId()));
                if (diff != null) {
                    diffS.write(diff.getBytes());
                }
                //remove current tribe from list of tribes
                old_tribes.remove(t.getId());
            }
        }

        //remaining tribes are not found in new data, so they where deleted
        Enumeration<Integer> deletedTribesIDs = old_tribes.keys();
        while (deletedTribesIDs.hasMoreElements()) {
            Tribe deleted = old_tribes.get(deletedTribesIDs.nextElement());
            diffS.write(new String(deleted.getId() + "\n").getBytes());
        }

        diffS.finish();
        diffS.close();
        //</editor-fold>  

        // <editor-fold defaultstate="collapsed" desc="Diff'ing allies">
        logger.info("Reading old ally data");
        currentFile = pServerDir + "/ally.txt.gz";
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(currentFile))));
        line = "";
        Hashtable<Integer, Ally> old_allies = new Hashtable<Integer, Ally>();
        while ((line = reader.readLine()) != null) {
            Ally a = Ally.parseFromPlainData(line);
            if (a != null) {
                old_allies.put(a.getId(), a);
            }
        }
        logger.info("Reading new ally data and creating diff");

        currentFile = "ally_tmp.txt.gz";

        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(currentFile))));
        line = "";
        diffS = new GZIPOutputStream(new FileOutputStream(pServerDir + "/ally" + pDayOfMonth + ".txt.gz"));
        while ((line = reader.readLine()) != null) {
            Ally a = Ally.parseFromPlainData(line);
            if (a != null) {
                String diff = a.createDiff(old_allies.get(a.getId()));
                if (diff != null) {
                    diffS.write(diff.getBytes());
                }
                old_allies.remove(a.getId());
            }
        }

        //remaining allies are not found in new data, so they where deleted
        Enumeration<Integer> deletedAlliesIDs = old_allies.keys();
        while (deletedAlliesIDs.hasMoreElements()) {
            Ally deleted = old_allies.get(deletedAlliesIDs.nextElement());
            diffS.write(new String(deleted.getId() + "\n").getBytes());
        }
        diffS.finish();
        diffS.close();
    //</editor-fold>
    }

    /**Download all the server data for one single server to the local FS*/
    private void downloadServerData(String pUrl, String pServerDir) throws Exception {
        String sURL = pUrl;
        logger.info("Loading village.txt.gz from " + pUrl);
        URL file = new URL(sURL + "/map/village.txt.gz");
        downloadDataFile(file, "village_tmp.txt.gz");

        //download tribe.txt
        logger.info("Loading tribe.txt.gz from " + pUrl);
        file = new URL(sURL.toString() + "/map/tribe.txt.gz");
        downloadDataFile(file, "tribe_tmp.txt.gz");

        //download ally.txt
        logger.info("Loading ally.txt.gz from " + pUrl);
        file = new URL(sURL.toString() + "/map/ally.txt.gz");
        downloadDataFile(file, "ally_tmp.txt.gz");



        //kill files are not diff'ed but always fully downloaded
        //download kill_att.txt
        logger.info("Loading kill_att.txt.gz from " + pUrl);
        file = new URL(sURL.toString() + "/map/kill_att.txt.gz");
        downloadDataFile(file, pServerDir + "kill_att.txt.gz");

        //download kill_def.txt
        logger.info("Loading kill_def.txt.gz from " + pUrl);
        file = new URL(sURL.toString() + "/map/kill_def.txt.gz");
        downloadDataFile(file, pServerDir + "kill_def.txt.gz");
    }

    /**Download one single data file to the local FS*/
    private void downloadDataFile(URL pSource, String pLocalName) throws Exception {
        URLConnection ucon = pSource.openConnection();
        FileOutputStream tempWriter = new FileOutputStream(pLocalName);
        //BufferedReader reader = new BufferedReader(new InputStreamReader(ucon.getInputStream()));
        InputStream isr = ucon.getInputStream();
        int bytes = 0;
        while (bytes != -1) {
            byte[] data = new byte[1024];
            bytes = isr.read(data);
            if (bytes != -1) {
                tempWriter.write(data, 0, bytes);
            }

        }
        try {
            isr.close();
            tempWriter.close();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("log4j.xml");
        DataBuilderDaemon d = new DataBuilderDaemon();
        d.downloadData();
    }
}
