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
package de.tor.tribes.util.dsreal;

import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.dsreal.ui.ChartPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class DSRealManager {

    private static Logger logger = Logger.getLogger("DSRealManager");
    private static DSRealManager SINGLETON = null;
    boolean isError = false;
    private static final String CHART_PATH = "dsreal";
    private static final String TRIBE_PATH = "tribe";
    private static final String ALLY_PATH = "ally";
    private static final String POINTS_EXT = "_points.png";
    private static final String BASH_OFF_EXT = "_bash_off.png";
    private static final String BASH_DEF_EXT = "_bash_def.png";
    private static final String BASH_ALL_EXT = "_bash_all.png";

    public static synchronized DSRealManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSRealManager();
        }
        return SINGLETON;
    }

    DSRealManager() {
    }

    public void checkFilesystem() {
        logger.debug("Checking DSReal file system");
        String mainPath = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/";
        if (!new File(mainPath).exists()) {
            logger.debug("Creating main path at '" + mainPath + "'");
            if (new File(mainPath).mkdir()) {
                logger.debug(" - Main path successfully created");
            } else {
                isError = true;
            }
        }

        if (!new File(mainPath + TRIBE_PATH).exists()) {
            logger.debug("Creating tribe path at '" + mainPath + TRIBE_PATH + "'");
            if (new File(mainPath + TRIBE_PATH).mkdir()) {
                logger.debug(" - Tribe path successfully created");
            } else {
                isError = true;
            }
        }
        if (!new File(mainPath + ALLY_PATH).exists()) {
            logger.debug("Creating ally path at '" + mainPath + ALLY_PATH + "'");
            if (new File(mainPath + ALLY_PATH).mkdir()) {
                logger.debug(" - Ally path successfully created");
            } else {
                isError = true;
            }
        }
        if (isError) {
            logger.warn("One or more errors occured. DSReal features won't be available");
        }
    }

    public void getTribeBashChart(Tribe pTribe) {
        int id = pTribe.getId();
        String off_file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + TRIBE_PATH + "/";
        off_file += id + BASH_OFF_EXT;
        String def_file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + TRIBE_PATH + "/";
        def_file += id + BASH_DEF_EXT;
        String all_file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + TRIBE_PATH + "/";
        all_file += id + BASH_ALL_EXT;
        /* String off_url = "http://dsreal.de/chart/bash_chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=player&art=off";
        String def_url = "http://dsreal.de/chart/bash_chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=player&art=def";
        String all_url = "http://dsreal.de/chart/bash_chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=player&art=all";
         */

        String off_url = "http://dsreal.de/charts/playerBashall.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer();
        String def_url = "http://dsreal.de/charts/playerBashoff.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer();
        String all_url = "http://dsreal.de/charts/playerBashdef.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer();

        checkFile(off_file, off_url);
        checkFile(def_file, def_url);
        checkFile(all_file, all_url);
        //build and show frame
        ChartPanel.showBashChart(off_file, def_file, all_file, pTribe);
    }

    public void getAllyBashChart(Ally pAlly) {
        int id = pAlly.getId();
        //./server/de26/ally/<id>_off.png
        //./server/de26/ally/<id>_def.png
        //./server/de26/ally/<id>_all.png
        String off_file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + ALLY_PATH + "/";
        off_file += id + BASH_OFF_EXT;
        String def_file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + ALLY_PATH + "/";
        def_file += id + BASH_DEF_EXT;
        String all_file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + ALLY_PATH + "/";
        all_file += id + BASH_ALL_EXT;
        //http://dsreal.de/chart/bash_chart.php?id=9755&world=de26&mode=ally&art=off
        //http://dsreal.de/chart/bash_chart.php?id=9755&world=de26&mode=ally&art=all
        //http://dsreal.de/chart/bash_chart.php?id=9755&world=de26&mode=ally&art=def
        /*String off_url = "http://dsreal.de/chart/bash_chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=off";
        String def_url = "http://dsreal.de/chart/bash_chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=def";
        String all_url = "http://dsreal.de/chart/bash_chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=all";
         */
        String off_url = "http://dsreal.de/charts/allyBashall.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=off";
        String def_url = "http://dsreal.de/charts/allyBashoff.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=def";
        String all_url = "http://dsreal.de/charts/allyBashdef.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=all";

        checkFile(off_file, off_url);
        checkFile(def_file, def_url);
        checkFile(all_file, all_url);
        //build and show frame
        ChartPanel.showBashChart(off_file, def_file, all_file, pAlly);
    }

    public void getTribePointsChart(Tribe pTribe) {
        int id = pTribe.getId();
        //./servers/de26/tribe/<id>_points.png;
        String file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + TRIBE_PATH + "/";
        file += id + POINTS_EXT;
        //http://dsreal.de/charts/playerPoints.php?id=3296746&world=de43
        //   String url = "http://dsreal.de/chart/chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=player&art=points";
        String url = "http://dsreal.de/charts/playerPoints.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer();
        checkFile(file, url);
        ChartPanel.showPointChart(file, pTribe);
        //build and show frame
    }

    public void getAllyPointsChart(Ally pAlly) {
        int id = pAlly.getId();
        //./servers/de26/ally/<id>_points.png;
        String file = "./servers/" + GlobalOptions.getSelectedServer() + "/" + CHART_PATH + "/" + ALLY_PATH + "/";
        file += id + POINTS_EXT;
        //http://dsreal.de/chart/chart.php?id=9755&world=de26&mode=ally
        //String url = "http://dsreal.de/chart/chart.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer() + "&mode=ally&art=points";
        String url = "http://dsreal.de/charts/allyPoints.php?id=" + id + "&world=" + GlobalOptions.getSelectedServer();
        checkFile(file, url);
        //build and show frame
        ChartPanel.showPointChart(file, pAlly);
    }

    private void checkFile(String pLocalFile, String pUrl) {
        File local = new File(pLocalFile);
        //reload if not exist or older than 24h
        if (!local.exists() || (System.currentTimeMillis() - 24 * 60 * 60 * 1000 > local.lastModified())) {
            BufferedImage img = null;
            try {
                logger.debug("Getting chart from '" + pUrl + "'");
                URL u = new URL(pUrl);
                URLConnection con = u.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
                img = ImageIO.read(con.getInputStream());
            } catch (Exception e) {
                logger.error("Failed to read from url '" + pUrl + "'", e);
            }

            if (img != null) {
                try {
                    ImageIO.write(img, "png", local);
                } catch (Exception e) {
                    logger.error("Failed to write to file '" + pLocalFile + "'");
                }
            }
            try {
                //sleep due to connection limit of ds real (5 conns per second)
                Thread.sleep(500);
            } catch (Exception ignored) {
            }
        }

    }
}
