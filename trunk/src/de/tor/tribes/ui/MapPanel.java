/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 *
 * @author  Charon
 */
public class MapPanel extends javax.swing.JPanel implements ActionListener {

    private Village[][] mVisibleVillages = null;
    private Image mBuffer = null;
    private VillageInfoPanel mInfoPanel = null;
    private Village mTempVillage = null;
    private double dScaling = 1.0;
    private JMenuItem infoItem;
    private int downX = 0;
    private int downY = 0;
    private int mX = 456;
    private int mY = 468;
    private RepaintThread mRepaintThread = null;
    boolean updating = false;
    private Timer mTooltipTimer = null;
    private MapFrame mParent;
    private List<Cursor> mCursors = null;
    private int iCurrentCursor = 0;

    // private VillageTooltipFrame mTooltipFrame = null;
    /** Creates new form MapPanel */
    public MapPanel(MapFrame pParent) {
        initComponents();
        mCursors = new LinkedList<Cursor>();
        final JPanel parent = this;
        mParent = pParent;
        infoItem = new JMenuItem("Info");
        infoItem.addActionListener(this);
        jPopupMenu1.add(infoItem);
        mTooltipTimer = new Timer("TooltipTimer", true);

        try {
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/default.png"), new Point(0, 0), "default"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_light.png"), new Point(0, 0), "attack_light"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_heavy.png"), new Point(0, 0), "attack_heavy"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_spy.png"), new Point(0, 0), "attack_snob"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_sqord.png"), new Point(0, 0), "attack_ram"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/move.png"), new Point(0, 0), "move"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/zoom.png"), new Point(0, 0), "zoom"));
            mCursors.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/mark.png"), new Point(0, 0), "mark"));
            setCursor(mCursors.get(iCurrentCursor));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {

                iCurrentCursor += e.getWheelRotation();
                if (iCurrentCursor < 0) {
                    iCurrentCursor = mCursors.size() - 1;
                } else if (iCurrentCursor > mCursors.size() - 1) {
                    iCurrentCursor = 0;
                }
                setCursor(mCursors.get(iCurrentCursor));
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                //switch between left and right clicking the map panel
                if (e.getButton() == MouseEvent.BUTTON1) {
                    fireShowVillageInfoEvent();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
            //reset tooltip timer
               /* mTooltipFrame.setVisible(false);
            resetTimer();*/
            }
        });

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

        jPopupMenu1 = new javax.swing.JPopupMenu();

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    /**Draw buffer into panel*/
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paint(g);
        int dx = 0;
        int dy = 0;

        g2d.drawImage(mBuffer, 0, 0, null);
        Toolkit.getDefaultToolkit().sync();

    }

    /**Update map to new position*/
    protected void updateMap(int pX, int pY) {
        updating = true;
        if (mRepaintThread == null) {
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

            x /= GlobalOptions.getSkin().getFieldWidth() / dScaling;
            y /= GlobalOptions.getSkin().getFieldHeight() / dScaling;

            return mVisibleVillages[x][y];
        } catch (Exception e) {
            return null;
        }
    }

    /**Show info of village located at current mouse position*/
    public void fireShowVillageInfoEvent() {
        Village v = getVillageAtMousePos();
        if (v != null) {
            final JFrame f = new JFrame();
            f.setTitle("Dorf " + v.getName());
            VillageInfoPanel vinp = new VillageInfoPanel(GlobalOptions.getDataHolder());
            vinp.showVillage(v.getX(), v.getY());
            f.add(vinp);
            f.setLocationRelativeTo(this);
            f.setLocation(getMousePosition());
            f.pack();
            f.setVisible(true);
        }
    }

    /**Update operation perfomed by the RepaintThread was completed*/
    protected void updateComplete(Village[][] pVillages, Image pBuffer) {
        mBuffer = pBuffer;
        mVisibleVillages = pVillages;
        repaint();
        Village v = getVillageAtMousePos();
        mParent.showInfo(v);
        updating = false;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == infoItem) {
            fireShowVillageInfoEvent();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu jPopupMenu1;
    // End of variables declaration//GEN-END:variables
}

/**Thread for updating after scroll operations*/
class RepaintThread extends Thread {

    private Village[][] mVisibleVillages = null;
    private BufferedImage mBuffer = null;
    private int iFieldWidth = 0;
    private int iFieldHeight = 0;
    private int iVillagesX = 0;
    private int iVillagesY = 0;
    private MapPanel mParent = null;
    private int iX = 456;
    private int iY = 468;
    private double dScaling = 1.0;

    public RepaintThread(MapPanel pParent, int pX, int pY) {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        setCoordinates(pX, pY);
        mParent = pParent;
        iFieldWidth = GlobalOptions.getSkin().getFieldWidth();
        iFieldHeight = GlobalOptions.getSkin().getFieldHeight();
    //mBuffer = new BufferedImage(mParent.getWidth(), mParent.getHeight(), BufferedImage.TYPE_INT_RGB);
    }
//boolean painted = false;
    protected void setCoordinates(int pX, int pY) {
        iX = pX;
        iY = pY;
    }

    @Override
    public void run() {
        while (true) {
            updateMap(iX, iY);
            mParent.updateComplete(mVisibleVillages, mBuffer.getScaledInstance(mParent.getWidth(), mParent.getHeight(), BufferedImage.SCALE_FAST));
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
        }
    }

    public void setZoom(double pZoom) {
        dScaling = pZoom;
    }

    /**Extract the selected villages*/
    private void updateMap(int pX, int pY) {
        iX = pX;
        iY = pY;

        Village[][] villages = GlobalOptions.getDataHolder().getVillages();

        iVillagesX = (int) ((double) mParent.getWidth() / (double) iFieldWidth * dScaling);
        iVillagesY = (int) ((double) mParent.getHeight() / (double) iFieldHeight * dScaling);

        if (iVillagesX % 2 == 0) {
            iVillagesX++;
        }
        if (iVillagesY % 2 == 0) {
            iVillagesY++;
        }
        int xStart = pX - iVillagesX / 2;
        int yStart = pY - iVillagesY / 2;
        xStart = (xStart < 0) ? 0 : xStart;
        yStart = (yStart < 0) ? 0 : yStart;
        int xEnd = iX + iVillagesX / 2;
        int yEnd = iY + iVillagesY / 2;

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
//BufferStrategy b = new BufferStrategy() {}
        int x = 0;
        int y = 0;
        Graphics2D g2d = null;

        if ((mBuffer == null) || ((mBuffer.getWidth(null) * mBuffer.getHeight(null)) != (mParent.getWidth() * mParent.getHeight()))) {
            mBuffer = new BufferedImage(mParent.getWidth(), mParent.getHeight(), BufferedImage.TYPE_INT_RGB);
            g2d = (Graphics2D) mBuffer.getGraphics();

        } else {
            g2d = (Graphics2D) mBuffer.getGraphics();
            g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        }
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
        // long s = System.currentTimeMillis();
        //System.out.println("Villages# " + (iVillagesX * iVillagesY));
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];
                Color marker = Color.WHITE;
                g2d.setColor(marker);
                try {
                    marker = GlobalOptions.getMarkers().get(GlobalOptions.getDataHolder().getTribes().get(v.getTribe()).getName());
                    if (marker == null) {
                        String name = GlobalOptions.getDataHolder().getAllies().get(GlobalOptions.getDataHolder().getTribes().get(v.getTribe()).getAlly()).getName();
                        marker = GlobalOptions.getMarkers().get(name);

                        if (marker == null) {
                            throw new NullPointerException("");
                        }
                    }
                } catch (Exception e) {
                }


                if (v == null) {
                    g2d.drawImage(GlobalOptions.getWorldDecorationHolder().getTexture(xPos, yPos, dScaling), x, y, null);
                //g2d.fillRect(xPos, yPos, 10, 10);
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
                y += iFieldHeight / dScaling;
                yPos++;

            }
            y = 0;
            x += iFieldWidth / dScaling;
            yPos = pYStart;
            xPos++;
        }

        // System.out.println("Villages " + (System.currentTimeMillis() - s));
        g2d.dispose();
    }
}
