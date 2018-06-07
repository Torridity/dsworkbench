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

import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.util.bb.AttackListFormatter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Torridity
 */
public class AttackToTextWriter {

    private static Logger logger = LogManager.getLogger("AttackToTextWriter");

    public static boolean writeAttacks(Attack[] pAttacks, File pPath, int pAttacksPerFile, boolean pExtendedInfo, boolean pZipResults) {

        Hashtable<Tribe, List<Attack>> attacks = new Hashtable<>();

        for (Attack a : pAttacks) {
            Tribe t = a.getSource().getTribe();
            List<Attack> attsForTribe = attacks.get(t);
            if (attsForTribe == null) {
                attsForTribe = new LinkedList<>();
                attacks.put(t, attsForTribe);
            }
            attsForTribe.add(a);
        }

        Set<Entry<Tribe, List<Attack>>> entries = attacks.entrySet();
        for (Entry<Tribe, List<Attack>> entry : entries) {
            Tribe t = entry.getKey();
            List<Attack> tribeAttacks = entry.getValue();
            List<String> blocks = new LinkedList<>();

            while (!tribeAttacks.isEmpty()) {
                List<Attack> attsForBlock = new LinkedList<>();
                for (int i = 0; i < pAttacksPerFile; i++) {
                    if (!tribeAttacks.isEmpty()) {
                        attsForBlock.add(tribeAttacks.remove(0));
                    }
                }

                String fileContent = new AttackListFormatter().formatElements(attsForBlock, pExtendedInfo);
                blocks.add(fileContent);
            }
            if (!pZipResults) {
                writeBlocksToFiles(blocks, t, pPath);
            } else {
                writeBlocksToZip(blocks, t, pPath);
            }

        }
        return true;
    }

    private static boolean writeBlocksToFiles(List<String> pBlocks, Tribe pTribe, File pPath) {
        int fileNo = 1;
        String baseFilename = pTribe.getName().replaceAll("\\W+", "");
        for (String block : pBlocks) {
            String fileName = FilenameUtils.concat(pPath.getPath(), baseFilename + fileNo + ".txt");
            FileWriter w = null;
            try {
                w = new FileWriter(fileName);
                w.write(block);
                w.flush();
                w.close();
            } catch (IOException ioe) {
                logger.error("Failed to write attack to textfile", ioe);
                return false;
            } finally {
                if (w != null) {
                    try {
                        w.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            fileNo++;
        }
        return true;
    }

    private static boolean writeBlocksToZip(List<String> pBlocks, Tribe pTribe, File pPath) {
        int fileNo = 1;
        String baseFilename = pTribe.getName().replaceAll("\\W+", "");
        ZipOutputStream zout = null;
        try {
            zout = new ZipOutputStream(new FileOutputStream(FilenameUtils.concat(pPath.getPath(), baseFilename + ".zip")));
            for (String block : pBlocks) {
                String entryName = baseFilename + fileNo + ".txt";
                ZipEntry entry = new ZipEntry(entryName);
                try {
                    zout.putNextEntry(entry);
                    zout.write(block.getBytes());
                    zout.closeEntry();
                } catch (IOException ioe) {
                    logger.error("Failed to write attack to zipfile", ioe);
                    return false;
                }
                fileNo++;
            }
        } catch (IOException ioe) {
            logger.error("Failed to write content to zip file", ioe);
            return false;
        } finally {
            if (zout != null) {
                try {
                    zout.flush();
                    zout.close();
                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }
}
