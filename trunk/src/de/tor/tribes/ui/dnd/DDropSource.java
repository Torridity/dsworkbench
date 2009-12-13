/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.dnd;

import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

/**
 *
 * @author Torridity
 */
public class DDropSource implements DropTargetListener {

    GhostGlassPane panel = null;

    public DDropSource(GhostGlassPane pane) {
        panel = pane;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        panel.repaint();
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
        dropTargetDrag(dtde);
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        dropTargetDrag(dtde);
    }

    void dropTargetDrag(DropTargetDragEvent ev) {
        ev.acceptDrag(ev.getDropAction());
    }

    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(dtde.getDropAction());
        try {
            Object target = dtde.getSource();

            /*Object source = dtde.getTransferable().getTransferData
            (supportedFlavors[0]);*/

            Object source = dtde.getTransferable().getTransferData(DTransferable.supportedFlavors[0]);

            Component component = ((DragSourceContext) source).getComponent();
            Container oldContainer = component.getParent();
            Container container = (Container) ((DropTarget) target).getComponent();
            container.add(component);
            oldContainer.validate();
            oldContainer.repaint();
            container.validate();
            container.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        dtde.dropComplete(true);
    }
}
