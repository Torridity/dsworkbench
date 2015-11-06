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

import de.tor.tribes.io.DataHolder;

/**
 *
 * @author Torridity
 */
public class RegExpHelper {

    public static String getTroopsPattern(boolean pTrailingSpace, boolean pMilitia) {
        // ([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+)
        StringBuilder b = new StringBuilder();
        if (pTrailingSpace) {
            b.append("(\\s+[0-9]+");
        } else {
            b.append("([0-9]+");
        }
        for (int i = 1; i < DataHolder.getSingleton().getUnits().size(); i++) {
            if (pMilitia || !DataHolder.getSingleton().getUnits().get(i).getPlainName().equals("militia")) {
                b.append("\\s+[0-9]+");
            }
        }
        b.append(")");
        return b.toString();
    }
}
