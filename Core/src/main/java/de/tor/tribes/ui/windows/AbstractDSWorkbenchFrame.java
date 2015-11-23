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
package de.tor.tribes.ui.windows;

import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.util.interfaces.DSWorkbenchFrameListener;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public abstract class AbstractDSWorkbenchFrame extends DSWorkbenchGesturedFrame implements DropTargetListener, DragGestureListener, DragSourceListener {

    private static Logger logger = Logger.getLogger("AbstractDSWorkbenchFrame");

    @Override
    public void fireCloseGestureEvent() {
        setVisible(false);
    }

    @Override
    public void fireExportAsBBGestureEvent() {
    }

    @Override
    public void fireNextPageGestureEvent() {
    }

    @Override
    public void firePlainExportGestureEvent() {
    }

    @Override
    public void firePreviousPageGestureEvent() {
    }

    @Override
    public void fireRenameGestureEvent() {
    }

    @Override
    public void fireToBackgroundGestureEvent() {
        toBack();
    }
    private List<DSWorkbenchFrameListener> mFrameListeners = null;
    private DragSource dragSource;

    public AbstractDSWorkbenchFrame() {
        mFrameListeners = new LinkedList<DSWorkbenchFrameListener>();
        // getContentPane().setBackground(Constants.DS_BACK);
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        DropTarget dropTarget = new DropTarget(this, this);
        this.setDropTarget(dropTarget);
    }

    public abstract void resetView();

    public abstract void storeCustomProperties(Configuration pConfig);

    public abstract void restoreCustomProperties(Configuration pConfig);

    public abstract String getPropertyPrefix();

    public void storeProperties() {
        String dataDir = DataHolder.getSingleton().getDataDirectory();
        if (!new File(dataDir).exists()) {
            logger.warn("Data directory '" + dataDir + "' does not exist. Skip writing properties");
            return;

        }
        String prefix = getPropertyPrefix();
        PropertiesConfiguration config = null;
        boolean newConfig = false;
        if (!new File(dataDir + "/usergui.properties").exists()) {
            config = new PropertiesConfiguration();
            newConfig = true;
        }
        try {
            if (config == null) {
                config = new PropertiesConfiguration(dataDir + "/usergui.properties");
            }
            config.setProperty(prefix + ".width", getWidth());
            config.setProperty(prefix + ".height", getHeight());
            config.setProperty(prefix + ".x", getX());
            config.setProperty(prefix + ".y", getY());
            // config.setProperty(prefix + ".visible", isVisible());
            config.setProperty(prefix + ".alwaysOnTop", isAlwaysOnTop());
        } catch (ConfigurationException ex) {
            logger.error("Failed to create properties", ex);
            return;
        }

        storeCustomProperties(config);

        try {
            if (newConfig) {
                config.save(dataDir + "/usergui.properties");
            } else {
                config.save();
            }
        } catch (ConfigurationException ex) {
            logger.error("Failed to write properties", ex);
        }
    }

    public void restoreProperties() {
        String dataDir = DataHolder.getSingleton().getDataDirectory();
        if (!new File(dataDir).exists()) {
            logger.warn("Data directory '" + dataDir + "' does not exist. Skip reading properties");
            return;
        }
        String prefix = getPropertyPrefix();
        PropertiesConfiguration config = null;
        try {
            config = new PropertiesConfiguration(dataDir + "/usergui.properties");
            config.setThrowExceptionOnMissing(false);
            Dimension size = new Dimension(config.getInteger(prefix + ".width", getWidth()), config.getInteger(prefix + ".height", getHeight()));
            setPreferredSize(size);
            setSize(size);
            setLocation(config.getInteger(prefix + ".x", getX()), config.getInteger(prefix + ".y", getY()));
            //setVisible(config.getBoolean(prefix + ".visible", false));
            setAlwaysOnTop(config.getBoolean(prefix + ".alwaysOnTop", false));
        } catch (ConfigurationException ex) {
            logger.info("Cannot read properties", ex);
            return;
        }

        restoreCustomProperties(config);
    }

    public synchronized void addFrameListener(DSWorkbenchFrameListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mFrameListeners.contains(pListener)) {
            mFrameListeners.add(pListener);
        }
    }

    public synchronized void removeFrameListener(DSWorkbenchFrameListener pListener) {
        mFrameListeners.remove(pListener);
    }

    @Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        fireVisibilityChangedEvents(v);
    }

    public synchronized void fireVisibilityChangedEvents(boolean v) {
        for (DSWorkbenchFrameListener listener : mFrameListeners) {
            listener.fireVisibilityChangedEvent(this, v);
        }
    }

    public abstract void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation);

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrop();
            return;
        }

        Transferable t = dtde.getTransferable();
        List<Village> v;
        MapPanel.getSingleton().setCurrentCursor(MapPanel.getSingleton().getCurrentCursor());
        try {
            v = (List<Village>) t.getTransferData(VillageTransferable.villageDataFlavor);
            fireVillagesDraggedEvent(v, dtde.getLocation());
        } catch (Exception ex) {
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    public void processGesture(String pGestureString) {
    }
}
