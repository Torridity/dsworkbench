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
package de.tor.tribes.ui.components;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.ui.wiz.red.ResourceDistributorDataReadPanel;
import de.tor.tribes.util.*;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXList;

/**
 * @author Torridity
 */
public class VillageSelectionPanel extends javax.swing.JPanel{

    public enum SELECTION_ELEMENT {

        ALLY, TRIBE, GROUP, CONTINENT, VILLAGE
    }
    private IconizedList allyList = null;
    private IconizedList tribeList = null;
    private IconizedList groupList = null;
    private IconizedList continentList = null;
    private IconizedList villageList = null;
    private FilterPipeline<Ally, Tribe> allyTribePipeline = null;
    private FilterPipeline<Tribe, GroupSelectionList.ListItem> tribeGroupPipeline = null;
    private FilterPipeline<GroupSelectionList.ListItem, ContinentVillageSelection> groupContinentPipeline = null;
    private FilterPipeline<ContinentVillageSelection, Village> continentVillagePipeline = null;
    private VillageSelectionPanelListener listener = null;

    /**
     * Creates new form VillageSelectionPanel
     */
    public VillageSelectionPanel(VillageSelectionPanelListener pListener) {
        initComponents();
        listener = pListener;

        jXButton2.setIcon(new ImageIcon(ResourceDistributorDataReadPanel.class.getResource("/res/ui/clipboard.png")));
        
        allyList = new IconizedList("/res/awards/ally.png");
        jAllyScrollPane.setViewportView(allyList);

        tribeList = new IconizedList("/res/awards/tribe.png");
        jTribeScrollPane.setViewportView(tribeList);

        groupList = new GroupSelectionList("/res/awards/group.png");
        jGroupScrollPane.setViewportView(groupList);

        continentList = new IconizedList("/res/awards/continent.png");
        jContinentScrollPane.setViewportView(continentList);

        villageList = new IconizedList("/res/awards/village.png");
        jVillageScrollPane.setViewportView(villageList);
        enableSelectionElement(SELECTION_ELEMENT.ALLY, true);
        enableSelectionElement(SELECTION_ELEMENT.TRIBE, true);
        enableSelectionElement(SELECTION_ELEMENT.GROUP, true);
        enableSelectionElement(SELECTION_ELEMENT.CONTINENT, true);
        enableSelectionElement(SELECTION_ELEMENT.VILLAGE, true);
        setUnitSelectionEnabled(false);
        setFakeSelectionEnabled(false);
        setAmountSelectionEnabled(false);
        jExpertHelpLabel.setVisible(false);
    }

    public void setup() {
        if (allyTribePipeline != null) {
            allyTribePipeline.setActive(false);
        }
        if (tribeGroupPipeline != null) {
            tribeGroupPipeline.setActive(false);
        }

        if (!allyList.isVisible()) {
            allyList.setListData(new Ally[]{GlobalOptions.getSelectedProfile().getTribe().getAlly()});
        } else {
            allyList.setListData(AllyUtils.getAlliesByFilter("", Ally.CASE_INSENSITIVE_ORDER));
        }

        if (!tribeList.isVisible()) {
            tribeList.setListData(new Tribe[]{GlobalOptions.getSelectedProfile().getTribe()});
        }

        jXTextField1.setText("");

        setupFilters();
        //do initial selection

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!unit.getPlainName().equals("militia")) {
                model.addElement(unit);
            }
        }

        jUnitBox.setModel(model);
        jUnitBox.setRenderer(new UnitListCellRenderer());
        jUnitBox.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));

        //enable and perform initial selection
        allyTribePipeline.setActive(true);
        tribeGroupPipeline.setActive(true);
        allyList.setSelectedIndex(0);
    }

    private void setupFilters() {
        if (allyTribePipeline == null) {
            allyTribePipeline = new FilterPipeline<Ally, Tribe>(allyList, tribeList) {

                @Override
                public Tribe[] filter() {
                    if (!tribeList.isVisible()) {
                        return new Tribe[]{GlobalOptions.getSelectedProfile().getTribe()};
                    }

                    List<Tribe> res = new LinkedList<>();
                    for (Object o : getSelection()) {
                        Ally a = (Ally) o;
                        res.addAll(Arrays.asList(a.getTribes()));
                    }

                    Collections.sort(res, Tribe.CASE_INSENSITIVE_ORDER);
                    return res.toArray(new Tribe[res.size()]);
                }
            };
        }

        if (tribeGroupPipeline == null) {
            tribeGroupPipeline = new FilterPipeline<Tribe, GroupSelectionList.ListItem>(tribeList, groupList) {

                @Override
                public GroupSelectionList.ListItem[] filter() {
                    List<Tag> usedTags = new LinkedList<>();
                    List<Village> villages = new LinkedList<>();
                    List<GroupSelectionList.ListItem> items = new LinkedList<>();
                    for (Object o : getSelection()) {
                        Tribe t = (Tribe) o;
                        Collections.addAll(villages, t.getVillageList());
                        for (Tag tag : TagUtils.getTagsByTribe(t, Tag.CASE_INSENSITIVE_ORDER)) {
                            if (!usedTags.contains(tag)) {
                                items.add(new GroupSelectionList.ListItem(tag));
                                usedTags.add(tag);
                            }
                        }
                    }
                    ((GroupSelectionList) getOutputList()).setRelevantVillages(villages.toArray(new Village[villages.size()]));
                    return items.toArray(new GroupSelectionList.ListItem[items.size()]);
                }
            };
        }
        if (groupContinentPipeline == null) {
            groupContinentPipeline = new FilterPipeline<GroupSelectionList.ListItem, ContinentVillageSelection>(groupList, continentList) {

                @Override
                public ContinentVillageSelection[] filter() {
                    HashMap<Integer, ContinentVillageSelection> map = new HashMap<>();
                    for (Village v : ((GroupSelectionList) getInputList()).getValidVillages()) {
                        int cont = v.getContinent();
                        ContinentVillageSelection s = map.get(cont);
                        if (s == null) {
                            s = new ContinentVillageSelection(cont);
                            map.put(cont, s);
                        }
                        s.addVillage(v);
                    }

                    ContinentVillageSelection[] result = map.values().toArray(new ContinentVillageSelection[map.size()]);
                    Arrays.sort(result, new Comparator<ContinentVillageSelection>() {

                        @Override
                        public int compare(ContinentVillageSelection o1, ContinentVillageSelection o2) {
                            return String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString());
                        }
                    });
                    return result;
                }

                @Override
                public void updateOutputSelection() {
                    getOutputList().getSelectionModel().setSelectionInterval(0, getOutputList().getElementCount() - 1);
                }
            };
        }
        if (continentVillagePipeline == null) {

            continentVillagePipeline = new FilterPipeline<ContinentVillageSelection, Village>(continentList, villageList) {

                @Override
                public Village[] filter() {
                    List<Village> res = new LinkedList<>();
                    for (Object o : getSelection()) {
                        ContinentVillageSelection c = (ContinentVillageSelection) o;
                        Collections.addAll(res, c.getVillages());
                    }
                    Collections.sort(res, Village.CASE_INSENSITIVE_ORDER);
                    return res.toArray(new Village[res.size()]);
                }

                @Override
                public void updateOutputSelection() {
                    getOutputList().getSelectionModel().setSelectionInterval(0, getOutputList().getElementCount() - 1);
                }
            };
        }
    }

    public final void setUnitSelectionEnabled(boolean pValue) {
        jUnitBox.setVisible(pValue);
        try {
            if (!pValue) {
                ((GridBagLayout) getLayout()).getConstraints(jVillageScrollPane).gridheight += 1;
            } else {
                ((GridBagLayout) getLayout()).getConstraints(jVillageScrollPane).gridheight -= 1;
            }
        } catch (Exception ignored) {
        }
    }

    public final void setFakeSelectionEnabled(boolean pValue) {
        jFakeBox.setVisible(pValue);
        try {
            if (!pValue) {
                ((GridBagLayout) getLayout()).getConstraints(jVillageScrollPane).gridheight += 1;
            } else {
                ((GridBagLayout) getLayout()).getConstraints(jVillageScrollPane).gridheight -= 1;
            }
        } catch (Exception ignored) {
        }
    }

    public final void setAmountSelectionEnabled(boolean pValue) {
        jAmountLabel.setVisible(pValue);
        jAmountField.setVisible(pValue);
        try {
            if (!pValue) {
                ((GridBagLayout) getLayout()).getConstraints(jVillageScrollPane).gridheight += 1;
            } else {
                ((GridBagLayout) getLayout()).getConstraints(jVillageScrollPane).gridheight -= 1;
            }
        } catch (Exception ignored) {
        }
    }

    public final void enableSelectionElement(SELECTION_ELEMENT pElement, boolean pEnable) {
        switch (pElement) {
            case ALLY:
                changeSelectionElementVisibility(jAllyScrollPane, allyList, pEnable);
                jXTextField1.setVisible(pEnable);
                break;
            case TRIBE:
                changeSelectionElementVisibility(jTribeScrollPane, tribeList, pEnable);
                break;
            case GROUP:
                changeSelectionElementVisibility(jGroupScrollPane, groupList, pEnable);
                jExpertHelpLabel.setVisible(pEnable);
                jExpertSelection.setVisible(pEnable);
                break;
            case CONTINENT:
                changeSelectionElementVisibility(jContinentScrollPane, continentList, pEnable);
                break;
            case VILLAGE:
                changeSelectionElementVisibility(jVillageScrollPane, villageList, pEnable);
                break;
        }
    }

    private void changeSelectionElementVisibility(JScrollPane pScrollPane, JXList pList, boolean pShow) {
        pScrollPane.setVisible(pShow);
        pList.setVisible(pShow);
    }

    public interface VillageSelectionPanelListener {

        void fireVillageSelectionEvent(Village[] pSelection);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jXButton2 = new org.jdesktop.swingx.JXButton();
        jAllyScrollPane = new javax.swing.JScrollPane();
        jTribeScrollPane = new javax.swing.JScrollPane();
        jGroupScrollPane = new javax.swing.JScrollPane();
        jContinentScrollPane = new javax.swing.JScrollPane();
        jVillageScrollPane = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jExpertHelpLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jXButton1 = new org.jdesktop.swingx.JXButton();
        jUnitBox = new org.jdesktop.swingx.JXComboBox();
        jFakeBox = new javax.swing.JCheckBox();
        jXTextField1 = new org.jdesktop.swingx.JXTextField();
        jAmountLabel = new javax.swing.JLabel();
        jAmountField = new javax.swing.JTextField();
        jExpertSelection = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(600, 350));
        setLayout(new java.awt.GridBagLayout());

        jXButton2.setToolTipText("Sucht in der Zwischenablage nach Dörfern und fügt diese ein. ");
        jXButton2.setMaximumSize(new java.awt.Dimension(90, 23));
        jXButton2.setMinimumSize(new java.awt.Dimension(90, 23));
        jXButton2.setPreferredSize(new java.awt.Dimension(90, 23));
        jXButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jXButton2fireTransferClipboardVillagesEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jXButton2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(jAllyScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTribeScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jGroupScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jContinentScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jVillageScrollPane, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(153, 153, 153));
        jLabel1.setText("<html>Elemente der Reihe nach ausw&auml;hlen. F&uuml;r eine Suche nach Elementen in die entsprechende Liste klicken und den Elementnamen tippen oder per STRG+F die Suche &ouml;ffnen. Mehrere Elemente mit gedr&uuml;ckter STRG-Taste ausw&auml;hlen.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabel1, gridBagConstraints);

        jExpertHelpLabel.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jExpertHelpLabel.setForeground(new java.awt.Color(153, 153, 153));
        jExpertHelpLabel.setText("<html>Gruppeneintrag doppelt klicken, um Art der Verkn&uuml;pfung zu &auml;ndern. Verwendung einer Gruppe &uuml;ber <b>ENTF</b> l&ouml;schen.</html>");
        jExpertHelpLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jExpertHelpLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jSeparator1, gridBagConstraints);

        jXButton1.setText("Auswahl verwenden");
        jXButton1.setToolTipText("Fügt alle gewählten Dörfer ein.");
        jXButton1.setMaximumSize(new java.awt.Dimension(90, 23));
        jXButton1.setMinimumSize(new java.awt.Dimension(90, 23));
        jXButton1.setPreferredSize(new java.awt.Dimension(90, 23));
        jXButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferVillageSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jXButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jUnitBox, gridBagConstraints);

        jFakeBox.setText("Als Fake einfügen");
        jFakeBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/no_fake.png"))); // NOI18N
        jFakeBox.setMaximumSize(new java.awt.Dimension(80, 27));
        jFakeBox.setMinimumSize(new java.awt.Dimension(80, 27));
        jFakeBox.setPreferredSize(new java.awt.Dimension(80, 27));
        jFakeBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/fake.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jFakeBox, gridBagConstraints);

        jXTextField1.setPrompt("Name/Tag eingeben");
        jXTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireAllyNameTagChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jXTextField1, gridBagConstraints);

        jAmountLabel.setText("Anzahl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jAmountLabel, gridBagConstraints);

        jAmountField.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jAmountField, gridBagConstraints);

        jExpertSelection.setText("Expertenansicht");
        jExpertSelection.setToolTipText("<html>Aktiviert die Expertenansicht. Hierbei können komplexe Verknüpfungen zwischen<br/>Gruppen erstellt werden, um Gruppen gezielt ein- und auszuschließen.</html>");
        jExpertSelection.setMaximumSize(new java.awt.Dimension(70, 23));
        jExpertSelection.setMinimumSize(new java.awt.Dimension(70, 23));
        jExpertSelection.setPreferredSize(new java.awt.Dimension(70, 23));
        jExpertSelection.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeExpertSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        add(jExpertSelection, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireTransferVillageSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferVillageSelectionEvent
        List<Village> result = new LinkedList<>();
        Object[] selection = villageList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            //transfer all
            if (villageList.getElementCount() != 0) {
                for (int i = 0; i < villageList.getElementCount(); i++) {
                    result.add((Village) villageList.getElementAt(i));
                }
            }
        } else {
            for (Object s : selection) {
                int cnt = 1;
                if (jAmountField.isVisible()) {
                    cnt = UIHelper.parseIntFromField(jAmountField, 1);
                }
                for (int i = 0; i < cnt; i++) {
                    result.add((Village) s);
                }
            }
        }

        if (!result.isEmpty()) {
            listener.fireVillageSelectionEvent(result.toArray(new Village[result.size()]));
        }

    }//GEN-LAST:event_fireTransferVillageSelectionEvent

    private void fireAllyNameTagChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireAllyNameTagChangedEvent
        allyList.setListData(AllyUtils.getAlliesByFilter(jXTextField1.getText(), Ally.CASE_INSENSITIVE_ORDER));
    }//GEN-LAST:event_fireAllyNameTagChangedEvent

    private void fireChangeExpertSelectionEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeExpertSelectionEvent
        ((GroupSelectionList) groupList).setExpertSelection(jExpertSelection.isSelected());
        jExpertHelpLabel.setVisible(jExpertSelection.isSelected());
    }//GEN-LAST:event_fireChangeExpertSelectionEvent

    private void jXButton2fireTransferClipboardVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXButton2fireTransferClipboardVillagesEvent
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String clipboard;
        try {
            clipboard = (String) t.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            return;
        }
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(clipboard);
        if(!villages.isEmpty())
            listener.fireVillageSelectionEvent(villages.toArray(new Village[villages.size()]));
    }//GEN-LAST:event_jXButton2fireTransferClipboardVillagesEvent

    public boolean isExpertSelection() {
        return jExpertSelection.isSelected();
    }

    public void setExpertSelection(boolean pValue) {
        jExpertSelection.setSelected(pValue);
    }

    public void setSelectedUnit(UnitHolder pUnit) {
        jUnitBox.setSelectedItem(pUnit);
    }

    public UnitHolder getSelectedUnit() {
        return (UnitHolder) jUnitBox.getSelectedItem();
    }

    public void setFake(boolean pFake) {
        jFakeBox.setSelected(pFake);
    }

    public boolean isFake() {
        return jFakeBox.isSelected();
    }

    public int getAmount() {
        return UIHelper.parseIntFromField(jAmountField, 1);
    }

    public void setAmount(int pAmount) {
        UIHelper.setText(jAmountField, Integer.toString(pAmount), "1");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jAllyScrollPane;
    private javax.swing.JTextField jAmountField;
    private javax.swing.JLabel jAmountLabel;
    private javax.swing.JScrollPane jContinentScrollPane;
    private javax.swing.JLabel jExpertHelpLabel;
    private javax.swing.JCheckBox jExpertSelection;
    private javax.swing.JCheckBox jFakeBox;
    private javax.swing.JScrollPane jGroupScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JScrollPane jTribeScrollPane;
    private org.jdesktop.swingx.JXComboBox jUnitBox;
    private javax.swing.JScrollPane jVillageScrollPane;
    private org.jdesktop.swingx.JXButton jXButton1;
    private org.jdesktop.swingx.JXButton jXButton2;
    private org.jdesktop.swingx.JXTextField jXTextField1;
    // End of variables declaration//GEN-END:variables
}

abstract class FilterPipeline<C, D> implements ListSelectionListener {

    private JXList inputList = null;
    private JXList outputList = null;
    private boolean active = true;

    public FilterPipeline(JXList pThisList, JXList pRightList) {
        inputList = pThisList;
        outputList = pRightList;
        pThisList.addListSelectionListener(FilterPipeline.this);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (active && !e.getValueIsAdjusting()) {
            outputList.setListData(filter());
            updateOutputSelection();
        }
    }

    /**
     * Special constructor for group list, as selection handling is managed by the list itself
     */
    public FilterPipeline(GroupSelectionList pThisList, JXList pRightList) {
        inputList = pThisList;
        outputList = pRightList;
    }

    public Object[] getSelection() {
        List<C> result = new LinkedList<>();
        if (inputList.isVisible() || inputList.getSelectedValues().length > 0) {
            for (Object o : inputList.getSelectedValues()) {
                result.add((C) o);
            }
        } else {
            for (int i = 0; i < inputList.getModel().getSize(); i++) {
                result.add((C) inputList.getModel().getElementAt(i));
            }
        }

        return result.toArray();
    }

    public JXList getInputList() {
        return inputList;
    }

    public JXList getOutputList() {
        return outputList;
    }

    public abstract D[] filter();

    public void updateOutputSelection() {
        if (!outputList.isVisible() && outputList instanceof GroupSelectionList) {
            //for group selection lists all elements are selected if list is not visible...for all other lists the first element is selected
            outputList.getSelectionModel().setSelectionInterval(0, outputList.getElementCount() - 1);
            return;
        }

        outputList.setSelectedIndex(0);
    }
}

class ContinentVillageSelection {

    private int continent = 0;
    private String continentString = null;
    private List<Village> villages = null;

    public ContinentVillageSelection(int pContinent) {
        continent = pContinent;
        continentString = "K" + ((continent < 10) ? "0" + continent : continent);
        villages = new LinkedList<>();
    }

    public void addVillage(Village pVillage) {
        villages.add(pVillage);
    }

    public Village[] getVillages() {
        return villages.toArray(new Village[villages.size()]);
    }

    @Override
    public String toString() {
        return continentString;
    }
}
