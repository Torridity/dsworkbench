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
package de.tor.tribes.ui.wiz.dep;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.TargetInformation;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.generator.ui.SOSGenerator;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.apache.commons.lang3.time.DateUtils;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.api.wizard.WizardResultReceiver;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Torridity
 */
public class DefensePlanerWizard extends WizardPanelProvider {

    private static final String ID_WELCOME = "welcome-id";
    private static final String ID_ANALYSE = "analyse-id";
    private static final String ID_VILLAGES = "villages-id";
    private static final String ID_FILTER = "filter-id";
    private static final String ID_CALCULATION = "calculation-id";
    private static final String ID_FINISH = "finish-id";
    private static JFrame parent = null;

    public DefensePlanerWizard() {
        super("DS Workbench - Verteidigungsplaner",
                new String[]{ID_WELCOME, ID_ANALYSE, ID_VILLAGES, ID_FILTER, ID_CALCULATION, ID_FINISH},
                new String[]{"Willkommen", "Angriffe analysieren", "Unterstützende Dörfer", "Filter", "Berechnung", "Fertigstellung"});
    }

    @Override
    protected JComponent createPanel(WizardController wc, String string, Map map) {
        if (string.equals(ID_WELCOME)) {
            return WelcomePanel.getSingleton();
        } /*else if (string.equals(ID_ANALYSE)) {
            DefenseAnalysePanel.getSingleton().setController(wc);
            return DefenseAnalysePanel.getSingleton();
        } else if (string.equals(ID_VILLAGES)) {
            DefenseSourcePanel.getSingleton().setController(wc);
            return DefenseSourcePanel.getSingleton();
        } else if (string.equals(ID_FILTER)) {
            DefenseFilterPanel.getSingleton().setController(wc);
            return DefenseFilterPanel.getSingleton();
        } else if (string.equals(ID_CALCULATION)) {
            DefenseCalculationSettingsPanel.getSingleton().setController(wc);
            return DefenseCalculationSettingsPanel.getSingleton();
        } else if (string.equals(ID_FINISH)) {
            return DefenseFinishPanel.getSingleton();
        }*/
        return null;
    }

    private static List<SOSRequest> createSampleRequests() {
        int wallLevel = 20;
        int supportCount = 100;
        int maxAttackCount = 50;
        int maxFakeCount = 0;

        List<SOSRequest> result = new LinkedList<>();
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        Village[] attackerVillages = DataHolder.getSingleton().getTribeByName("Alexander25").getVillageList();

        for (int i = 0; i < supportCount; i++) {
            int id = (int) Math.rint(Math.random() * (villages.length - 1));
            Village target = villages[id];
            SOSRequest r = new SOSRequest(target.getTribe());
            r.addTarget(target);
            TargetInformation info = r.getTargetInformation(target);
            info.setWallLevel(wallLevel);

            TroopAmountFixed troops = new TroopAmountFixed();
            troops.setAmountForUnit("spear", (int) Math.rint(Math.random() * 14000));
            troops.setAmountForUnit("sword", (int) Math.rint(Math.random() * 14000));
            troops.setAmountForUnit("heavy", (int) Math.rint(Math.random() * 5000));
            info.setTroops(troops);

            int cnt = (int) Math.rint(maxAttackCount * Math.random());
            for (int j = 0; j < cnt; j++) {
                int idx = (int) Math.rint(Math.random() * (attackerVillages.length - 2));
                Village v = attackerVillages[idx];
                info.addAttack(v, new Date(System.currentTimeMillis() + Math.round(DateUtils.MILLIS_PER_DAY * 7 * Math.random())));
                for (int k = 0; k < (int) Math.rint(maxFakeCount * Math.random()); k++) {
                    idx = (int) Math.rint(Math.random() * (attackerVillages.length - 2));
                    v = attackerVillages[idx];
                    info.addAttack(v, new Date(System.currentTimeMillis() + Math.round(3600 * Math.random())));
                }
            }
            result.add(r);
        }

        return result;
    }

    public static void show() {
        if (parent != null) {
            parent.toFront();
            return;
        }
        
        parent = new JFrame();
        parent.setTitle("Verteidigungsplaner");
        WizardPanelProvider provider = new DefensePlanerWizard();
        Wizard wizard = provider.createWizard();
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
        new SOSGenerator().setVisible(true);
    }
}
