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
package de.tor.tribes.ui.models;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.LinkedTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.editors.MultiBooleanTableCellEditor;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.tag.TagManager;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class TagLinkMatrixModel extends AbstractTableModel {

    private String[] columnNames = null;
    private Object[][] values = null;

    public TagLinkMatrixModel() {
        initModel();
    }

    private void initModel() {
        List<Integer> continents = new LinkedList<Integer>();
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            if (!continents.contains(v.getContinent())) {
                continents.add(v.getContinent());
            }
        }
        Collections.sort(continents);

        List<String> sColumns = new LinkedList<String>();
        sColumns.add("");
        sColumns.add("Alle");
        for (Integer iCont : continents) {
            String contString = "K" + ((iCont < 10) ? "0" : "") + iCont;
            sColumns.add(contString);
        }

        columnNames = sColumns.toArray(new String[]{});
        //show only non LinkedTags
        List<Tag> usableTags = new LinkedList<Tag>();
        for (ManageableType e : TagManager.getSingleton().getAllElements()) {
            if (!(e instanceof LinkedTag)) {
                usableTags.add((Tag) e);
            }
        }


        values = new Object[usableTags.size()][columnNames.length];
        for (int i = 0; i < usableTags.size(); i++) {
            for (int j = 0; j < columnNames.length; j++) {
                if (j == 0) {
                    values[i][j] = usableTags.get(i);
                } else {
                    values[i][j] = 0;
                }
            }
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Tag.class;
        }
        return Integer.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col > 0);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return values[rowIndex][columnIndex];
    }

    @Override
    public void setValueAt(Object pValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            values[rowIndex][columnIndex] = pValue;

        } else {
            int newValue = (Integer) pValue;
            if (newValue != 0) {
                int oldValue = (Integer) getValueAt(rowIndex, columnIndex);

                int setValue = newValue | oldValue;
                for (int i = 1; i < columnNames.length; i++) {
                    oldValue = (Integer) getValueAt(rowIndex, i);
                    if (MultiBooleanTableCellEditor.isOptionSet(oldValue, setValue)) {
                        //don't set
                        return;
                    }
                }
            }
            values[rowIndex][columnIndex] = pValue;
        }
    }

    public String getEquation() {
        StringBuilder group1 = new StringBuilder();
        StringBuilder group2 = new StringBuilder();
        StringBuilder group3 = new StringBuilder();
        StringBuilder group4 = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            //handle tags (rows)
            Tag t = (Tag) values[i][0];

            for (int j = 1; j < columnNames.length; j++) {
                String relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE1)) {
                    if (group1.length() > 0) {
                        group1.append(" UND ");
                    }
                    if (j == 1) {
                        relation += t.getName();
                    } else {
                        relation += "(" + t.getName() + " UND " + columnNames[j] + ")";
                    }
                    group1.append(relation);
                }
                relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE2)) {
                    if (group2.length() > 0) {
                        group2.append(" UND ");
                    }
                    if (j == 1) {
                        relation += t.getName();
                    } else {
                        relation += "(" + t.getName() + " UND " + columnNames[j] + ")";
                    }
                    group2.append(relation);
                }
                relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE3)) {
                    if (group3.length() > 0) {
                        group3.append(" UND ");
                    }
                    if (j == 1) {
                        relation += t.getName();
                    } else {
                        relation += "(" + t.getName() + " UND " + columnNames[j] + ")";
                    }
                    group3.append(relation);
                }
                relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE4)) {
                    if (group4.length() > 0) {
                        group4.append(" UND ");
                    }
                    if (j == 1) {
                        relation += t.getName();
                    } else {
                        relation += "(" + t.getName() + " UND " + columnNames[j] + ")";
                    }
                    group4.append(relation);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if (group1.length() > 0) {
            builder.append("(").append(group1.toString()).append(")");
        }
        if (group2.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" ODER ");
            }
            builder.append("(").append(group2.toString()).append(")");
        }
        if (group3.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" ODER ");
            }
            builder.append("(").append(group3.toString()).append(")");
        }
        if (group4.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" ODER ");
            }
            builder.append("(").append(group4.toString()).append(")");
        }
        return builder.toString();
    }

    public String getEquationAsHtml() {
        StringBuilder group1 = new StringBuilder();
        StringBuilder group2 = new StringBuilder();
        StringBuilder group3 = new StringBuilder();
        StringBuilder group4 = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            //handle tags (rows)
            Tag t = (Tag) values[i][0];

            for (int j = 1; j < columnNames.length; j++) {
                String relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE1)) {
                    if (group1.length() > 0) {
                        group1.append(" und ");
                    }
                    if (j == 1) {
                        relation += "in Gruppe '" + t.getName() + "'";
                    } else {
                        relation += "in Gruppe '" + t.getName() + "' sowie auf Kontinent " + columnNames[j];
                    }
                    group1.append(relation);
                }
                relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE2)) {
                    if (group2.length() > 0) {
                        group2.append(" und ");
                    }
                    if (j == 1) {
                        relation += "in Gruppe '" + t.getName() + "'";
                    } else {
                        relation += "in Gruppe '" + t.getName() + "' sowie auf Kontinent " + columnNames[j];
                    }
                    group2.append(relation);
                }
                relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE3)) {
                    if (group3.length() > 0) {
                        group3.append(" und ");
                    }
                    if (j == 1) {
                        relation += "in Gruppe '" + t.getName() + "'";
                    } else {
                        relation += "in Gruppe '" + t.getName() + "' sowie auf Kontinent " + columnNames[j];
                    }
                    group3.append(relation);
                }
                relation = "";
                if (MultiBooleanTableCellEditor.isOptionSet((Integer) values[i][j], MultiBooleanTableCellEditor.VALUE4)) {
                    if (group4.length() > 0) {
                        group4.append(" und ");
                    }
                    if (j == 1) {
                        relation += "in Gruppe '" + t.getName() + "'";
                    } else {
                        relation += "in Gruppe '" + t.getName() + "' sowie auf Kontinent " + columnNames[j];
                    }
                    group4.append(relation);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if (group1.length() > 0) {
            builder.append("<b><font color='#FF0000'>").append(group1.toString()).append("</font></b>");
        }
        if (group2.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" <BR/>oder<BR/> ");
            }
            builder.append("<b><font color='#1C7B38'>").append(group2.toString()).append("</font></b>");
        }
        if (group3.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" <BR/>oder<BR/> ");
            }
            builder.append("<b><font color='#0000FF'>").append(group3.toString()).append("</font></b>");
        }
        if (group4.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" <BR/>oder<BR/> ");
            }
            builder.append("<b><font color='#FF00FF'>").append(group4.toString()).append("</font></b>");
        }
        return builder.toString();

    }

    public static void main(String[] args) {
        String data = "<html>Der verknüpfte Tag ist<BR/><b><font color='#0000FF'>in Gruppe 'Off' und auf Kontinent K74 und in Gruppe 'Off_F' und auf Kontinent K74 und in Gruppe 'Off_R' und auf Kontinent K83</font></b><BR/> oder <BR/> <b><font color='#FF00FF'>in Gruppe 'Def_R' und in Gruppe 'Deff'</font></b></html>";
        JOptionPane.showMessageDialog(null, data);
    }
}
