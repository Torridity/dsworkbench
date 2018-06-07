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
package de.tor.tribes.ui.panels;

import de.tor.tribes.types.Marker;
import de.tor.tribes.ui.editors.ColorChooserCellEditor;
import de.tor.tribes.ui.editors.CustomCheckBoxEditor;
import de.tor.tribes.ui.models.MarkerTableModel;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.CustomBooleanRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.MarkerCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.UIHelper;
import de.tor.tribes.util.bb.MarkerListFormatter;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.*;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Torridity
 */
public class MarkerTableTab extends javax.swing.JPanel implements ListSelectionListener {

    private static Logger logger = LogManager.getLogger("MarkerTableTab");

    public enum TRANSFER_TYPE {

        CLIPBOARD_BB, CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD, FROM_INTERNAL_CLIPBOARD
    }
    private String sMarkerSet = null;
    private final static JXTable jxMarkerTable = new JXTable();
    private static MarkerTableModel markerModel = null;
    private static boolean KEY_LISTENER_ADDED = false;
    private static PainterHighlighter highlighter = null;

    static {
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0, 2);
        jxMarkerTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));
        jxMarkerTable.setColumnControlVisible(true);
        jxMarkerTable.setDefaultRenderer(Color.class, new ColorCellRenderer());
        jxMarkerTable.setDefaultRenderer(Marker.class, new MarkerCellRenderer());
        ColorChooserCellEditor editor = new ColorChooserCellEditor(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        jxMarkerTable.setDefaultEditor(Color.class, editor);
        markerModel = new MarkerTableModel(MarkerManager.DEFAULT_GROUP);
        jxMarkerTable.setModel(markerModel);
        TableColumnExt visibilityCol = jxMarkerTable.getColumnExt("Sichtbar");
        visibilityCol.setCellRenderer(new CustomBooleanRenderer(CustomBooleanRenderer.LayoutStyle.VISIBLE_INVISIBLE));
        visibilityCol.setCellEditor(new CustomCheckBoxEditor(CustomBooleanRenderer.LayoutStyle.VISIBLE_INVISIBLE));

        BufferedImage back = ImageUtils.createCompatibleBufferedImage(5, 5, BufferedImage.BITMASK);
        Graphics2D g = back.createGraphics();
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 0);
        p.lineTo(5, 5);
        p.closePath();
        g.setColor(Color.GREEN.darker());
        g.fill(p);
        g.dispose();
        jxMarkerTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }

    /** Creates new form MarkerTableTab
     * @param pMarkerSet
     * @param pActionListener
     */
    public MarkerTableTab(String pMarkerSet, final ActionListener pActionListener) {
        sMarkerSet = pMarkerSet;
        initComponents();
        jScrollPane1.setViewportView(jxMarkerTable);
        if (!KEY_LISTENER_ADDED) {
            KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
            KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
            KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
            KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
            KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
            jxMarkerTable.registerKeyboardAction(pActionListener, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxMarkerTable.registerKeyboardAction(pActionListener, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxMarkerTable.registerKeyboardAction(pActionListener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxMarkerTable.registerKeyboardAction(pActionListener, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxMarkerTable.registerKeyboardAction(pActionListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

            jxMarkerTable.getActionMap().put("find", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //pActionListener.actionPerformed(new ActionEvent(jxMarkerTable, 0, "Find"));
                    //disable find
                }
            });

            KEY_LISTENER_ADDED = true;
        }
        jxMarkerTable.getSelectionModel().addListSelectionListener(MarkerTableTab.this);
    }

    public void deregister() {
        jxMarkerTable.getSelectionModel().removeListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jxMarkerTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Markierung gewählt" : " Markierungen gewählt"));
            }
        }
    }

    public void changeVisibility(boolean pValue) {
        List<Marker> markers = getSelectedMarkers();
        if (markers.isEmpty()) {
            showInfo("Keine Markierungen gewählt");
            return;
        }
        MarkerManager.getSingleton().invalidate();
        for (Marker m : markers) {
            m.setShownOnMap(pValue);
        }
        markerModel.fireTableDataChanged();
        MarkerManager.getSingleton().revalidate(true);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }

    public String getMarkerSet() {
        return sMarkerSet;
    }

    public JXTable getMarkerTable() {
        return jxMarkerTable;
    }

    public void updateSet() {
        markerModel.setMarkerSet(sMarkerSet);
        UIHelper.initTableColums(jxMarkerTable, "Markierung", "Sichtbar");
        jScrollPane1.setViewportView(jxMarkerTable);
        jxMarkerTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
    }

    public void updateFilter(final String pValue, final List<String> columns, final boolean pCaseSensitive, final boolean pFilterRows) {
        if (highlighter != null) {
            jxMarkerTable.removeHighlighter(highlighter);
        }
        if (!pFilterRows) {
            jxMarkerTable.setRowFilter(null);
            final List<Integer> relevantCols = new LinkedList<>();
            List<TableColumn> cols = jxMarkerTable.getColumns(true);
            for (int i = 0; i < jxMarkerTable.getColumnCount(); i++) {
                TableColumnExt col = jxMarkerTable.getColumnExt(i);
                if (col.isVisible() && columns.contains(col.getTitle())) {
                    relevantCols.add(cols.indexOf(col));
                }
            }
            for (Integer col : relevantCols) {
                PatternPredicate patternPredicate0 = new PatternPredicate((pCaseSensitive ? "" : "(?i)") + Matcher.quoteReplacement(pValue), col);
                MattePainter mp = new MattePainter(new Color(0, 0, 0, 120));
                highlighter = new PainterHighlighter(new HighlightPredicate.NotHighlightPredicate(patternPredicate0), mp);
                jxMarkerTable.addHighlighter(highlighter);
            }
        } else {
            jxMarkerTable.setRowFilter(new RowFilter<TableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    final List<Integer> relevantCols = new LinkedList<>();
                    List<TableColumn> cols = jxMarkerTable.getColumns(true);
                    for (int i = 0; i < jxMarkerTable.getColumnCount(); i++) {
                        TableColumnExt col = jxMarkerTable.getColumnExt(i);
                        if (col.isVisible() && columns.contains(col.getTitle())) {
                            relevantCols.add(cols.indexOf(col));
                        }
                    }

                    for (Integer col : relevantCols) {
                        if (pCaseSensitive) {
                            if (entry.getStringValue(col).contains(pValue)) {
                                return true;
                            }
                        } else {
                            if (entry.getStringValue(col).toLowerCase().contains(pValue.toLowerCase())) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setForeground(new java.awt.Color(240, 240, 240));
        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        add(infoPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        infoPanel.setCollapsed(true);
    }//GEN-LAST:event_fireHideInfoEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables

    public void transferSelection(TRANSFER_TYPE pType) {
        switch (pType) {
            case CUT_TO_INTERNAL_CLIPBOARD:
                cutToClipboard();
                break;
            case COPY_TO_INTERNAL_CLIPBOARD:
                copyToInternalClipboard();
                break;
            case FROM_INTERNAL_CLIPBOARD:
                copyFromInternalClipboard();
                break;
            case CLIPBOARD_BB:
                copyBBToExternalClipboardEvent();
                break;
        }
    }

    private boolean copyToInternalClipboard() {
        List<Marker> selection = getSelectedMarkers();
        if (selection.isEmpty()) {
            showInfo("Keine Markierung gewählt");
            return false;
        }
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        for (Marker a : selection) {
            b.append(Marker.toInternalRepresentation(a)).append("\n");
            cnt++;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            showSuccess(cnt + ((cnt == 1) ? " Markierung kopiert" : " Markierungen kopiert"));
            return true;
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Markierungen");
            return false;
        }
    }

    private void cutToClipboard() {
        int size = getSelectedMarkers().size();
        if (size == 0) {
            showInfo("Keine Markierung gewählt");
            return;
        }
        if (copyToInternalClipboard() && deleteSelection(false)) {
            showSuccess(size + ((size == 1) ? " Markierung ausgeschnitten" : " Markierungen ausgeschnitten"));
        } else {
            showError("Fehler beim Ausschneiden der Markierungen");
        }
    }

    private void copyFromInternalClipboard() {
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            for (String line : lines) {
                Marker a = Marker.fromInternalRepresentation(line);
                if (a != null) {
                    MarkerManager.getSingleton().addManagedElement(sMarkerSet, a);
                    cnt++;
                }
            }
            showSuccess(cnt + ((cnt == 1) ? " Markierung eingefügt" : " Markierungen eingefügt"));
        } catch (UnsupportedFlavorException ufe) {
            logger.error("Failed to copy markers from internal clipboard", ufe);
            showError("Fehler beim Einfügen der Markierungen");
        } catch (IOException ioe) {
            logger.error("Failed to copy markersfrom internal clipboard", ioe);
            showError("Fehler beim Einfügen der Markierungen");
        }
        markerModel.fireTableDataChanged();
    }

    private void copyBBToExternalClipboardEvent() {
        try {
            List<Marker> markers = getSelectedMarkers();
            if (markers.isEmpty()) {
                showInfo("Keine Markierungen ausgewählt");
                return;
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Markierungen[/size][/u]\n\n");
            } else {
                buffer.append("[u]Markierungen[/u]\n\n");
            }

            buffer.append(new MarkerListFormatter().formatElements(markers, extended));

            if (extended) {
                buffer.append("\n[size=8]Erstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/size]\n");
            } else {
                buffer.append("\nErstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "\n");
            }

            String b = buffer.toString();
            StringTokenizer t = new StringTokenizer(b, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Markierungen benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = "Daten in Zwischenablage kopiert.";
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }

    public boolean deleteSelection(boolean pAsk) {
        List<Marker> selectedMarkers = getSelectedMarkers();

        if (pAsk) {
            String message = ((selectedMarkers.size() == 1) ? "Markierung " : (selectedMarkers.size() + " Markierungen ")) + "wirklich löschen?";
            if (selectedMarkers.isEmpty() || JOptionPaneHelper.showQuestionConfirmBox(this, message, "Markierungen löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        jxMarkerTable.editingCanceled(new ChangeEvent(this));
        MarkerManager.getSingleton().removeElements(sMarkerSet, selectedMarkers);
        markerModel.fireTableDataChanged();
        showSuccess(selectedMarkers.size() + " Markierung(en) gelöscht");
        return true;
    }

    public void deleteSelection() {
        deleteSelection(true);
    }

    private List<Marker> getSelectedMarkers() {
        final List<Marker> selectedMarkers = new LinkedList<>();
        int[] selectedRows = jxMarkerTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedMarkers;
        }
        for (Integer selectedRow : selectedRows) {
            Marker a = (Marker) MarkerManager.getSingleton().getAllElements(sMarkerSet).get(jxMarkerTable.convertRowIndexToModel(selectedRow));
            if (a != null) {
                selectedMarkers.add(a);
            }
        }
        return selectedMarkers;
    }
}
