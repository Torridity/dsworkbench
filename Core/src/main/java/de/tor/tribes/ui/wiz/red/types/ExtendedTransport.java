/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.red.types;

import de.tor.tribes.types.Resource;
import de.tor.tribes.types.Transport;
import de.tor.tribes.types.ext.Village;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class ExtendedTransport extends Transport {

    private Village source = null;
    private boolean transferredToBrowser = false;

    public ExtendedTransport(Village pSource, List<Resource> pResources, Village pTarget) {
        super(pTarget, pResources);
        source = pSource;
    }

    public Village getSource() {
        return source;
    }

    public int getWood() {
        return getSingleTransports().get(0).getAmount();
    }

    public int getClay() {
        return getSingleTransports().get(1).getAmount();
    }

    public int getIron() {
        return getSingleTransports().get(2).getAmount();
    }

    public int getMerchants() {
        int result = 0;
        for (Resource r : getSingleTransports()) {
            result += r.getAmount() / 1000;
        }
        return result;
    }

    public void setTransferredToBrowser(boolean transferredToBrowser) {
        this.transferredToBrowser = transferredToBrowser;
    }

    public boolean isTransferredToBrowser() {
        return transferredToBrowser;
    }
}

