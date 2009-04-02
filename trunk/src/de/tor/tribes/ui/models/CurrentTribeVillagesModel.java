/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class CurrentTribeVillagesModel {

    private static Logger logger = Logger.getLogger("TribeVillagesModel");

    public static DefaultComboBoxModel getModel() {
        try {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            String playerID = GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer());
            Tribe t = DataHolder.getSingleton().getTribeByName(playerID);
            Village[] villages = t.getVillageList().toArray(new Village[]{});
            Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
            for (Village v : villages) {
                model.addElement(v);
            }
            return model;
        } catch (Exception e) {
            logger.error("Failed to update tribe villages model", e);
            return new DefaultComboBoxModel(new String[]{"keine Dörfer"});
        }
    }
}
