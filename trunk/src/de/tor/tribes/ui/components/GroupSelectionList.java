/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.components;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.renderer.GroupListCellRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.TagUtils;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.UIManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

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
        if (item.isSpecial()) {
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

        ///  System.out.println(getVillagesByEquation());
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
        if (isExpertSelection()) {
            setToolTipText("<html>In dieser Liste k&ouml;nnen Gruppen beliebig kombiniert werden, "
                    + "um die darin enthaltenen D&ouml;rfer anzuzeigen.<br/>"
                    + "M&ouml;gliche Verkn&uuml;pfungen sind dabei:"
                    + "<ul>"
                    + "<li>NICHT: D&ouml;rfer d&uuml;rfen nicht in dieser Gruppe sein.</li>"
                    + "<li>ODER: D&ouml;rfer k&ouml;nnen in dieser oder einer anderen Gruppe sein.</li>"
                    + "<li>UND: D&ouml;rfer m&uuml;ssen in dieser Gruppe und allen anderen mit UND gekennzeichneten Gruppen sein.</li>"
                    + "<li>OHNE: Diese Gruppe wird nicht ber&uuml;cksichtigt.</li>"
                    + "</ul>"
                    + "Der erste Eintrag der Liste beinhaltet alle D&ouml;rfer die in keiner Gruppe enthalten sind. Ist dieser Eintrag gew&auml;hlt, "
                    + "werden alle anderen Eintr&auml;ge deaktiviert.<br/>"
                    + "Bei der Verkn&uuml;pfung gilt stets, dass UND-Verkn&uuml;pfungen immer zuerst aufgel&ouml;st werden, also Vorrang haben.<br/>"
                    + "&Auml;nderung von Verkn&uuml;pfungen:"
                    + "<ul>"
                    + "<li>Doppelklick (links) oder Pfeil rechts: N&auml;chste Verkn&uuml;pfung</li>"
                    + "<li>Pfeil rechts: Vorherige Verkn&uuml;pfung</li>"
                    + "<li>Doppelklick (rechts) oder ENTF: Gruppe ignorieren</li>"
                    + "<li>ENTF auf 'Keine Gruppe': Alle Gruppen ignorieren, 'Keine Gruppe' ausw&auml;hlen</li>"
                    + "</ul>"
                    + "</html>");
        } else {
            setToolTipText("");
        }
        repaint();
    }

    public boolean isExpertSelection() {
        return expertSelection;
    }

    private void resetModel() {
        HashMap<Tag, ListItem.RELATION_TYPE> storedState = new HashMap<Tag, ListItem.RELATION_TYPE>();
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
        if (isExpertSelection() && getItemAt(0).getState() != ListItem.RELATION_TYPE.DISABLED) {
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
        List<Village> result = new LinkedList<Village>();
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


        if (!isExpertSelection()) {
            Object[] values = getSelectedValues();
            if (values.length == 0) {
                return result;
            }
            List<Tag> selection = new LinkedList<Tag>();
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
        List<Tag> relevantTags = new LinkedList<Tag>();
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


        List<Village> result = new LinkedList<Village>();
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }

        new GroupListCellRenderer().view();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        DataHolder.getSingleton().loadData(false);
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        GlobalOptions.loadUserData();
    }

    public static class ListItem {

        public enum RELATION_TYPE {

            OR(0), AND(1), NOT(2), DISABLED(3);
            private int value = 0;

            private RELATION_TYPE(int pValue) {
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
            if (isSpecial()) {
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
            if (isSpecial()) {
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

        public boolean isSpecial() {
            return tag.equals(NoTag.getSingleton());
        }
    }
}
