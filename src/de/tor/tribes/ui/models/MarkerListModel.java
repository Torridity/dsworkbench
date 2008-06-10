/*
 * MarkerListModel.java
 *
 * Created on 07.10.2007, 14:18:22
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.models;

import de.tor.tribes.ui.MarkerPanel;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author Charon
 */
public class MarkerListModel extends AbstractListModel {

    private List<MarkerPanel> mMarkers;

    public MarkerListModel() {
        mMarkers = new LinkedList<MarkerPanel>();
    }

    public void updateModel(){
        mMarkers.clear();
        Hashtable<String, Color> markers = GlobalOptions.getMarkers();
        Enumeration<String> keys = markers.keys();
        while(keys.hasMoreElements()){
            String name = keys.nextElement();
            mMarkers.add(new MarkerPanel(name, markers.get(name)));
        }
    }
    
    public int getSize() {
        return mMarkers.size();
    }

    public Object getElementAt(int index) {
        return mMarkers.get(index);
    }
}