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

import java.util.Comparator;

/**
 *
 * @author Torridity
 */
public class SlashComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        try {
            String s1 = o1.split("/")[0];
            String s2 = o2.split("/")[0];
            return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
        } catch (Exception e) {
            return 0;
        }
    }
}
