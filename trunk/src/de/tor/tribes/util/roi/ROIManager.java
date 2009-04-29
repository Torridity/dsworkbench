/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.roi;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class ROIManager {

    private static ROIManager SINGLETON = null;
    private List<String> rois = null;

    public static synchronized ROIManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ROIManager();
        }
        return SINGLETON;
    }

    ROIManager() {
        rois = new LinkedList<String>();
    }

    public void loadROIsFromFile() {
    }

    public void saveROIsToFile() {
    }

    public String[] getROIs() {
        return rois.toArray(new String[]{});
    }

    public boolean containsROI(String pRoi) {
        return rois.contains(pRoi);
    }

    public void addROI(int pIdx, String pRoi) {
        if (pIdx > rois.size() - 1) {
            rois.add(pRoi);
        } else {
            rois.add(pIdx, pRoi);
        }
    }

    public void removeROI(String pRoi) {
        rois.remove(pRoi);
    }
}
