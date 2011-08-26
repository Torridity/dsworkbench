/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * InteractiveAttackPlanerPanel.java
 *
 * Created on Mar 7, 2011, 12:04:23 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.InteractiveAttackElement;
import de.tor.tribes.types.InteractiveAttackElementGroup;
import de.tor.tribes.types.InteractiveAttackSource;
import de.tor.tribes.types.InteractiveAttackTarget;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;

/**
 *
 * @author Torridity
 */
public class InteractiveAttackPlanerPanel extends javax.swing.JPanel {

    private BufferedImage back = null;
    private ArrayList<InteractiveAttackSource> mSources = new ArrayList<InteractiveAttackSource>();
    private ArrayList<InteractiveAttackTarget> mTargets = new ArrayList<InteractiveAttackTarget>();
    private InteractiveAttackElement mSelectedElement = null;
    private Point selectionStart = null;
    private Point selectionEnd = null;
    private InteractiveAttackElementGroup selectionGroup = null;
    private Point mPopupLocation = null;

    /** Creates new form InteractiveAttackPlanerPanel */
    public InteractiveAttackPlanerPanel() {
        initComponents();
        ToolTipManager.sharedInstance().registerComponent(this);
        try {
            back = ImageIO.read(new File("graphics/skins/default/default_underground.png"));
            selectionGroup = new InteractiveAttackElementGroup();
            for (int i = 0; i < 20; i++) {
                mTargets.add(new InteractiveAttackTarget(new DummyVillage((short) 10, (short) (i * 3)), new Point2D.Double(i * 10, 0)));
                selectionGroup.addElement(mTargets.get(i));
            }
            for (int i = 0; i < 10; i++) {
                mSources.add(new InteractiveAttackSource(new DummyVillage((short) (i * 2), (short) (i * 3)), new Point2D.Double(i * 20, 50), "graphics/icons/snob.png"));
                selectionGroup.addElement(mSources.get(i));
            }
            for (int i = 0; i < 10; i++) {
                mSources.add(new InteractiveAttackSource(new DummyVillage((short) (i * 2), (short) (i * 3)), new Point2D.Double(i * 20, 50), "graphics/icons/ram.png"));
                selectionGroup.addElement(mSources.get(i));
            }
        } catch (Exception e) {
        }

        final InteractiveAttackPlanerPanel thisPanel = this;
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    mPopupLocation = e.getPoint();
                    jPopupMenu1.show(thisPanel, e.getX(), e.getY());
                } else {
                    selectionGroup = null;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (selectionGroup == null) {
                    mSelectedElement = getElementAtPosition(e.getPoint());
                    if (mSelectedElement == null) {
                        selectionStart = e.getPoint();
                        selectionEnd = e.getPoint();
                        return;
                    }
                    if (mSelectedElement instanceof InteractiveAttackSource) {
                        ((InteractiveAttackSource) mSelectedElement).unlinkTarget();
                    }
                } else {
                    InteractiveAttackElement elem = getElementAtPosition(e.getPoint());
                    if (elem == null || selectionGroup.containsElement(elem)) {
                        selectionGroup.resetPosition();
                        selectionGroup.movePosition(e.getX(), e.getY());
                    } else {
                        selectionGroup = null;
                        mSelectedElement = elem;
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    return;
                }
                if (mSelectedElement != null) {
                    if (mSelectedElement instanceof InteractiveAttackSource) {
                        InteractiveAttackTarget target = getIntersectedTarget((InteractiveAttackSource) mSelectedElement);
                        if (target != null && target.isLinkageAllowed(mSelectedElement)) {
                            target.linkSource((InteractiveAttackSource) mSelectedElement);
                            ((InteractiveAttackSource) mSelectedElement).linkTarget(target);
                        }
                    }
                }
                if (selectionGroup == null) {
                    List<InteractiveAttackElement> selection = getSelection();
                    if (!selection.isEmpty()) {
                        selectionGroup = new InteractiveAttackElementGroup();
                        for (InteractiveAttackElement elem : selection) {
                            selectionGroup.addElement(elem);
                        }
                    }
                } else {
                    if (selectionGroup.isSourcesOnly()) {
                        //link only if this group just contains sources
                        for (InteractiveAttackElement element : selectionGroup.getGroupElements()) {
                            if (element instanceof InteractiveAttackSource) {
                                InteractiveAttackTarget target = getTargetAtPosition(e.getPoint());
                                if (target != null && target.isLinkageAllowed(element)) {
                                    target.linkSource((InteractiveAttackSource) element);
                                    ((InteractiveAttackSource) element).linkTarget(target);
                                }
                            }
                        }
                    }
                }
                selectionStart = null;
                selectionEnd = null;
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectionGroup == null) {
                    if (mSelectedElement != null) {
                        mSelectedElement.movePosition(e.getX(), e.getY());
                        /* if (mSelectedElement instanceof InteractiveAttackSource) {
                        for (int i = mTargets.size() - 1; i >= 0; i--) {
                        if (mTargets.get(i).isLinkageAllowed(mSelectedElement)) {
                        mTargets.get(i).applyForce(mSelectedElement.getDelta().x, mSelectedElement.getDelta().y, mSelectedElement.getPosition());
                        }
                        }
                        }*/
                        repaint();
                    }
                    if (selectionStart != null) {
                        selectionEnd = e.getPoint();
                        repaint();
                    }
                } else {
                    selectionGroup.movePosition(e.getX(), e.getY());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (selectionGroup == null) {
                    mSelectedElement = getElementAtPosition(e.getPoint());
                    repaint();
                }
            }
        });
    }

    public List<InteractiveAttackElement> getSelection() {
        List<InteractiveAttackElement> selection = new LinkedList<InteractiveAttackElement>();
        if (selectionStart == null || selectionEnd == null) {
            return selection;
        }
        int x = ((selectionStart.x < selectionEnd.x) ? selectionStart.x : selectionEnd.x);
        int y = ((selectionStart.y < selectionEnd.y) ? selectionStart.y : selectionEnd.y);
        int w = (int) Math.rint(Math.abs(selectionStart.x - selectionEnd.x));
        int h = (int) Math.rint(Math.abs(selectionStart.y - selectionEnd.y));
        Rectangle selectionRect = new Rectangle(x, y, w, h);
        for (int i = mSources.size() - 1; i >= 0; i--) {
            if (selectionRect.intersects(mSources.get(i).getBounds()) && !mSources.get(i).isLinked()) {
                selection.add(mSources.get(i));
            }
        }
        for (int i = mTargets.size() - 1; i >= 0; i--) {
            if (selectionRect.intersects(mTargets.get(i).getBounds())) {
                selection.add(mTargets.get(i));
            }
        }
        return selection;
    }

    public InteractiveAttackElement getElementAtPosition(Point pPoint) {
        try {
            for (int i = mSources.size() - 1; i >= 0; i--) {
                if (mSources.get(i).getBounds().contains(pPoint)) {
                    return mSources.get(i);
                }
            }
            for (int i = mTargets.size() - 1; i >= 0; i--) {
                if (mTargets.get(i).getBounds().contains(pPoint)) {
                    return mTargets.get(i);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public InteractiveAttackTarget getTargetAtPosition(Point pPoint) {
        for (int i = mTargets.size() - 1; i >= 0; i--) {
            if (mTargets.get(i).getBounds().contains(pPoint)) {
                return mTargets.get(i);
            }
        }
        return null;
    }

    public InteractiveAttackTarget getIntersectedTarget(InteractiveAttackSource pSource) {
        for (InteractiveAttackTarget e : mTargets) {
            if (e.getBounds().intersects(pSource.getBounds())) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String getToolTipText() {
        InteractiveAttackElement elem = getElementAtPosition(getMousePosition());
        if (elem != null) {
            return elem.getVillage().toString();
        }
        return "";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight())));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        if (selectionGroup != null) {
            selectionGroup.render(g2d);
        }
        for (InteractiveAttackTarget e : mTargets) {
            if (!e.equals(mSelectedElement)) {
                boolean linkable = false;
                for (InteractiveAttackSource s : mSources) {
                    if (e.supportsAttackSourceLinkage(s)) {
                        linkable = true;
                        continue;
                    }
                }
                Composite c = g2d.getComposite();
                if (!linkable) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                }

                if (selectionGroup != null && selectionGroup.containsElement(e)) {
                    e.render(g2d, true);
                } else {
                    e.render(g2d, e.supportsAttackSourceLinkage(mSelectedElement));
                }
                g2d.setComposite(c);
            }
        }
        for (InteractiveAttackSource e : mSources) {
            if (!e.equals(mSelectedElement)) {
                boolean linkable = false;
                for (InteractiveAttackTarget t : mTargets) {
                    if (t.supportsAttackSourceLinkage(e)) {
                        linkable = true;
                        continue;
                    }
                }
                Composite c = g2d.getComposite();
                if (!linkable) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                }

                if (selectionGroup != null && selectionGroup.containsElement(e)) {
                    e.render(g2d, true);
                } else {
                    if (!e.isLinked()) {
                        if (mSelectedElement instanceof InteractiveAttackTarget) {
                            e.render(g2d, ((InteractiveAttackTarget) mSelectedElement).supportsAttackSourceLinkage(e));
                        } else {
                            e.render(g2d);
                        }
                    }
                }
                g2d.setComposite(c);
            }
        }
        if (mSelectedElement != null) {
            g2d.drawString(mSelectedElement.toString(), 10, 10);
            mSelectedElement.render(g2d, true);
        }
        if (selectionStart != null) {
            Point s = selectionStart;
            Point e = selectionEnd;
            int x = ((s.x < e.x) ? s.x : e.x);
            int y = ((s.y < e.y) ? s.y : e.y);
            int w = (int) Math.rint(Math.abs(s.x - e.x));
            int h = (int) Math.rint(Math.abs(s.y - e.y));
            g2d.setColor(Color.YELLOW);
            g2d.drawRect(x, y, w, h);
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

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireEvent1(evt);
            }
        });
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setText("jMenuItem2");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireEvent2(evt);
            }
        });
        jPopupMenu1.add(jMenuItem2);

        setToolTipText("Test");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 434, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireEvent1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireEvent1
        groupSourceTarget();
    }//GEN-LAST:event_fireEvent1

    private void fireEvent2(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireEvent2
        groupSquared();
    }//GEN-LAST:event_fireEvent2

    private void groupSquared() {
        if (selectionGroup == null) {
            return;
        }
        int colElem = (int) Math.ceil(Math.sqrt(selectionGroup.getGroupElements().length));
        int dx = (colElem - 1) * 35;
        int row = 0;
        int col = 0;
        int x = mPopupLocation.x - dx;
        int y = mPopupLocation.y;
        for (InteractiveAttackElement elem : selectionGroup.getGroupElements()) {
            elem.setPosition(x, y);
            x += 70;
            col++;
            if (col == colElem) {
                col = 0;
                row++;
                y += 50;
                x = mPopupLocation.x - dx;
            }
        }
        mPopupLocation = null;
        repaint();
    }

    private void groupSourceTarget() {
        InteractiveAttackElement elem = getElementAtPosition(mPopupLocation);
        mPopupLocation = null;
        if (elem instanceof InteractiveAttackSource) {
            List<InteractiveAttackElement> targets = new LinkedList<InteractiveAttackElement>();
            for (int i = mTargets.size() - 1; i >= 0; i--) {
                if (mTargets.get(i).isLinkageAllowed(elem)) {
                    targets.add(mTargets.get(i));
                }
            }
            double deltaDeg = 360 / targets.size();
            int cnt = 0;
            int diameter = 60;
            for (InteractiveAttackElement interactiveAttackTarget : targets) {
                double cd = cnt * deltaDeg;
                int posX = (int) Math.rint(elem.getPosition().x + diameter * Math.cos(2 * Math.PI * cd / 360) - interactiveAttackTarget.getBounds().getWidth() / 2 + elem.getBounds().getWidth() / 2);
                int posY = (int) Math.rint(elem.getPosition().y + diameter * Math.sin(2 * Math.PI * cd / 360) - interactiveAttackTarget.getBounds().getHeight() / 2 + elem.getBounds().getHeight() / 2);
                interactiveAttackTarget.setPosition(posX, posY);
                cnt++;
            }
            if (selectionGroup == null) {
                selectionGroup = new InteractiveAttackElementGroup();
            }

            selectionGroup.removeAllElements();
            selectionGroup.addElement(elem);
            selectionGroup.addElements(targets);

            repaint();
        } else if (elem instanceof InteractiveAttackTarget) {
            List<InteractiveAttackElement> sources = new LinkedList<InteractiveAttackElement>();
            for (int i = mSources.size() - 1; i >= 0; i--) {
                if (elem.isLinkageAllowed(mSources.get(i))) {
                    sources.add(mSources.get(i));
                }
            }
            double deltaDeg = 360 / sources.size();
            int cnt = 0;
            int diameter = 60;
            for (InteractiveAttackElement interactiveAttackSource : sources) {
                double cd = cnt * deltaDeg;
                int posX = (int) Math.rint(elem.getPosition().x + diameter * Math.cos(2 * Math.PI * cd / 360) - interactiveAttackSource.getBounds().getWidth() / 2 + elem.getBounds().getWidth() / 2);
                int posY = (int) Math.rint(elem.getPosition().y + diameter * Math.sin(2 * Math.PI * cd / 360) - interactiveAttackSource.getBounds().getHeight() / 2 + elem.getBounds().getHeight() / 2);
                interactiveAttackSource.setPosition(posX, posY);
                cnt++;
            }
            if (selectionGroup == null) {
                selectionGroup = new InteractiveAttackElementGroup();
            }

            selectionGroup.removeAllElements();
            selectionGroup.addElement(elem);
            selectionGroup.addElements(sources);

            repaint();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPopupMenu jPopupMenu1;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(300, 300);
        f.add(new InteractiveAttackPlanerPanel());
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
