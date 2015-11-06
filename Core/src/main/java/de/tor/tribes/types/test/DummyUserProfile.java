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
