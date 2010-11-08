package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.MapPanel;
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
        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        setZoom(zoom);
        setFieldWidth(GlobalOptions.getSkin().getCurrentFieldWidth(zoom));
        setFieldHeight(GlobalOptions.getSkin().getCurrentFieldHeight(zoom));
        setMapBounds(pCurrentMapBounds);
    }

    public void calculateSettings(Rectangle2D pNewBounds) {
        if (getMapBounds() != null && pNewBounds != null) {
            setMovementX(getFieldWidth() * (getMapBounds().getX() - pNewBounds.getX()));
            setMovementY(getFieldHeight() * (getMapBounds().getY() - pNewBounds.getY()));
        }

        if (getMovementX() != 0.0) {
            setDeltaX(getMovementX() / (double) getFieldWidth());
        }
        if (getMovementY() != 0.0) {
            setDeltaY(getMovementY() / (double) getFieldHeight());
        }


        int fieldsX = (getDeltaX() > 0) ? (int) Math.round(getDeltaX()) : (int) Math.floor(getDeltaX());
        int fieldsY = (getDeltaY() > 0) ? (int) Math.round(getDeltaY()) : (int) Math.floor(getDeltaY());
        fieldsX += (fieldsX >= 0) ? 1 : -1;
        fieldsY += (fieldsY >= 0) ? 1 : -1;

        setColumnsToRender(fieldsX);
        setRowsToRender(fieldsY);

        if (pNewBounds != null) {
            setDeltaX(0 - ((pNewBounds.getX() - Math.floor(pNewBounds.getX())) * getFieldWidth()));
            setDeltaY(0 - ((pNewBounds.getY() - Math.floor(pNewBounds.getY())) * getFieldHeight()));
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

    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    /**
     * @return the fieldHeight
     */
    public int getFieldHeight() {
        return fieldHeight;
    }

    public void setFieldHeight(int fieldHeight) {
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

    public void setZoom(double zoom) {
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
    public void setMapBounds(Rectangle2D mMapBounds) {
        this.mMapBounds = (Rectangle2D) mMapBounds.clone();
    }

    /**
     * @return the mVisibleVillages
     */
    public Village[][] getVisibleVillages() {
        return mVisibleVillages;
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
