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
package de.tor.tribes.ui.components;

import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.interfaces.LayerOrderTooltipListener;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 *
 * @author Torridity
 */
public class LayerOrderPanel extends javax.swing.JPanel {

    int activeLayer = -1;
    int focusLayer = -1;
    private List<Layer> layers = new ArrayList<>();
    private HashMap<String, BufferedImage> iconMap = new HashMap<>();
    private LayerOrderTooltipListener tooltipListener = null;

    /** Creates new form LayerOrderPanel */
    public LayerOrderPanel(LayerOrderTooltipListener pListener) {
        initComponents();
        tooltipListener = pListener;
        Vector<String> v = new Vector<>(Constants.LAYERS.size());
        for (int i = 0; i < Constants.LAYERS.size(); i++) {
            v.add("");
        }

        String layerOrder = GlobalOptions.getProperty("layer.order");
        if (layerOrder == null) {
            Enumeration<String> values = Constants.LAYERS.keys();
            while (values.hasMoreElements()) {
                String layer = values.nextElement();
                v.set(Constants.LAYERS.get(layer), layer);
            }
        } else {
            //try to use stored layers
            String[] layers = layerOrder.split(";");
            if (layers.length == Constants.LAYER_COUNT) {
                //layer sizes are equal, so set layers in stored order
                int cnt = 0;
                for (String layer : layers) {
                    if (layer.equals("Formen")) {
                        //disable old forms id
                        v.set(cnt, "Zeichnungen");
                    } else {
                        v.set(cnt, layer);
                    }
                    cnt++;
                }
            } else {
                //layer number has changed since value was stored, so rebuild
                Enumeration<String> values = Constants.LAYERS.keys();
                while (values.hasMoreElements()) {
                    String layer = values.nextElement();
                    v.set(Constants.LAYERS.get(layer), layer);
                }
            }
        }

        for (String layer : v) {
            layers.add(new Layer(layer, null, Constants.LAYERS.get(layer)));
        }

        try {
            iconMap.put("Angriffe", ImageIO.read(LayerOrderPanel.class.getResource("/res/barracks.png")));
            iconMap.put("Dorfsymbole", ImageIO.read(new File("graphics/icons/village_symbols.png")));
            iconMap.put("Zeichnungen", ImageIO.read(LayerOrderPanel.class.getResource("/res/ui/draw_small.gif")));
            iconMap.put("Notizmarkierungen", ImageIO.read(new File("graphics/icons/note.png")));
            iconMap.put("Dörfer", ImageIO.read(new File("graphics/icons/village.png")));
            iconMap.put("Unterstützungen", ImageIO.read(new File("graphics/icons/def.png")));
            iconMap.put("Markierungen", ImageIO.read(new File("graphics/icons/mark.png")));
            iconMap.put("Truppendichte", ImageIO.read(new File("graphics/icons/village_troops.png")));
            iconMap.put("Kirchenradien", ImageIO.read(new File("graphics/icons/church_layer.png")));
        } catch (Exception ignored) {
        }

        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (activeLayer != -1) {
                    return;
                }
                focusLayer = -1;
                int idx = 0;
                for (Layer l : layers) {
                    if (l.getDragEllipse() != null && l.getDragEllipse().contains(e.getPoint())) {
                        activeLayer = idx;
                        break;
                    }
                    idx++;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activeLayer = -1;
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (activeLayer != -1) {
                    return;
                }
                int idx = 0;
                focusLayer = -1;
                for (Layer l : layers) {
                    if (l.getDragEllipse() != null && l.getDragEllipse().contains(e.getPoint())) {
                        focusLayer = idx;
                        break;
                    }
                    idx++;
                }

                if (focusLayer != -1) {
                    tooltipListener.fireShowTooltipEvent(layers.get(idx).getName());
                } else {
                    tooltipListener.fireShowTooltipEvent(null);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                repaint();
            }
        });
        propagateLayerOrder();
    }

    private BufferedImage getLayerImage(boolean active, boolean visible, boolean marker) {
        BufferedImage img = ImageUtils.createCompatibleBufferedImage(100, 100, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = img.createGraphics();
        AffineTransform sat = AffineTransform.getTranslateInstance(10, 10);
        sat.shear(0, -.5);
        g2.setTransform(sat);

        if (!active && visible) {
            g2.setColor(Color.DARK_GRAY);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
        } else if (!active && !visible) {
            if (marker) {
                g2.setColor(Color.DARK_GRAY);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
            } else {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
            }
        } else {
            g2.setColor(Color.decode("#F0E68C"));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
        g2.fillRect(0, 30, 50, 40);
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 30, 50, 40);
        g2.dispose();
        return img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Paint p = g2d.getPaint();
        int i = 0;
        int ellipseDiameter = 24;
        Layer active = null;
        Ellipse2D dragEllipse = null;
        boolean visible = false;
        boolean gotVillages = false;
        boolean gotMarkers = false;

        boolean markerLayer = false;
        boolean villageLayer = false;
        Ellipse2D villageEllipse = null;
        Ellipse2D markerEllipse = null;
        for (Layer l : layers) {
            g2d.setColor(Color.LIGHT_GRAY);
            switch (l.getName()) {
                case "Dörfer":
                    gotVillages = true;
                    markerLayer = false;
                    villageLayer = true;
                    break;
                case "Markierungen":
                    gotMarkers = true;
                    markerLayer = true;
                    villageLayer = false;
                    break;
                default:
                    markerLayer = false;
                    villageLayer = false;
                    break;
            }

            //check if this layer is visible or if it is the marker/village layer
            visible = (gotVillages && gotMarkers) || villageLayer || markerLayer;

            if (i == activeLayer) {//this is the active layer
                active = l;
                Point loc = getMousePosition();
                BufferedImage img = getLayerImage(true, visible, markerLayer);
                if (loc != null) {
                    g2d.drawImage(img, loc.x, i * 15, null);
                    g2d.drawLine(loc.x + 10, i * 15 + 80, loc.x + 10, 220);
                    dragEllipse = new Ellipse2D.Double(loc.x, 210, ellipseDiameter, ellipseDiameter);
                    l.setDragEllipse(dragEllipse);
                } else {
                    g2d.drawImage(img, 20 + i * 30, i * 15, null);
                    g2d.drawLine(20 + i * 30 + 10, i * 15 + 80, 20 + i * 30 + 10, 220);
                    l.setDragEllipse(new Ellipse2D.Double(20 + i * 30, 210, ellipseDiameter, ellipseDiameter));
                }
            } else {//this is no active layer
                BufferedImage img = getLayerImage(false, visible, markerLayer);
                g2d.drawImage(img, 20 + i * 30, i * 15, null);
                g2d.drawLine(20 + i * 30 + 10, i * 15 + 80, 20 + i * 30 + 10, 220);

                if (i == focusLayer) {
                    dragEllipse = new Ellipse2D.Double(20 + i * 30, 210, ellipseDiameter, ellipseDiameter);
                    l.setDragEllipse(dragEllipse);
                } else {
                    l.setDragEllipse(new Ellipse2D.Double(20 + i * 30, 210, ellipseDiameter, ellipseDiameter));
                }
            }

            //connect marker and village layer
            if (markerLayer) {
                markerEllipse = l.getDragEllipse();
            }
            if (villageLayer) {
                villageEllipse = l.getDragEllipse();
            }

            //this is the active/focussed layer
            if (i == focusLayer || i == activeLayer) {
                g2d.setColor(Color.decode("#F0E68C"));
                g2d.fill(l.getDragEllipse());
            } else {
                g2d.fill(l.getDragEllipse());
            }

            g2d.setColor(Color.BLACK);
            g2d.draw(l.getDragEllipse());
            BufferedImage im = iconMap.get(l.getName());
            if (im != null) {
                g2d.drawImage(im.getScaledInstance(16, 16, BufferedImage.SCALE_SMOOTH), (int) l.getDragEllipse().getX() + 4, (int) l.getDragEllipse().getY() + 4, this);
            }
            i++;
        }
        if (markerEllipse != null && villageEllipse != null) {
            g2d.setColor(Color.RED);
            double mcx = markerEllipse.getCenterX();
            double mcy = markerEllipse.getCenterY();
            double vcx = villageEllipse.getCenterX();
            double vcy = villageEllipse.getCenterY();
            GeneralPath path = new GeneralPath();
            path.moveTo(mcx, mcy - 5);
            path.lineTo(mcx, mcy + 5);
            path.lineTo(vcx, vcy + 5);
            path.lineTo(vcx, vcy - 5);
            path.closePath();
            g2d.setColor(Color.GREEN);
            Composite before = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
            Area a = new Area(villageEllipse);
            a.add(new Area(path));
            a.add(new Area(markerEllipse));
            g2d.fill(a);
            g2d.setColor(Color.BLACK);
            g2d.draw(a);
            g2d.setComposite(before);
            markerEllipse = null;
            villageEllipse = null;
        }
        //sort layers
        Collections.sort(layers, new Comparator<Layer>() {

            @Override
            public int compare(Layer o1, Layer o2) {
                return Double.compare(o1.getDragEllipse().getCenterX(), o2.getDragEllipse().getCenterX());
            }
        });

        //reset active layer idx
        if (active != null) {
            for (i = 0; i < layers.size(); i++) {
                if (layers.get(i).equals(active)) {
                    activeLayer = i;
                    break;
                }
            }
        }
        propagateLayerOrder();
    }

    private void propagateLayerOrder() {
        List<Integer> layerOrder = new ArrayList<>();
        for (Layer l : layers) {
            layerOrder.add(Constants.LAYERS.get(l.getName()));
        }
        MapPanel.getSingleton().getMapRenderer().setDrawOrder(layerOrder);
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public static class Layer {

        private String name = null;
        private int idx = -1;
        private Color col = null;
        private Ellipse2D dragEllipse = null;

        public Layer(String pName, Color pColor, int pIdx) {
            name = pName;
            idx = pIdx;
            col = pColor;
        }

        public Ellipse2D getDragEllipse() {
            return dragEllipse;
        }

        public void setDragEllipse(Ellipse2D pEl) {
            dragEllipse = pEl;
        }

        public Color getColor() {
            return col;
        }

        public void setIndex(int pIdx) {
            idx = pIdx;
        }

        public int getIndex() {
            return idx;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(200, 200));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
