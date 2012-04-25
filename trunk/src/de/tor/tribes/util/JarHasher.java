/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.io.*;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author Torridity
 */
public class JarHasher {

    public static void main(String[] args) throws Exception {
        String jar = args[0];
        String output = args[1];
        try {
            Properties props = new Properties();
            JarInputStream jarin2 = new JarInputStream(new FileInputStream(jar));
            JarEntry entry = jarin2.getNextJarEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    props.put(entry.getName(), new Long(entry.getCrc()).toString());
                }
                entry = jarin2.getNextJarEntry();
            }

            props.store(new FileOutputStream(output), null);
            jarin2.close();
        } catch (IOException ioe) {
            System.out.println("Failed to hash jar " + jar + " to " + output);
            ioe.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
