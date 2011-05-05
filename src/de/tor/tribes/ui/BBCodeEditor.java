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
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.test.DummyUnit;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BBChangeListener;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.bb.BasicFormatter;
import de.tor.tribes.util.bb.NoteListFormatter;
import de.tor.tribes.util.bb.ReportListFormatter;
import de.tor.tribes.util.bb.SosListFormatter;
import de.tor.tribes.util.bb.TagListFormatter;
import de.tor.tribes.util.bb.VillageListFormatter;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.UIManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class BBCodeEditor extends javax.swing.JFrame {

    private final List<Attack> sampleAttacks = new LinkedList<Attack>();
    private final List<Village> sampleVillages = new LinkedList<Village>();
    private List<FightReport> sampleReports = new LinkedList<FightReport>();
    private List<SOSRequest> sampleRequests = new LinkedList<SOSRequest>();
    private List<Tag> sampleTags = new LinkedList<Tag>();
    private List<Note> sampleNotes = new LinkedList<Note>();
    private BasicFormatter element = null;
    private BasicFormatter<Attack> attackFormatter = new AttackListFormatter();
    private BasicFormatter<Village> villageFormatter = new VillageListFormatter();
    private BasicFormatter<FightReport> reportFormatter = new ReportListFormatter();
    private BasicFormatter<SOSRequest> sosFormatter = new SosListFormatter();
    private BasicFormatter<Note> noteFormatter = new NoteListFormatter();
    private BasicFormatter<Tag> tagFormatter = new TagListFormatter();

    /** Creates new form BBCodeEditor */
    public BBCodeEditor() {
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

        fireExportTypeChangedEvent(new ItemEvent(jButton1, 0, null, ItemEvent.SELECTED));
    }

    private void buildSampleData() {
        //sample village
        Village sampleVillage = DataHolder.getSingleton().getRandomVillage();
        //sample attack
        Village sourceVillage = DataHolder.getSingleton().getRandomVillage();
        Village targetVillage = DataHolder.getSingleton().getRandomVillage();
        Village noteVillage1 = DataHolder.getSingleton().getRandomVillage();
        Village noteVillage2 = DataHolder.getSingleton().getRandomVillage();
        sampleVillages.add(sourceVillage);
        sampleVillages.add(targetVillage);
        sampleVillages.add(noteVillage1);
        sampleVillages.add(noteVillage2);
        Attack sampleAttack = new Attack();
        sampleAttack.setSource(sourceVillage);
        sampleAttack.setTarget(targetVillage);
        sampleAttack.setArriveTime(new Date());
        sampleAttack.setType(Attack.CLEAN_TYPE);
        sampleAttack.setUnit(new DummyUnit());
        Attack sampleAttack2 = new Attack();
        sampleAttack2.setSource(sourceVillage);
        sampleAttack2.setTarget(targetVillage);
        sampleAttack2.setArriveTime(new Date());
        sampleAttack2.setType(Attack.CLEAN_TYPE);
        sampleAttack2.setUnit(new DummyUnit());
        sampleAttacks.add(sampleAttack);
        sampleAttacks.add(sampleAttack2);
        //sample note
        Note sampleNote = new Note();
        sampleNote.setNoteSymbol(ImageManager.NOTE_SYMBOL_BALL_BLUE);
        sampleNote.setTimestamp(System.currentTimeMillis());
        sampleNote.addVillage(sampleVillage);
        sampleNote.addVillage(noteVillage1);
        sampleNote.addVillage(noteVillage2);
        sampleNote.setNoteText("[u]Dies[/u] ist eine [b]Beispielnotiz[/b]");
        Note sampleNote2 = new Note();
        sampleNote2.setNoteSymbol(ImageManager.NOTE_SYMBOL_BALL_RED);
        sampleNote2.setTimestamp(System.currentTimeMillis());
        sampleNote2.addVillage(noteVillage1);
        sampleNote2.addVillage(noteVillage2);
        sampleNote2.setNoteText("[u]Dies[/u] ist eine weitere [b]Beispielnotiz[/b]");
        sampleNotes.add(sampleNote);
        sampleNotes.add(sampleNote2);
        //sample SOS request
        SOSRequest sampleSOSRequest = new SOSRequest();
        sampleSOSRequest.setDefender(Barbarians.getSingleton());
        sampleSOSRequest.addTarget(targetVillage);
        sampleSOSRequest.getTargetInformation(targetVillage).setWallLevel(20);
        sampleSOSRequest.getTargetInformation(targetVillage).addAttack(sourceVillage, new Date());
        sampleSOSRequest.getTargetInformation(targetVillage).addAttack(sourceVillage, new Date());
        sampleSOSRequest.getTargetInformation(targetVillage).addTroopInformation(DataHolder.getSingleton().getRandomUnit(), 300);
        SOSRequest sampleSOSRequest2 = new SOSRequest();
        sampleSOSRequest2.setDefender(Barbarians.getSingleton());
        sampleSOSRequest2.addTarget(targetVillage);
        sampleSOSRequest2.getTargetInformation(targetVillage).setWallLevel(10);
        sampleSOSRequest2.getTargetInformation(targetVillage).addAttack(sourceVillage, new Date());
        sampleSOSRequest2.getTargetInformation(targetVillage).addAttack(sourceVillage, new Date());
        sampleSOSRequest2.getTargetInformation(targetVillage).addTroopInformation(DataHolder.getSingleton().getRandomUnit(), 100);
        sampleRequests.add(sampleSOSRequest);
        sampleRequests.add(sampleSOSRequest2);
        //sample report
        FightReport sampleReport = new FightReport();
        sampleReport.setAimedBuilding("Wall");
        sampleReport.setAttacker(sourceVillage.getTribe());
        sampleReport.setConquered(false);
        sampleReport.setDefender(targetVillage.getTribe());
        sampleReport.setLuck(0d);
        sampleReport.setMoral(100d);
        sampleReport.setSourceVillage(sourceVillage);
        sampleReport.setTargetVillage(targetVillage);
        sampleReport.setWallAfter((byte) 15);
        sampleReport.setWallBefore((byte) 20);
        FightReport sampleReport2 = new FightReport();
        sampleReport2.setAcceptanceAfter((byte) 70);
        sampleReport2.setAcceptanceBefore((byte) 100);
        sampleReport2.setAimedBuilding("Wall");
        sampleReport2.setAttacker(sourceVillage.getTribe());
        sampleReport2.setConquered(false);
        sampleReport2.setDefender(targetVillage.getTribe());
        sampleReport2.setLuck(0d);
        sampleReport2.setMoral(100d);
        sampleReport2.setSourceVillage(sourceVillage);
        sampleReport2.setTargetVillage(targetVillage);
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
        t.tagVillage(sourceVillage.getId());
        t.tagVillage(targetVillage.getId());
        t2.tagVillage(noteVillage1.getId());
        t3.tagVillage(noteVillage2.getId());
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
        jButton1 = new javax.swing.JButton();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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

        jButton1.setText("Übernehmen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        getContentPane().add(jButton1, gridBagConstraints);

        jButton2.setText("Abbrechen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
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

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Angriffe", "Notizen", "Dorflisten", "SOS-Anfragen", "Kampfbericht", "Gruppen" }));
        jComboBox1.setMinimumSize(new java.awt.Dimension(66, 20));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireExportTypeChangedEvent(evt);
            }
        });
        jPanel1.add(jComboBox1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Zu exportierende Daten");
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
        }
        jTextPane1.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(result) + "</body></html>");
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
                new BBCodeEditor().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.BBPanel bBPanel2;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JPanel jBBPanel;
    private javax.swing.JButton jButton1;
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
