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
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.GlobalOptions;
import java.awt.geom.Rectangle2D;

public class RenderSettings {

    private int columnsToRender = 0;
    private int rowsToRender = 0;
    private int fieldWidth = 0;
    private int fieldHeight = 0;
    private double movementX = 0.0;
    private double movementY = 0.0;
    private double deltaX = 0.0;
    private double deltaY = 0.0;
    private double zoom = 1.0;
    private Rectangle2D mMapBounds = null;
    private Village[][] mVisibleVillages = null;
    private boolean layerVisible = false;

    protected RenderSettings(Rectangle2D pCurrentMapBounds) {
        refreshZoom();
        setMapBounds(pCurrentMapBounds);
    }

    public final void refreshZoom() {
        double currentZoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        setZoom(currentZoom);
        setFieldWidth(GlobalOptions.getSkin().getCurrentFieldWidth(zoom));
        setFieldHeight(GlobalOptions.getSkin().getCurrentFieldHeight(zoom));
    }

    public void calculateSettings(Rectangle2D pNewBounds) {
        if (getMapBounds() != null && pNewBounds != null) {
            setMovementX(Math.round((double) getFieldWidth() * (getMapBounds().getX() - pNewBounds.getX())));
            setMovementY(Math.round((double) getFieldHeight() * (getMapBounds().getY() - pNewBounds.getY())));
        }

        if (getMovementX() != 0.0) {
            setDeltaX(getMovementX() / (double) getFieldWidth());
        }
        if (getMovementY() != 0.0) {
            setDeltaY(getMovementY() / (double) getFieldHeight());
        }

        int facX = 1;
        int facY = 1;
        if (getDeltaX() < 0) {
            facX = -1;
            setDeltaX(Math.abs(getDeltaX()));
        }
        if (getDeltaY() < 0) {
            facY = -1;
            setDeltaY(Math.abs(getDeltaY()));
        }
        int fieldsX = (int) Math.round(getDeltaX()) + 1;
        int fieldsY = (int) Math.round(getDeltaY()) + 1;

        setColumnsToRender((fieldsX + 1) * facX);
        setRowsToRender((fieldsY + 1) * facY);

        if (pNewBounds != null) {
            setDeltaX(0 - ((pNewBounds.getX() - Math.floor(pNewBounds.getX())) * (double) getFieldWidth()));
            setDeltaY(0 - ((pNewBounds.getY() - Math.floor(pNewBounds.getY())) * (double) getFieldHeight()));
        } else {
            setDeltaX(0);
            setDeltaY(0);
        }

        setMapBounds(pNewBounds);
    }

    /**
     * @return the fieldWidth
     */
    public int getFieldWidth() {
        return fieldWidth;
    }

    public final void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    /**
     * @return the fieldHeight
     */
    public int getFieldHeight() {
        return fieldHeight;
    }

    public final void setFieldHeight(int fieldHeight) {
        this.fieldHeight = fieldHeight;
    }

    /**
     * @return the deltaX
     */
    public double getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    /**
     * @return the deltaY
     */
    public double getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    /**
     * @return the columnsToRender
     */
    public int getColumnsToRender() {
        return columnsToRender;
    }

    public void setColumnsToRender(int columnsToRender) {
        this.columnsToRender = columnsToRender;
    }

    /**
     * @return the rowsToRender
     */
    public int getRowsToRender() {
        return rowsToRender;
    }

    public void setRowsToRender(int rowsToRender) {
        this.rowsToRender = rowsToRender;
    }

    /**
     * @return the zoom
     */
    public double getZoom() {
        return zoom;
    }

    public final void setZoom(double zoom) {
        this.zoom = zoom;
    }

    /**
     * @return the mMapBounds
     */
    public Rectangle2D getMapBounds() {
        return mMapBounds;
    }

    /**
     * @param mMapBounds the mMapBounds to set
     */
    public final void setMapBounds(Rectangle2D mMapBounds) {
        this.mMapBounds = (Rectangle2D) mMapBounds.clone();
    }

    public int getVillagesInX() {
        if (mVisibleVillages == null) {
            return 0;
        }
        return mVisibleVillages.length;
    }

    public int getVillagesInY() {
        if (mVisibleVillages == null || mVisibleVillages.length == 0) {
            return 0;
        }
        return mVisibleVillages[0].length;
    }

    public Village getVisibleVillage(int x, int y) {
        if (mVisibleVillages == null) {
            return null;
        }
        try {
            return mVisibleVillages[x][y];
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return null;
        }
    }

    /**
     * @param mVisibleVillages the mVisibleVillages to set
     */
    public void setVisibleVillages(Village[][] mVisibleVillages) {
        this.mVisibleVillages = mVisibleVillages;
    }

    /**
     * @return the movementX
     */
    public double getMovementX() {
        return movementX;
    }

    /**
     * @param movementX the movementX to set
     */
    public void setMovementX(double movementX) {
        this.movementX = movementX;
    }

    /**
     * @return the movementY
     */
    public double getMovementY() {
        return movementY;
    }

    /**
     * @param movementY the movementY to set
     */
    public void setMovementY(double movementY) {
        this.movementY = movementY;
    }

    /**
     * @return the layerVisible
     */
    public boolean isLayerVisible() {
        return layerVisible;
    }

    /**
     * @param layerVisible the layerVisible to set
     */
    public void setLayerVisible(boolean layerVisible) {
        this.layerVisible = layerVisible;
    }
}
