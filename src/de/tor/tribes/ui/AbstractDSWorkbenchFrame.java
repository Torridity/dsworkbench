/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSWorkbenchFrameListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Charon
 */
public abstract class AbstractDSWorkbenchFrame extends JFrame {

    private List<DSWorkbenchFrameListener> mFrameListeners = null;

    public AbstractDSWorkbenchFrame() {
        mFrameListeners = new LinkedList<DSWorkbenchFrameListener>();
        getContentPane().setBackground(Constants.DS_BACK);
    }

    public synchronized void addFrameListener(DSWorkbenchFrameListener pListener) {
        mFrameListeners.add(pListener);
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
}
