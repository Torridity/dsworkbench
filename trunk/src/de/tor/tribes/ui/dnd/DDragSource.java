/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.dnd;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

/**
 *
 * @author Torridity
 */
public class DDragSource implements DragGestureListener, DragSourceListener {

    GhostGlassPane dragPane = null;
    public DDragSource(GhostGlassPane pPane) {
        dragPane = pPane;
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        Transferable t = new DTransferable();
        dge.startDrag(null, t, this);
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
        DTransferable.object = dsde.getSource();
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
    }
}
