/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.components;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.renderer.GroupListCellRenderer;
import de.tor.tribes.util.GlobalOptions;
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
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *
 * @author Torridity
 */
public class GroupSelectionList extends JList implements GenericManagerListener {

    public GroupSelectionList() {
        setCellRenderer(new GroupListCellRenderer());

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

    private void fireIncrementEvent() {
        ListItem item = getItemAt(getSelectedIndex());
        item.setState(item.getState() + 1);
        checkStateAndRepaint();
    }

    private void fireDecrementEvent() {
        ListItem item = getItemAt(getSelectedIndex());
        item.setState(item.getState() - 1);
        checkStateAndRepaint();
    }

    private void fireResetEvent() {
        ListItem item = getItemAt(getSelectedIndex());
        if (getSelectedIndex() == 0) {
            item.setState(1);
            for (int i = 1; i < getModel().getSize(); i++) {
                getItemAt(i).resetState();
            }
            repaint();
        } else {
            item.resetState();
            checkStateAndRepaint();
        }
    }

    private void fireClickedEvent(Point pMousePos) {
        int idx = locationToIndex(pMousePos);
        ListItem item = getItemAt(idx);
        if (item.isSpecial()) {
            item.switchState();
            for (int i = 1; i < getModel().getSize(); i++) {
                ListItem item1 = getItemAt(i);
                item1.resetState();
            }
        } else {
            item.switchState();
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
        boolean oneSet = false;
        for (int i = 1; i < getModel().getSize(); i++) {
            ListItem item = getItemAt(i);
            if (item.getState() != 0) {
                oneSet = true;
                break;
            }
        }
        if (!oneSet) {
            getItemAt(0).setState(1);
        } else {
            getItemAt(0).setState(0);
        }
        repaint();
        getVillagesByEquation();
    }

    private void resetModel() {
        HashMap<Tag, Integer> storedState = new HashMap<Tag, Integer>();
        for (int i = 0; i < getModel().getSize(); i++) {
            ListItem item = getItemAt(i);
            storedState.put(item.getTag(), item.getState());
        }
        DefaultListModel model = new DefaultListModel();
        model.addElement(new ListItem(NoTag.getSingleton()));

        List<Tag> tags = new LinkedList<Tag>();
        for (int i = 1; i < 30; i++) {
            tags.add(new Tag("Test" + i, false));
        }

        // for (ManageableType element : TagManager.getSingleton().getAllElements()) {
        //Tag tag = (Tag) element;
        for (Tag tag : tags) {
            ListItem item = new ListItem(tag);
            if (storedState.get(tag) != null) {
                item.setState(storedState.get(tag));
            }
            model.addElement(item);
        }
        setModel(model);
        checkStateAndRepaint();
    }

    public List<Village> getValidVillages() {
        List<Village> result = new LinkedList<Village>();
        if (getItemAt(0).getState() != 0) {
            //NoTag selected
            Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
            for (Village v : villages) {
                if (TagManager.getSingleton().getTags(v).isEmpty()) {
                    result.add(v);
                }
            }
        } else {
            //(complex) relation selected
            result = getVillagesByEquation();
        }
        return result;
    }

    private List<Village> getVillagesByEquation() {
        long s = System.currentTimeMillis();
        StringBuilder b = new StringBuilder();
        boolean isFirst = true;
        for (int i = 1; i < getModel().getSize(); i++) {
            ListItem item = getItemAt(i);
            boolean ignore = false;
            if (!isFirst) {
                switch (item.getState()) {
                    case 1:
                        b.append(" && !");
                        break;
                    case 2:
                        b.append(" && ");
                        break;
                    case 3:
                        b.append(" || ");
                        break;
                    default:
                        ignore = true;
                }
            } else {
                if (item.getState() == 0) {//ignore
                    ignore = true;
                } else if (item.getState() == 1) {//NOT Tag 1
                    b.append("!");
                    isFirst = false;
                } else {
                    isFirst = false;
                }
            }
            if (!ignore) {
                b.append(item.getTag().toString()).append(" ");
            }
        }

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String equation = b.toString();

        /*  for (int i = 3; i < 30; i++) {
        equation = equation.replaceFirst(Pattern.quote("Test" + i), "false");
        }*/

        List<Village> result = new LinkedList<Village>();
        List<Tag> tags = new LinkedList<Tag>();
        for (int i = 1; i < 30; i++) {
            tags.add(new Tag("Test" + i, false));
        }
        try {

            for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
                for (Tag t : tags) {
                    equation = equation.replaceFirst(Pattern.quote(t.toString()), "true");
                }
                // System.out.println(equation);
                engine.eval("var b = eval(\"" + equation + "\")");
                if ((Boolean) engine.get("b")) {
                    result.add(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR");
        }
        System.out.println("DUR " + (System.currentTimeMillis() - s));
        System.out.println("S " + result.size());
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

    public static class ListItem {

        private Tag tag = null;
        private int state = 0;

        public ListItem(Tag pTag) {
            tag = pTag;
        }

        public void resetState() {
            state = 0;
        }

        public void setState(int pValue) {
            state = pValue;
            validateState();
        }

        private void validateState() {
            if (isSpecial()) {
                if (state > 1) {
                    state = 0;
                } else if (state < 0) {
                    state = 1;
                }
            } else {
                if (state > 3) {
                    state = 0;
                } else if (state < 0) {
                    state = 3;
                }
            }
        }

        public void switchState() {
            if (isSpecial()) {
                state = (state == 0) ? 1 : 0;
            } else {
                state = (state == 0) ? 1 : (state == 1) ? 2 : (state == 2) ? 3 : 0;
            }
        }

        public Tag getTag() {
            return tag;
        }

        public int getState() {
            return state;
        }

        public boolean isSpecial() {
            return tag.equals(NoTag.getSingleton());
        }
    }
}
