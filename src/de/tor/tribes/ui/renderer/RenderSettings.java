package de.tor.tribes.ui.renderer;

class RenderSettings {

    private int columnsToRender = 0;
    private int rowsToRender = 0;
    private int fieldWidth = 0;
    private int fieldHeight = 0;
    private double deltaX = 0.0;
    private double deltaY = 0.0;
    private double zoom = 1.0;
    AbstractBufferedLayerRenderer outer;

    protected RenderSettings(AbstractBufferedLayerRenderer outer) {
        this.outer = outer;
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
}
