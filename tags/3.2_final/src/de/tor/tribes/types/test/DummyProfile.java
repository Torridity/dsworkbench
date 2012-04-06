/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.UserProfile;

/**
 *
 * @author Torridity
 */
public class DummyProfile extends UserProfile {

    Tribe t = new DummyTribe();

    @Override
    public long getProfileId() {
        return 1;
    }

    @Override
    public Tribe getTribe() {
        return t;
    }
}
