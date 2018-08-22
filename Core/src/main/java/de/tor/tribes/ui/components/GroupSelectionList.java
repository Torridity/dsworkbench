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

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.renderer.GroupListCellRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.TagUtils;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author Torridity
 */
public class GroupSelectionList extends IconizedList implements GenericManagerListener {

    private Village[] relevantVillages = null;
    private boolean expertSelection = false;
    private GroupListCellRenderer renderer = new GroupListCellRenderer();

    public GroupSelectionList(String pResourceURL) {
        super(pResourceURL);

        setCellRenderer(renderer);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    fireDecrementEvent();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    fireIncrementEvent();
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    fireResetEvent();
                } else if (e.getKeyCode() == KeyEvent.VK_O) {
                    fireSetStateEvent(ListItem.RELATION_TYPE.OR);
                } else if (e.getKeyCode() == KeyEvent.VK_U) {
                    fireSetStateEvent(ListItem.RELATION_TYPE.AND);
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    fireSetStateEvent(ListItem.RELATION_TYPE.NOT);
                } else if (e.getKeyCode() == KeyEvent.VK_I) {
                    fireSetStateEvent(ListItem.RELATION_TYPE.DISABLED);
                } else if (e.getKeyCode() == KeyEvent.VK_H) {
                    JOptionPaneHelper.showInformationBox(GroupSelectionList.this, getRelationAsPlainText(), "Information");
                }
            }
        });
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        fireClickedEvent(e.getPoint());
                    } else {
                        fireResetEvent(e.getPoint());
                    }
                }
            }
        });
        TagManager.getSingleton().addManagerListener(GroupSelectionList.this);
    }

    public void setRelevantVillages(Village[] pVillages) {
        relevantVillages = pVillages;
    }

    private void fireIncrementEvent() {
        ListItem item = getItemAt(getSelectedIndex());
        item.incrementState();
        checkStateAndRepaint();
    }

    private void fireDecrementEvent() {
        ListItem item = getItemAt(getSelectedIndex());
        item.decrementState();
        checkStateAndRepaint();
    }

    private void fireSetStateEvent(ListItem.RELATION_TYPE pType) {
        ListItem item = getItemAt(getSelectedIndex());
        if (item.isNoTagEntry()) {
            if (!pType.equals(ListItem.RELATION_TYPE.DISABLED)) {
                for (int i = 1; i < getModel().getSize(); i++) {
                    ListItem item1 = getItemAt(i);
                    item1.resetState();
                }
            } else {
                for (int i = 1; i < getModel().getSize(); i++) {
                    ListItem item1 = getItemAt(i);
                    item1.setState(ListItem.RELATION_TYPE.OR);
                }
                getItemAt(0).resetState();
            }
        } else {
            item.setState(pType);
            getItemAt(0).resetState();
        }
        checkStateAndRepaint();
    }

    private void fireResetEvent() {
        ListItem item = getItemAt(getSelectedIndex());
        if (getSelectedIndex() == 0) {
            item.setState(ListItem.RELATION_TYPE.AND);
            for (int i = 1; i < getModel().getSize(); i++) {
                getItemAt(i).resetState();
            }
            repaint();
            updateBySelection();
        } else {
            item.resetState();
            checkStateAndRepaint();
        }
    }

    private void fireClickedEvent(Point pMousePos) {
        int idx = locationToIndex(pMousePos);
        ListItem item = getItemAt(idx);
        if (item.isNoTagEntry()) {
            item.incrementState();
            for (int i = 1; i < getModel().getSize(); i++) {
                ListItem item1 = getItemAt(i);
                item1.resetState();
            }
        } else {
            item.incrementState();
            getItemAt(0).resetState();
        }
        checkStateAndRepaint();
    }

    private void fireResetEvent(Point pMousePos) {
        int idx = locationToIndex(pMousePos);
        getItemAt(idx).resetState();
        checkStateAndRepaint();
    }

    private void checkStateAndRepaint() {
        if (getElementCount() == 0) {
            repaint();
            return;
        }

        if (getItemAt(0).getState() == ListItem.RELATION_TYPE.DISABLED) {//only check if first element is disabled
            boolean oneSet = false;
            for (int i = 1; i < getModel().getSize(); i++) {
                ListItem item = getItemAt(i);
                if (item.getState() != ListItem.RELATION_TYPE.DISABLED) {
                    oneSet = true;
                    break;
                }
            }

            if (!oneSet) {//none set, put all to 'OR'
                getItemAt(0).setState(ListItem.RELATION_TYPE.AND);
            }
        } else {
            for (int i = 1; i < getModel().getSize(); i++) {
                ListItem item = getItemAt(i);
                item.setState(ListItem.RELATION_TYPE.OR);
            }
        }
        repaint();
        updateBySelection();
    }

    private void updateBySelection() {
        int[] selection = getSelectedIndices();
        getSelectionModel().setValueIsAdjusting(true);
        getSelectionModel().clearSelection();
        setSelectedIndices(selection);
        getSelectionModel().setValueIsAdjusting(false);
    }

    public void setExpertSelection(boolean expertSelection) {
        this.expertSelection = expertSelection;
        renderer.setExpertMode(expertSelection);
        setToolTipText("");
        repaint();
    }

    public boolean isExpertSelection() {
        return expertSelection;
    }

    private void resetModel() {
        HashMap<Tag, ListItem.RELATION_TYPE> storedState = new HashMap<>();
        for (int i = 0; i < getModel().getSize(); i++) {
            ListItem item = getItemAt(i);
            storedState.put(item.getTag(), item.getState());
        }
        DefaultListModel model = new DefaultListModel();

        for (Tag tag : TagUtils.getTags(Tag.CASE_INSENSITIVE_ORDER)) {
            ListItem item = new ListItem(tag);
            if (storedState.get(tag) != null) {
                item.setState(storedState.get(tag));
            }
            model.addElement(item);
        }
        setModel(model);
        checkStateAndRepaint();
    }

    @Override
    public void setListData(Object[] listData) {
        super.setListData(listData);
        checkStateAndRepaint();
    }

    public boolean isVillageValid(Village pVillage) {
        if (expertSelection && getItemAt(0).getState() != ListItem.RELATION_TYPE.DISABLED) {
            //NoTag selected
            if (TagManager.getSingleton().getTags(pVillage).isEmpty()) {
                return true;
            }
        }
        //(complex) relation selected
        relevantVillages = new Village[]{pVillage};

        return !getValidVillages().isEmpty();
        //return !getVillagesByEquation().isEmpty();
    }

    public List<Village> getValidVillages() {
        List<Village> result = new LinkedList<>();
        if (relevantVillages == null) {
            relevantVillages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        }

        if (getElementCount() == 0) {
            return result;
        }

        if (!isVisible()) {
            CollectionUtils.addAll(result, relevantVillages);
            return result;
        }


        if (!expertSelection) {
            Object[] values = getSelectedValues();
            if (values.length == 0) {
                return result;
            }
            List<Tag> selection = new LinkedList<>();
            for (Object v : values) {
                ListItem i = (ListItem) v;
                selection.add(i.getTag());
            }
            for (Village village : relevantVillages) {
                boolean use = false;
                for (Tag t : selection) {
                    if (t.tagsVillage(village.getId())) {
                        use = true;
                        break;
                    }
                }
                if (use) {//all tags are valid for this village
                    result.add(village);
                }
            }
        } else {
            if (getItemAt(0).getState() != ListItem.RELATION_TYPE.DISABLED) {
                //NoTag selected
                for (Village v : relevantVillages) {
                    if (TagManager.getSingleton().getTags(v).isEmpty()) {
                        result.add(v);
                    }
                }
            } else {
                //(complex) relation selected
                result = getVillagesByEquation();
            }
        }
        return result;
    }

    private List<Village> getVillagesByEquation() {
        StringBuilder b = new StringBuilder();
        boolean isFirst = true;
        List<Tag> relevantTags = new LinkedList<>();
        for (int i = 1; i < getModel().getSize(); i++) {
            ListItem item = getItemAt(i);
            boolean ignore = false;
            if (!isFirst) {
                switch (item.getState()) {
                    case NOT:
                        b.append(" && !");
                        break;
                    case AND:
                        b.append(" && ");
                        break;
                    case OR:
                        b.append(" || ");
                        break;
                    default:
                        ignore = true;
                }
            } else {
                if (item.getState() == ListItem.RELATION_TYPE.DISABLED) {//ignore
                    ignore = true;
                } else if (item.getState() == ListItem.RELATION_TYPE.NOT) {//NOT Tag 1
                    b.append("!");
                    isFirst = false;
                } else {
                    isFirst = false;
                }
            }

            if (!ignore) {
                b.append(item.getTag().toString()).append(" ");
                relevantTags.add(item.getTag());
            }
        }

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String baseEquation = b.toString();

        List<Village> result = new LinkedList<>();
        try {
            if (relevantVillages == null) {
                relevantVillages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
            }
            for (Village v : relevantVillages) {
                String evaluationEquation = baseEquation;
                for (Tag tag : relevantTags) {
                    evaluationEquation = evaluationEquation.replaceFirst(Pattern.quote(tag.toString()), Boolean.toString(tag.tagsVillage(v.getId())));
                }
                engine.eval("var b = eval(\"" + evaluationEquation + "\")");
                if ((Boolean) engine.get("b")) {
                    result.add(v);
                }
            }
        } catch (Exception e) {
            //no result
        }
        return result;
    }

    public String getRelationAsPlainText() {
        StringBuilder b = new StringBuilder();
        if (expertSelection) {
            b.append("Alle Dörfer die ");
            boolean isFirst = true;
            for (int i = 0; i < getElementCount(); i++) {
                ListItem current = (ListItem) getElementAt(i);
                switch (current.getState()) {
                    case NOT:
                        b.append("NICHT in Gruppe '").append(current.getTag()).append("' ");
                        isFirst = false;
                        break;
                    case AND:
                        if (isFirst) {
                            b.append("in Gruppe '").append(current.getTag()).append("' ");
                            isFirst = false;
                        } else {
                            b.append("UND in Gruppe '").append(current.getTag()).append("' ");
                        }
                        break;
                    case OR:
                        if (isFirst) {
                            isFirst = false;
                            b.append("in Gruppe '").append(current.getTag()).append("' ");
                        } else {
                            b.append("ODER in Gruppe '").append(current.getTag()).append("' ");
                        }
                        break;
                }
            }
            b.append("sind");
        } else {
            Object[] values = getSelectedValues();
            if (values.length == 0) {
                b.append("Keine Einträge gewählt");
            } else {
                List<Tag> selection = new LinkedList<>();
                for (Object v : values) {
                    ListItem i = (ListItem) v;
                    selection.add(i.getTag());
                }
                b.append("Alle Dörfer in ");
                b.append((selection.size() == 1) ? "der Gruppe " : "den Gruppen ");
                b.append("'").append(selection.get(0)).append("'");
                for (int i = 1; i < selection.size(); i++) {
                    b.append(" UND ").append("'").append(selection.get(i)).append("'");
                }
            }
        }

        return b.toString();
    }

    private ListItem getItemAt(int pIndex) {
        return (ListItem) getModel().getElementAt(pIndex);
    }

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        resetModel();
    }

    public static class ListItem {

        public enum RELATION_TYPE {

            OR(0), AND(1), NOT(2), DISABLED(3);
            private int value = 0;

            RELATION_TYPE(int pValue) {
                value = pValue;
            }

            public int getValue() {
                return value;
            }

            public static RELATION_TYPE getRelation(int pValue) {
                switch (pValue) {
                    case 0:
                        return RELATION_TYPE.OR;
                    case 1:
                        return RELATION_TYPE.AND;
                    case 2:
                        return RELATION_TYPE.NOT;
                    default:
                        return RELATION_TYPE.DISABLED;
                }
            }
        }
        private Tag tag = null;
        private RELATION_TYPE state = RELATION_TYPE.DISABLED;

        public ListItem(Tag pTag) {
            tag = pTag;
        }

        public void resetState() {
            state = RELATION_TYPE.DISABLED;
        }

        public void setState(RELATION_TYPE pState) {
            state = pState;
        }

        public void incrementState() {
            if (isNoTagEntry()) {
                switch (state) {
                    case DISABLED:
                        state = RELATION_TYPE.AND;
                        break;
                    default:
                        state = RELATION_TYPE.DISABLED;
                        break;
                }
            } else {
                switch (state) {
                    case DISABLED:
                        state = RELATION_TYPE.OR;
                        break;
                    default:
                        state = RELATION_TYPE.getRelation(state.getValue() + 1);
                        break;
                }
            }
        }

        public void decrementState() {
            if (isNoTagEntry()) {
                switch (state) {
                    case DISABLED:
                        state = RELATION_TYPE.AND;
                        break;
                    default:
                        state = RELATION_TYPE.DISABLED;
                        break;
                }
            } else {
                switch (state) {
                    case OR:
                        state = RELATION_TYPE.DISABLED;
                        break;
                    default:
                        state = RELATION_TYPE.getRelation(state.getValue() - 1);
                        break;
                }
            }
        }

        public Tag getTag() {
            return tag;
        }

        public RELATION_TYPE getState() {
            return state;
        }

        public boolean isNoTagEntry() {
            return tag.equals(NoTag.getSingleton());
        }
    }
}
