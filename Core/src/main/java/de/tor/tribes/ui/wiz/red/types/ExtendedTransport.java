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

