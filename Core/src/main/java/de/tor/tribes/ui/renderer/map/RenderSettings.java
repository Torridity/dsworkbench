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
        this.zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        this.fieldWidth = GlobalOptions.getSkin().getCurrentFieldWidth(zoom);
        this.fieldHeight = GlobalOptions.getSkin().getCurrentFieldHeight(zoom);
    }

    public void calculateSettings(Rectangle2D pNewBounds) {
        if (mMapBounds != null && pNewBounds != null) {
            this.movementX = (double) Math.round((double) fieldWidth * (mMapBounds.getX() - pNewBounds.getX()));
            this.movementY = (double) Math.round((double) fieldHeight * (mMapBounds.getY() - pNewBounds.getY()));
        }

        if (movementX != 0.0) {
            this.deltaX = movementX / (double) fieldWidth;
        }
        if (movementY != 0.0) {
            this.deltaY = movementY / (double) fieldHeight;
        }

        int facX = 1;
        int facY = 1;
        if (deltaX < 0) {
            facX = -1;
            this.deltaX = Math.abs(deltaX);
        }
        if (deltaY < 0) {
            facY = -1;
            this.deltaY = Math.abs(deltaY);
        }
        int fieldsX = (int) Math.round(deltaX) + 1;
        int fieldsY = (int) Math.round(deltaY) + 1;

        this.columnsToRender = (fieldsX + 1) * facX;
        this.rowsToRender = (fieldsY + 1) * facY;

        if (pNewBounds != null) {
            this.deltaX = 0 - ((pNewBounds.getX() - Math.floor(pNewBounds.getX())) * (double) fieldWidth);
            this.deltaY = 0 - ((pNewBounds.getY() - Math.floor(pNewBounds.getY())) * (double) fieldHeight);
        } else {
            this.deltaX = (double) 0;
            this.deltaY = (double) 0;
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
