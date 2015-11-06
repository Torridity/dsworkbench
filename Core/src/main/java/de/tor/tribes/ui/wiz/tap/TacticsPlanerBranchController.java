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
package de.tor.tribes.ui.wiz.tap;

import de.tor.tribes.ui.wiz.dep.DefenseAnalysePanel;
import de.tor.tribes.ui.wiz.dep.DefenseCalculationSettingsPanel;
import de.tor.tribes.ui.wiz.dep.DefenseFilterPanel;
import de.tor.tribes.ui.wiz.dep.DefenseFinishPanel;
import de.tor.tribes.ui.wiz.dep.DefenseSourcePanel;
import de.tor.tribes.ui.wiz.ref.*;
import de.tor.tribes.ui.wiz.ret.RetimerFinishPanel;
import de.tor.tribes.ui.wiz.ret.RetimerCalculationPanel;
import de.tor.tribes.ui.wiz.ret.RetimerDataPanel;
import de.tor.tribes.ui.wiz.ret.RetimerSourceFilterPanel;
import de.tor.tribes.ui.wiz.ret.RetimerSourcePanel;
import java.util.Map;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

/**
 *
 * @author Torridity
 */
public class TacticsPlanerBranchController extends WizardBranchController {

    private static final WizardResultProducer ERGEBNIS = new WizardResultProducer() {

        @Override
        public Object finish(Map wizardData) throws WizardException {
            return wizardData;
        }

        @Override
        public boolean cancel(Map settings) {
            return true;
        }
    };
    private static final Wizard ATTACK_PATH = WizardPage.createWizard(new WizardPage[]{AttackSourcePanel.getSingleton(), AttackSourceFilterPanel.getSingleton(), AttackTargetPanel.getSingleton(), AttackTargetFilterPanel.getSingleton(), TimeSettingsPanel.getSingleton(), ValidationPanel.getSingleton(), AttackCalculationPanel.getSingleton(), AttackFinishPanel.getSingleton()}, ERGEBNIS);
    private static final Wizard RETIME_PATH = WizardPage.createWizard(new WizardPage[]{RetimerDataPanel.getSingleton(), RetimerSourcePanel.getSingleton(), RetimerSourceFilterPanel.getSingleton(), RetimerCalculationPanel.getSingleton(),  RetimerFinishPanel.getSingleton()}, ERGEBNIS);
    private static final Wizard DEFENSE_PATH = WizardPage.createWizard(new WizardPage[]{DefenseAnalysePanel.getSingleton(), DefenseSourcePanel.getSingleton(), DefenseFilterPanel.getSingleton(), DefenseCalculationSettingsPanel.getSingleton(), DefenseFinishPanel.getSingleton()}, ERGEBNIS);
    private static final Wizard REFILL_PATH = WizardPage.createWizard(new WizardPage[]{SupportRefillTargetPanel.getSingleton(), SupportRefillSettingsPanel.getSingleton(), SupportRefillSourcePanel.getSingleton(), SupportRefillCalculationPanel.getSingleton(), SupportRefillFinishPanel.getSingleton()}, ERGEBNIS);

    public TacticsPlanerBranchController() {
        super(new TacticsPlanerWizard());
    }

    @Override
    protected Wizard getWizardForStep(String step, Map settings) {
        if (TAPWelcomePanel.getStep().equals(step)) {
            if (TAPWelcomePanel.ATTACK_TYPE.equals(settings.get(TAPWelcomePanel.TYPE))) {
                return ATTACK_PATH;
            } else if (TAPWelcomePanel.DEFENSE_TYPE.equals(settings.get(TAPWelcomePanel.TYPE))) {
                return DEFENSE_PATH;
            } else if (TAPWelcomePanel.REFILL_TYPE.equals(settings.get(TAPWelcomePanel.TYPE))) {
                return REFILL_PATH;
            } else if (TAPWelcomePanel.RETIME_TYPE.equals(settings.get(TAPWelcomePanel.TYPE))) {
                return RETIME_PATH;
            }
        }
        return null;
    }
}
