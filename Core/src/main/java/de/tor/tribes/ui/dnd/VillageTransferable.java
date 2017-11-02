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
package de.tor.tribes.ui.dnd;

import de.tor.tribes.types.ext.Village;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class VillageTransferable implements Transferable {

    private List<Village> villages = null;

    public VillageTransferable(Village pVillage) {
        villages = new LinkedList<>();
        villages.add(pVillage);
    }

    public VillageTransferable(List<Village> pVillage) {
        villages = pVillage;
    }
    // This is the custom DataFlavor for Scribble objects
    public static DataFlavor villageDataFlavor = new DataFlavor(VillageTransferable.class, "Village");
    // This is a list of the flavors we know how to work with
    public static DataFlavor[] supportedFlavors = {villageDataFlavor, DataFlavor.stringFlavor};

    /** Return the data formats or "flavors" we know how to transfer */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors.clone();
    }

    /** Check whether we support a given flavor */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.equals(villageDataFlavor) || flavor.equals(DataFlavor.stringFlavor));
    }

    /**
     * Return the scribble data in the requested format, or throw an exception
     * if we don't support the requested format
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(villageDataFlavor)) {
            return villages;
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            return villages.get(0).getX() + "|" + villages.get(0).getY();
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
