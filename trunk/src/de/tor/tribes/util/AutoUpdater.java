/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Torridity
 */
public class AutoUpdater {

    public static String obtainChangeLog() throws IOException {
        URL u = new URL("http://www.dsworkbench.de/downloads/Update/changelog.gz");
        GZIPInputStream in = new GZIPInputStream(u.openConnection().getInputStream());
        InputStreamReader ir = new InputStreamReader(in, "ISO-8859-1");
        StringBuilder buffer = new StringBuilder();
        char[] data = new char[2048];
        int br = 0;
        while (br != -1) {
            br = ir.read(data);
            if (br != -1) {
                buffer.append(data, 0, br);
                data = new char[2048];
            }
        }
        in.close();
        return buffer.toString();
    }

    public static List<String> getUpdatedResources() throws IOException {

        Properties props = new Properties();
        URL u = new URL("http://www.dsworkbench.de/downloads/Update/hash.props");
        props.load(u.openConnection().getInputStream());

        String currentJar = "./store/core.jar";
        currentJar = "H:/Software/DSWorkbench-Distribute/DSWorkbench3.01/DSWorkbench/lib/core.jar";

        HashMap<String, Long> existingEntries = new HashMap<String, Long>();
        JarInputStream jarin = new JarInputStream(new FileInputStream(currentJar));
        JarEntry entry = jarin.getNextJarEntry();
        while (entry != null) {
            if (!entry.isDirectory()) {
                existingEntries.put(entry.getName(), entry.getCrc());
            }
            entry = jarin.getNextJarEntry();
        }

        int newFiles = 0;
        int changedFiles = 0;

        List<String> modified = new ArrayList<String>();
        Set<Object> entries = props.keySet();

        for (Object e : entries) {
            String resource = (String) e;
            Long newHash = Long.parseLong((String) props.get(resource));
            Long existingHash = existingEntries.get(resource);
            if (existingHash == null) {
                newFiles++;
                modified.add(resource);
            } else if (existingHash.compareTo(newHash) != 0) {
                changedFiles++;
                modified.add(resource);
            }
        }

        return modified;
    }

    public static void downloadUpdate(List<String> modifiedResources, UpdateListener pListener) throws IOException {
        URL u = new URL("http://www.dsworkbench.de/downloads/Update/core.jar");
        ZipInputStream zin = new ZipInputStream(u.openConnection().getInputStream());

        JarOutputStream jout = new JarOutputStream(new FileOutputStream("./update.jar"));
        ZipEntry en = zin.getNextEntry();
        int updateCnt = 0;
        int toUpdate = modifiedResources.size();
        while (en != null) {
            String resourceName = en.getName();
            if (modifiedResources.contains(resourceName)) {
                ZipEntry n = new ZipEntry(resourceName);
                jout.putNextEntry(n);
                byte[] data = new byte[2048];
                int br = 0;
                while (br != -1) {
                    br = zin.read(data);
                    if (br != -1) {
                        jout.write(data, 0, br);
                        data = new byte[2048];
                    }
                }
                jout.closeEntry();
                updateCnt++;
                if (pListener != null) {
                    pListener.fireResourceUpdatedEvent(resourceName, 100.0 * updateCnt / toUpdate);
                }
                modifiedResources.remove(resourceName);
            }
            en = zin.getNextEntry();
        }

        jout.finish();
        jout.flush();
        jout.close();
        if (pListener != null) {
            if (!modifiedResources.isEmpty()) {
                pListener.fireUpdateFinishedEvent(false, "Es konnten nicht alle Daten geladen werden");
            } else {
                pListener.fireUpdateFinishedEvent(true, null);
            }

        }

    }

    public static void main(String[] args) throws Exception {

        System.out.println(obtainChangeLog());

        //////////////////////////////////////////////
        List<File> files = new ArrayList<File>();
        getImages(new File("./graphics"), files);
        System.out.println(files.size());
        List<File> sources = new ArrayList<File>();
        getClasses(new File("./src"), sources);
        List<File> toRemove = new ArrayList<File>();
        System.out.println("Testing " + sources.size() + " sources");
        for (File image : files) {
            System.out.println("Checking file " + image);
            boolean used = false;
            String cleanPath = image.getName();
            // String cleanPath = FilenameUtils.normalizeNoEndSeparator(path, true);
            for (File source : sources) {
                byte[] data = new byte[(int) source.length()];
                DataInputStream din = new DataInputStream(new FileInputStream(source));
                din.readFully(data);
                din.close();
                if (new String(data).indexOf(cleanPath) > 0) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                toRemove.add(image);
            }
        }
        System.out.println("Remove:");
        int size = 0;
        for (File tr : toRemove) {
            System.out.println(" - " + tr);
            size += tr.length();
        }
        System.out.println("Amount: " + size);
        System.out.println("-------------");


        toRemove = new ArrayList<File>();
        for (File source1 : sources) {
            System.out.println("Checking file " + source1);
            boolean used = false;
            String cleanPath = source1.getName();
            cleanPath = cleanPath.substring(0, cleanPath.indexOf("."));
            // String cleanPath = FilenameUtils.normalizeNoEndSeparator(path, true);
            for (File source : sources) {
                if (!source.equals(source1)) {
                    byte[] data = new byte[(int) source.length()];
                    DataInputStream din = new DataInputStream(new FileInputStream(source));
                    din.readFully(data);
                    din.close();
                    if (new String(data).indexOf(cleanPath) > 0) {
                        used = true;
                        break;
                    }
                }
            }
            if (!used) {
                toRemove.add(source1);
            }
        }

        System.out.println("Remove:");
        size = 0;
        for (File tr : toRemove) {
            System.out.println(" - " + tr + "(" + tr.length() + ")");
            size += tr.length();
        }
        System.out.println("Amount: " + size);
    }

    public static void getImages(File pFolder, List<File> files) {
        for (File f : pFolder.listFiles()) {
            if (f.getPath().indexOf(".svn") == -1 && f.getPath().indexOf("skins") == -1 && f.getPath().indexOf("world") == -1 && f.getPath().indexOf("tex") == -1 && f.getPath().indexOf("splash") == -1) {
                if (f.isDirectory()) {
                    getImages(f, files);
                } else {
                    if (f.getName().endsWith("gif") || f.getName().endsWith("png")) {
                        files.add(f);
                    }
                }
            }
        }
    }

    public static void getClasses(File pFolder, List<File> files) {
        for (File f : pFolder.listFiles()) {
            if (f.getPath().indexOf(".svn") == -1 && f.getPath().indexOf("res") == -1) {
                if (f.isDirectory()) {
                    getClasses(f, files);
                } else {
                    if (f.getName().endsWith("java")) {
                        files.add(f);
                    }
                }
            }
        }
    }
}
