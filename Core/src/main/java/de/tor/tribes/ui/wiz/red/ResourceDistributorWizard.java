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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.Constants;
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
        if (parent != null) {
            GlobalOptions.addProperty("red.width", Integer.toString(parent.getWidth()));
            GlobalOptions.addProperty("red.height", Integer.toString(parent.getHeight()));
        }
        ResourceDistributorSettingsPanel.getSingleton().storeProperties();
        ResourceDistributorCalculationPanel.getSingleton().storeProperties();
        ResourceDistributorFinishPanel.getSingleton().storeProperties();
    }

    public static void restoreProperties() {
        ResourceDistributorSettingsPanel.getSingleton().restoreProperties();
        ResourceDistributorCalculationPanel.getSingleton().restoreProperties();
        ResourceDistributorFinishPanel.getSingleton().restoreProperties();
        if (parent != null) {
            parent.dispose();
            parent = null;
        }
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
                try {
                    GlobalOptions.addProperty("red.width", Integer.toString(parent.getWidth()));
                    GlobalOptions.addProperty("red.height", Integer.toString(parent.getHeight()));
                } catch (Exception ex) {
                }
                super.windowClosing(e);
                parent = null;
            }
        });
        parent.pack();
        int w = GlobalOptions.getProperties().getInt("red.width", 0);
        int h = GlobalOptions.getProperties().getInt("red.height", 0);
        if (w != 0 && h != 0) {
            parent.setSize(w, h);
        }

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(parent.getRootPane(), "pages.merchant_distributor", GlobalOptions.getHelpBroker().getHelpSet());
        }       // </editor-fold>

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
