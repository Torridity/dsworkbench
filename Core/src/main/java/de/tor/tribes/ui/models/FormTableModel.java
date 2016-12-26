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

import de.tor.tribes.types.drawing.AbstractForm;
import de.tor.tribes.types.drawing.Arrow;
import de.tor.tribes.types.drawing.Circle;
import de.tor.tribes.types.drawing.Line;
import de.tor.tribes.types.drawing.Rectangle;
import de.tor.tribes.util.map.FormManager;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class FormTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Boolean.class};
    private String[] colNames = new String[]{"Name", "Typ", "X", "Y", "Breite", "Höhe", "Sichtbar"};
    private boolean[] editableColumns = new boolean[]{true, false, true, true, true, true, false};

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableColumns[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public int getRowCount() {
        return FormManager.getSingleton().getAllElements().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AbstractForm f = (AbstractForm) FormManager.getSingleton().getAllElements().get(rowIndex);
        AbstractForm.FORM_TYPE type = f.getFormType();
        switch (columnIndex) {
            case 0: {
                String name = f.getFormName();
                if (name == null || name.length() == 0) {
                    return "Kein Name";
                }
                return name;
            }
            case 1: {
                switch (type) {
                    case ARROW:
                        return "Pfeil";
                    case CIRCLE:
                        return "Kreis";
                    case FREEFORM:
                        return "Freihandzeichnung";
                    case LINE:
                        return "Linie";
                    case RECTANGLE:
                        return "Rechteck";
                    default:
                        return "Text";
                }
            }
            case 2:
                return f.getBounds().x;
            case 3:
                return f.getBounds().y;
            case 4:
                switch (type) {
                    case ARROW:
                        return f.getBounds().width;
                    case CIRCLE:
                        return f.getBounds().width;
                    case FREEFORM:
                        return 0;
                    case LINE:
                        return f.getBounds().width;
                    case RECTANGLE:
                        return f.getBounds().width;
                    default:
                        return 0;
                }
            case 5:
                switch (type) {
                    case ARROW:
                        return f.getBounds().height;
                    case CIRCLE:
                        return f.getBounds().height;
                    case FREEFORM:
                        return 0;
                    case LINE:
                        return f.getBounds().height;
                    case RECTANGLE:
                        return f.getBounds().height;
                    default:
                        return 0;
                }
            default:
                return f.isVisibleOnMap();
        }
    }

    @Override
    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        AbstractForm f = (AbstractForm) FormManager.getSingleton().getAllElements().get(rowIndex);
        AbstractForm.FORM_TYPE type = f.getFormType();
        Integer v = null;
        try {
            if (o == null) {
                v = 0;
            } else {
                v = (Integer) o;
            }
            if (v < 0) {
                throw new NumberFormatException();
            }
        } catch (ClassCastException cce) {
            if (columnIndex != 0) {
                return;
            }
        } catch (NumberFormatException nfe) {
            if (columnIndex != 0) {
                return;
            }
        }
        switch (columnIndex) {
            case 2:
                f.setXPos(v);
                break;
            case 3:
                f.setYPos(v);
                break;
            case 4:
                switch (type) {
                    case ARROW:
                        ((Arrow) f).setXPosEnd(f.getXPos() + v);
                        break;
                    case CIRCLE:
                        ((Circle) f).setXPosEnd(f.getXPos() + v);
                        break;
                    case FREEFORM:
                        break;
                    case LINE:
                        ((Line) f).setXPosEnd(f.getXPos() + v);
                        break;
                    case RECTANGLE:
                        ((Rectangle) f).setXPosEnd(f.getXPos() + v);
                        break;
                    default:
                        break;
                }
                break;
            case 5:
                switch (type) {
                    case ARROW:
                        ((Arrow) f).setYPosEnd(f.getYPos() + v);
                        break;
                    case CIRCLE:
                        ((Circle) f).setYPosEnd(f.getYPos() + v);
                        break;
                    case FREEFORM:
                        break;
                    case LINE:
                        ((Line) f).setYPosEnd(f.getYPos() + v);
                        break;
                    case RECTANGLE:
                        ((Rectangle) f).setYPosEnd(f.getYPos() + v);
                        break;
                    default:
                        break;
                }
                break;
            default:
                f.setFormName((String) o);
                break;
        }
        FormManager.getSingleton().revalidate(true);
    }
}
