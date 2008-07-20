/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author Jejkal
 */
public class IncServerSync {

    private static Logger logger = Logger.getLogger(IncServerSync.class);
    /**Full update every week*/
    private final static long FULL_UPDATE_INTERVAL = 1000 * 60 * 60 * 24 * 7;

    private static void createDiff(URL pRemoteFile, String pLocalFile) throws Exception {
        logger.info("Creating diff between " + pRemoteFile + " and " + pLocalFile);
        try {//new GZIPInputStream()
            BufferedReader remoteReader = new BufferedReader(new InputStreamReader((pRemoteFile.openStream())));
            BufferedReader localReader = new BufferedReader(new InputStreamReader(new FileInputStream(pLocalFile)));

            StringBuffer diffBuffer = new StringBuffer();
            String remoteLine = "";
            int parsedLines = 0;
            int changedTokens = 0;
            int newLines = 0;
            int unchangedLines = 0;
            while ((remoteLine = remoteReader.readLine()) != null) {
                String localLine = localReader.readLine();
                if (localLine != null) {
                    //local and remote line available
                    if (localLine.equals(remoteLine)) {
                        //no change
                        diffBuffer.append("\n");
                        unchangedLines++;
                    } else {
                        //changes found, check tokens
                        StringTokenizer remoteTokens = new StringTokenizer(remoteLine, ",");
                        StringTokenizer localTokens = new StringTokenizer(localLine, ",");
                        if (remoteTokens.countTokens() == localTokens.countTokens()) {
                            //only parse tokens if the format of both files is equal
                            while (remoteTokens.hasMoreTokens()) {
                                String remoteToken = remoteTokens.nextToken();
                                String localToken = localTokens.nextToken();
                                if (localToken.equals(remoteToken)) {
                                    //local and remote tokens are equal, so don't change
                                    if (remoteTokens.hasMoreTokens()) {
                                        diffBuffer.append(" ,");
                                    } else {
                                        diffBuffer.append(" ");
                                    }
                                } else {
                                    //local and remote token are different, so insert the new value
                                    diffBuffer.append(remoteToken);
                                    if (remoteTokens.hasMoreTokens()) {
                                        diffBuffer.append(",");
                                    } else {
                                        diffBuffer.append(" ");
                                    }
                                    changedTokens++;
                                }
                            }
                            diffBuffer.append("\n");
                        } else {
                            logger.warn("Local and remote file seem to have a different format (#LocalTokens (" + localTokens.countTokens() + ") != #RemoteTokens (" + remoteTokens.countTokens() + "). Current line skipped");
                            diffBuffer.append("\n");
                        }
                    }
                } else {
                    //only remote line available -> complete new data
                    diffBuffer.append(remoteLine);
                    diffBuffer.append("\n");
                    newLines++;
                }
                parsedLines++;
            }

            logger.info("===Start of diff statistics for " + pLocalFile + "===");
            logger.info("File size: " + new File(pLocalFile).length() + " bytes");
            logger.info("Parsed lines: " + parsedLines);
            logger.info("Unchanged lines: " + unchangedLines);
            logger.info("New lines: " + newLines);
            logger.info("Changed tokens: " + changedTokens);
            logger.info("Diff size: " + diffBuffer.toString().length() + " bytes");
            logger.info("===End of diff statistics===");


            String diffFile = pLocalFile.substring(0, pLocalFile.indexOf(".")) + ".diff";
            logger.info("Writing diff to " + diffFile);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File("file.zip")));
            out.setLevel(9);
            out.putNextEntry(new ZipEntry("village.txt"));
            out.write(diffBuffer.toString().getBytes());
            out.finish();
            GZIPOutputStream out2 = new GZIPOutputStream(new FileOutputStream(new File(diffFile)));
             out2.write(diffBuffer.toString().getBytes());
            out2.finish();
        } catch (Exception e) {
            logger.error("Failed to create diff from URL " + pRemoteFile + " for local file " + pLocalFile, e);
        }
    }

    private static void updateServerData(String pServerURL, String pServerDir, boolean pIncremental) throws Exception {
        if (!pIncremental) {
            logger.info("Removing old diffs");
            new File(pServerDir + "/village.diff").delete();
            new File(pServerDir + "/tribe.diff").delete();
            new File(pServerDir + "/ally.diff").delete();
            new File(pServerDir + "/kill_att.diff").delete();
            new File(pServerDir + "/kill_def.diff").delete();
            logger.info("Performing full update");
            downloadDataFile(new URL(pServerURL + "/map/village.txt.gz"), pServerDir + "/village.txt.gz");
            downloadDataFile(new URL(pServerURL + "/map/tribe.txt.gz"), pServerDir + "/tribe.txt.gz");
            downloadDataFile(new URL(pServerURL + "/map/ally.txt.gz"), pServerDir + "/ally.txt.gz");
            downloadDataFile(new URL(pServerURL + "/map/kill_att.txt.gz"), pServerDir + "/kill_att.txt.gz");
            downloadDataFile(new URL(pServerURL + "/map/kill_def.txt.gz"), pServerDir + "/kill_def.txt.gz");
        } else {
            if (!new File(pServerDir + "/village.txt.gz").exists()) {
                logger.info(pServerDir + "/village.txt.gz does not exist yet. Downloading file");
                downloadDataFile(new URL(pServerURL + "/map/village.txt.gz"), pServerDir + "/village.txt.gz");
            } else {
                createDiff(new URL(pServerURL + "/map/village.txt.gz"), pServerDir + "/village.txt.gz");
            }
            if (!new File(pServerDir + "/tribe.txt.gz").exists()) {
                logger.info(pServerDir + "/tribe.txt.gz does not exist yet. Downloading file");
                downloadDataFile(new URL(pServerURL + "/map/tribe.txt.gz"), pServerDir + "/tribe.txt.gz");
            } else {
                createDiff(new URL(pServerURL + "/map/tribe.txt.gz"), pServerDir + "/tribe.txt.gz");
            }
            if (!new File(pServerDir + "/ally.txt.gz").exists()) {
                logger.info(pServerDir + "/ally.txt.gz does not exist yet. Downloading file");
                downloadDataFile(new URL(pServerURL + "/map/ally.txt.gz"), pServerDir + "/ally.txt.gz");
            } else {
                createDiff(new URL(pServerURL + "/map/ally.txt.gz"), pServerDir + "/ally.txt.gz");
            }
            if (!new File(pServerDir + "/kill_att.txt.gz").exists()) {
                logger.info(pServerDir + "/kill_att.txt.gz does not exist yet. Downloading file");
                downloadDataFile(new URL(pServerURL + "/map/kill_att.txt.gz"), pServerDir + "/kill_att.txt.gz");
            } else {
                createDiff(new URL(pServerURL + "/map/kill_att.txt.gz"), pServerDir + "/kill_att.txt.gz");
            }
            if (!new File(pServerDir + "/kill_def.txt.gz").exists()) {
                logger.info(pServerDir + "/kill_def.txt.gz does not exist yet. Downloading file");
                downloadDataFile(new URL(pServerURL + "/map/kill_def.txt.gz"), pServerDir + "/kill_def.txt.gz");
            } else {
                createDiff(new URL(pServerURL + "/map/kill_def.txt.gz"), pServerDir + "/kill_def.txt.gz");
            }
        }
    }

    private static void testDiff() throws Exception {
        createDiff(new File("h:/servers/tribe_new.txt").toURI().toURL(), "h:/servers/tribe_old.txt");
    }

    private static void downloadDataFile(URL pSource, String pLocalName) throws Exception {
        logger.info("Downloading " + pSource);
        URLConnection ucon = pSource.openConnection();
        FileOutputStream tempWriter = new FileOutputStream(pLocalName);
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

    private static void mergeDiff() throws Exception {
        String localFile = "c:/tribe.txt.gz";
        /*URL remoteDiffFile = new URL("http://www.torridity.de/servers/" + GlobalOptions.getSelectedServer() + "/" + pFile + ".diff");
        InputStream diffIn = null;
        try {
        diffIn = remoteDiffFile.openStream();
        } catch (FileNotFoundException fnf) {
        //no diff available, perform full update
        logger.info("No differential update available. Doing full update");
        downloadFull(pFile);
        return;
        }
         */
        String remoteFile = "c:/tribe.diff";
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(remoteFile))));
        BufferedReader localReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(localFile))));
        String diffLine = "";

        StringBuffer merged = new StringBuffer();
        while ((diffLine = diffReader.readLine()) != null) {
            String localLine = localReader.readLine();
            if (localLine != null) {
                //read all diff content
                if (diffLine.trim().length() == 0) {
                    //no change
                    merged.append(localLine + "\n");
                } else {
                    StringTokenizer diffTokens = new StringTokenizer(diffLine, ",");
                    StringTokenizer localTokens = new StringTokenizer(localLine, ",");
                    while (diffTokens.hasMoreTokens()) {
                        //loop over all tokens
                        String diffToken = diffTokens.nextToken();
                        if (diffToken.trim().length() == 0) {
                            //diff token is empty, no change
                            if (diffTokens.hasMoreTokens()) {
                                merged.append(localTokens.nextToken() + ",");
                            } else {
                                merged.append(localTokens.nextToken());
                            }
                        } else {
                            //diff token is not empty, value has changed
                            if (diffTokens.hasMoreTokens()) {
                                merged.append(diffToken + ",");
                            } else {
                                merged.append(diffToken);
                            }
                            //drop the local token
                            localTokens.nextToken();
                        }
                    }
                    merged.append("\n");
                }
            } else {
                merged.append(diffLine + "\n");
            }
        }
        logger.debug("Writing merged data");
        GZIPOutputStream mergedOut = new GZIPOutputStream(new FileOutputStream("c:/merge.txt.gz"));
        mergedOut.write(merged.toString().getBytes());
        mergedOut.finish();
        mergedOut.close();
    }

    public static void main(String[] args) {
        DOMConfigurator.configure("log4j.xml");
try{
        testDiff();
}catch(Exception e){e.printStackTrace();}
        if(true){
            return;
        }
        
        long start = System.currentTimeMillis();
        try {String name = "village";
            BufferedReader oldReader = new BufferedReader(new InputStreamReader(new FileInputStream("h:/servers/" + name + "_old.txt")));
            BufferedReader newReader = new BufferedReader(new InputStreamReader(new FileInputStream("h:/servers/" + name + "_new.txt")));
            String line = "";
            Hashtable<Integer, String> oldLines = new Hashtable<Integer, String>();
            while ((line = oldReader.readLine()) != null) {
                Integer id = Integer.parseInt(line.substring(0, line.indexOf(",")));
                oldLines.put(id, line.substring(line.indexOf(",") + 1));
            }

            line = "";
            Hashtable<Integer, String> newLines = new Hashtable<Integer, String>();

            while ((line = newReader.readLine()) != null) {
                Integer id = Integer.parseInt(line.substring(0, line.indexOf(",")));
                newLines.put(id, line.substring(line.indexOf(",") + 1));
            }
            Integer[] oldIDs = oldLines.keySet().toArray(new Integer[]{});
            Arrays.sort(oldIDs);
            Integer[] newIDs = newLines.keySet().toArray(new Integer[]{});
            Arrays.sort(newIDs);

            StringBuffer b = new StringBuffer();
            for (Integer id : newIDs) {
                String line_old = oldLines.get(id);
                //insert the ID 
                if (line_old == null) {
                    //new line -> append to new file
                     
                    b.append(id + "," + newLines.get(id) + "\n");
                } else {
                    //parse line
                    if (newLines.get(id).equals(line_old)) {
                        //empty line because unchanged
                        //b.append("\n");
                    } else {
                        //diff -> insert differing fields
                        StringTokenizer oldTokens = new StringTokenizer(line_old, ",");
                        StringTokenizer newTokens = new StringTokenizer(newLines.get(id), ",");
                        //String diffLine = "";
                       b.append(id + ",");
                        while (oldTokens.hasMoreTokens()) {
                            String oldToken = oldTokens.nextToken();
                            String newToken = newTokens.nextToken();
                            if (oldToken.equals(newToken)) {
                                if (oldTokens.hasMoreTokens()) {
                                    b.append(", ");
                                } else {
                                    //append nothing
                                }
                            } else {
                                if (oldTokens.hasMoreTokens()) {
                                    b.append(newToken + ", ");
                                } else {
                                    b.append(newToken);
                                }
                            }
                        }
                        b.append("\n");
                    }
                }
            }
            GZIPOutputStream gout = new GZIPOutputStream(new FileOutputStream(new File("h:/servers/" + name + ".diff")));
            gout.write(b.toString().getBytes());
            gout.finish();
            gout.close();
            new FileOutputStream(new File("h:/servers/" + name + "_diff.txt")).write(b.toString().getBytes());
        //lines1.keySet().toArray(new String[]{})
        /*String[] ll1 = (String[]) lines1.toArray(new String[]{});
        Arrays.sort(ll1);
        String[] ll2 = (String[]) lines2.toArray(new String[]{});
        Arrays.sort(ll2);
         */

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Dur: " + (System.currentTimeMillis() - start));

        /*try {
        BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream("c:/tribe_target.txt")));
        String l = "";
        List<String> lines = new LinkedList<String>();
        while ((l = b.readLine()) != null) {
        lines.add(l);
        }
        String[] ll = (String[]) lines.toArray(new String[]{});
        Arrays.sort(ll);
        FileWriter w = new FileWriter("c:/sorted.txt");
        for (String line : ll) {
        w.write(line + "\n");
        }
        w.close();
        } catch (Exception e) {
        e.printStackTrace();
        }
         */


        /* try {
        //testDiff();
        BufferedReader b = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("c:/village.diff"))));
        String l = "";
        int cnt = 0;
        while ((l = b.readLine()) != null) {
        cnt++;
        
        }
        System.out.println("diff " + cnt);
        } catch (Exception e) {
        e.printStackTrace();
        }
        if (true) {
        return;
        }*/

        /*try {
        mergeDiff();
        //testDiff();
        } catch (Exception e) {
        e.printStackTrace();
        }*/
        if (true) {
            return;
        }
        if (args.length < 1) {
            logger.error("args length is smaller 1");
            System.out.println("Usage");
            System.out.println("IncServerSync <sync_properties>");
            System.exit(1);
        }
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(args[0]));
        } catch (Exception e) {
            logger.error("Failed to read properties file '" + args[0] + "'", e);
            System.out.println("Failed to load properties file from '" + args[0] + "'");
            System.exit(1);
        }

        //use proxy settings if available
        System.setProperty("proxyUse", (p.getProperty("proxyUse") != null) ? p.getProperty("proxyUse") : "");
        System.setProperty("proxyHost", (p.getProperty("proxyHost") != null) ? p.getProperty("proxyHost") : "");
        System.setProperty("proxyPort", (p.getProperty("proxyPort") != null) ? p.getProperty("proxyPort") : "");
        /* System.setProperty("proxyUse", "true");
        System.setProperty("proxyHost", "proxy.fzk.de");
        System.setProperty("proxyPort", "8000");
        try {
        URL u = new URL("http://www.torridity.de/servers/de14/test.diff");
        u.openStream();
        System.out.println("OPEN");
        } catch (Exception e) {
        System.out.println("ERROR");
        e.printStackTrace();
        }
        if (true) {
        return;
        }*/

        //get server base dir
        String baseDir = p.getProperty("server.base.dir");
        if (!new File(baseDir).exists()) {
            logger.info("Base dir '" + baseDir + "' does not exist. Creating new.");
            if (!new File(baseDir).mkdirs()) {
                logger.info("Failed to create base dir '" + baseDir + "'.");
                System.exit(1);
            }
        }

        logger.info("Using base dir " + baseDir);
        long lastFullUpdate = 0;
        try {
            lastFullUpdate = Long.parseLong(p.getProperty("last.full.update"));
        } catch (NumberFormatException nfe) {
            //ignored
        }

        String serverList = baseDir + "/server.list";
        if (!new File(serverList).exists()) {
            logger.error("'server.list' not found under " + serverList);
            System.exit(1);
        }
        Properties servers = new Properties();
        try {
            servers.load(new FileInputStream(serverList));
        } catch (Exception e) {
            logger.error("Failed to load 'server.list' from " + serverList);
            System.exit(1);
        }
        Enumeration elems = servers.propertyNames();
        boolean fullUpdate = false;

        logger.info("Starting data update");
        while (elems.hasMoreElements()) {
            String serverID = (String) elems.nextElement();
            String serverURL = (String) servers.get(serverID);
            String serverDir = baseDir + "/" + serverID;
            if (new File(serverDir).exists()) {
                //server was already updated, check for inc update
                if (lastFullUpdate + FULL_UPDATE_INTERVAL < System.currentTimeMillis()) {
                    //inc update
                    try {
                        IncServerSync.updateServerData(serverURL, serverDir, true);
                    } catch (Exception e) {
                        logger.error("Incremental update for serverID '" + serverID + "' failed", e);
                    }
                } else {
                    //full update
                    fullUpdate = true;
                    try {
                        IncServerSync.updateServerData(serverURL, serverDir, false);
                    } catch (Exception e) {
                        logger.error("Full update for serverID '" + serverID + "' failed", e);
                    }
                }
            } else {
                //server does not exist yet, create and perform full update
                if (!new File(serverDir).mkdirs()) {
                    logger.error("Failed to create server directory for serverID '" + serverID + "' under " + serverDir);
                } else {
                    try {
                        IncServerSync.updateServerData(serverURL, serverDir, false);
                    } catch (Exception e) {
                        logger.error("Initial download for serverID '" + serverID + "' failed", e);
                    }
                }
            }
        }
        if (fullUpdate) {
            p.put("last.full.update", Long.toString(System.currentTimeMillis()));
            try {
                p.store(new FileOutputStream("sync.properties"), null);
            } catch (Exception e) {
                logger.error("Failed to update sync.properties", e);
            }
        }

        logger.info("Data updated");
    }
}
