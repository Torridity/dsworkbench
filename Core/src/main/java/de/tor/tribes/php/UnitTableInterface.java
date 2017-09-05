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
package de.tor.tribes.php;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import java.util.Hashtable;

/**
 *
 * @author Torridity
 */
public class UnitTableInterface {

  private static final String[] units = new String[]{"spear", "sword", "axe", "archer", "spy", "light", "marcher", "heavy", "ram", "catapult", "knight", "snob", "militia"};

  public static String createAttackerUnitTableLink(Hashtable<UnitHolder, Integer> pIn, Hashtable<UnitHolder, Integer> pOut) {
    return createUnitTableLink(pIn, pOut, false);
  }

  public static String createAttackerUnitTableLink(Hashtable<UnitHolder, Integer> pIn) {
    return createUnitTableLink(pIn, false);
  }

  public static String createDefenderUnitTableLink(Hashtable<UnitHolder, Integer> pIn, Hashtable<UnitHolder, Integer> pOut) {
    return createUnitTableLink(pIn, pOut, true);
  }

  public static String createDefenderUnitTableLink(Hashtable<UnitHolder, Integer> pIn) {
    return createUnitTableLink(pIn, false);
  }

  public static String createUnitTableLink(Hashtable<UnitHolder, Integer> pIn, Hashtable<UnitHolder, Integer> pOut, boolean pMilitia) {
    StringBuilder b = new StringBuilder();
    b.append("http://torridity.de/tools/unitTable.php?in=");

    for (int i = 0; i < units.length; i++) {
      UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(units[i]);
      if (!u.equals(UnknownUnit.getSingleton()) && (pMilitia || !u.getPlainName().equals("militia"))) {
        Integer amount = pIn.get(u);
        b.append(i).append(".").append((amount == null) ? 0 : amount);
        if (i < units.length - 1) {
          b.append("_");
        }
      }
    }

    b.append("&out=");
    for (int i = 0; i < units.length; i++) {
      UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(units[i]);
      if (!u.equals(UnknownUnit.getSingleton()) && (pMilitia || !u.getPlainName().equals("militia"))) {
        Integer amount = pOut.get(u);
        b.append(i).append(".").append((amount == null) ? 0 : amount);
        b.append("_");
      }
    }
    String result = b.toString();
    return result.substring(0, result.length() - 1);
  }

  public static String createUnitTableLink(Hashtable<UnitHolder, Integer> pIn, boolean pMilitia) {
    StringBuilder b = new StringBuilder();
    b.append("http://torridity.de/tools/unitTable.php?in=");

    for (int i = 0; i < units.length; i++) {
      UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(units[i]);
      if (!u.equals(UnknownUnit.getSingleton()) && (pMilitia || !u.getPlainName().equals("militia"))) {
        Integer amount = pIn.get(u);
        b.append(i).append(".").append((amount == null) ? 0 : amount);
        b.append("_");
      }
    }

    String result = b.toString();
    return result.substring(0, result.length() - 1);
  }
}
