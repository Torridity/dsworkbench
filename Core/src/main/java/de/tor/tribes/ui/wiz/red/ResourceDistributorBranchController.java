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
package de.tor.tribes.ui.wiz.red;

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
public class ResourceDistributorBranchController extends WizardBranchController {

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
    private static final Wizard BALANCE_TYPE_PATH = WizardPage.createWizard(new WizardPage[]{ResourceDistributorDataReadPanel.getSingleton(), ResourceDistributorCalculationPanel.getSingleton(), ResourceDistributorFinishPanel.getSingleton()}, ERGEBNIS);
    private static final Wizard FILL_TYPE_PATH = WizardPage.createWizard(new WizardPage[]{ResourceDistributorDataReadPanel.getSingleton(), ResourceDistributorSettingsPanel.getSingleton(), ResourceDistributorCalculationPanel.getSingleton(), ResourceDistributorFinishPanel.getSingleton()}, ERGEBNIS);
    private static final Wizard DATA_LOAD_PATH = WizardPage.createWizard(new WizardPage[]{ResourceDistributorFinishPanel.getSingleton()});

    public ResourceDistributorBranchController() {
        super(new ResourceDistributorWizard());
    }

    @Override
    protected Wizard getWizardForStep(String step, Map settings) {
        if (ResourceDistributorWelcomePanel.getStep().equals(step)) {
            if (ResourceDistributorWelcomePanel.BALANCE_DISTRIBUTION.equals(settings.get(ResourceDistributorWelcomePanel.TYPE))) {
                return BALANCE_TYPE_PATH;
            } else if (ResourceDistributorWelcomePanel.FILL_DISTRIBUTION.equals(settings.get(ResourceDistributorWelcomePanel.TYPE))) {
                return FILL_TYPE_PATH;
            } else if (ResourceDistributorWelcomePanel.LOAD_DISTRIBUTION.equals(settings.get(ResourceDistributorWelcomePanel.TYPE))) {
                return DATA_LOAD_PATH;
            }
        }
        return null;
    }
}
