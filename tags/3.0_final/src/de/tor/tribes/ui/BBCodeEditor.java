/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BBCodeEditor.java
 *
 * Created on Apr 27, 2011, 8:04:03 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.AllyStatResult;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.test.DummyUnit;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.OverallStatResult;
import de.tor.tribes.types.Rectangle;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.SingleAttackerStat;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TribeStatResult;
import de.tor.tribes.types.TribeStatsElement;
import de.tor.tribes.types.TribeStatsElement.Stats;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BBChangeListener;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.bb.AllyReportStatsFormatter;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.bb.BasicFormatter;
import de.tor.tribes.util.bb.DefStatsFormatter;
import de.tor.tribes.util.bb.FormListFormatter;
import de.tor.tribes.util.bb.KillStatsFormatter;
import de.tor.tribes.util.bb.NoteListFormatter;
import de.tor.tribes.util.bb.OverallReportStatsFormatter;
import de.tor.tribes.util.bb.PointStatsFormatter;
import de.tor.tribes.util.bb.ReportListFormatter;
import de.tor.tribes.util.bb.SosListFormatter;
import de.tor.tribes.util.bb.TagListFormatter;
import de.tor.tribes.util.bb.TribeReportStatsFormatter;
import de.tor.tribes.util.bb.TroopListFormatter;
import de.tor.tribes.util.bb.VillageListFormatter;
import de.tor.tribes.util.bb.WinnerLoserStatsFormatter;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.UIManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class BBCodeEditor extends javax.swing.JDialog {

    private final List<Attack> sampleAttacks = new LinkedList<Attack>();
    private final List<Village> sampleVillages = new LinkedList<Village>();
    private List<FightReport> sampleReports = new LinkedList<FightReport>();
    private List<SOSRequest> sampleRequests = new LinkedList<SOSRequest>();
    private List<Tag> sampleTags = new LinkedList<Tag>();
    private List<Note> sampleNotes = new LinkedList<Note>();
    private List<Stats> sampleStats = new LinkedList<Stats>();
    private List<VillageTroopsHolder> sampleTroops = new LinkedList<VillageTroopsHolder>();
    private List<AbstractForm> sampleForms = new LinkedList<AbstractForm>();
    private List<OverallStatResult> sampleOverallResult = new LinkedList<OverallStatResult>();
    private List<AllyStatResult> sampleAllyResult = new LinkedList<AllyStatResult>();
    private List<TribeStatResult> sampleTribeResult = new LinkedList<TribeStatResult>();
    private BasicFormatter element = null;
    private BasicFormatter<Attack> attackFormatter = new AttackListFormatter();
    private BasicFormatter<Village> villageFormatter = new VillageListFormatter();
    private BasicFormatter<FightReport> reportFormatter = new ReportListFormatter();
    private BasicFormatter<SOSRequest> sosFormatter = new SosListFormatter();
    private BasicFormatter<Note> noteFormatter = new NoteListFormatter();
    private BasicFormatter<Tag> tagFormatter = new TagListFormatter();
    private BasicFormatter<Stats> pointStatsFormatter = new PointStatsFormatter();
    private BasicFormatter<Stats> offStatsFormatter = new KillStatsFormatter();
    private BasicFormatter<Stats> defStatsFormatter = new DefStatsFormatter();
    private BasicFormatter<Stats> winnerLoserStatsFormatter = new WinnerLoserStatsFormatter();
    private BasicFormatter<VillageTroopsHolder> troopsFormatter = new TroopListFormatter();
    private BasicFormatter<AbstractForm> formFormatter = new FormListFormatter();
    private BasicFormatter<OverallStatResult> overallStatFormatter = new OverallReportStatsFormatter();
    private BasicFormatter<AllyStatResult> allyStatFormatter = new AllyReportStatsFormatter();
    private BasicFormatter<TribeStatResult> tribeStatFormatter = new TribeReportStatsFormatter();
    private static BBCodeEditor SINGLETON = null;

    public static synchronized BBCodeEditor getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new BBCodeEditor();
        }
        return SINGLETON;
    }

    /** Creates new form BBCodeEditor */
    BBCodeEditor() {
        super();
        setModal(true);
        initComponents();
        buildSampleData();
        jTextPane1.setBackground(Constants.DS_BACK_LIGHT);

        element = attackFormatter;

        bBPanel2.setBBChangeListener(new BBChangeListener() {

            @Override
            public void fireBBChangedEvent() {
                updatePreview();
            }
        });

        fireExportTypeChangedEvent(new ItemEvent(jApplyButton, 0, null, ItemEvent.SELECTED));
    }

    public void reset() {
        buildSampleData();
        updatePreview();
    }

    private void buildSampleData() {
        sampleAttacks.clear();
        sampleVillages.clear();
        sampleReports.clear();
        sampleRequests.clear();
        sampleTags.clear();
        sampleNotes.clear();
        sampleStats.clear();
        sampleTroops.clear();
        sampleForms.clear();
        //sample village
        Village sampleVillage1 = DataHolder.getSingleton().getRandomVillageWithOwner();
        //sample attack
        Village sampleVillage2 = DataHolder.getSingleton().getRandomVillageWithOwner();
        Village sampleVillage3 = DataHolder.getSingleton().getRandomVillageWithOwner();
        Village sampleVillage4 = DataHolder.getSingleton().getRandomVillageWithOwner();
        Village sampleVillage5 = DataHolder.getSingleton().getRandomVillageWithOwner();
        sampleVillages.add(sampleVillage2);
        sampleVillages.add(sampleVillage3);
        sampleVillages.add(sampleVillage4);
        sampleVillages.add(sampleVillage5);
        Attack sampleAttack = new Attack();
        sampleAttack.setSource(sampleVillage2);
        sampleAttack.setTarget(sampleVillage3);
        sampleAttack.setArriveTime(new Date());
        sampleAttack.setType(Attack.CLEAN_TYPE);
        sampleAttack.setUnit(new DummyUnit());
        Attack sampleAttack2 = new Attack();
        sampleAttack2.setSource(sampleVillage2);
        sampleAttack2.setTarget(sampleVillage3);
        sampleAttack2.setArriveTime(new Date());
        sampleAttack2.setType(Attack.CLEAN_TYPE);
        sampleAttack2.setUnit(new DummyUnit());
        sampleAttacks.add(sampleAttack);
        sampleAttacks.add(sampleAttack2);
        //sample note
        Note sampleNote = new Note();
        sampleNote.setNoteSymbol(ImageManager.NOTE_SYMBOL_BALL_BLUE);
        sampleNote.setTimestamp(System.currentTimeMillis());
        sampleNote.addVillage(sampleVillage1);
        sampleNote.addVillage(sampleVillage4);
        sampleNote.addVillage(sampleVillage5);
        sampleNote.setNoteText("[u]Dies[/u] ist eine [b]Beispielnotiz[/b]");
        Note sampleNote2 = new Note();
        sampleNote2.setNoteSymbol(ImageManager.NOTE_SYMBOL_BALL_RED);
        sampleNote2.setTimestamp(System.currentTimeMillis());
        sampleNote2.addVillage(sampleVillage4);
        sampleNote2.addVillage(sampleVillage5);
        sampleNote2.setNoteText("[u]Dies[/u] ist eine weitere [b]Beispielnotiz[/b]");
        sampleNotes.add(sampleNote);
        sampleNotes.add(sampleNote2);
        //sample SOS request
        SOSRequest sampleSOSRequest = new SOSRequest();
        sampleSOSRequest.setDefender(Barbarians.getSingleton());
        sampleSOSRequest.addTarget(sampleVillage4);
        sampleSOSRequest.getTargetInformation(sampleVillage4).setWallLevel(20);
        sampleSOSRequest.getTargetInformation(sampleVillage4).addAttack(sampleVillage2, new Date());
        sampleSOSRequest.getTargetInformation(sampleVillage4).addAttack(sampleVillage2, new Date());
        sampleSOSRequest.getTargetInformation(sampleVillage4).addTroopInformation(DataHolder.getSingleton().getRandomUnit(), 300);
        sampleSOSRequest.addTarget(sampleVillage5);
        sampleSOSRequest.getTargetInformation(sampleVillage5).setWallLevel(10);
        sampleSOSRequest.getTargetInformation(sampleVillage5).addAttack(sampleVillage2, new Date());
        sampleSOSRequest.getTargetInformation(sampleVillage5).addAttack(sampleVillage2, new Date());
        sampleSOSRequest.getTargetInformation(sampleVillage5).addTroopInformation(DataHolder.getSingleton().getRandomUnit(), 100);
        sampleRequests.add(sampleSOSRequest);
        //sampleRequests.add(sampleSOSRequest2);
        //sample report
        FightReport sampleReport = new FightReport();
        sampleReport.setAimedBuilding("Wall");
        sampleReport.setAttacker(sampleVillage2.getTribe());
        sampleReport.setConquered(false);
        sampleReport.setDefender(sampleVillage3.getTribe());
        sampleReport.setLuck(0d);
        sampleReport.setMoral(100d);
        sampleReport.setSourceVillage(sampleVillage2);
        sampleReport.setTargetVillage(sampleVillage3);
        sampleReport.setWallAfter((byte) 15);
        sampleReport.setWallBefore((byte) 20);
        FightReport sampleReport2 = new FightReport();
        sampleReport2.setAcceptanceAfter((byte) 70);
        sampleReport2.setAcceptanceBefore((byte) 100);
        sampleReport2.setAimedBuilding("Wall");
        sampleReport2.setAttacker(sampleVillage2.getTribe());
        sampleReport2.setConquered(false);
        sampleReport2.setDefender(sampleVillage3.getTribe());
        sampleReport2.setLuck(0d);
        sampleReport2.setMoral(100d);
        sampleReport2.setSourceVillage(sampleVillage2);
        sampleReport2.setTargetVillage(sampleVillage3);
        sampleReport2.setWallAfter((byte) 15);
        sampleReport2.setWallBefore((byte) 20);
        sampleReports.add(sampleReport);
        sampleReports.add(sampleReport2);
        Tag t = new Tag("Meine Gruppe", false);
        t.setTagIcon(0);
        t.setTagColor(Color.RED);
        Tag t2 = new Tag("Gruppe2", false);
        t2.setTagIcon(1);
        t2.setTagColor(Color.BLUE);
        Tag t3 = new Tag("Noch eine Gruppe", false);
        t3.setTagIcon(5);
        t3.setTagColor(Color.GREEN);
        sampleTags.add(t);
        sampleTags.add(t2);
        sampleTags.add(t3);
        t.tagVillage(sampleVillage2.getId());
        t.tagVillage(sampleVillage3.getId());
        t2.tagVillage(sampleVillage4.getId());
        t3.tagVillage(sampleVillage5.getId());
        //sample troops
        VillageTroopsHolder h = new VillageTroopsHolder(sampleVillage1, new Date());
        Hashtable<UnitHolder, Integer> troops = new Hashtable<UnitHolder, Integer>();
        troops.put(DataHolder.getSingleton().getUnitByPlainName("axe"), 6600);
        troops.put(DataHolder.getSingleton().getUnitByPlainName("light"), 2200);
        troops.put(DataHolder.getSingleton().getUnitByPlainName("ram"), 300);
        troops.put(DataHolder.getSingleton().getUnitByPlainName("snob"), 2);
        h.setTroops(troops);
        VillageTroopsHolder h2 = new VillageTroopsHolder(sampleVillage3, new Date());
        troops.put(DataHolder.getSingleton().getUnitByPlainName("axe"), 5500);
        troops.put(DataHolder.getSingleton().getUnitByPlainName("light"), 2000);
        troops.put(DataHolder.getSingleton().getUnitByPlainName("marcher"), 300);
        troops.put(DataHolder.getSingleton().getUnitByPlainName("ram"), 240);
        h2.setTroops(troops);
        sampleTroops.add(h);
        sampleTroops.add(h2);
        //build stats
        TribeStatsElement e1 = new TribeStatsElement(sampleVillage2.getTribe());
        e1.addRandomSnapshots();
        TribeStatsElement e2 = new TribeStatsElement(sampleVillage3.getTribe());
        e2.addRandomSnapshots();
        sampleStats.add(e1.generateStats(e1.getTimestamps()[0], e1.getTimestamps()[1]));
        sampleStats.add(e2.generateStats(e2.getTimestamps()[0], e2.getTimestamps()[1]));
        //build form
        Rectangle r = new Rectangle();
        r.setFormName("Beispielzeichnung");
        r.setDrawColor(Color.BLUE);
        r.setXPos(500);
        r.setYPos(500);
        r.setXPosEnd(505);
        r.setYPosEnd(505);
        sampleForms.add(r);
        //build report stats

        SingleAttackerStat tribeStat1 = SingleAttackerStat.createRandomElement(sampleVillage1.getTribe());
        SingleAttackerStat tribeStat2 = SingleAttackerStat.createRandomElement(sampleVillage2.getTribe());
        TribeStatResult tribeResult1 = new TribeStatResult();
        tribeResult1.setTribeStats(tribeStat1, true);
        TribeStatResult tribeResult2 = new TribeStatResult();
        tribeResult2.setTribeStats(tribeStat2, true);

        AllyStatResult allyStat1 = new AllyStatResult();
        allyStat1.setAlly((sampleVillage1.getTribe() != null && sampleVillage1.getTribe().getAlly() != null) ? sampleVillage1.getTribe().getAlly() : NoAlly.getSingleton());
        allyStat1.addTribeStatResult(tribeResult1);

        AllyStatResult allyStat2 = new AllyStatResult();
        allyStat2.setAlly((sampleVillage2.getTribe() != null && sampleVillage2.getTribe().getAlly() != null) ? sampleVillage2.getTribe().getAlly() : NoAlly.getSingleton());
        allyStat2.addTribeStatResult(tribeResult2);

        OverallStatResult overallResult = new OverallStatResult();
        overallResult.addAllyStatsResult(allyStat1);
        overallResult.addAllyStatsResult(allyStat2);
        overallResult.setDefenders(10);
        overallResult.setAttackerAllies(2);
        overallResult.setDefenderAllies(1);
        overallResult.setReportCount(100);
        overallResult.setStartDate(new Date());
        overallResult.setEndDate(new Date());

        allyStat1.setOverallKills(overallResult.getKills());
        allyStat1.setOverallLosses(overallResult.getLosses());
        allyStat2.setOverallKills(overallResult.getKills());
        allyStat2.setOverallLosses(overallResult.getLosses());
        tribeResult1.setAllyKills(allyStat1.getKills());
        tribeResult1.setAllyLosses(allyStat1.getLosses());
        tribeResult1.setOverallKills(allyStat1.getOverallKills());
        tribeResult1.setOverallLosses(allyStat1.getOverallLosses());

        tribeResult2.setAllyKills(allyStat2.getKills());
        tribeResult2.setAllyLosses(allyStat2.getLosses());
        tribeResult2.setOverallKills(allyStat2.getOverallKills());
        tribeResult2.setOverallLosses(allyStat2.getOverallLosses());


        sampleTribeResult.add(tribeResult1);
        sampleTribeResult.add(tribeResult2);
        sampleAllyResult.add(allyStat1);
        sampleAllyResult.add(allyStat2);
        sampleOverallResult.add(overallResult);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jApplyButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jBBPanel = new javax.swing.JPanel();
        bBPanel2 = new de.tor.tribes.ui.BBPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jVarsList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setTitle("BB-Code Editor");
        setModal(true);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Vorschau"));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(33, 150));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(33, 150));

        jTextPane1.setContentType("text/html");
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jApplyButton.setText("Schließen");
        jApplyButton.setToolTipText("Beendet den BB-Editor. Änderungen werden während dem Editiervorgang automatisch gespeichert.");
        jApplyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireApplyBBTemplatesEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        getContentPane().add(jApplyButton, gridBagConstraints);

        jButton2.setText("Standard wiederherstellen");
        jButton2.setToolTipText("Stellt den Standardwert für das gewählte Template her");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireResetEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 5);
        getContentPane().add(jButton2, gridBagConstraints);

        jBBPanel.setBackground(new java.awt.Color(239, 235, 223));
        jBBPanel.setPreferredSize(new java.awt.Dimension(363, 251));
        jBBPanel.setLayout(new java.awt.BorderLayout());

        bBPanel2.setMinimumSize(new java.awt.Dimension(363, 150));
        bBPanel2.setPreferredSize(new java.awt.Dimension(363, 251));
        jBBPanel.add(bBPanel2, java.awt.BorderLayout.CENTER);

        infoPanel.setAnimated(false);
        infoPanel.setCollapsed(true);
        infoPanel.setDirection(org.jdesktop.swingx.JXCollapsiblePane.Direction.LEFT);
        infoPanel.setInheritAlpha(false);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Variablen"));

        jVarsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jVarsList);

        infoPanel.add(jScrollPane2, java.awt.BorderLayout.PAGE_START);

        jBBPanel.add(infoPanel, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jBBPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.BorderLayout(5, 0));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Angriffe", "Notizen", "Dorflisten", "SOS-Anfragen", "Kampfbericht", "Gruppen", "Truppen", "Statistik (Punkte)", "Statistik (Angriff)", "Statistik (Verteidigung)", "Statistik (Gewinner/Verlierer)", "Zeichnungen", "Berichtauswertung (Zusammenfassung)", "Berichtauswertung (Stämme)", "Berichtauswertung (Spieler)" }));
        jComboBox1.setMinimumSize(new java.awt.Dimension(66, 20));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireExportTypeChangedEvent(evt);
            }
        });
        jPanel1.add(jComboBox1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Export Template für");
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/information.png"))); // NOI18N
        jButton3.setToolTipText("Verfügbare Platzhalter");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireShowTemplateVarsDialogEvent(evt);
            }
        });
        jPanel1.add(jButton3, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jLabel3, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireExportTypeChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireExportTypeChangedEvent
        String result = "";
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            switch (jComboBox1.getSelectedIndex()) {
                case 0:
                    element = attackFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleAttacks, true);
                    break;
                case 1:
                    element = noteFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleNotes, true);
                    break;
                case 2:
                    element = villageFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleVillages, true);
                    break;
                case 3:
                    element = sosFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleRequests, true);
                    break;
                case 4:
                    element = reportFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleReports, true);
                    break;
                case 5:
                    element = tagFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleTags, true);
                    break;
                case 6:
                    element = troopsFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleTroops, true);
                    break;
                case 7:
                    element = pointStatsFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleStats, true);
                    break;
                case 8:
                    element = offStatsFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleStats, true);
                    break;
                case 9:
                    element = defStatsFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleStats, true);
                    break;
                case 10:
                    element = winnerLoserStatsFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleStats, true);
                    break;
                case 11:
                    element = formFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleForms, true);
                    break;
                case 12:
                    element = overallStatFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleOverallResult, true);
                    break;
                case 13:
                    element = allyStatFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleAllyResult, true);
                    break;
                case 14:
                    element = tribeStatFormatter;
                    bBPanel2.setBBCode(element.getTemplate());
                    result = element.formatElements(sampleTribeResult, true);
                    break;
            }
        }

        DefaultListModel model = new DefaultListModel();
        for (String var : element.getTemplateVariables()) {
            model.addElement(var);
        }

        jTextPane1.setText(BBCodeFormatter.toHtml(result));

        jVarsList.setModel(model);
    }//GEN-LAST:event_fireExportTypeChangedEvent

    private void fireShowTemplateVarsDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowTemplateVarsDialogEvent
        infoPanel.setCollapsed(!infoPanel.isCollapsed());
    }//GEN-LAST:event_fireShowTemplateVarsDialogEvent

    private void fireApplyBBTemplatesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyBBTemplatesEvent
        setVisible(false);
    }//GEN-LAST:event_fireApplyBBTemplatesEvent

    private void fireResetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireResetEvent

        switch (jComboBox1.getSelectedIndex()) {
            case 0:
                element = attackFormatter;
                break;
            case 1:
                element = noteFormatter;
                break;
            case 2:
                element = villageFormatter;
                break;
            case 3:
                element = sosFormatter;
                break;
            case 4:
                element = reportFormatter;
                break;
            case 5:
                element = tagFormatter;
                break;
            case 6:
                element = troopsFormatter;
                break;
            case 7:
                element = pointStatsFormatter;
                break;
            case 8:
                element = offStatsFormatter;
                break;
            case 9:
                element = defStatsFormatter;
                break;
            case 10:
                element = winnerLoserStatsFormatter;
                break;
            case 11:
                element = formFormatter;
                break;
            case 12:
                element = overallStatFormatter;
                break;
            case 13:
                element = allyStatFormatter;
                break;
            case 14:
                element = tribeStatFormatter;
                break;
        }
        bBPanel2.setBBCode(element.getStandardTemplate());
        updatePreview();
    }//GEN-LAST:event_fireResetEvent

    private void updatePreview() {
        String result = "";
        switch (jComboBox1.getSelectedIndex()) {
            case 0:
                element = attackFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleAttacks, true);
                break;
            case 1:
                element = noteFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleNotes, true);
                break;
            case 2:
                element = villageFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleVillages, true);
                break;
            case 3:
                element = sosFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleRequests, true);
                break;
            case 4:
                element = reportFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleReports, true);
                break;
            case 5:
                element = tagFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleTags, true);
                break;
            case 6:
                element = troopsFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleTroops, true);
                break;
            case 7:
                element = pointStatsFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleStats, true);
                break;
            case 8:
                element = offStatsFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleStats, true);
                break;
            case 9:
                element = defStatsFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleStats, true);
                break;
            case 10:
                element = winnerLoserStatsFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleStats, true);
                break;
            case 11:
                element = formFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleForms, true);
                break;
            case 12:
                element = overallStatFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleOverallResult, true);
                break;
            case 13:
                element = allyStatFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleAllyResult, true);
                break;
            case 14:
                element = tribeStatFormatter;
                GlobalOptions.addProperty(element.getPropertyKey(), bBPanel2.getBBCode());
                result = element.formatElements(sampleTribeResult, true);
                break;
        }
        try {
            jTextPane1.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(result) + "</body></html>");
        } catch (Exception e) {
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        GlobalOptions.setSelectedServer("de43");
        ServerManager.loadServerList();
        DataHolder.getSingleton().loadData(false);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                BBCodeEditor ed = new BBCodeEditor();
                ed.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                ed.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.BBPanel bBPanel2;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JButton jApplyButton;
    private javax.swing.JPanel jBBPanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JList jVarsList;
    // End of variables declaration//GEN-END:variables
}
