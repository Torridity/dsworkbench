/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
//      Properties props1 = new Properties();
//      long s = System.currentTimeMillis();
//      props1.load(new GZIPInputStream(u.openConnection().getInputStream()));
//      System.out.println((System.currentTimeMillis() - s));

        String jar;
        String output;
        try {
            jar = args[0];
            output = args[1];
        } catch (Exception e) {
            jar = "D:\\GRID\\src\\DSWorkbench\\store\\core.jar";
            if (!new File(jar).exists()) {
                throw new Exception("Input file " + jar + " does not exist");
            }
            output = "D:/GRID/src/DSWorkbench/hash.props";
        }

        try {
            Properties props = new Properties();
            JarInputStream jarin2 = new JarInputStream(new FileInputStream(jar));
            JarEntry entry = jarin2.getNextJarEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    props.put(entry.getName(), Long.toString(entry.getCrc()));
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
