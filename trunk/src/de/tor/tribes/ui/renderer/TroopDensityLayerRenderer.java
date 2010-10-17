/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Torridity
 */
public class TroopDensityLayerRenderer extends AbstractDirectLayerRenderer {

    @Override
    public void performRendering(Rectangle2D pVirtualBounds, Village[][] pVisibleVillages, Graphics2D pG2d) {
        RenderSettings settings = getRenderSettings(pVirtualBounds);

        settings.setRowsToRender(pVisibleVillages[0].length);

        //Set new bounds
        setRenderedBounds((Rectangle2D.Double) pVirtualBounds.clone());
        renderRows(pVisibleVillages, settings, pG2d);
    }

    private void renderRows(Village[][] pVillages, RenderSettings pSettings, Graphics2D pG2D) {
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());

        //iterate through entire rows
        int cnt = 0;
        int dx = (int) Math.floor(pSettings.getDeltaX());
        int dy = (int) Math.floor(pSettings.getDeltaY());
        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        for (int x = 0; x < pVillages.length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;

                renderField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), dx, dy, zoom, pG2D);
            }
        }
    }

    private void renderField(Village v, int row, int colu, int pFieldWidth, int pFieldHeight, int pDx, int pDy, double pZoom, Graphics2D g2d) {
        VillageTroopsHolder holder = null;
        if (v != null && (holder = TroopsManager.getSingleton().getTroopsForVillage(v)) != null) {
            int maxDef = 650000;
            try {
                maxDef = Integer.parseInt(GlobalOptions.getProperty("max.density.troops"));
            } catch (Exception e) {
                maxDef = 650000;
            }

            double defIn = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
            double defOwn = holder.getDefValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
            double percOfMax = defIn / maxDef;

            //limit to 100%
            percOfMax = (percOfMax > 1) ? 1 : percOfMax;
            double defFor = (defIn > 0) ? (defIn * percOfMax - defOwn) / defIn * percOfMax : 0;

            double half = (double) maxDef / 2.0;
            Color col = null;
            Color cb = g2d.getColor();
            if (defIn <= maxDef && defIn > half) {
                float ratio = (float) (defIn - half) / (float) half;
                Color c1 = Color.YELLOW;
                Color c2 = Color.GREEN;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                col = new Color(red, green, blue);
            } else if (defIn <= half) {
                float ratio = (float) defIn / (float) half;
                Color c1 = Color.RED;
                Color c2 = Color.YELLOW;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                col = new Color(red, green, blue);
            } else {
                col = new Color(0, 255, 0);
            }

            //calculate circle size
            int wOwn = (int) Math.rint(percOfMax * pFieldWidth);
            int wFor = (int) Math.rint(pFieldWidth * defFor);
            int h = (int) Math.rint(pFieldHeight / 5 / pZoom);
            if (h < 3) {
                h = 3;
            }
            g2d.setColor(Color.WHITE);
            g2d.fill(new Rectangle2D.Double(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, pFieldWidth - 2, h));
            g2d.setColor(col);
            g2d.fill(new Rectangle2D.Double(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, wOwn - 2, h));
            g2d.setColor(Color.BLUE);
            g2d.fill(new Rectangle2D.Double(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, wFor - 2, h));
            g2d.setColor(Color.BLACK);
            g2d.draw(new Rectangle2D.Double(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, pFieldWidth - 2, h));

            g2d.setColor(cb);
        }

    }
}
