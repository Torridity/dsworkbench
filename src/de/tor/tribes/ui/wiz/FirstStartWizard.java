/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Torridity
 */
public class FirstStartWizard extends WizardPanelProvider {

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
            return new WelcomePage();
        } else if (str.equals(ID_NETWORK)) {
            return new NetworkSettings(wizardController, map);
        } else if (str.equals(ID_ACCOUNT)) {
            return new AccountSettings(wizardController, map);
        } else if (str.equals(ID_SERVER)) {
            return new ServerSettings(wizardController, map);
        } else if (str.equals(ID_FINISH)) {
            return new FinishPage();
        }

        return null;
    }

    @Override
    protected Object finish(Map settings) throws WizardException {
        return settings;
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.GERMAN);
        WizardPanelProvider provider = new FirstStartWizard();
        Wizard wizard = provider.createWizard();
        Object result = WizardDisplayer.showWizard(wizard);
    }
}
