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

import de.tor.tribes.types.StorageStatus;
import java.util.Comparator;

/**
 *
 * @author Torridity
 */
public class ResourceComparator implements Comparator<StorageStatus> {

    public enum COMPARE_RESOURCE {

        ALL, WOOD, CLAY, IRON
    }
    private COMPARE_RESOURCE resource = COMPARE_RESOURCE.ALL;

    public ResourceComparator() {
    }

    public ResourceComparator(COMPARE_RESOURCE pResource) {
        resource = pResource;
    }

    @Override
    public int compare(StorageStatus o1, StorageStatus o2) {
        double criteria1 = 0;
        double criteria2 = 0;

        switch (resource) {
            case WOOD:
                criteria1 = o1.getWoodStatus();
                criteria2 = o2.getWoodStatus();
                break;
            case CLAY:
                criteria1 = o1.getClayStatus();
                criteria2 = o2.getClayStatus();
                break;
            case IRON:
                criteria1 = o1.getIronStatus();
                criteria2 = o2.getIronStatus();
                break;
            case ALL:
                criteria1 = o1.getWoodStatus() + o1.getClayStatus() + o1.getIronStatus();
                criteria2 = o2.getWoodStatus() + o2.getClayStatus() + o2.getIronStatus();
                break;
        }
                
        return Double.valueOf(criteria1).compareTo(Double.valueOf(criteria2));
    }
}