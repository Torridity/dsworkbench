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
package de.tor.tribes.ui.algo;

import de.tor.tribes.types.test.AnyTribe;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class TimeFrameVisualizer extends javax.swing.JPanel {

    private TimeFrame mTimeFrame = null;
    private BufferedImage STROKED = null;
    private BufferedImage DAILY_START_FRAME_FILL = null;
    private BufferedImage ONE_DAY_START_FRAME_FILL = null;
    private BufferedImage EXACT_START_FRAME_FILL = null;
    private BufferedImage ARRIVE_FRAME_FILL = null;
    private JScrollPane mParent = null;
    private HashMap<String, Object> popupInfo = null;

    /** Creates new form TimeFrameVisualizer */
    public TimeFrameVisualizer() {
        initComponents();
        try {
            STROKED = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = STROKED.createGraphics();
            g2d.setColor(Constants.DS_BACK_LIGHT);
            g2d.fillRect(0, 0, 3, 3);
            g2d.setColor(Constants.DS_BACK);
            g2d.drawLine(0, 2, 2, 0);
            g2d.dispose();
            DAILY_START_FRAME_FILL = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
            g2d = DAILY_START_FRAME_FILL.createGraphics();
            g2d.setColor(Color.CYAN);
            g2d.fillRect(0, 0, 3, 3);
            g2d.setColor(Color.BLUE);
            g2d.drawLine(0, 2, 2, 0);
            g2d.dispose();
            EXACT_START_FRAME_FILL = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
            g2d = EXACT_START_FRAME_FILL.createGraphics();
            g2d.setColor(Color.CYAN);
            g2d.fillRect(0, 0, 3, 3);
            g2d.dispose();
            ONE_DAY_START_FRAME_FILL = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
            g2d = ONE_DAY_START_FRAME_FILL.createGraphics();
            g2d.setColor(Color.CYAN);
            g2d.fillRect(0, 0, 3, 3);
            g2d.setColor(Color.BLUE);
            g2d.drawLine(1, 0, 1, 2);
            g2d.dispose();
            ARRIVE_FRAME_FILL = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
            g2d = ARRIVE_FRAME_FILL.createGraphics();
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, 3, 3);
            g2d.setColor(Color.BLACK);
            g2d.drawLine(0, 2, 2, 0);
            g2d.dispose();
        } catch (Exception e) {
        }

        popupInfo = new HashMap<String, Object>();
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                repaint();
            }
        });

        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                fireClickEvent(e);
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

    private void fireClickEvent(MouseEvent e) {
        /*  LongRange activeRange = (LongRange) popupInfo.get("active.range");
        if (activeRange == null) {
        return;
        }
        TimeSpan spanForRange = (TimeSpan) popupInfo.get("span.for.range");
        if (spanForRange == null) {
        return;
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
        spanForRange.setSpan(new IntRange(spanForRange.getSpan().getMinimumInteger() - 1, spanForRange.getSpan().getMaximumInteger() - 1));
        } else {
        spanForRange.setSpan(new IntRange(spanForRange.getSpan().getMinimumInteger() + 1, spanForRange.getSpan().getMaximumInteger() + 1));
        }
        repaint();*/
    }

    public void setScrollPane(JScrollPane pParent) {
        mParent = pParent;
    }

    public void refresh(TimeFrame pTimeFrame) {
        mTimeFrame = pTimeFrame;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mTimeFrame == null) {
            renderNoInfoView(g);
        } else {
            updateSize();
            LongRange startRange = mTimeFrame.getStartRange();
            LongRange arriveRange = mTimeFrame.getArriveRange();
            HashMap<LongRange, TimeSpan> startRanges = mTimeFrame.startTimespansToRangesMap(AnyTribe.getSingleton());
            HashMap<LongRange, TimeSpan> arriveRanges = mTimeFrame.arriveTimespansToRangesMap(null);
            long minValue = startRange.getMinimumLong();
            long maxValue = arriveRange.getMaximumLong();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(new TexturePaint(STROKED, new Rectangle(0, 0, 3, 3)));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            //draw frame around the entire range
            renderRange(new LongRange(startRange.getMinimumLong(), arriveRange.getMaximumLong()), startRange, arriveRange, false, false, g2d, null, popupInfo);
            g2d.setColor(Constants.DS_BACK);
            popupInfo.clear();
            //fill start range
            renderRange(startRange, startRange, arriveRange, true, false, g2d, null, popupInfo);
            //fill arrive range
            renderRange(arriveRange, startRange, arriveRange, false, true, g2d, null, popupInfo);
            Paint p = g2d.getPaint();
            Iterator<LongRange> rangeKeys = startRanges.keySet().iterator();
            while (rangeKeys.hasNext()) {
                LongRange currentRange = rangeKeys.next();

                TimeSpan spanForRange = startRanges.get(currentRange);
                if (spanForRange != null) {
                    if (spanForRange.isValidAtEveryDay()) {
                        g2d.setPaint(new TexturePaint(DAILY_START_FRAME_FILL, new Rectangle(0, 0, 3, 3)));
                    } else if (spanForRange.isValidAtExactTime()) {
                        g2d.setPaint(new TexturePaint(EXACT_START_FRAME_FILL, new Rectangle(0, 0, 3, 3)));
                    } else {
                        g2d.setPaint(new TexturePaint(ONE_DAY_START_FRAME_FILL, new Rectangle(0, 0, 3, 3)));
                    }
                }
                renderRange(currentRange, startRange, arriveRange, false, false, g2d, spanForRange, popupInfo);
            }

            Composite c = g2d.getComposite();
            rangeKeys = arriveRanges.keySet().iterator();
            while (rangeKeys.hasNext()) {
                LongRange currentRange = rangeKeys.next();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2d.setPaint(new TexturePaint(ARRIVE_FRAME_FILL, new Rectangle(0, 0, 3, 3)));
                TimeSpan spanForRange = arriveRanges.get(currentRange);
                renderRange(currentRange, startRange, arriveRange, false, false, g2d, spanForRange, popupInfo);
            }
            g2d.setComposite(c);
            g2d.setPaint(p);
            renderDayMarkers(minValue, maxValue, g2d);
            renderPopup(popupInfo, g2d);
        }
    }

    private void renderRange(LongRange pRange,
            LongRange pStartRange,
            LongRange pArriveRange,
            boolean pIsStartRange,
            boolean pIsArriveRange,
            Graphics2D pG2D,
            TimeSpan pSpanForRange,
            HashMap<String, Object> pPopupInfo) {
        int rangeStart = 0;
        int rangeWidth = 0;

        if (pRange.overlapsRange(pStartRange)) {
            //start range rendering
            long startDelta = pStartRange.getMinimumLong();
            rangeStart = Math.round((pRange.getMinimumLong() - startDelta) / DateUtils.MILLIS_PER_MINUTE);
            // int rangeEnd = Math.round((pRange.getMaximumLong() - startDelta) / DateUtils.MILLIS_PER_MINUTE);
            rangeWidth = Math.round((pRange.getMaximumLong() - pRange.getMinimumLong()) / DateUtils.MILLIS_PER_MINUTE);
        } else if (pRange.overlapsRange(pArriveRange)) {
            //end range rendering
            long startDelta = pStartRange.getMinimumLong();
            rangeStart = Math.round((pRange.getMinimumLong() - startDelta) / DateUtils.MILLIS_PER_MINUTE);
            // int rangeEnd = Math.round((pRange.getMaximumLong() - arriveDelta) / DateUtils.MILLIS_PER_MINUTE);
            rangeWidth = Math.round((pRange.getMaximumLong() - pRange.getMinimumLong()) / DateUtils.MILLIS_PER_MINUTE);
        }
        //correct small widths
        if (rangeWidth == 0) {
            rangeWidth = 5;
        }

        long max = Math.round((pArriveRange.getMaximumLong() - pStartRange.getMinimumLong()) / DateUtils.MILLIS_PER_MINUTE);

        if (rangeStart > max) {
            return;
        }

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String labelString = "";
        if (pSpanForRange != null) {
            labelString = pSpanForRange.toString();
        } else {
            labelString = f.format(new Date(pRange.getMinimumLong())) + " bis " + f.format(new Date(pRange.getMaximumLong()));
        }
        Rectangle2D labelBounds = pG2D.getFontMetrics().getStringBounds(labelString, pG2D);
        if (pIsStartRange) {
            pG2D.setColor(Color.RED);
            pG2D.fillRect(rangeStart, 20, rangeWidth, 20);
            pG2D.setColor(Color.BLACK);
            pG2D.drawRect(rangeStart, 20, rangeWidth, 20);
            pG2D.setColor(Color.RED);
            pG2D.setFont(pG2D.getFont().deriveFont(Font.BOLD, 14.0f));
            pG2D.drawString(labelString, rangeStart, (int) labelBounds.getHeight());
        } else if (pIsArriveRange) {
            pG2D.setColor(Color.GREEN.darker());
            pG2D.fillRect(rangeStart, 20, rangeWidth, 20);
            pG2D.setColor(Color.BLACK);
            pG2D.drawRect(rangeStart, 20, rangeWidth, 20);
            pG2D.setColor(Color.GREEN.darker());
            pG2D.setFont(pG2D.getFont().deriveFont(Font.BOLD, 14.0f));
            pG2D.drawString(labelString, rangeStart, (int) labelBounds.getHeight() + 40);
        } else {
            pG2D.fillRect(rangeStart, 20, rangeWidth, 20);
            pG2D.setColor(Color.BLACK);
            pG2D.drawRect(rangeStart, 20, rangeWidth, 20);
            Point loc = getMousePosition();
            if (loc != null && new Rectangle(rangeStart, 20, rangeWidth, 20).contains(loc)) {
                pPopupInfo.put("popup.location", loc);
                pPopupInfo.put("popup.label", labelString);
                pPopupInfo.put("active.range", pRange);
                pPopupInfo.put("span.for.range", pSpanForRange);

            }
        }
    }

    private void renderDayMarkers(long pStart, long pEnd, Graphics2D pG2D) {
        Date d = new Date(pStart);
        d = DateUtils.setHours(d, 0);
        d = DateUtils.setMinutes(d, 0);
        d = DateUtils.setSeconds(d, 0);
        d = DateUtils.setMilliseconds(d, 0);

        for (long mark = d.getTime(); mark <= pEnd; mark += DateUtils.MILLIS_PER_HOUR) {
            int markerPos = Math.round((mark - pStart) / DateUtils.MILLIS_PER_MINUTE);
            pG2D.setColor(Color.YELLOW);
            pG2D.fillRect(markerPos, 20, 2, 10);
            pG2D.setColor(Color.WHITE);
            pG2D.setFont(pG2D.getFont().deriveFont(Font.BOLD, 10.0f));
            pG2D.fillRect(markerPos - 5, 15, 12, 10);
            pG2D.setColor(Color.BLACK);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(mark));
            NumberFormat f = NumberFormat.getNumberInstance();
            f.setMinimumIntegerDigits(2);
            f.setMaximumFractionDigits(2);
            pG2D.drawString(f.format(cal.get(Calendar.HOUR_OF_DAY)), markerPos - 5, 23);
        }

        long currentDay = d.getTime();
        while (currentDay < pEnd) {
            currentDay += DateUtils.MILLIS_PER_DAY;
            int markerPos = Math.round((currentDay - pStart) / DateUtils.MILLIS_PER_MINUTE);
            pG2D.setColor(new Color(123, 123, 123));
            pG2D.fillRect(markerPos, 15, 3, 30);
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
            String dayLabel = f.format(currentDay);
            Rectangle2D labelBounds = pG2D.getFontMetrics().getStringBounds(dayLabel, pG2D);
            pG2D.setColor(Color.YELLOW);
            int labelWidth = (int) labelBounds.getWidth() + 5;
            pG2D.fillRect(markerPos - (int) Math.rint(labelWidth / 2), 15, labelWidth, 10);
            pG2D.setColor(Color.BLACK);
            pG2D.setFont(pG2D.getFont().deriveFont(Font.BOLD, 10.0f));
            pG2D.drawString(dayLabel, markerPos - (int) Math.rint(labelWidth / 2) + 2, 23);
        }
    }

    private void renderPopup(HashMap<String, Object> pPopupInfo, Graphics2D pG2D) {
        Point location = (Point) pPopupInfo.get("popup.location");
        String label = (String) pPopupInfo.get("popup.label");
        if (location == null || label == null) {
            return;
        }
        pG2D.setColor(new Color(255, 255, 204));
        Rectangle2D labelBounds = pG2D.getFontMetrics().getStringBounds(label, pG2D);
        pG2D.fillRect(location.x, location.y - (int) labelBounds.getHeight(), (int) labelBounds.getWidth() + 5, (int) labelBounds.getHeight() + 5);
        pG2D.setColor(Color.BLACK);
        pG2D.drawRect(location.x, location.y - (int) labelBounds.getHeight(), (int) labelBounds.getWidth() + 5, (int) labelBounds.getHeight() + 5);
        pG2D.drawString(label, location.x + 2, location.y);

    }

    private void updateSize() {
        LongRange startRange = mTimeFrame.getStartRange();
        LongRange arriveRange = mTimeFrame.getArriveRange();
        long minValue = startRange.getMinimumLong();
        long maxValue = arriveRange.getMaximumLong();
        Dimension size = new Dimension(Math.round((maxValue - minValue + 240 * (int) DateUtils.MILLIS_PER_MINUTE) / DateUtils.MILLIS_PER_MINUTE), getHeight());
        if (size.getWidth() < mParent.getWidth()) {
            size = mParent.getSize();
        }
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);
        mParent.getViewport().setViewSize(size);
    }

    /**Render default view if there is no timeframe yet*/
    private void renderNoInfoView(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new TexturePaint(STROKED, new Rectangle(0, 0, 3, 3)));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        Font f = g2d.getFont().deriveFont(Font.BOLD, 14.0f);
        g2d.setFont(f);
        Rectangle2D bounds = g2d.getFontMetrics().getStringBounds("Kein Zeitfenster aktiv", g);
        int dx = 10;
        if (getWidth() > bounds.getWidth()) {
            dx = (int) Math.rint((getWidth() - bounds.getWidth()) / 2);
        }

        int dy = 10;
        if (getHeight() > bounds.getHeight()) {
            dy = (int) Math.rint((getHeight() - bounds.getHeight()) / 2);
        }
        g2d.setColor(Color.black);
        g2d.drawString("Kein Zeitfenster aktiv", dx, dy);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMaximumSize(new java.awt.Dimension(32767, 50));
        setMinimumSize(new java.awt.Dimension(100, 50));
        setPreferredSize(new java.awt.Dimension(400, 50));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) throws Exception {
        JFrame fr = new JFrame();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        TimeFrame frame = new TimeFrame(f.parse("29.12.2010 22:00:00"),
                f.parse("29.12.2010 23:00:00"),
                f.parse("31.12.2010 19:53:00"),
                f.parse("31.12.2010 22:53:00"));
        //frame.addStartTimeSpan(new TimeSpan(new IntRange(0, 1)));
        frame.addStartTimeSpan(new TimeSpan(new IntRange(17, 24)));
        //  frame.addStartTimeSpan(new TimeSpan(f.parse("30.12.2010 00:00:00"), new IntRange(8, 22), Barbarians.getSingleton()));
        // frame.addStartTimeSpan(new TimeSpan(f.parse("30.12.2010 13:41:14")));

       // frame.addArriveTimeSpan(new TimeSpan(f.parse("30.12.2010 20:00:00")));
        TimeFrameVisualizer tfv = new TimeFrameVisualizer();

        JScrollPane sp = new JScrollPane(tfv);
        tfv.setScrollPane(sp);
        fr.add(sp);

        fr.pack();
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tfv.refresh(frame);
        fr.setVisible(true);
    }
}
