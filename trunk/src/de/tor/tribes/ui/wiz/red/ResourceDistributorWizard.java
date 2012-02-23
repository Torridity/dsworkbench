/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.red;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.api.wizard.WizardResultReceiver;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 * @author Torridity
 */
public class ResourceDistributorWizard extends WizardPanelProvider {

    private static final String ID_WELCOME = ResourceDistributorWelcomePanel.getStep();
    private static JFrame parent = null;

    public ResourceDistributorWizard() {
        super("DS Workbench - Rohstoffverteiler",
                new String[]{ID_WELCOME},
                new String[]{"Willkommen"});
    }

    @Override
    protected JComponent createPanel(WizardController wc, String string, final Map map) {
        if (string.equals(ID_WELCOME)) {
            return ResourceDistributorWelcomePanel.getSingleton();
        }
        return null;
    }

    public static void storeProperties() {
        ResourceDistributorSettingsPanel.getSingleton().storeProperties();
        ResourceDistributorCalculationPanel.getSingleton().storeProperties();
        ResourceDistributorFinishPanel.getSingleton().storeProperties();
    }

    public static void restoreProperties() {
        ResourceDistributorSettingsPanel.getSingleton().restoreProperties();
        ResourceDistributorFinishPanel.getSingleton().restoreProperties();
    }

    public static void show() {
        if (parent != null) {
            parent.toFront();
            return;
        }
        parent = new JFrame();
        parent.setTitle("Rohstoffverteiler");
        Wizard wizard = new ResourceDistributorBranchController().createWizard();
        parent.getContentPane().setLayout(new BorderLayout());
        WizardDisplayer.installInContainer(parent, BorderLayout.CENTER, wizard, null, null, new WizardResultReceiver() {

            @Override
            public void finished(Object o) {
                parent.dispose();
                parent = null;
            }

            @Override
            public void cancelled(Map map) {
                parent.dispose();
                parent = null;
            }
        });
        parent.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        parent.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                parent = null;
            }
        });
        parent.pack();
        parent.setVisible(true);
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

        //  Wizard wizard = new ResourceDistributorBranchController().createWizard();
        new ResourceDistributorWizard().show();
        //  System.out.println("RES: " + WizardDisplayer.showWizard(wizard));
    }
}
