/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**
 *
 * @author  Charon
 */
public class MapPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger(MapPanel.class);
    private Village[][] mVisibleVillages = null;
    private Image mBuffer = null;
    private double dScaling = 1.0;
    private int downX = 0;
    private int downY = 0;
    private int mX = 456;
    private int mY = 468;
    private RepaintThread mRepaintThread = null;
    boolean updating = false;
    private DSWorkbenchMainFrame mParent;
    private int iCurrentCursor = GlobalOptions.CURSOR_DEFAULT;
    private Village mSourceVillage = null;
    private Village mTargetVillage = null;
    private MarkerAddFrame mMarkerAddFrame = null;
    private AttackAddFrame mAttackAddFrame = null;
    private VillageTagFrame mTagFrame = null;
    boolean mouseDown = false;
    private boolean isOutside = false;
    private Rectangle2D screenRect = null;
    private Point mousePos = null;

    /** Creates new form MapPanel */
    public MapPanel(DSWorkbenchMainFrame pParent) {
        initComponents();
        logger.info("Creating MapPanel");
        mMarkerAddFrame = new MarkerAddFrame(pParent);
        mAttackAddFrame = new AttackAddFrame(pParent);
        mTagFrame = new VillageTagFrame();
        mParent = pParent;
        setCursor(GlobalOptions.getCursor(iCurrentCursor));
        initListeners();
    }

    private void initListeners() {

        // <editor-fold defaultstate="collapsed" desc="MouseWheelListener for Tool changes">
        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                iCurrentCursor += e.getWheelRotation();
                if (iCurrentCursor < 0) {
                    iCurrentCursor = GlobalOptions.CURSOR_ATTACK_HEAVY;
                } else if (iCurrentCursor > GlobalOptions.CURSOR_ATTACK_HEAVY) {
                    iCurrentCursor = GlobalOptions.CURSOR_DEFAULT;

                }
                setCursor(GlobalOptions.getCursor(iCurrentCursor));
                mParent.changeTool(iCurrentCursor);
            }
        });
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="MouseListener for cursor events">
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int unit = -1;
                boolean isAttack = false;
                if ((iCurrentCursor == GlobalOptions.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }
                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_MARK: {
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            if (current.getTribe() == null) {
                                //empty village
                                return;
                            }
                            mMarkerAddFrame.setLocation(e.getPoint());
                            mMarkerAddFrame.setVillage(current);
                            mMarkerAddFrame.setVisible(true);
                        }
                        break;
                    }
                    case GlobalOptions.CURSOR_TAG: {
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            if (current.getTribe() == null) {
                                //empty village
                                return;
                            }
                        }
                        mTagFrame.setLocation(e.getPoint());
                        mTagFrame.showTagsFrame(current);
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_INGAME: {
                        if (e.getClickCount() == 2) {
                            Village v = getVillageAtMousePos();
                            Village u = mParent.getCurrentUserVillage();
                            if ((u != null) && (v != null)) {
                                BrowserCommandSender.sendTroops(u, v);
                            }
                        }
                    }
                    case GlobalOptions.CURSOR_SEND_RES_INGAME: {
                        if (e.getClickCount() == 2) {
                            Village v = getVillageAtMousePos();
                            Village u = mParent.getCurrentUserVillage();
                            if ((u != null) && (v != null)) {
                                BrowserCommandSender.sendRes(u, v);
                            }
                        }
                    }
                    case GlobalOptions.CURSOR_ATTACK_AXE: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Axtkämpfer");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_SWORD: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Schwertkämpfer");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_SPY: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Späher");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_LIGHT: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Leichte Kavallerie");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_HEAVY: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Schwere Kavallerie");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_RAM: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Ramme");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_SNOB: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Adelsgeschlecht");
                        break;
                    }
                }

                if (e.getClickCount() == 2) {
                    if (isAttack) {
                        mAttackAddFrame.setLocation(e.getLocationOnScreen());
                        mAttackAddFrame.setupAttack(mParent.getCurrentUserVillage(), getVillageAtMousePos(), unit);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                boolean isAttack = false;
                mouseDown = true;
                if ((iCurrentCursor == GlobalOptions.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }

                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_MEASURE: {
                        //start drag if attack tool is active
                        downX = e.getX();
                        downY = e.getY();
                        mSourceVillage = getVillageAtMousePos();
                        mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        break;
                    }
                    default: {
                        if (isAttack) {
                            downX = e.getX();
                            downY = e.getY();
                            mSourceVillage = getVillageAtMousePos();
                            mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int unit = -1;
                boolean isAttack = false;
                if ((iCurrentCursor == GlobalOptions.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }

                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_MEASURE: {
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_AXE: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Axtkämpfer");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_SWORD: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Schwertkämpfer");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_SPY: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Späher");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_LIGHT: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Leichte Kavallerie");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_HEAVY: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Schwere Kavallerie");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_RAM: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Ramme");
                        break;
                    }
                    case GlobalOptions.CURSOR_ATTACK_SNOB: {
                        unit = GlobalOptions.getDataHolder().getUnitID("Adelsgeschlecht");
                        break;
                    }
                }
                downX = 0;
                downY = 0;

                mouseDown = false;
                if (isAttack) {
                    mAttackAddFrame.setLocation(e.getLocationOnScreen());
                    mAttackAddFrame.setupAttack(mSourceVillage, mTargetVillage, unit);
                }
                mSourceVillage = null;
                mTargetVillage = null;
                mRepaintThread.setDragLine(0, 0, 0, 0);
            }

            /**
             * @TODO Implement handling if cursor leaves map while dragging
             */
            @Override
            public void mouseEntered(MouseEvent e) {
                isOutside = false;
                screenRect = null;
                mousePos = null;
            }

            /**
             * @TODO Implement handling if cursor leaves map while dragging
             */
            @Override
            public void mouseExited(MouseEvent e) {
                if (mouseDown) {
                    isOutside = true;
                    //handle drag-outside-panel events
                    mousePos = e.getLocationOnScreen();
                    Point panelPos = getLocationOnScreen();
                    screenRect = new Rectangle2D.Double(panelPos.getX(), panelPos.getY(), getWidth(), getHeight());
                }
            }
        });
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="MouseMotionListener for dragging operations">
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                mParent.updateDetailedInfoPanel(getVillageAtMousePos());
                boolean isAttack = false;
                if ((iCurrentCursor == GlobalOptions.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == GlobalOptions.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }

                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_MEASURE: {
                        //update drag if attack tool is active
                        mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        mTargetVillage = getVillageAtMousePos();
                        mParent.updateDistancePanel(mSourceVillage, mTargetVillage);
                        break;
                    }
                    default: {
                        if (isAttack) {
                            mRepaintThread.setDragLine((int) mSourceVillage.getX(), (int) mSourceVillage.getY(), e.getX(), e.getY());
                            mTargetVillage = getVillageAtMousePos();
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mParent.updateDetailedInfoPanel(getVillageAtMousePos());
                if (isOutside) {
                    mousePos = e.getLocationOnScreen();
                }
            }
        });
    //</editor-fold>
    }

    protected boolean isOutside() {
        return isOutside;
    }

    public Village getSourceVillage() {
        return mSourceVillage;
    }

    public void setCurrentCursor(int pCurrentCursor) {
        iCurrentCursor = pCurrentCursor;
        setCursor(GlobalOptions.getCursor(iCurrentCursor));
        mParent.changeTool(iCurrentCursor);
    }

    public void setZoom(double pZoom) {
        dScaling = pZoom;
        mRepaintThread.setZoom(dScaling);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 89, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**Draw buffer into panel*/
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        //super.paint(g);
        g2d.drawImage(mBuffer, 0, 0, null);
        if (isOutside) {
            mousePos = MouseInfo.getPointerInfo().getLocation();
            int outcodes = screenRect.outcode(mousePos);
            int xDir = 0;
            int yDir = 0;

            if ((outcodes & Rectangle2D.OUT_LEFT) != 0) {
                xDir = -1;
            } else if ((outcodes & Rectangle2D.OUT_RIGHT) != 0) {
                xDir = 1;
            }

            if ((outcodes & Rectangle2D.OUT_TOP) != 0) {
                yDir = -1;
            } else if ((outcodes & Rectangle2D.OUT_BOTTOM) != 0) {
                yDir = 1;
            }
            downX += xDir;
            downY += yDir;
            mParent.scroll(xDir, yDir);
        }
    }

    /**Update map to new position*/
    protected synchronized void updateMap(int pX, int pY) {
        updating = true;
        if (mRepaintThread == null) {
            logger.info("Creating MapPaintThread");
            mRepaintThread = new RepaintThread(this, pX, pY);
            mRepaintThread.start();
            mX = pX;
            mY = pY;
        } else {
            mX = pX;
            mY = pY;
            mRepaintThread.setCoordinates(mX, mY);
        }
    }

    /**Get village at current mouse position, null if there is no village*/
    public Village getVillageAtMousePos() {
        try {
            int x = (int) getMousePosition().getX();
            int y = (int) getMousePosition().getY();

            x /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling).getWidth(null);
            y /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling).getHeight(null);

            return mVisibleVillages[x][y];
        } catch (Exception e) {
        }
        return null;
    }

    /**Update operation perfomed by the RepaintThread was completed*/
    protected void updateComplete(Village[][] pVillages, Image pBuffer) {
        mBuffer = pBuffer;
        mVisibleVillages = pVillages;

        updating = false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

/**Thread for updating after scroll operations*/
class RepaintThread extends Thread {

    private static Logger logger = Logger.getLogger(RepaintThread.class);
    private Village[][] mVisibleVillages = null;
    private BufferedImage mBuffer = null;
    private int iVillagesX = 0;
    private int iVillagesY = 0;
    private MapPanel mParent = null;
    private int iX = 456;
    private int iY = 468;
    private double dScaling = 1.0;
    private int xe = 0;
    private int ye = 0;
    private Village mSourceVillage = null;
    private BufferedImage mDistBorder = null;
    private final NumberFormat nf = NumberFormat.getInstance();

    public RepaintThread(MapPanel pParent, int pX, int pY) {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        setCoordinates(pX, pY);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        mParent = pParent;
        try {
            mDistBorder = ImageIO.read(new File("./graphics/dist_border.png"));
        } catch (Exception e) {
        }
    }

    protected void setCoordinates(int pX, int pY) {
        iX = pX;
        iY = pY;
    }

    @Override
    public void run() {
        while (true) {
            try {
                updateMap(iX, iY);
                //logger.info("Scal " + dScaling);
                // logger.info("w " + mParent.getWidth() + " h: " +mParent.getHeight() );
                mParent.updateComplete(mVisibleVillages, mBuffer.getScaledInstance(mParent.getWidth(), mParent.getHeight(), BufferedImage.SCALE_FAST));
                mParent.repaint();
            } catch (Throwable t) {
                logger.error("Redrawing map failed", t);
            }
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
        }
    }

    public void setZoom(double pZoom) {
        dScaling = pZoom;
    }

    public void setDragLine(int pXS, int pYS, int pXE, int pYE) {
        mSourceVillage = GlobalOptions.getDataHolder().getVillages()[pXS][pYS];
        xe = pXE;
        ye = pYE;
    }

    /**Extract the selected villages*/
    private void updateMap(int pX, int pY) {
        iX = pX;
        iY = pY;

        Village[][] villages = GlobalOptions.getDataHolder().getVillages();

        iVillagesX = (int) Math.rint((double) mParent.getWidth() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling).getWidth(null));
        iVillagesY = (int) Math.rint((double) mParent.getHeight() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling).getHeight(null));

        if (iVillagesX % 2 == 0) {
            iVillagesX++;
        }
        if (iVillagesY % 2 == 0) {
            iVillagesY++;
        }
        int xStart = (int) Math.rint((double) pX - (double) iVillagesX / 2.0);
        int yStart = (int) Math.rint((double) pY - (double) iVillagesY / 2.0);
        xStart = (xStart < 0) ? 0 : xStart;
        yStart = (yStart < 0) ? 0 : yStart;
        int xEnd = (int) Math.rint((double) iX + (double) iVillagesX / 2);
        int yEnd = (int) Math.rint((double) iY + (double) iVillagesY / 2);

        xEnd = (xEnd > 999) ? 999 : xEnd;
        yEnd = (yEnd > 999) ? 999 : yEnd;

        iVillagesX += 3;
        iVillagesY += 3;
        mVisibleVillages = new Village[iVillagesX][iVillagesY];

        int x = 0;
        int y = 0;

        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                mVisibleVillages[x][y] = villages[i][j];
                y++;
            }
            x++;
            y = 0;
        }
        redraw(xStart, yStart);
    }

    /**Redraw the buffer*/
    private void redraw(int pXStart, int pYStart) {
     //   long dStart = System.currentTimeMillis();

        int x = 0;
        int y = 0;
        Graphics2D g2d = null;
        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling).getHeight(null);

        if ((mBuffer == null) || ((mBuffer.getWidth(null) * mBuffer.getHeight(null)) != (mParent.getWidth() * mParent.getHeight()))) {
            mBuffer = new BufferedImage(mParent.getWidth(), mParent.getHeight(), BufferedImage.TYPE_INT_RGB);
            g2d = (Graphics2D) mBuffer.getGraphics();

        } else {
            g2d = (Graphics2D) mBuffer.getGraphics();
            g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        }
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Speed
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int xPos = pXStart;
        int yPos = pYStart;
        //disable decoration if field size is not equal the decoration texture size
        boolean useDecoration = true;
        if ((GlobalOptions.getWorldDecorationHolder().getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getWidth(null)) || (GlobalOptions.getWorldDecorationHolder().getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getHeight(null))) {
            useDecoration = false;
        }

        Line2D.Double dragLine = new Line2D.Double(-1, -1, xe, ye);
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];

                // <editor-fold defaultstate="collapsed" desc="Marker settings">
                Color marker = Color.WHITE;
                g2d.setColor(marker);
                if (v != null) {
                    if (v.getTribe() != null) {
                        if (v.getTribe().getName().equals(GlobalOptions.getProperty("player." + GlobalOptions.getProperty("default.server")))) {
                            marker = Color.YELLOW;
                        } else {
                            try {
                                marker = GlobalOptions.getMarkers().get(v.getTribe().getName());
                                if (marker == null) {
                                    marker = GlobalOptions.getMarkers().get(v.getTribe().getAlly().getName());
                                    if (marker == null) {
                                        throw new NullPointerException("");
                                    }
                                }
                            } catch (Throwable t) {
                                marker = Color.WHITE;
                            }
                        }
                    }
                }
                //</editor-fold>

                if ((v != null) && (mSourceVillage != null)) {
                    if ((mSourceVillage.getX() == v.getX()) && (mSourceVillage.getY() == v.getY())) {
                        dragLine.setLine(x + width / 2, y + height / 2, dragLine.getX2(), dragLine.getY2());
                    }
                }

                // <editor-fold defaultstate="collapsed" desc="Village drawing">
                if (v == null) {
                    if (useDecoration) {
                        g2d.drawImage(GlobalOptions.getWorldDecorationHolder().getTexture(xPos, yPos, dScaling), x, y, null);
                    } else {
                        g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, dScaling), x, y, null);
                    }

                } else {
                    boolean isLeft = false;
                    if (v.getTribe() == null) {
                        isLeft = true;
                    }

                    if (v.getPoints() < 300) {
                        if (!isLeft) {
                            Image img = GlobalOptions.getSkin().getImage(Skin.ID_V1, dScaling);
                            if (v.getType() != 0) {
                                img = GlobalOptions.getSkin().getImage(Skin.ID_B1, dScaling);
                            }
                            g2d.setColor(marker);
                            g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                            g2d.drawImage(img, x, y, null);
                        } else {
                            if (v.getType() == 0) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V1_LEFT, dScaling), x, y, null);
                            } else {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B1_LEFT, dScaling), x, y, null);
                            }
                        }
                    } else if (v.getPoints() < 1000) {
                        if (!isLeft) {
                            Image img = GlobalOptions.getSkin().getImage(Skin.ID_V2, dScaling);
                            if (v.getType() != 0) {
                                img = GlobalOptions.getSkin().getImage(Skin.ID_B2, dScaling);
                            }
                            g2d.setColor(marker);
                            g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                            g2d.drawImage(img, x, y, null);
                        } else {
                            if (v.getType() == 0) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V2_LEFT, dScaling), x, y, null);
                            } else {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B2_LEFT, dScaling), x, y, null);
                            }
                        }
                    } else if (v.getPoints() < 3000) {
                        if (!isLeft) {
                            Image img = GlobalOptions.getSkin().getImage(Skin.ID_V3, dScaling);
                            if (v.getType() != 0) {
                                img = GlobalOptions.getSkin().getImage(Skin.ID_B3, dScaling);
                            }
                            g2d.setColor(marker);
                            g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                            g2d.drawImage(img, x, y, null);
                        } else {
                            if (v.getType() == 0) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V3_LEFT, dScaling), x, y, null);
                            } else {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B3_LEFT, dScaling), x, y, null);
                            }
                        }
                    } else if (v.getPoints() < 9000) {
                        if (!isLeft) {
                            Image img = GlobalOptions.getSkin().getImage(Skin.ID_V4, dScaling);
                            if (v.getType() != 0) {
                                img = GlobalOptions.getSkin().getImage(Skin.ID_B4, dScaling);
                            }
                            g2d.setColor(marker);
                            g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                            g2d.drawImage(img, x, y, null);
                        } else {
                            if (v.getType() == 0) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V4_LEFT, dScaling), x, y, null);
                            } else {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B4_LEFT, dScaling), x, y, null);
                            }
                        }
                    } else if (v.getPoints() < 11000) {
                        if (!isLeft) {
                            Image img = GlobalOptions.getSkin().getImage(Skin.ID_V5, dScaling);
                            if (v.getType() != 0) {
                                img = GlobalOptions.getSkin().getImage(Skin.ID_B5, dScaling);
                            }
                            g2d.setColor(marker);
                            g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                            g2d.drawImage(img, x, y, null);
                        } else {
                            if (v.getType() == 0) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V5_LEFT, dScaling), x, y, null);
                            } else {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B5_LEFT, dScaling), x, y, null);
                            }
                        }
                    } else {
                        if (!isLeft) {
                            Image img = GlobalOptions.getSkin().getImage(Skin.ID_V6, dScaling);
                            if (v.getType() != 0) {
                                img = GlobalOptions.getSkin().getImage(Skin.ID_B6, dScaling);
                            }
                            g2d.setColor(marker);
                            g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                            g2d.drawImage(img, x, y, null);
                        } else {
                            if (v.getType() == 0) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V6_LEFT, dScaling), x, y, null);
                            } else {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B6_LEFT, dScaling), x, y, null);
                            }
                        }
                    }
                }
                //</editor-fold>

                y += height;
                yPos++;
            }
            y = 0;
            x += width;
            yPos = pYStart;
            xPos++;
        }

        if (mSourceVillage != null) {
            //draw dragging line for distance and attack
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(5.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            if (dragLine.getX1() == -1) {
                int xx = pXStart + xe / width;
                int yy = pYStart + ye / height;
                int tx = xx - mSourceVillage.getX();
                int ty = yy - mSourceVillage.getY();

                tx = xe - width * tx;
                ty = ye - height * ty;
                dragLine.setLine(tx, ty, xe, ye);
            }


            if ((dragLine.getX2() != 0) && (dragLine.getY2() != 0)) {
                int x1 = (int) dragLine.getX1();
                int y1 = (int) dragLine.getY1();
                int x2 = (int) dragLine.getX2();
                int y2 = (int) dragLine.getY2();
                g2d.drawLine(x1, y1, x2, y2);
                boolean drawDistance = false;
                try {
                    drawDistance = Boolean.parseBoolean(GlobalOptions.getProperty("draw.distance"));
                } catch (Exception e) {
                }
                if (drawDistance) {

                    Village t = mParent.getVillageAtMousePos();
                    if (t != null) {
                        double d = DSCalculator.calculateDistance(mSourceVillage, t);
                        String dist = nf.format(d);

                        Rectangle2D b = g2d.getFontMetrics().getStringBounds(dist, g2d);
                        g2d.drawImage(mDistBorder, null, x2 - 6, y2 - (int) Math.rint(b.getHeight()));
                        g2d.drawString(dist, x2, y2);
                    }
                }
            }
        }
       
        // <editor-fold defaultstate="collapsed" desc="Attack-line drawing">

        g2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
      
        for (Attack attack : GlobalOptions.getAttacks()) {
            //go through all attacks
            if (attack.isShowOnMap()) {
                 //only enter if attack should be visible
                //get line for this attack
                Line2D.Double attackLine = new Line2D.Double(attack.getSource().getX(), attack.getSource().getY(), attack.getTarget().getX(), attack.getTarget().getY());
                Rectangle2D.Double bounds = new Rectangle2D.Double(pXStart, pYStart, iVillagesX, iVillagesY);

                int xStart = ((int) attackLine.getX1() - pXStart) * width + width / 2;
                int yStart = ((int) attackLine.getY1() - pYStart) * height + height / 2;
                int xEnd = (int) (attackLine.getX2() - pXStart) * width + width / 2;
                int yEnd = (int) (attackLine.getY2() - pYStart) * height + height / 2;
                ImageIcon unitIcon = null;
                if (attack.getUnit().getName().equals("Speerträger")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_SPEAR);
                } else if (attack.getUnit().getName().equals("Schwertkämpfer")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_SWORD);
                } else if (attack.getUnit().getName().equals("Axtkämpfer")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_AXE);
                } else if (attack.getUnit().getName().equals("Bogenschütze")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_ARCHER);
                } else if (attack.getUnit().getName().equals("Späher")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_SPY);
                } else if (attack.getUnit().getName().equals("Leichte Kavallerie")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_LKAV);
                } else if (attack.getUnit().getName().equals("Berittener Bogenschütze")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_MARCHER);
                } else if (attack.getUnit().getName().equals("Schwere Kavallerie")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_HEAVY);
                } else if (attack.getUnit().getName().equals("Ramme")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_RAM);
                } else if (attack.getUnit().getName().equals("Katapult")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_CATA);
                } else if (attack.getUnit().getName().equals("Adelsgeschlecht")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_SNOB);
                } else if (attack.getUnit().getName().equals("Paladin")) {
                    unitIcon = GlobalOptions.getUnitIcon(GlobalOptions.ICON_KNIGHT);
                }

                long dur = (long) (DSCalculator.calculateMoveTimeInSeconds(attack.getSource(), attack.getTarget(),attack.getUnit().getSpeed()) * 1000);                  
                long arrive = attack.getArriveTime().getTime();
                long start = arrive - dur;
                long current = System.currentTimeMillis();
                int unitXPos = 0;
                int unitYPos = 0;
                if((start < current) && (arrive > current)){
                    //attack running
                    long runTime = System.currentTimeMillis() - start;
                    double perc = 100*runTime/dur;
                    perc /= 100;
                    double xTar = xStart + (xEnd - xStart)* perc;
                    double yTar = yStart  + (yEnd - yStart) * perc;
                    unitXPos = (int)xTar - unitIcon.getIconWidth()/2;
                    unitYPos = (int)yTar-unitIcon.getIconHeight()/2;
                }else if ((start > System.currentTimeMillis()) && (arrive > current)) {
                    //attack not running, draw unit in source village
                    unitXPos = (int)xStart - unitIcon.getIconWidth()/2;
                    unitYPos = (int)yStart-unitIcon.getIconHeight()/2;
                } else {
                    //attack arrived
                     unitXPos = (int)xEnd - unitIcon.getIconWidth()/2;
                    unitYPos = (int)yEnd-unitIcon.getIconHeight()/2;
                }
                g2d.setColor(Color.RED);
                g2d.drawLine(xStart, yStart, xEnd, yEnd);
                g2d.setColor(Color.YELLOW);
                if (bounds.contains(attackLine.getP1())) {
                    g2d.fillRect((int) xStart - 3, yStart - 1, 6, 6);
                }
                if (bounds.contains(attackLine.getP2())) {
                    g2d.fillOval((int) xEnd - 3, (int) yEnd - 3, 6, 6);
                }
                
                if (unitIcon != null) {
                    g2d.drawImage(unitIcon.getImage(), unitXPos, unitYPos, null);
                }
            }
        }
        //</editor-fold>
        g2d.dispose();
      //  System.out.println("DUr " + (System.currentTimeMillis() - dStart));
    }

    static boolean getLineLineIntersection(Line2D.Double l1,
            Line2D.Double l2,
            Point2D.Double intersection) {
        if (!l1.intersectsLine(l2)) {
            return false;
        }
        double x1 = l1.getX1(), y1 = l1.getY1(),
                x2 = l1.getX2(), y2 = l1.getY2(),
                x3 = l2.getX1(), y3 = l2.getY1(),
                x4 = l2.getX2(), y4 = l2.getY2();

        intersection.x = det(det(x1, y1, x2, y2), x1 - x2,
                det(x3, y3, x4, y4), x3 - x4) /
                det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
        intersection.y = det(det(x1, y1, x2, y2), y1 - y2,
                det(x3, y3, x4, y4), y3 - y4) /
                det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);

        return true;
    }

    static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }
}
