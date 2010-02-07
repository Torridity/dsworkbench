/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.map;

import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.MapPanelListener;
import de.tor.tribes.util.ToolChangeListener;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class MapPanelManager {

    private MapPanel[][] panelArray = null;
    private MapPanel activePanel = null;
    private static MapPanelManager SINGLETON = null;
    public static final int UPPER_LEFT = 0;
    public static final int UPPER_RIGHT = 1;
    public static final int LOWER_LEFT = 2;
    public static final int LOWER_RIGHT = 3;

    public static synchronized MapPanelManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MapPanelManager();
        }
        return SINGLETON;
    }

    public MapPanelManager() {
       /* panelArray = new MapPanel[2][2];
        panelArray[0][0] = new MapPanel();
        panelArray[1][1] = new MapPanel();
        panelArray[0][1] = new MapPanel();
        panelArray[1][0] = new MapPanel();
        activePanel = panelArray[0][0];*/
    }

    public MapPanel getActivePanel() {
        return activePanel;
    }

    public void setActivePanel(MapPanel pPanel) {
        activePanel = pPanel;
    }

    public MapPanel getPanelByPosition(int pPosition) {
        switch (pPosition) {
            case UPPER_RIGHT:
                return panelArray[1][0];
            case LOWER_LEFT:
                return panelArray[0][1];
            case LOWER_RIGHT:
                return panelArray[1][1];
            default:
                return panelArray[0][0];
        }
    }

    public void addMapPanelListener(MapPanelListener pListener) {
        panelArray[0][0].addMapPanelListener(pListener);
        panelArray[1][1].addMapPanelListener(pListener);
        panelArray[0][1].addMapPanelListener(pListener);
        panelArray[1][0].addMapPanelListener(pListener);
    }

    public void addToolChangeListener(ToolChangeListener pListener) {
        panelArray[0][0].addToolChangeListener(pListener);
        panelArray[1][1].addToolChangeListener(pListener);
        panelArray[0][1].addToolChangeListener(pListener);
        panelArray[1][0].addToolChangeListener(pListener);
    }
}
