/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author Jejkal
 */
public class TribeStatsElement {

    private Tribe tribe = null;

    public static TribeStatsElement loadFromFile(File pStatFile) {
        TribeStatsElement elem = new TribeStatsElement();
        Integer tribeId = Integer.parseInt(pStatFile.getName());
        elem.setTribe(DataHolder.getSingleton().getTribes().get(tribeId));
        try {
            BufferedReader r = new BufferedReader(new FileReader(pStatFile));
            String line = "";
            while ((line = r.readLine()) != null) {
            
            }
        } catch (Exception e) {
            return null;
        }
        return elem;
    }

    public Tribe getTribe() {
        return tribe;
    }

    private void setTribe(Tribe pTribe) {
        tribe = pTribe;
    }
}
