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
package de.tor.tribes.util;

import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.panels.MapPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Torridity
 */
public class ScreenshotSaver extends Thread {

    private BufferedImage mScreen = null;
    private File fTargetFile = null;
    private String mTargetType = null;

    public ScreenshotSaver() {
        setDaemon(true);
        setName("ScreenshotSaver");
        setPriority(MIN_PRIORITY);
    }

    public void planMapShot(File pTargetFile, String pTargetType, BufferedImage pScreen) {
        fTargetFile = pTargetFile;
        mTargetType = pTargetType;
        mScreen = pScreen;
    }

    public void run() {
        while (true) {
            if (mScreen != null) {
                try {
                    Point2D.Double pos = MapPanel.getSingleton().getCurrentPosition();
                    String first = "";
                    first = "Zentrum: " + (int) Math.floor(pos.getX()) + "|" + (int) Math.floor(pos.getY());

                    BufferedImage result = ImageUtils.createCompatibleBufferedImage(mScreen.getWidth(null), mScreen.getHeight(null), BufferedImage.OPAQUE);
                    Graphics2D g2d = (Graphics2D) result.getGraphics();
                    g2d.drawImage(mScreen, 0, 0, null);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    FontMetrics fm = g2d.getFontMetrics();
                    Rectangle2D firstBounds = fm.getStringBounds(first, g2d);
                    String second = "Erstellt mit DS Workbench " + Constants.VERSION + Constants.VERSION_ADDITION;
                    Rectangle2D secondBounds = fm.getStringBounds(second, g2d);
                    g2d.setColor(Constants.DS_BACK_LIGHT);
                    g2d.fill3DRect(0, (int) (result.getHeight() - firstBounds.getHeight() - secondBounds.getHeight() - 9), (int) (secondBounds.getWidth() + 6), (int) (firstBounds.getHeight() + secondBounds.getHeight() + 9), true);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(first, 3, (int) (result.getHeight() - firstBounds.getHeight() - secondBounds.getHeight() - firstBounds.getY() - 6));
                    g2d.drawString(second, 3, (int) (result.getHeight() - secondBounds.getHeight() - secondBounds.getY() - 3));
                    g2d.dispose();
                    ImageIO.write(result, mTargetType, fTargetFile);
                    DSWorkbenchMainFrame.getSingleton().fireMapShotDoneEvent();
                } catch (Exception e) {
                    DSWorkbenchMainFrame.getSingleton().fireMapShotFailedEvent();
                }
                mScreen = null;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
    }
}
