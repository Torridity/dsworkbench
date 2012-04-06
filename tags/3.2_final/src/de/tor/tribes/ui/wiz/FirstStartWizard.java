/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz;

import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardObserver;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Torridity
 */
public class FirstStartWizard extends WizardPanelProvider {

    private static Logger logger = Logger.getLogger("Wizard");
    private static final String ID_WELCOME = "welcome-id";
    private static final String ID_NETWORK = "network-id";
    private static final String ID_ACCOUNT = "account-id";
    private static final String ID_SERVER = "server-id";
    private static final String ID_FINISH = "finish-id";

    public FirstStartWizard() {
        super("DS Workbench - Erster Start",
                new String[]{ID_WELCOME, ID_NETWORK, ID_ACCOUNT, ID_SERVER, ID_FINISH},
                new String[]{"Willkommen", "Netzwerkeinstellungen", "Accounteinstellungen", "Servereinstellungen", "Fertig"});
    }

    @Override
    protected JComponent createPanel(final WizardController wizardController, String str, final Map map) {
        if (str.equals(ID_WELCOME)) {
            logger.debug("Returning welcome page");
            return new WelcomePage();
        } else if (str.equals(ID_NETWORK)) {
            logger.debug("Returning network page");
            return new NetworkSettings(wizardController, map);
        } else if (str.equals(ID_ACCOUNT)) {
            logger.debug("Returning account page");
            return new AccountSettings(wizardController, map);
        } else if (str.equals(ID_SERVER)) {
            logger.debug("Returning server settings page");
            return new ServerSettings(wizardController, map);
        } else if (str.equals(ID_FINISH)) {
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

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.GERMAN);
        WizardPanelProvider provider = new FirstStartWizard();
        
        Wizard wizard = provider.createWizard();
        Object result = WizardDisplayer.showWizard(wizard);
    }
}
