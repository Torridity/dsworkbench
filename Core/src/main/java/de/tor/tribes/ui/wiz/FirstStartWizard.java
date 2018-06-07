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
package de.tor.tribes.ui.wiz;

import java.util.Map;
import javax.swing.JComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Torridity
 */
public class FirstStartWizard extends WizardPanelProvider {

    private static Logger logger = LogManager.getLogger("Wizard");
    private static final String ID_WELCOME = "welcome-id";
    private static final String ID_NETWORK = "network-id";
    private static final String ID_SERVER = "server-id";
    private static final String ID_FINISH = "finish-id";

    public FirstStartWizard() {
        super("DS Workbench - Erster Start",
                new String[]{ID_WELCOME, ID_NETWORK,  ID_SERVER, ID_FINISH},
                new String[]{"Willkommen", "Netzwerkeinstellungen", "Servereinstellungen", "Fertig"});
    }

    @Override
    protected JComponent createPanel(final WizardController wizardController, String str, final Map map) {
        switch (str) {
            case ID_WELCOME:
                logger.debug("Returning welcome page");
                return new WelcomePage();
            case ID_NETWORK:
                logger.debug("Returning network page");
                return new NetworkSettings(wizardController, map);
            case ID_SERVER:
                logger.debug("Returning server settings page");
                return new ServerSettings(wizardController, map);
            case ID_FINISH:
                logger.debug("Returning finish page");
                return new FinishPage();
        }
        logger.debug("Returning 'null' page");
        return null;
    }

    @Override
    protected Object finish(Map settings) throws WizardException {
        logger.debug("'Finish' action called");
        return settings;
    }
}
