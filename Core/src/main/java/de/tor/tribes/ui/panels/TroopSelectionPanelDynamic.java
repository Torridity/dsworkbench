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
import de.tor.tribes.io.TroopAmountDynamic;
import de.tor.tribes.io.TroopAmountElement;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.StandardAttack;
import java.awt.Point;

/**
 *
 * @author extremeCrazyCoder
 */
public class TroopSelectionPanelDynamic extends TroopSelectionPanel<TroopAmountDynamic> {
    @Override
    public TroopAmountDynamic getAmounts() {
        TroopAmountDynamic values = new TroopAmountDynamic(0);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            values.setAmount(this.getAmountForUnit(unit));
        }
        return values;
    }

    private TroopAmountElement getAmountForUnit(UnitHolder pUnit) {
        LabeledTextField field = getFieldForUnit(pUnit);
        if(field != null) {
            return new TroopAmountElement(pUnit, field.getText());
        }
        return new TroopAmountElement(pUnit, "0");
    }

    @Override
    public void setAmounts(TroopAmountDynamic pAmounts) {
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            setAmountForUnit(unit, pAmounts.getElementForUnit(unit).toString());
        }
    }


    public void setAmountForUnit(UnitHolder pUnit, String pValue) {
        LabeledTextField field = getFieldForUnit(pUnit);
        if (field != null) {
            field.setText(pValue);
        }
    }

    @Override
    protected void loadFromStandardAttack() {
        StandardAttack att = getSelectedAttack();
        if (att == null) {
            return;
        }

        setAmounts(att.getTroops());
    }
}
