/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.tap;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
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
    private static final String ID_FILTER = "filter-id";
    private static final String ID_TARGET = "target-id";
    private static final String ID_TIME = "time-id";
    private static final String ID_VALIDATE = "validate-id";
    private static final String ID_CALCULATION = "calculation-id";
    private static final String ID_FINISH = "finish-id";

    public TacticsPlanerWizard() {
        super("DS Workbench - Taktikplaner",
                new String[]{ID_WELCOME, ID_SOURCE, ID_FILTER, ID_TARGET, ID_TIME, ID_VALIDATE, ID_CALCULATION, ID_FINISH},
                new String[]{"Willkommen", "Herkunft", "Herkunft filtern", "Ziel", "Zeiteinstellungen", "Überprüfung", "Berechnung", "Fertigstellung"});
    }

    @Override
    protected JComponent createPanel(WizardController wc, String string, Map map) {
        if (string.equals(ID_WELCOME)) {
            return WelcomePanel.getSingleton();
        } else if (string.equals(ID_SOURCE)) {
            AttackSourcePanel.getSingleton().setController(wc);
            return AttackSourcePanel.getSingleton();
        } else if (string.equals(ID_FILTER)) {
            AttackSourceFilterPanel.getSingleton().setController(wc);
            return AttackSourceFilterPanel.getSingleton();
        } else if (string.equals(ID_TARGET)) {
            AttackTargetPanel.getSingleton().setController(wc);
            return AttackTargetPanel.getSingleton();
        } else if (string.equals(ID_TIME)) {
            TimeSettingsPanel.getSingleton().setController(wc);
            return TimeSettingsPanel.getSingleton();
        } else if (string.equals(ID_VALIDATE)) {
            ValidationPanel.getSingleton().setController(wc);
            return ValidationPanel.getSingleton();
        } else if (string.equals(ID_CALCULATION)) {
            AttackCalculationPanel.getSingleton().setController(wc);
            return AttackCalculationPanel.getSingleton();
        } else if (string.equals(ID_FINISH)) {
            return AttackFinishPanel.getSingleton();
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }


        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();

        TacticsPlanerWizard provider = new TacticsPlanerWizard();
        Wizard wizard = provider.createWizard();
        System.out.println(WizardDisplayer.showWizard(wizard));
    }
}
