/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewTimePanel.java
 *
 * Created on 19.09.2009, 21:30:23
 */
package de.tor.tribes.ui.algo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.JFrame;

/**
 *
 * @author Charon
 */
public class NewTimePanel extends javax.swing.JPanel {

    /** Creates new form NewTimePanel */
    public NewTimePanel() {
        /*  initComponents();
        TimePicker p = new TimePicker();
        p.setInterval(0, 60);
        p.setMonoValue(true);
        p.setBackground(Constants.DS_BACK_LIGHT);
        //p.setSelectInterval(10);
        p.setWatch(true);
        p.addTimePickerListener(new TimePickerListener() {

        @Override
        public void TimeChanged(TimePicker timepicker) {
        System.out.println("H: " + timepicker.getFirstHour());
        System.out.println("M: " + timepicker.getFirstMinute());
        System.out.println("P: " + timepicker.getFirstPeriod());

        }
        });
        jPanel2.add(p);*/
        /*    JXDatePicker picker = new JXDatePicker();
        picker.setDate(Calendar.getInstance().getTime());
        picker.setLocale(Locale.getDefault());
        jPanel3.add(picker);*/
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1) {
                    Point c = new Point((int) (getSize().getWidth() / 2), (int) (getSize().getHeight() / 2));
                    Point p = e.getPoint();
                    double dist = c.distance(p);
                   
                    double alpha = Math.acos((p.x - c.x) / dist);
                    double adeg = Math.toDegrees(alpha);
                    alpha = (alpha - Math.PI / 2d) / (Math.PI / 180d * -1);
                    double hours = alpha / (360 / 12);

                    if (adeg > 0 && adeg <= 90) {
                        hours = 6 - hours;
                    } else if (adeg > 90 && adeg <= 180) {
                        hours = 6 + Math.abs(hours);
                    } else if (adeg > 180 && adeg <= 270) {
                        
                      //  hours = 12 - hours;
                    }else{
                        
                        hours = 12 - hours;
                    }

                    //alpha = (alpha - Math.PI/2d) / (Math.PI/180d * -1);

                    ///alpha = -alpha * Math.PI / 180d + Math.PI / 2d;
                    //alpha = -alpha + Math.toRadians(90);

                } else {
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public double[] hoursToLocation(double hours, double minutes, double seconds, Point center, double scale) {
        // alpha is the angle of the hour hand

        double alpha = hours * (360 / 12) + minutes / 2;
        //beta is the angle of the minute hand
        double beta = minutes * 6 + seconds / 10;
        //gama is the angle of the second hand
        double gamma = seconds;
        alpha = -alpha * Math.PI / 180d + Math.PI / 2d;
        //alpha - Math.PI / 2d = -alpha * Math.PI / 180d;
        //-1 * (alpha - Math.PI / 2d ) / Math.PI / 180d = alpha
        beta = -beta * Math.PI / 180d + Math.PI / 2d;
        gamma = -gamma * Math.PI / 180d + Math.PI / 2d;

        /*        Point hPoint = new Point((int) (scale/2 * Math.cos(alpha) + center.x + .5), (int) (-scale/2 * Math.sin(alpha) + center.y + .5));
        Point mPoint = new Point((int) (scale * Math.cos(beta) + center.x + .5), (int) (-scale * Math.sin(beta) + center.y + .5));
        Point sPoint = new Point((int) (scale * Math.cos(gamma) + center.x + .5), (int) (-scale * Math.sin(gamma) + center.y + .5));
        return new Point[]{hPoint, mPoint, sPoint};*/
        return new double[]{alpha, beta, gamma};

    }

    public void paint(Graphics g) {
        Point c = new Point((int) (getSize().getWidth() / 2), (int) (getSize().getHeight() / 2));
        double[] hands = hoursToLocation(13, 22, 0, c, 50.0);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform t = g2d.getTransform();
        g2d.setTransform(AffineTransform.getRotateInstance(-hands[0] + Math.toRadians(90), c.x + .5, c.y + .5));
        GeneralPath p = new GeneralPath();
        p.moveTo(c.x, c.y);
        p.lineTo(c.x + 5, c.y - 5);
        p.lineTo(c.x, c.y - 30);
        p.lineTo(c.x - 5, c.y - 5);
        p.closePath();
        g2d.fill(p);
        g2d.setTransform(AffineTransform.getRotateInstance(-hands[1] + Math.toRadians(90), c.x + .5, c.y + .5));
        p = new GeneralPath();
        p.moveTo(c.x, c.y);
        p.lineTo(c.x + 5, c.y - 5);
        p.lineTo(c.x, c.y - 80);
        p.lineTo(c.x - 5, c.y - 5);
        p.closePath();
        g2d.fill(p);
        g2d.setTransform(t);
        Point sPoint = new Point((int) (50.0 * Math.cos(hands[2]) + c.x + .5), (int) (-50.0 * Math.sin(hands[2]) + c.y + .5));
        g2d.drawLine(c.x, c.y, sPoint.x, sPoint.y);

    //g2d.drawLine(c.x, c.y, hands[0].x, hands[0].y);
    //g2d.drawLine(c.x, c.y, hands[1].x, hands[1].y);
    //  g2d.drawLine(c.x, c.y, hands[2].x, hands[2].y);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Startzeit"));

        jPanel2.setBackground(new java.awt.Color(102, 255, 102));
        jPanel2.setPreferredSize(new java.awt.Dimension(120, 400));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(255, 0, 51));
        jPanel3.setPreferredSize(new java.awt.Dimension(110, 20));
        jPanel3.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(310, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new NewTimePanel());
        f.pack();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
