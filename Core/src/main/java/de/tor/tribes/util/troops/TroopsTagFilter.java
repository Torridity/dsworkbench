/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.tag.TagManagerListener;
import java.util.List;

/**
 *
 * @author Charon
 */
public class TroopsTagFilter implements TroopsFilterInterface {

    private List<Tag> validTags = null;

    @Override
    public void setup(Object pFilterComponent) {
        validTags = (List<Tag>) pFilterComponent;
    }

    @Override
    public boolean isValid(Village pVillage) {
        if (validTags == null || validTags.isEmpty()) {
            return true;
        }
        //check NoTag
        if (validTags.contains(NoTag.getSingleton())) {
            if (TagManager.getSingleton().getTags(pVillage).isEmpty()) {
                return true;
            }
        }

        //check if any tag tags the village
        for (Tag t : validTags) {
            if (t.tagsVillage(pVillage.getId())) {
                return true;
            }
        }
        return false;
    }
}
