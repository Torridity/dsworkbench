/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.util.GlobalOptions;

/**
 *
 * @author Torridity
 */
public class DummyUserProfile extends UserProfile {

    @Override
    public long getProfileId() {
	return -1;
    }

    @Override
    public String getServerId() {
	return GlobalOptions.getSelectedServer();
    }

    private static DummyUserProfile SINGLETON = null;

    public static DummyUserProfile getSingleton() {
	if ( SINGLETON == null ) {
	    SINGLETON = new DummyUserProfile();
	}
	return SINGLETON;
    }

    public DummyUserProfile() {
	super();
	super.addProperty("last.x", "500");
	super.addProperty("last.y", "500");
	super.addProperty("zoom", "1.0");
	super.addProperty("active.attack.plan", "default");
	super.addProperty("uv.id", -1);
    }

    @Override
    public Tribe getTribe() {
	return InvalidTribe.getSingleton();
    }

    @Override
    public void addProperty( String pKey, Object pValue ) {
    }

    @Override
    public void restoreProperties() {
    }

    @Override
    public boolean storeProfileData() {
	return true;
    }

    @Override
    public void updateProperties() {
    }

    @Override
    public String toString() {
	return "-NoProfile-";
    }

}
