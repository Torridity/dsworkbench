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
package de.tor.tribes.ui.panels;

import com.jidesoft.swing.LabeledTextField;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.UIHelper;

/**
 *
 * @author extremeCrazyCoder
 */
public class TroopSelectionPanelFixed extends TroopSelectionPanel<TroopAmountFixed> {
    @Override
    public TroopAmountFixed getAmounts() {
        TroopAmountFixed values = new TroopAmountFixed(0);
        for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
            values.setAmountForUnit(unit, this.getAmountForUnit(unit));
        }
        return values;
    }

    private int getAmountForUnit(UnitHolder pUnit) {
        LabeledTextField field = getFieldForUnit(pUnit);
        if(field != null) {
            return UIHelper.parseIntFromField(field);
        }
        return 0;
    }

    @Override
    public void setAmounts(TroopAmountFixed pAmounts) {
        for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
            setAmountForUnit(unit, pAmounts.getAmountForUnit(unit));
        }
    }


    public void setAmountForUnit(UnitHolder pUnit, int pValue) {
        LabeledTextField field = getFieldForUnit(pUnit);
        if (field != null) {
            field.setText(Integer.toString(pValue));
        }
    }

    @Override
    protected void loadFromStandardAttack() {
        StandardAttack att = getSelectedAttack();
        if (att == null) {
            return;
        }

        if (att.getTroops().isFixed()) {
            setAmounts(att.getTroops().transformToFixed(null));
        } else {
            JOptionPaneHelper.showInformationBox(this, "Der gewählte Standardangriff enthält dynamische Werte,\n"
                    + "die abhängig von der Truppenzahl eines Dorfes bestimmt werden.\n"
                    + "Er kann daher hier nicht verwendet werden.", "Information");
        }
    }
}
