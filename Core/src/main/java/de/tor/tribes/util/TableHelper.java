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
package de.tor.tribes.util;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.Attack;
import de.tor.tribes.util.attack.AttackManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *
 * @author Torridity
 */
public class TableHelper {

    private final static List<Color> highlightColors = new ArrayList<>();
    private static boolean sortBySaturation = false;

    static {
        highlightColors.add(Color.decode("#7979FF"));
        highlightColors.add(Color.decode("#8ADCFF"));
        highlightColors.add(Color.decode("#36F200"));
        highlightColors.add(Color.decode("#FF8A8A"));
        highlightColors.add(Color.decode("#CF8D72"));
        highlightColors.add(Color.decode("#74BAAC"));
        highlightColors.add(Color.decode("#F5CAFF"));
        highlightColors.add(Color.decode("#6094DB"));
        highlightColors.add(Color.decode("#EAFFEF"));
    }

    private static Color getColorCode(int num) {
        int initialColorAngle = 60;
        float hueDiv = 0;
        float satDiv = 0;
        float valMax = 0;
        float satSec = 0;
        float hueSec = 0;
        float valSec = 0;
        if (sortBySaturation) {
            hueDiv = 10;
            satDiv = 6;
            valMax = 3;
            satSec = num % satDiv;
            hueSec = num / satDiv % hueDiv;
            valSec = num / satDiv / hueDiv;
        } else {
            hueDiv = 15;
            satDiv = 4;
            valMax = 3;
            hueSec = num % hueDiv;
            satSec = num / hueDiv % satDiv;
            valSec = num / hueDiv / satDiv;
        }
        float colorMaximum = hueDiv * satDiv * valMax;
        if (num > colorMaximum) {
            return new Color(50, 10, 100);
        } //we ran out of colors
        else {
            float h = -360 / hueDiv * hueSec + initialColorAngle;
            float s = 20 + 80 / (satDiv - 1) * satSec;
            float v = 100 - 20 * valSec;
            return Color.decode(hsv2rgb_hex(h, s, v));
        }
    }

    static String hsv2rgb_hex(float h, float s, float v) {

        while (h < 0) {
            h += 360;
        }
        while (h > 360) {
            h -= 360f;
        }

        s = (s <= 0) ? 0f : (s > 100f) ? 100f : s;

        v = (v <= 0) ? 0f : (v > 100f) ? 100f : v;

        h /= 60f;
        s /= 100f;
        v /= 100f;

        float hi = (float) Math.floor(h);
        float f = h - hi;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));

        float r, g, b;
        if (hi == 0) {
            r = v;
            g = t;
            b = p;
        } else if (hi == 1) {
            r = q;
            g = v;
            b = p;
        } else if (hi == 2) {
            r = p;
            g = v;
            b = t;
        } else if (hi == 3) {
            r = p;
            g = q;
            b = v;
        } else if (hi == 4) {
            r = t;
            g = p;
            b = v;
        } else {
            r = v;
            g = p;
            b = q;
        }

        return "#" + Integer.toHexString((int) Math.rint(r * 255)) + Integer.toHexString((int) Math.rint(g * 255)) + Integer.toHexString((int) Math.rint(b * 255));
    }

    public static int deleteSelectedRows(JXTable pTable) {
        int[] selrows = pTable.getSelectedRows();

        if (selrows == null || selrows.length == 0) {
            return 0;
        }

        List<Integer> rowsToDelete = new ArrayList<>();

        for (int row : selrows) {
            rowsToDelete.add(pTable.convertRowIndexToModel(row));
        }
        DefaultTableModel theModel = ((DefaultTableModel) pTable.getModel());
        List rowsToKeep = new ArrayList(theModel.getRowCount() - selrows.length);

        for (int i = 0; i < pTable.getRowCount(); i++) {
            int row = pTable.convertRowIndexToModel(i);
            if (!rowsToDelete.contains(row)) {
                //row should not be deleted
                List rowToKeep = new ArrayList(pTable.getColumnCount());
                for (int j = 0; j < pTable.getColumnCount(); j++) {
                    rowToKeep.add(theModel.getValueAt(row, j));
                }
                //add row to keep
                rowsToKeep.add(rowToKeep);
            }
        }
        //remove all rows fast
        theModel.setRowCount(0);

        //restore kept rows
        for (Object keptRow : rowsToKeep) {
            theModel.addRow(((List) keptRow).toArray());
        }
        return selrows.length;
    }

    public static void applyTableColoring(JXTable pTable, String pPlan, List<Highlighter> pHighlighters) {
        int cnt = 0;
        for (Highlighter h : pHighlighters) {
            pTable.removeHighlighter(h);
            cnt++;
        }
        pHighlighters.clear();
        int modelIdx = pTable.convertColumnIndexToModel(pTable.getSortedColumnIndex());
        List<ManageableType> attacks = AttackManager.getSingleton().getAllElements(pPlan);
        List<Object> sortedOjs = new ArrayList<>();
        for (ManageableType t : attacks) {
            Attack a = (Attack) t;
            Object idxElem = null;
            switch (modelIdx) {
                case 0:
                    idxElem = a.getSource().getTribe();
                    break;
                case 1:
                    idxElem = a.getSource().getTribe().getAlly();
                    break;
                case 2:
                    idxElem = a.getSource();
                    break;
                case 3:
                    idxElem = a.getTarget().getTribe();
                    break;
                case 4:
                    idxElem = a.getTarget().getTribe().getAlly();
                    break;
                case 5:
                    idxElem = a.getTarget();
                    break;
                case 6:
                    idxElem = a.getUnit();
                    break;
                case 7:
                    idxElem = a.getType();
                    break;
                case 8:
                    idxElem = a.getSendTime();
                    break;
                case 9:
                    idxElem = a.getArriveTime();
                    break;
                case 10:
                    idxElem = a.getArriveTime().getTime() - a.getSendTime().getTime();
                    break;
                case 11:
                    idxElem = a.isShowOnMap();
                    break;
                case 12:
                    idxElem = a.isTransferredToBrowser();
                    break;
            }

            if (idxElem != null) {
                if (!sortedOjs.contains(idxElem)) {
                    sortedOjs.add(idxElem);
                }
            }
        }

        Collections.sort(sortedOjs, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        int v = 0;
        cnt = 0;

        for (Object o : sortedOjs) {
            PatternPredicate patternPredicate0 = new PatternPredicate(Matcher.quoteReplacement(o.toString()), modelIdx);
            MattePainter mp = new MattePainter(getColorCode(v));
            PainterHighlighter h = new PainterHighlighter(patternPredicate0, mp);
            pHighlighters.add(h);
            cnt++;
            pTable.addHighlighter(h);
            v++;
        }
    }

    public static <T extends AbstractTableModel> T getTableModel(JXTable pTable) {
        return (T) pTable.getModel();
    }
}
