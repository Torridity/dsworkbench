/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Note;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.note.NoteManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class NoteLayerRenderer extends AbstractBufferedLayerRenderer {

    private BufferedImage mLayer = null;
    private Point mapPos = null;
    private boolean shouldReset = true;
    private Hashtable<Village, NoteAnimator> animators = new Hashtable<Village, NoteAnimator>();

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (shouldReset) {
            //setRenderedBounds(null);
            setFullRenderRequired(true);
            shouldReset = false;
            if (mLayer != null && (MapPanel.getSingleton().getWidth() > mLayer.getWidth()
                    || MapPanel.getSingleton().getWidth() < mLayer.getWidth() - 100
                    || MapPanel.getSingleton().getHeight() > mLayer.getHeight()
                    || MapPanel.getSingleton().getHeight() < mLayer.getHeight() - 100)) {
                mLayer.flush();
                mLayer = null;
            }
        }
        if (!pSettings.isLayerVisible()) {
            return;
        }
        Graphics2D g2d = null;

        if (mLayer == null) {
            if (pSettings.getVisibleVillages().length * pSettings.getFieldWidth() > ImageManager.ID_NOTE_ICON_13 * 32) {
                mLayer = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight() + 100, BufferedImage.TRANSLUCENT);
            } else {
                mLayer = ImageUtils.createCompatibleBufferedImage(ImageManager.ID_NOTE_ICON_13 * 32, pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight() + 100, BufferedImage.TRANSLUCENT);
            }

            g2d = mLayer.createGraphics();
        } else {
            g2d = (Graphics2D) mLayer.getGraphics();
            Composite c = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
            g2d.fillRect(0, 0, mLayer.getWidth(), mLayer.getHeight() - 100);
            g2d.setComposite(c);
        }

        ImageUtils.setupGraphics(g2d);

        renderNoteRows(pSettings, mLayer.getHeight() - 100, g2d);
        g2d.dispose();
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        trans.setToTranslation((int) Math.floor(pSettings.getDeltaX()), (int) Math.floor(pSettings.getDeltaY()));
        pG2d.drawRenderedImage(mLayer, trans);
    }

    private void renderNoteRows(RenderSettings pSettings, int pCopyPosition, Graphics2D g2d) {
        //calculate first row that will be rendered
        int firstRow = 0;
        ImageUtils.setupGraphics(g2d);
        for (int i = 0; i <= ImageManager.ID_NOTE_ICON_13; i++) {
            BufferedImage icon = ImageManager.getNoteIcon(i);
            g2d.drawImage(icon, i * 32, pCopyPosition + 68, null);
        }
        Village lastVillageToDraw = null;
        int lastVillageRow = 0;
        int lastVillageCol = 0;
        //iterate through entire row
        int cnt = 0;
        Village currentMouseVillage = MapPanel.getSingleton().getVillageAtMousePos();
        for (int x = 0; x < pSettings.getVisibleVillages().length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < pSettings.getVisibleVillages()[0].length; y++) {
                cnt++;
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y - firstRow;
                int col = x;
                if (v != null && currentMouseVillage != null && v.equals(currentMouseVillage)) {
                    lastVillageToDraw = currentMouseVillage;
                    lastVillageRow = row;
                    lastVillageCol = col;
                } else {
                    renderNoteField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pCopyPosition, g2d);
                }
            }
        }
        renderNoteField(lastVillageToDraw, lastVillageRow, lastVillageCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pCopyPosition, g2d);
    }

    private void renderNoteField(Village v, int row, int col, int pFieldWidth, int pFieldHeight, int pCopyPosition, Graphics2D g2d) {
        if (v != null) {
            List<Note> notes = NoteManager.getSingleton().getNotesForVillage(v);
            int half = notes.size() / 2;
            int dx = -half * 2;
            int dy = -half * 2;

            if (NoteManager.getSingleton().getNotesForVillage(v).isEmpty()) {
                return;
            }

            NoteAnimator animator = animators.get(v);
            if (animator != null) {
                //update existing animator for village
                if (animator.isFinished()) {
                    animators.remove(animator.getVillage());
                } else {
                    animator.update(row, col, pFieldWidth, pFieldHeight, g2d);
                }
            } else {
                //animator is null and needs to be started if mouse pointer is located at village
                Village current = MapPanel.getSingleton().getVillageAtMousePos();
                if (current != null && current.equals(v)) {
                    animator = new NoteAnimator(v);
                    animators.put(v, animator);
                    animator.update(row, col, pFieldWidth, pFieldHeight, g2d);
                } else {
                    //village without animator
                    for (Note n : notes) {
                        int noteX = (int) Math.floor((double) col * pFieldWidth + pFieldWidth / 2.0 - 10);
                        int noteY = (int) Math.floor((double) row * pFieldHeight + pFieldHeight / 2.0 - 23);
                        int noteIcon = n.getMapMarker();
                        g2d.copyArea(noteIcon * 32, pCopyPosition + 68, 32, 32, noteX + dx - noteIcon * 32, noteY + dy - pCopyPosition - 68);
                        dx += 2;
                        dy += 2;
                    }
                }
            }
        }
    }

    public void reset() {
        shouldReset = true;
    }
}

class NoteAnimator {

    private int iDistance = 0;
    private boolean pRise = true;
    private boolean bFinished = false;
    private int deltaDeg = 0;
    private Village v = null;

    public NoteAnimator(Village pVillage) {
        v = pVillage;
    }

    public Village getVillage() {
        return v;
    }

    public boolean isFinished() {
        return bFinished;
    }

    public void update(int row, int col, int pFieldWidth, int pFieldHeight, Graphics2D g2d) {
        Village villageAtMousePos = MapPanel.getSingleton().getVillageAtMousePos();
        if (villageAtMousePos != null && villageAtMousePos.equals(v)) {
            pRise = true;
        } else {
            pRise = false;
        }
        if (pRise) {
            if (iDistance < 36) {
                iDistance += 5;
            }
        } else {
            iDistance -= 5;
            if (iDistance <= 0) {
                bFinished = true;
                iDistance = 0;
            }
        }
        double deg = 360 / NoteManager.getSingleton().getNotesForVillage(v).size();
        int cnt = 0;
        int centerX = (int) Math.floor((double) col * pFieldWidth + pFieldWidth / 2.0 - 8);
        int centerY = (int) Math.floor((double) row * pFieldHeight + pFieldHeight / 2.0 - 8);
        if (iDistance >= 35) {
            Color c = g2d.getColor();
            Composite com = g2d.getComposite();
            Area a = new Area();
            a.add(new Area(new Ellipse2D.Double(centerX - 50 + 8, centerY - 50 + 8, 100, 100)));
            a.subtract(new Area(new Ellipse2D.Double(centerX - 30 + 8, centerY - 30 + 8, 60, 60)));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2d.setColor(Constants.DS_BACK_LIGHT);
            g2d.fill(a);
            g2d.setComposite(com);
            g2d.setColor(Constants.DS_BACK);
            g2d.drawOval(centerX - 50 + 8, centerY - 50 + 8, 100, 100);
            g2d.drawOval(centerX - 30 + 8, centerY - 30 + 8, 60, 60);
            g2d.setColor(c);
        }
        for (Note note : NoteManager.getSingleton().getNotesForVillage(v)) {
            //take next degree
            double cd = cnt * deg + deltaDeg;
            int noteX = (int) Math.rint(centerX + iDistance * Math.cos(2 * Math.PI * cd / 360));
            int noteY = (int) Math.rint(centerY + iDistance * Math.sin(2 * Math.PI * cd / 360));
            int noteIcon = note.getMapMarker();
            //   g2d.copyArea(noteIcon * 32, pCopyPosition + 68, 32, 32, noteX - noteIcon * 32, noteY - pCopyPosition - 68);
            //g2d.drawImage(ImageManager.getNoteIcon(noteIcon).getScaledInstance(16, 16, BufferedImage.SCALE_DEFAULT), noteX, noteY, null);
            cnt++;
        }
        deltaDeg += 1;
        if (deltaDeg == 360) {
            deltaDeg = 0;
        }
    }
}
