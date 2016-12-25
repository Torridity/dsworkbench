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
package de.tor.tribes.util.algo;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class ChurchRangeCalculator {

    final static Point2D.Double[] translations1 = new Point2D.Double[]{
        new Point2D.Double(1, 0),
        new Point2D.Double(1, 1),
        new Point2D.Double(0, 1),
        new Point2D.Double(-1, 1),
        new Point2D.Double(-1, 0),
        new Point2D.Double(-1, -1),
        new Point2D.Double(1, -1)
    };
    final static Point2D.Double[] translations2 = new Point2D.Double[]{
        new Point2D.Double(0, 1),
        new Point2D.Double(-1, 1),
        new Point2D.Double(-1, 0),
        new Point2D.Double(-1, -1),
        new Point2D.Double(1, -1),
        new Point2D.Double(1, 0),
        new Point2D.Double(1, 1)
    };
    final static Point2D.Double[] translations3 = new Point2D.Double[]{
        new Point2D.Double(-1, 0),
        new Point2D.Double(-1, -1),
        new Point2D.Double(0, -1),
        new Point2D.Double(1, -1),
        new Point2D.Double(1, 0),
        new Point2D.Double(1, 1),
        new Point2D.Double(0, 1)
    };
    final static Point2D.Double[] translations4 = new Point2D.Double[]{
        new Point2D.Double(0, -1),
        new Point2D.Double(-1, -1),
        new Point2D.Double(0, -1),
        new Point2D.Double(1, -1),
        new Point2D.Double(1, 0),
        new Point2D.Double(0, 1),
        new Point2D.Double(-1, 1)
    };

    //public static GeneralPath getChurchRange(Village pVillage, int pRadius) {
    public static List<Point2D.Double> getChurchRange(int pX, int pY, int pRadius) {
        List<Point2D.Double> villagePos = new LinkedList<Point2D.Double>();
        //Set InitialStartVillage (x, y-r)
        //search around village, turning right if one village is in range
        //x+1 -> x+1,y+1 -> y+1 -> x-1, y+1 -> x-1 -> x-1, y-1 -> y-1 -> x+1,y-1
        //take first village found and use this as StartVillage
        //loop until StartVillage = InitialStartVillage

        Point2D.Double initStart = new Point2D.Double(pX, pY - pRadius);
        villagePos.add(initStart);
        calculateSurroundingVillageRecursive(new Point2D.Double(pX, pY), pRadius, initStart, initStart, villagePos, 1);
        return villagePos;
 
    }

    private static void calculateSurroundingVillageRecursive(Point2D.Double pCenter, int pRadius, Point2D.Double pInitStart, Point2D.Double pCurrentStart, List<Point2D.Double> pVillages, int pQuadrant) {
        Point2D.Double[] translationMatrix = null;
        switch (pQuadrant) {
            case 1:
                translationMatrix = translations1;
                break;
            case 2:
                translationMatrix = translations2;
                break;
            case 3:
                translationMatrix = translations3;
                break;
            default:
                translationMatrix = translations4;
                break;
        }
        for (Point2D.Double translation : translationMatrix) {
            //walk translations
            Point2D.Double check = new Point2D.Double(pCurrentStart.x + translation.x, pCurrentStart.y + translation.y);
            if (pCenter.distance(check) <= pRadius) {
                //got next border point
                pVillages.add(check);
                pCurrentStart = check;
                break;
            }
        }

        if (pCurrentStart.x == pCenter.x + pRadius && pCurrentStart.y == pCenter.y) {
            //entering quadrant 2
           // pVillages.add(pCurrentStart);
            pQuadrant = 2;
        } else if (pCurrentStart.x == pCenter.x && pCurrentStart.y == pCenter.y + pRadius) {
            //entering quadrant 3
            //pVillages.add(pCurrentStart);
            pQuadrant = 3;
        } else if (pCurrentStart.x == pCenter.x - pRadius && pCurrentStart.y == pCenter.y) {
            //entering quadrant 4
           // pVillages.add(pCurrentStart);
            pQuadrant = 4;
        }

        if (pCurrentStart.x == pInitStart.x && pCurrentStart.y == pInitStart.y) {
            //arrived at start
            pVillages.remove(pVillages.size() - 1);
        } else {
            //next recursion
            calculateSurroundingVillageRecursive(pCenter, pRadius, pInitStart, pCurrentStart, pVillages, pQuadrant);
        }
    }

   
}
