/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.dnd;

import de.tor.tribes.types.Village;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import javax.swing.JComponent;

public class GhostGlassPane extends JComponent implements DropTargetListener, DragGestureListener, DragSourceListener // For processing drag source events
{

    private AlphaComposite composite;
    private Image dragged = null;
    private Point location = new Point(0, 0);

    public GhostGlassPane() {
        /*   composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        this.dragged = GlobalOptions.getSkin().getImage(Skin.ID_V6, 1.0);*/
        //dragSource = DragSource.getDefaultDragSource();
    }

    /* public void setImage(BufferedImage dragged) {
    this.dragged = dragged;
    }*/
    public void setPoint(Point location) {
        this.location = location;
    }

    public void paintComponent(Graphics g) {
        // super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        /*if (dragged == null) {
        return;
        }*/


        /* g2.setComposite(composite);
        g2.drawImage(dragged, (int) (location.getX() - (dragged.getWidth(this) / 2)), (int) (location.getY() - (dragged.getHeight(this) / 2)), null);
         */    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        System.out.println("DE1");
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        System.out.println("DE2");
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        System.out.println("DE3");
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        System.out.println("DROP");
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrop();
            return;
        }

        Transferable t = dtde.getTransferable();
        Village v;

        try {
            System.out.println("FP: " + t.getTransferData(DataFlavor.stringFlavor));
        } catch (Exception ex) {
            ex.printStackTrace();
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
}

