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
package de.tor.tribes.types.ext;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class NoAlly extends Ally {
    
    private static NoAlly SINGLETON = null;
    private List<Tribe> tribes = null;
    
    public static synchronized NoAlly getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoAlly();
        }
        return SINGLETON;
    }
    
    public NoAlly() {
        tribes = new LinkedList<>();
    }
    
    public void reset() {
        tribes.clear();
    }
    
    @Override
    public void addTribe(Tribe t) {
        tribes.add(t);
    }
    
    @Override
    public int getId() {
        return -1;
    }
    
    @Override
    public String getName() {
        return "Kein Stamm";
    }
    
    @Override
    public String getTag() {
        return "-";
    }
    
    @Override
    public Tribe[] getTribes() {
        return tribes.toArray(new Tribe[tribes.size()]);
    }
    
    @Override
    public short getMembers() {
        return (short) tribes.size();
    }
    
    @Override
    public double getPoints() {
        return 0;
    }
    
    @Override
    public int getRank() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "Kein Stamm";
    }
    
    @Override
    public String getToolTipText() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>";
        res += "<tr><td><b>Stamm:</b> </td><td>" + toString() + "</td></tr>";
        res += "</table></html>";
        return res;
    }
}
