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
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.TwoD.ShapeStroke;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.attack.AttackManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author Torridity
 */
public class AttackLayerRenderer extends AbstractDirectLayerRenderer {

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (!pSettings.isLayerVisible()) {
            return;
        }
        Point2D.Double mapPos = new Point2D.Double(pSettings.getMapBounds().getX(), pSettings.getMapBounds().getY());
        Stroke s = pG2d.getStroke();
        pG2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        renderAttacks(mapPos, pSettings, pG2d);
        pG2d.setStroke(s);
    }

    private void renderAttacks(Point2D.Double viewStartPoint, RenderSettings pSettings, Graphics2D pG2D) {
        HashMap<String, Color> attackColors = new HashMap<>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Color unitColor = Color.decode(GlobalOptions.getProperty(unit.getName() + ".color"));
            attackColors.put(unit.getName(), unitColor);
        }

        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(10, 5);
        p.lineTo(0, 10);
        p.lineTo(0, 0);
        ShapeStroke stroke_attack = new ShapeStroke(
                new Shape[]{
                    p,
                    new Rectangle2D.Float(0, 0, 10, 2)
                },
                20.0f);

        p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 3);
        p.lineTo(0, 6);
        p.lineTo(0, 0);
        ShapeStroke stroke_fake = new ShapeStroke(
                new Shape[]{
                    p,
                    new Rectangle2D.Float(0, 0, 10, 2)
                },
                10.0f);
        Iterator<String> keys = AttackManager.getSingleton().getGroupIterator();

        boolean drawExtendedVectors = GlobalOptions.getProperties().getBoolean("extended.attack.vectors");
        boolean showAttackMovement = GlobalOptions.getProperties().getBoolean("attack.movement");
        while (keys.hasNext()) {
            String plan = keys.next();
            List<ManageableType> elements = AttackManager.getSingleton().getAllElements(plan);
            for (ManageableType element : elements) {
                Attack attack = (Attack) element;
                //go through all attacks
                //render if shown on map or if either source or target are visible
                if (attack.isShowOnMap() && (attack.getSource().isVisibleOnMap() || attack.getTarget().isVisibleOnMap())) {
                    long s = System.currentTimeMillis();
                    //only enter if attack should be visible
                    //get line for this attack
                    Line2D.Double attackLine = new Line2D.Double(attack.getSource().getX(), attack.getSource().getY(), attack.getTarget().getX(), attack.getTarget().getY());
                    double xStart = (attackLine.getX1() - viewStartPoint.x) * pSettings.getFieldWidth() + pSettings.getFieldWidth() / 2;
                    double yStart = (attackLine.getY1() - viewStartPoint.y) * pSettings.getFieldHeight() + pSettings.getFieldHeight() / 2;
                    double xEnd = (attackLine.getX2() - viewStartPoint.x) * pSettings.getFieldWidth() + pSettings.getFieldWidth() / 2;
                    double yEnd = (attackLine.getY2() - viewStartPoint.y) * pSettings.getFieldHeight() + pSettings.getFieldHeight() / 2;
                    ImageIcon unitIcon = null;
                    int unitXPos = 0;
                    int unitYPos = 0;
                    if (showAttackMovement) {
                        unitIcon = ImageManager.getUnitIcon(attack.getUnit());
                        if (unitIcon != null) {
                            long arrive = attack.getArriveTime().getTime();
                            long start = attack.getSendTime().getTime();
                            long dur = arrive - start;
                            long current = System.currentTimeMillis();

                            if ((start < current) && (arrive > current)) {
                                //attack running
                                long runTime = System.currentTimeMillis() - start;
                                double perc = 100 * runTime / dur;
                                perc /= 100;
                                double xTar = xStart + (xEnd - xStart) * perc;
                                double yTar = yStart + (yEnd - yStart) * perc;
                                unitXPos = (int) xTar - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yTar - unitIcon.getIconHeight() / 2;
                            } else if (start > current) {
                                //attack not running, draw unit between source and target
                                double perc = .5;
                                double xTar = xStart + (xEnd - xStart) * perc;
                                double yTar = yStart + (yEnd - yStart) * perc;
                                unitXPos = (int) xTar - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yTar - unitIcon.getIconHeight() / 2;
                            } else {
                                //attack arrived
                                unitXPos = (int) xEnd - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yEnd - unitIcon.getIconHeight() / 2;
                            }

                        }
                    }

                    pG2D.setColor(attackColors.get(attack.getUnit().getName()));
                    if (drawExtendedVectors) {
                        if (attack.getType() == Attack.FAKE_TYPE || attack.getType() == Attack.FAKE_DEFF_TYPE) {
                            pG2D.setStroke(stroke_fake);
                        } else {
                            pG2D.setStroke(stroke_attack);
                        }
                    }

                    pG2D.drawLine((int) Math.floor(xStart), (int) Math.floor(yStart), (int) Math.floor(xEnd), (int) Math.floor(yEnd));
                    pG2D.fillOval((int) Math.floor(xEnd) - 5, (int) Math.floor(yEnd) - 5, 10, 10);
                    if (unitIcon != null) {
                        pG2D.drawImage(unitIcon.getImage(), unitXPos, unitYPos, null);
                    }
                }
            }
        }
    }
}
