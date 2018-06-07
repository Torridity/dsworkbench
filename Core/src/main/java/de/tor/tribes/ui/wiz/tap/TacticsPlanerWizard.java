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

import de.tor.tribes.ui.panels.TAPAttackInfoPanel;
import de.tor.tribes.ui.wiz.dep.DefenseFilterPanel;
import de.tor.tribes.ui.wiz.dep.DefenseSourcePanel;
import de.tor.tribes.ui.wiz.ref.SupportRefillCalculationPanel;
import de.tor.tribes.ui.wiz.ref.SupportRefillSourcePanel;
import de.tor.tribes.ui.wiz.ref.SupportRefillTargetPanel;
import de.tor.tribes.ui.wiz.ret.RetimerCalculationPanel;
import de.tor.tribes.ui.wiz.ret.RetimerDataPanel;
import de.tor.tribes.ui.wiz.ret.RetimerFinishPanel;
import de.tor.tribes.ui.wiz.ret.RetimerSourcePanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.api.wizard.WizardResultReceiver;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Torridity
 */
public class TacticsPlanerWizard extends WizardPanelProvider {

    private static JFrame parent = null;

    public TacticsPlanerWizard() {
        super("DS Workbench - Taktikplaner",
                new String[]{TAPWelcomePanel.getStep()},
                new String[]{"Willkommen"});
    }

    @Override
    protected JComponent createPanel(WizardController wc, String string, Map map) {
        if (string.equals(TAPWelcomePanel.getStep())) {
            return TAPWelcomePanel.getSingleton();
        }
        return null;
    }

    public static JFrame getFrame() {
        return parent;
    }

    public static void show() {
        if (parent != null) {
            parent.toFront();
            return;
        }
        parent = new JFrame();
        parent.setTitle("Taktikplaner");
        Wizard wizard = new TacticsPlanerBranchController().createWizard();
        parent.getContentPane().setLayout(new BorderLayout());
        System.setProperty("WizardDisplayer.default", "de.tor.tribes.ui.wiz.AttackWizardDisplayerImpl");
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
        //restore property
        System.setProperty("WizardDisplayer.default", "org.netbeans.api.wizard.displayer.WizardDisplayerImpl");
        TAPAttackInfoPanel.getSingleton().setVisible(false);
        parent.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        parent.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    GlobalOptions.addProperty("tap.width", Integer.toString(parent.getWidth()));
                    GlobalOptions.addProperty("tap.height", Integer.toString(parent.getHeight()));
                } catch (Exception ignored) {
                }
                super.windowClosing(e);
                parent = null;
            }
        });
        parent.pack();

        int w = GlobalOptions.getProperties().getInt("tap.width");
        int h = GlobalOptions.getProperties().getInt("tap.height");

        if (w != 0 && h != 0) {
            parent.setSize(w, h);
        }

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(parent.getRootPane(), "pages.attack_planer", GlobalOptions.getHelpBroker().getHelpSet());
        }       // </editor-fold>

        parent.setVisible(true);
    }

    public static void storeProperties() {
        if (parent != null) {
            GlobalOptions.addProperty("tap.width", Integer.toString(parent.getWidth()));
            GlobalOptions.addProperty("tap.height", Integer.toString(parent.getHeight()));
        }
        AttackSourcePanel.getSingleton().storeProperties();
        AttackSourceFilterPanel.getSingleton().storeProperties();
        AttackTargetPanel.getSingleton().storeProperties();
        AttackTargetFilterPanel.getSingleton().storeProperties();
        TimeSettingsPanel.getSingleton().storeProperties();
        AttackCalculationPanel.getSingleton().storeProperties();
        AttackFinishPanel.getSingleton().storeProperties();
        DefenseSourcePanel.getSingleton().storeProperties();
        DefenseFilterPanel.getSingleton().storeProperties();
        SupportRefillTargetPanel.getSingleton().storeProperties();
        SupportRefillSourcePanel.getSingleton().storeProperties();
        SupportRefillCalculationPanel.getSingleton().storeProperties();
        RetimerDataPanel.getSingleton().storeProperties();
        RetimerSourcePanel.getSingleton().storeProperties();
        RetimerCalculationPanel.getSingleton().storeProperties();
        RetimerFinishPanel.getSingleton().storeProperties();
    }

    public static void restoreProperties() {
        AttackSourcePanel.getSingleton().restoreProperties();
        AttackSourceFilterPanel.getSingleton().restoreProperties();
        AttackTargetPanel.getSingleton().restoreProperties();
        AttackTargetFilterPanel.getSingleton().restoreProperties();
        TimeSettingsPanel.getSingleton().restoreProperties();
        AttackCalculationPanel.getSingleton().restoreProperties();
        AttackFinishPanel.getSingleton().restoreProperties();
        DefenseSourcePanel.getSingleton().restoreProperties();
        DefenseFilterPanel.getSingleton().restoreProperties();
        SupportRefillTargetPanel.getSingleton().restoreProperties();
        SupportRefillSourcePanel.getSingleton().restoreProperties();
        SupportRefillCalculationPanel.getSingleton().restoreProperties();
        RetimerDataPanel.getSingleton().restoreProperties();
        RetimerSourcePanel.getSingleton().restoreProperties();
        RetimerCalculationPanel.getSingleton().restoreProperties();
        RetimerFinishPanel.getSingleton().restoreProperties();
        if (parent != null) {
            parent.dispose();
            parent = null;
        }
    }
}
