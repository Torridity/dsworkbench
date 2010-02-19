package de.tor.tribes.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimePicker.java
 *
 * Created on 18.02.2010, 14:25:29
 */
/**
 *
 * @author Jejkal
 */
public class TimePicker extends javax.swing.JPanel {

    private JPanel minutePanel = new JPanel();
    private boolean minutesExpanded = false;
    private static final Font smallFont = new Font("Dialog", 0, 10);
    private int pHour = 0;
    private int pMinute = 0;
    private JTextField selectedHour = null;
    private JTextField selectedMinute = null;
    JPanel hourPanel = new JPanel();

    /** Creates new form TimePicker */
    public TimePicker(Date pDate) {
        initComponents();
        Calendar cal = Calendar.getInstance();
        pHour = cal.get(Calendar.HOUR_OF_DAY);
        pMinute = cal.get(Calendar.MINUTE);
        addHourLabels();
        addMinuteLabels(false);

        setBorder(new javax.swing.plaf.BorderUIResource.EtchedBorderUIResource());
    }

    public void addHourLabels() {

        setLayout(new AbsoluteLayout());
        hourPanel.setLayout(new AbsoluteLayout());
        setMinimumSize(new Dimension(260, 220));
        setMaximumSize(getMinimumSize());
        setPreferredSize(getMinimumSize());
        hourPanel.setBackground(Color.RED);
        add(hourPanel, new AbsoluteConstraints(10, 10, 240, 40));
        int row = -1;
        for (int j = 0; j < 24; j++) {
            JTextField jLabel1 = new JTextField();
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            if (j < 10) {
                jLabel1.setText("0" + Integer.toString(j));
            } else {
                jLabel1.setText(Integer.toString(j));
            }
            jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
            jLabel1.setHorizontalAlignment(0);
            jLabel1.setOpaque(true);
            jLabel1.setEditable(false);
            jLabel1.setBackground(Color.LIGHT_GRAY);
            jLabel1.setFont(smallFont);
            jLabel1.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedHour != null) {
                        selectedHour.setBackground(Color.LIGHT_GRAY);
                        selectedHour.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
                    }
                    selectedHour = ((JTextField) e.getSource());
                    pHour = Integer.parseInt(selectedHour.getText());
                    selectedHour.setBackground(Color.YELLOW);
                    selectedHour.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    ((JTextField) e.getSource()).setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));//Background(Color.YELLOW.brighter());
                }

                public void mouseExited(MouseEvent e) {
                    if (e.getSource() != selectedHour) {
                        ((JTextField) e.getSource()).setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
                    } else {
                        selectedHour.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    }
                }
            });

            if (j == pHour) {
                jLabel1.setBackground(Color.YELLOW);
                selectedHour = jLabel1;
                selectedHour.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
            int col = j % 12;
            if (j % 12 == 0) {
                row++;
            }
            hourPanel.add(jLabel1, new AbsoluteConstraints(col * 20, row * 20, 20, 20));
        }
    }

    public void addMinuteLabels(boolean pEachMinute) {
        minutePanel.removeAll();

        minutesExpanded = pEachMinute;
        minutePanel.setLayout(new AbsoluteLayout());
        minutePanel.setBackground(Color.WHITE);
        int max = 0;
        int elemsPerRow = 0;

        if (pEachMinute) {
            //12 elems per row, 5 rows
            max = 60;
            elemsPerRow = 8;
        } else {
            //6 elems per row, 2 rows
            max = 12;
            elemsPerRow = 6;
        }
        int rowWidth = 240 / elemsPerRow;
        int rowHeight = 20;
        add(minutePanel, new AbsoluteConstraints(10, 50, 240, rowHeight * (max / elemsPerRow) + rowHeight));
        int row = -1;
        for (int j = 0; j < max; j++) {
            JTextField jLabel1 = new JTextField();
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            int num = j;
            if (!pEachMinute) {
                num = j * 5;
            }
            if (num < 10) {
                jLabel1.setText(":0" + Integer.toString(num));
            } else {
                jLabel1.setText(":" + Integer.toString(num));
            }
            jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
            jLabel1.setHorizontalAlignment(0);
            jLabel1.setOpaque(true);
            jLabel1.setEditable(false);
            jLabel1.setBackground(Color.WHITE);
            jLabel1.setFont(smallFont);

            jLabel1.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    if (selectedMinute != null) {
                        selectedMinute.setBackground(Color.WHITE);
                        selectedMinute.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
                    }
                    selectedMinute = ((JTextField) e.getSource());
                    pMinute = Integer.parseInt(selectedMinute.getText().replaceAll(":", ""));
                    selectedMinute.setBackground(Color.YELLOW);
                    selectedMinute.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    ((JTextField) e.getSource()).setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));//Background(Color.YELLOW.brighter());
                }

                public void mouseExited(MouseEvent e) {
                    if (e.getSource() != selectedMinute) {
                        ((JTextField) e.getSource()).setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
                    } else {
                        selectedMinute.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    }
                }
            });

            if (pEachMinute) {
                if (j == pMinute) {
                    jLabel1.setBackground(Color.YELLOW);
                    selectedMinute = jLabel1;
                    selectedMinute.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                }
            } else {
                int min = (int) Math.rint(pMinute / 5) * 5;
                if (j * 5 == min) {
                    jLabel1.setBackground(Color.YELLOW);
                    selectedMinute = jLabel1;
                    selectedMinute.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                }
            }
            int col = j % elemsPerRow;
            if (j % elemsPerRow == 0) {
                row++;
            }

            minutePanel.add(jLabel1, new AbsoluteConstraints(col * rowWidth, row * rowHeight, rowWidth, rowHeight));
        }
        JButton expandButton = new JButton();
        expandButton.setFont(smallFont);
        expandButton.setMargin(new Insets(2, 2, 2, 2));
        if (pEachMinute) {
            expandButton.setText("<<");
        } else {
            expandButton.setText(">>");
        }

        expandButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                addMinuteLabels(!minutesExpanded);
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
        minutePanel.add(expandButton, new AbsoluteConstraints(240 - rowWidth, rowHeight * (max / elemsPerRow), rowWidth, 20));

        minutePanel.updateUI();
    }

    public static void main(String[] args) {
        /*  JFrame f = new JFrame();
        f.add(new TimePicker(Calendar.getInstance().getTime()));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);*/
       
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
