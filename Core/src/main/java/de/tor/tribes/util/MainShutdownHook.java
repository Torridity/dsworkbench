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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.LayerOrderConfigurationFrame;
import java.io.File;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

/**
 * @author Charon
 */
public class MainShutdownHook extends Thread {

    private static Logger logger = Logger.getLogger("ShutdownHook");

    public MainShutdownHook() {
        setName("ShutdownHook");
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            logger.info("Performing ShutdownHook");
            if (!DataHolder.getSingleton().isDataValid()) {
                logger.error("Server data seems to be invalid. No user data will be stored!");
                return;
            }
            GlobalOptions.saveUserData();
            GlobalOptions.addProperty("layer.order", LayerOrderConfigurationFrame.getSingleton().getLayerOrder());
            DSWorkbenchMainFrame.getSingleton().storeProperties();
            GlobalOptions.saveProperties();
            GlobalOptions.storeViewStates();
            if (!FileUtils.deleteQuietly(new File(".running"))) {
                logger.warn("Failed to remove file '.running'");
            }
            logger.debug("Shutdown finished");
        } catch (Throwable t) {
            logger.error("Shutdown failed", t);
        }
    }
}
