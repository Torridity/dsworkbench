/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.dep;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.apache.commons.lang.time.DateUtils;
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
public class DefensePlanerWizard extends WizardPanelProvider {

    private static final String ID_WELCOME = "welcome-id";
    private static final String ID_ANALYSE = "analyse-id";
    private static final String ID_VILLAGES = "villages-id";
    private static final String ID_FILTER = "filter-id";
    private static final String ID_CALCULATION = "calculation-id";
    private static final String ID_FINISH = "finish-id";

    public DefensePlanerWizard() {
        super("DS Workbench - Verteidigungsplaner",
                new String[]{ID_WELCOME, ID_ANALYSE, ID_VILLAGES, ID_FILTER, ID_CALCULATION, ID_FINISH},
                new String[]{"Willkommen", "Angriffe analysieren", "Verwendete Dörfer", "Filter", "Berechnung", "Fertigstellung"});
    }

    @Override
    protected JComponent createPanel(WizardController wc, String string, Map map) {
        if (string.equals(ID_WELCOME)) {
            return WelcomePanel.getSingleton();
        } else if (string.equals(ID_ANALYSE)) {
            AnalysePanel.getSingleton().setController(wc);
            return AnalysePanel.getSingleton();
        } else if (string.equals(ID_VILLAGES)) {
            VillagePanel.getSingleton().setController(wc);
            return VillagePanel.getSingleton();
        } else if (string.equals(ID_FILTER)) {
            FilterPanel.getSingleton().setController(wc);
            return FilterPanel.getSingleton();
        } else if (string.equals(ID_CALCULATION)) {
            FinalSettingsPanel.getSingleton().setController(wc);
            return FinalSettingsPanel.getSingleton();
        } else if (string.equals(ID_FINISH)) {
            FinishPanel.getSingleton().setController(wc);
            return FinishPanel.getSingleton();
        }
        return null;
    }

    private static List<SOSRequest> createSampleRequests() {
        int wallLevel = 20;
        int supportCount = 50;
        int maxAttackCount = 10;
        int maxFakeCount = 0;

        List<SOSRequest> result = new LinkedList<SOSRequest>();
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();

        for (int i = 0; i < supportCount; i++) {
            int id = (int) Math.rint(Math.random() * (villages.length - 1));
            Village target = villages[id];
            SOSRequest r = new SOSRequest(target.getTribe());
            r.addTarget(target);
            SOSRequest.TargetInformation info = r.getTargetInformation(target);
            info.setWallLevel(wallLevel);

            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("spear"), (int) Math.rint(Math.random() * 14000));
            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("sword"), (int) Math.rint(Math.random() * 14000));
            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("heavy"), (int) Math.rint(Math.random() * 5000));

            int cnt = (int) Math.rint(maxAttackCount * Math.random());
            for (int j = 0; j < cnt; j++) {
                info.addAttack(DataHolder.getSingleton().getRandomVillageWithOwner(), new Date(System.currentTimeMillis() + Math.round(DateUtils.MILLIS_PER_DAY * 7 * Math.random())));
                for (int k = 0; k < (int) Math.rint(maxFakeCount * Math.random()); k++) {
                    info.addAttack(DataHolder.getSingleton().getRandomVillageWithOwner(), new Date(System.currentTimeMillis() + Math.round(3600 * Math.random())));
                }
            }
            result.add(r);
        }

        return result;
    }

    public static void main(String[] args) {

        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de47");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de47")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();
        WizardPanelProvider provider = new DefensePlanerWizard();
        Wizard wizard = provider.createWizard();
        AnalysePanel.getSingleton().setData(createSampleRequests());
        /* final JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        WizardDisplayer.installInContainer(p, BorderLayout.CENTER, wizard, null, null, new WizardResultReceiver() {
        
        @Override
        public void finished(Object o) {
        System.out.println(o);
        }
        
        @Override
        public void cancelled(Map map) {
        System.out.println("Cancel: " + map);
        f.dispose();
        }
        });
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);*/

        WizardDisplayer.showWizard(wizard);
    }
}
