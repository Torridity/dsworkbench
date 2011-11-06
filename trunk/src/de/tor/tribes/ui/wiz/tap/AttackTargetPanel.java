/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.tap;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.views.DSWorkbenchSelectionFrame;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.tag.TagManager;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author Torridity
 */
public class AttackTargetPanel extends AbstractAttackPanel {

    private static final String GENERAL_INFO = "Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Zield&ouml;rfer ausw&auml;hlen, die angegriffen werden sollen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten: <ul> <li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li> <li>Einf&uuml;gen der Zield&ouml;rfer aus der Gruppen&uuml;bersicht</li> <li>Einf&uuml;gen der Zield&ouml;rfer aus dem SOS-Analyzer</li> <li>Einf&uuml;gen der Zield&ouml;rfer aus Berichten</li> <li>Einf&uuml;gen aus der Auswahlübersicht</li> <li>Manuelle Eingabe</li> </ul> </html>";
    private static final String GROUP_INFO = "<html><h2>Datenquelle Gruppenübersicht</h2><br/>Hier k&ouml;nnen gezielt D&ouml;rfer verwendet werden, die sich in bestimmten Gruppen befinden. Die Auswahl der zu verwendenden Gruppe ist im Feld Set/Gruppe/Zeichnung durchzuf&uuml;hren.</html>";
    private static final String SOS_INFO = "<html><h2>Datenquelle SOS-Analyzer</h2><br/>Hier k&ouml;nnen die <b>Herkunfts&ouml;rfer</b> der Angriffe verwendet werden, die momentan im SOS-Analyzer eingetragen sind.</html>";
    private static final String REPORT_INFO = "<html><h2>Datenquelle Berichtdatenbank</h2><br/>Hier k&ouml;nnen die <b>Herkunftsd&ouml;rfer</b> der Berichte verwendet werden, die sich in einem bestimmten Berichtset befinden. Die Auswahl des zu verwendenden Berichtsets ist im Feld Set/Gruppe/Zeichnung durchzuf&uuml;hren.</html>";
    private static final String SELECTION_INFO = "<html><h2>Datenquelle Auswahlübersicht</h2><br/>Hier k&ouml;nnen die D&ouml;rfer verwendet werden, die sich momentan in der Auswahl&uuml;bersicht befinden.</html>";
    private static final String DRAWING_INFO = "<html><h2>Datenquelle Zeichnungen</h2><br/>Hier k&ouml;nnen die D&ouml;rfer verwendet werden, die sich innerhalb einer bestimmten Zeichnung auf der Hauptkarte befinden. Die Auswahl der zu verwendenden Zeichnung ist im Feld Set/Gruppe/Zeichnung durchzuf&uuml;hren.</html>";
    private static final String WORLDDATA_INFO = "<html><h2>Datenquelle Weltdaten</h2><br/>Mit dieser Option k&ouml;nnen D&ouml;rfer ausgehend von den kompletten Weltdaten gewählt werden.</html>";
    private List<Village> villages = new LinkedList<Village>();
    private static AttackTargetPanel singleton = null;

    public static synchronized AbstractAttackPanel getSingleton() {
        if (singleton == null) {
            singleton = new AttackTargetPanel();
        }
        return singleton;
    }

    AttackTargetPanel() {
        super();
        jValidateByTroops.setEnabled(false);
        jUnitList.setEnabled(false);
        jAdaptUnit.setEnabled(false);
    }

    @Override
    public List<Village> getVillages() {
        return villages;
    }

    @Override
    protected void updateSetSelection(Object[] pElements) {
        if (pElements == null) {
            jSetLabel.setEnabled(false);
            jSetBox.setEnabled(false);
            villages.clear();
            if (jSosSource.isSelected()) {
                System.out.println("Not yet implemented");
            } else if (jSelectionSource.isSelected()) {
                for (Village v : DSWorkbenchSelectionFrame.getSingleton().getSelectedElements()) {
                    villages.add(v);
                }
            } else if (jWorlddataSource.isSelected()) {
                for (Integer id : DataHolder.getSingleton().getVillagesById().keySet()) {
                    Village v = DataHolder.getSingleton().getVillagesById().get(id);
                    if (v.getTribe() != null) {
                        villages.add(v);
                    }
                }
                rebuildDataBoxes();
            } else {
                //should be drawing selected but no drawing available
                jSetBox.setModel(new DefaultComboBoxModel(new Object[]{"Nicht verfügbar"}));
            }
        } else {
            jSetLabel.setEnabled(true);
            jSetBox.setEnabled(true);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            if (pElements.length == 0) {
                jSetLabel.setEnabled(false);
                jSetBox.setEnabled(false);
                model.addElement(NO_DATA_AVAILABLE);
            } else {
                jSetLabel.setEnabled(true);
                jSetBox.setEnabled(true);
                model.addElement(ALL_DATA);
                for (Object set : pElements) {
                    model.addElement(set);
                }
            }

            jSetBox.setModel(model);
            jSetBox.setSelectedIndex(0);
        }
    }

    @Override
    protected void updateDataForGroupSource(Object pTag) {
        villages.clear();
        if (pTag != null) {
            if (pTag instanceof String) {
                for (ManageableType element : TagManager.getSingleton().getAllElements()) {
                    for (Integer id : ((Tag) element).getVillageIDs()) {
                        Village village = DataHolder.getSingleton().getVillagesById().get(id);
                        if (!villages.contains(village)) {
                            villages.add(village);
                        }
                    }
                }
            } else if (pTag instanceof Tag) {
                Tag tag = (Tag) pTag;
                for (Integer id : tag.getVillageIDs()) {
                    Village village = DataHolder.getSingleton().getVillagesById().get(id);
                    if (!villages.contains(village)) {
                        villages.add(village);
                    }
                }
            }
        }
        rebuildDataBoxes();
    }

    @Override
    protected void updateDataForReportSource(String pSet) {
        villages.clear();
        if (pSet != null) {
            List<ManageableType> relevantReports = null;

            if (pSet.equals(ALL_DATA)) {
                relevantReports = ReportManager.getSingleton().getAllElements();
            } else {
                relevantReports = ReportManager.getSingleton().getAllElements(pSet);
            }

            for (ManageableType element : relevantReports) {
                FightReport report = (FightReport) element;
                Village source = report.getSourceVillage();
                if (!villages.contains(source)) {
                    villages.add(source);
                }
            }
        }
        rebuildDataBoxes();
    }

    @Override
    protected void updateDataForDrawingSource(Object pDrawing) {
        villages.clear();
        if (pDrawing != null) {
            if (pDrawing instanceof String) {
                for (ManageableType element : FormManager.getSingleton().getAllElements()) {
                    for (Village village : ((AbstractForm) element).getContainedVillages()) {
                        if (!villages.contains(village)) {
                            villages.add(village);
                        }
                    }
                }
            } else if (pDrawing instanceof AbstractForm) {
                AbstractForm drawing = (AbstractForm) pDrawing;
                for (Village village : drawing.getContainedVillages()) {
                    if (!villages.contains(village)) {
                        villages.add(village);
                    }
                }
            }
        }
        rebuildDataBoxes();
    }

    @Override
    public String getGeneralInfo() {
        return GENERAL_INFO;
    }

    @Override
    public String getGroupInfo() {
        return GROUP_INFO;
    }

    @Override
    public String getSosInfo() {
        return SOS_INFO;
    }

    @Override
    public String getReportInfo() {
        return REPORT_INFO;
    }

    @Override
    public String getSelectionInfo() {
        return SELECTION_INFO;
    }

    @Override
    public String getDrawingInfo() {
        return DRAWING_INFO;
    }

    @Override
    public String getWorldDataInfo() {
        return WORLDDATA_INFO;
    }

    public static void main(String[] args) {
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);

        DataHolder.getSingleton().loadData(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }

        JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(500, 400);
        f.getContentPane().add(new AttackTargetPanel());
        f.pack();
        f.setVisible(true);
    }
}
