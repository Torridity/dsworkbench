/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.red.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchMerchantDistibutor;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class ExtendedTransport extends DSWorkbenchMerchantDistibutor.Transport {

    private Village source = null;
    private boolean transferredToBrowser = false;

    public ExtendedTransport(Village pSource, List<DSWorkbenchMerchantDistibutor.Resource> pResources, Village pTarget) {
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
        for (DSWorkbenchMerchantDistibutor.Resource r : getSingleTransports()) {
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

