/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSWorkbenchFrameListener;
import de.tor.tribes.util.DSWorkbenchGesturedFrame;
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
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public abstract class AbstractDSWorkbenchFrame extends DSWorkbenchGesturedFrame implements DropTargetListener, DragGestureListener, DragSourceListener {

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
        getContentPane().setBackground(Constants.DS_BACK);
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        DropTarget dropTarget = new DropTarget(this, this);
        this.setDropTarget(dropTarget);
    }

    public abstract void resetView();
    
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
