/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.tap;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Torridity
 */
public class TacticsPlanerWizard extends WizardPanelProvider {

    private static final String ID_WELCOME = "welcome-id";
    private static final String ID_SOURCE = "source-id";
    private static final String ID_TARGET = "target-id";

    public TacticsPlanerWizard() {
        super("DS Workbench - Taktikplaner",
                new String[]{ID_WELCOME, ID_SOURCE, ID_TARGET},
                new String[]{"Willkommen", "Herkunft", "Ziel"});
    }

    @Override
    protected JComponent createPanel(WizardController wc, String string, Map map) {
        if (string.equals(ID_WELCOME)) {
            return WelcomePanel.getSingleton();
        } else if (string.equals(ID_SOURCE)) {
            return SourcePanel.getSingleton();
        } else if (string.equals(ID_TARGET)) {
            return TargetPanel.getSingleton();
        }
        return null;
    }

    public static void main(String[] args) {
        WizardPanelProvider provider = new TacticsPlanerWizard();
        Wizard wizard = provider.createWizard();
        System.out.println(WizardDisplayer.showWizard(wizard));
    }
}
