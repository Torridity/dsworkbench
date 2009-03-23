/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class ChurchRangeCalculator {

    //public static GeneralPath getChurchRange(Village pVillage, int pRadius) {
    public static GeneralPath getChurchRange(int pX, int pY, int pRadius) {
        //double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        double zoom = 1.0;
        /*int fieldWidth = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, zoom).getWidth(null);
        int fieldHeight = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, zoom).getHeight(null);
        int x = pVillage.getX();
        int y = pVillage.getY();
         */
        int fieldWidth = 53;
        int fieldHeight = 38;
        int x = pX;
        int y = pY;
        double diam = 2 * pRadius * fieldWidth + fieldWidth;
        int horVill = pRadius;
        int vertVill = 0;

        List<Point2D.Double> villagePos = new LinkedList<Point2D.Double>();

        //add 4 corners of the 3 edges directly
        //go X/Y -> X+R/Y+R and take village which is outer most
        //mirror the found villages on y, x and x+y to get all villages and remember the 2 border coordinates


        for (int i = x; i <= x + pRadius;) {
            for (int j = y; j < y + pRadius; j++) {
                double dist = new Point2D.Double(i, j).distance(new Point2D.Double(x, y));
                if (dist > pRadius) {
                    villagePos.add(new Point2D.Double(i, j - 1));
                    break;
                }
            }
            i++;
        }

        for (Point2D.Double p : villagePos) {
            System.out.println(p);
        }

        return null;
    }

    public static void main(String[] args) {
        ChurchRangeCalculator.getChurchRange(500, 500, 4);
    }
}
