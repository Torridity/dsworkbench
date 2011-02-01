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
        if (v != null && v.isVisibleOnMap()) {
            List<Note> notes = NoteManager.getSingleton().getNotesForVillage(v);
            int half = notes.size() / 2;
            int dx = -half * 2;
            int dy = -half * 2;

            if (NoteManager.getSingleton().getNotesForVillage(v).isEmpty()) {
                return;
            }

            //render notes
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

    public void reset() {
        shouldReset = true;
    }
}
