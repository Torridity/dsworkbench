/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Note;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.note.NoteManager;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
        }
        if (!pSettings.isLayerVisible()) {
            return;
        }
        Graphics2D g2d = null;

        if (mLayer == null) {
            mLayer = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight() + 100, BufferedImage.BITMASK);
            g2d = mLayer.createGraphics();
        } else {
            g2d = (Graphics2D) mLayer.getGraphics();
            Composite c = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
            g2d.fillRect(0, 0, mLayer.getWidth(), mLayer.getHeight() - 100);
            g2d.setComposite(c);
        }
        //settings.setRowsToRender(pVisibleVillages[0].length);

        ImageUtils.setupGraphics(g2d);
        //setRenderedBounds((Rectangle2D.Double) pVirtualBounds.clone());

        renderNoteRows(pSettings, pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), g2d);
        g2d.dispose();
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        trans.setToTranslation((int) Math.floor(pSettings.getDeltaX()), (int) Math.floor(pSettings.getDeltaY()));
        pG2d.drawRenderedImage(mLayer, trans);
    }

    private void renderNoteRows(RenderSettings pSettings, int pCopyPosition, Graphics2D g2d) {
        //calculate first row that will be rendered
        int firstRow = 0;//(pSettings.getRowsToRender() > 0) ? 0 : pSettings.getVisibleVillages()[0].length - Math.abs(pSettings.getRowsToRender());
        ImageUtils.setupGraphics(g2d);
        for (int i = 0; i < 6; i++) {
            BufferedImage icon = ImageManager.getNoteIcon(i);
            g2d.drawImage(icon, i * 32, pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight() + 68, null);
        }
        //iterate through entire row
        int cnt = 0;
        for (int x = 0; x < pSettings.getVisibleVillages().length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < pSettings.getVisibleVillages()[0].length; y++) {
                cnt++;
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y - firstRow;
                int col = x;
                renderNoteField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pCopyPosition, g2d);
            }
        }
    }

    private void renderNoteField(Village v, int row, int col, int pFieldWidth, int pFieldHeight, int pCopyPosition, Graphics2D g2d) {
        if (v != null) {

            List<Note> notes = NoteManager.getSingleton().getNotesForVillage(v);
            int half = notes.size() / 2;
            int dx = -half * 2;
            int dy = -half * 2;
            for (Note n : notes) {
                int noteX = (int) Math.floor((double) col * pFieldWidth + pFieldWidth / 2.0 - 10);
                int noteY = (int) Math.floor((double) row * pFieldHeight + pFieldHeight / 2.0 - 23);
                int noteIcon = n.getMapMarker();
                // trans.scale(1.0 / zoom, 1.0 / zoom);
                //g2d.drawRenderedImage(icon, trans);
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
