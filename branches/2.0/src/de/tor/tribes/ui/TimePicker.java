/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TimePicker.java

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JFrame;

public class TimePicker extends Canvas
    implements MouseListener, MouseMotionListener
{

    public TimePicker()
    {
        gOff = null;
        imgOff = null;
        ld = null;
        _$3549 = 0;
        _$3553 = 0;
        _$3557 = 0;
        _$3561 = 24;
        _$3565 = 8;
        _$3567 = 19;
        _$3571 = true;
        _$3574 = true;
        s1 = -1;
        s2 = -1;
        _$3593 = null;
        _$3607 = -1;
        _$3628 = 16;
        _$3631 = false;
        _$3638 = 15;
        _$3644 = false;
        _$3569 = new Vector(5);
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.lightGray);
        setForeground(Color.darkGray);
    }

    public void setInterval(int i, int j)
    {
        _$3565 = Math.min(i, j);
        _$3567 = Math.max(i, j);
        if(_$3565 < _$3557)
            _$3565 = _$3557;
        if(_$3567 > _$3561)
            _$3567 = _$3561;
        gOff = null;
    }

    public void setMinMax(int i, int j)
    {
        _$3557 = Math.min(i, j);
        _$3561 = Math.max(i, j);
        if(_$3565 < i)
            _$3565 = i;
        if(_$3567 > j)
            _$3567 = j;
        gOff = null;
    }

    public int getFirstHour()
    {
        int i = sh1 % 24;
        return _$3644 ? toampm(i) : i;
    }

    public int getFirstPeriod()
    {
        int i = sh1 % 24;
        return i >= 12 ? 1 : 0;
    }

    public int getFirstMinute()
    {
        int i = _$3565 % 24;
        return (i * 60 + s1 * _$3638) % 60;
    }

    public int getLastHour()
    {
        int i = sh2 % 24;
        return _$3571 ? -1 : _$3644 ? toampm(i) : i;
    }

    public int getLastMinute()
    {
        return _$3571 ? -1 : (s2 * _$3638) % 60;
    }

    public int getLastPeriod()
    {
        int i = sh2 % 24;
        return i >= 12 ? 1 : 0;
    }

    public int getDurationHour()
    {
        return _$3571 ? -1 : (Math.abs(s2 - s1) * _$3638) / 60;
    }

    public int getDurationMinute()
    {
        return _$3571 ? 0 : (Math.abs(s2 - s1) * _$3638) % 60;
    }

    public void setMonoValue(boolean flag)
    {
        _$3571 = flag;
    }

    public boolean getMonoValue()
    {
        return _$3571;
    }

    public void setSelectInterval(int i)
    {
        _$3638 = i;
    }

    public int getSelectInterval()
    {
        return _$3638;
    }

    public void setWatch(boolean flag)
    {
        _$3574 = flag;
    }

    public boolean getWatch()
    {
        return _$3574;
    }

    public void setAMPMMode(boolean flag)
    {
        _$3644 = flag;
    }

    public boolean getAMPMMode(boolean flag)
    {
        return _$3644;
    }

    public int toampm(int i)
    {
        return i % (i <= 12 ? 13 : 12);
    }

    public void addTimePickerListener(TimePickerListener timepickerlistener)
    {
        if(_$3569.indexOf(timepickerlistener) == -1)
            _$3569.addElement(timepickerlistener);
    }

    public void removeTimePickerListener(TimePickerListener timepickerlistener)
    {
        _$3569.removeElement(timepickerlistener);
    }

    protected void notifyListener()
    {
        TimePickerListener timepickerlistener;
        for(Enumeration enumeration = _$3569.elements(); enumeration.hasMoreElements(); timepickerlistener.TimeChanged(this))
            timepickerlistener = (TimePickerListener)enumeration.nextElement();

    }

    public void paint(Graphics g)
    {
        if(g != null && (gOff == null || ld == null || ld != getSize()))
        {
            ld = getSize();
            imgOff = createImage(ld.width, ld.height);
            gOff = imgOff.getGraphics();
            createBackground();
        }
        update(g);
    }

    public synchronized void update(Graphics g)
    {
        if(gOff != null)
        {
            g.drawImage(imgOff, 0, 0, this);
            drawButton(g);
            if(s1 != -1)
                _$3622 = drawPointed(g, s1);
            if(_$3607 != -1 && _$3607 != s1)
            {
                _$3625 = drawPointed(g, _$3607);
                if(_$3574)
                    drawClock(g);
                if(s1 != -1)
                    drawDelay(g);
            }
        }
    }

    public void drawButton(Graphics g)
    {
        int i = ld.width / 2;
        byte byte0 = 3;
        int j = _$3628 - byte0 * 2;
        if(_$3565 > _$3557)
        {
            g.setColor(Color.gray);
            g.drawLine(i, byte0, i - j / 2, byte0 + j);
            g.setColor(Color.white);
            g.drawLine(i, byte0, i + j / 2, byte0 + j);
            g.drawLine(i - j / 2, byte0 + j, i + j / 2, byte0 + j);
        }
        if(_$3567 < _$3561)
        {
            g.setColor(Color.gray);
            g.drawLine(i - j / 2, ld.height - j - byte0, i + j / 2, ld.height - byte0 - j);
            g.drawLine(i, ld.height - byte0, i - j / 2, ld.height - byte0 - j);
            g.setColor(Color.white);
            g.drawLine(i, ld.height - byte0, i + j / 2, ld.height - byte0 - j);
        }
    }

    public void drawDelay(Graphics g)
    {
        int i = (Math.abs(_$3622 - _$3625) - _$3593.getHeight()) + 1;
        int j = Math.min(_$3622, _$3625) + _$3593.getHeight();
        int k = _$3595 / 4;
        g.setColor(Color.yellow);
        g.fillRect((ld.width - k) / 2, j, k, i);
        if(i > _$3593.getHeight())
        {
            int l = Math.abs(s1 - _$3607) * _$3638;
            String s = String.valueOf(l % 60);
            if(s.length() < 2)
                s = "0".concat(String.valueOf(String.valueOf(s)));
            String s3 = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(l / 60)))).append(":").append(s)));
            drawTip(g, j + i / 2, s3);
        }
    }

    public void createBackground()
    {
        _$3642 = (_$3567 - _$3565) * 60;
        Color color = getForeground();
        if(gOff == null)
            return;
        gOff.setColor(getBackground());
        gOff.clearRect(0, 0, ld.width, ld.height);
        _$3593 = getFontMetrics(getFont());
        _$3588 = (_$3567 - _$3565) * 4;
        _$3628 = ld.height / (_$3588 + 2);
        if(_$3628 < 16 || _$3628 > 20)
            _$3628 = 16;
        _$3640 = ld.height - _$3628 * 2 - _$3593.getHeight();
        _$3640 /= _$3642;
        _$3599 = (int)(((double)ld.height - _$3640 * (double)_$3642) / (double)2);
        if(_$3599 < _$3628)
            _$3599 = _$3628;
        _$3595 = _$3593.stringWidth("000:0000");
        gbase = (ld.width - _$3593.stringWidth("000:000")) / 3;
        _$3619 = gbase / 2;
        gOff.setColor(color);
        double d = 0.0D;
        for(int i = 0; i <= _$3588; i++)
        {
            double d1 = (double)_$3599 + (double)(i * 15) * _$3640;
            drawGrad((int)d1, i);
        }

        _$3603 = _$3599 + (int)(_$3640 * (double)_$3642);
        gOff.drawLine(_$3619, _$3599, _$3619, _$3603);
        gOff.setColor(Color.white);
        gOff.drawLine(_$3619 + 1, _$3599, _$3619 + 1, _$3603);
        gOff.setColor(color);
        gOff.drawLine(ld.width - _$3619, _$3599, ld.width - _$3619, _$3603);
        gOff.setColor(Color.white);
        gOff.drawLine((ld.width - _$3619) + 1, _$3599, (ld.width - _$3619) + 1, _$3603);
        ntip = _$3640 * (double)_$3638;
    }

    public int drawPointed(Graphics g, int i)
    {
        int j = i * _$3638 + _$3565 * 60;
        int k = (int)((double)i * ntip + (double)_$3599);
        String s3 = String.valueOf(j % 60);
        if(s3.length() < 2)
            s3 = "0".concat(String.valueOf(String.valueOf(s3)));
        int l = (j / 60) % 24;
        String s;
        if(_$3644)
            s = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(toampm(l))))).append(":").append(s3).append(l >= 12 ? "pm" : "am")));
        else
            s = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(l)))).append(":").append(s3)));
        return drawTip(g, k, s);
    }

    public int drawTip(Graphics g, int i, String s)
    {
        int j = _$3593.getHeight();
        if((double)i < (double)_$3599 - ntip / (double)2)
            i = _$3599;
        if((double)i > (double)_$3603 + ntip / (double)2)
            i = _$3603;
        i -= j / 2;
        g.setColor(Color.yellow);
        g.fillRect(gbase + _$3619, i, _$3595, j);
        g.setColor(Color.black);
        g.drawRect(gbase + _$3619, i, _$3595, j);
        g.drawString(s, gbase + _$3619 + (_$3595 - _$3593.stringWidth(s)) / 2, (i + j) - _$3593.getDescent());
        return i;
    }

    protected void drawGrad(int i, int j)
    {
        Color color = getForeground();
        int k = 0;
        switch(j % 4)
        {
        case 0: // '\0'
            k = gbase;
            int l = (_$3565 + j / 4) % 24;
            if(_$3644)
                l %= l <= 12 ? 13 : 12;
            String s = String.valueOf(String.valueOf(l)).concat(":00");
            int i1 = (ld.width - _$3593.stringWidth(s)) / 2;
            int j1 = i + _$3593.getHeight() / 2;
            if(_$3644 && l % 12 == 0)
            {
                gOff.setColor(Color.white);
                gOff.drawString(s, i1 - 1, j1 + 1);
            }
            gOff.setColor(color);
            gOff.drawString(s, i1, j1);
            break;

        case 1: // '\001'
        case 3: // '\003'
            k = 0;
            break;

        case 2: // '\002'
            k = gbase / 2;
            break;
        }
        if(k > 0)
        {
            gOff.setColor(Color.white);
            gOff.drawLine(_$3619 + 1, i - 1, _$3619 + k, i - 1);
            gOff.drawLine(ld.width - _$3619 - k - 1, i + 1, ld.width - _$3619 - 1, i + 1);
            gOff.setColor(color);
            gOff.drawLine(_$3619, i, _$3619 + k, i);
            gOff.drawLine(ld.width - _$3619 - k, i, ld.width - _$3619, i);
        }
    }

    public void drawClock(Graphics g)
    {
        int i = (int)((double)_$3607 * ntip) + _$3599;
        if((double)i < (double)_$3599 - ntip / (double)2)
            i = _$3599;
        if((double)i > (double)_$3603 + ntip / (double)2)
            i = _$3603;
        i -= _$3593.getHeight() / 2;
        Point point = new Point(ld.width - _$3619 - gbase, i + _$3593.getHeight() / 2);
        g.setColor(Color.yellow);
        g.fillOval(point.x, i, _$3593.getHeight(), _$3593.getHeight());
        g.setColor(Color.black);
        g.drawOval(point.x, i, _$3593.getHeight(), _$3593.getHeight());
        point.x += _$3593.getHeight() / 2;
        drawHand(g, (int)((double)_$3593.getHeight() * 0.34999999999999998D), _$3607 * _$3638, point);
        drawHand(g, (int)((double)_$3593.getHeight() * 0.25D), ((_$3607 * _$3638) / 60 + _$3565) * 5, point);
    }

    public static double minuteToAngle(int i)
    {
        return ((double)i * 3.1415926535897931D) / (double)30 - 1.5707963267948966D;
    }

    public Point minutePoint(Point point, int i, double d)
    {
        return new Point((int)((double)i * Math.cos(d)) + point.x, (int)((double)i * Math.sin(d)) + point.y);
    }

    protected void drawHand(Graphics g, int i, int j, Point point)
    {
        double d = minuteToAngle(j);
        Point point1 = minutePoint(point, i, d);
        g.drawLine(point.x, point.y, point1.x, point1.y);
    }

    protected int pointed(int i, int j)
    {
        if(i < gbase || i > ld.width - gbase)
            return -1;
        if(j < _$3599)
            return 0;
        if(j >= _$3603)
            return _$3642 / _$3638;
        else
            return (int)((double)(j - _$3599) / ntip);
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
    }

    public void mouseExited(MouseEvent mouseevent)
    {
        _$3607 = -1;
        s1 = -1;
        _$3631 = false;
        repaint();
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
        if(_$3631)
            mousePressed(mouseevent);
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent mouseevent)
    {
        _$3631 = false;
        if(mouseevent.getY() < _$3599 - _$3593.getHeight() / 2 && _$3565 > _$3557)
        {
            System.out.println("HERE");
            _$3565--;
            _$3567--;
            if(s1 != -1)
                s1 += 60 / _$3638;
            createBackground();
            repaint();
            return;
        }
        if(mouseevent.getY() > _$3603 + _$3593.getHeight() / 2 && _$3567 < _$3561)
        {
            _$3565++;
            _$3567++;
            if(s1 != -1)
                s1 -= 60 / _$3638;
            createBackground();
            repaint();
            return;
        }
        int i = pointed(mouseevent.getX(), mouseevent.getY());
        if(i == -1)
        {
            s1 = -1;
            repaint();
            return;
        }
        if(_$3571)
        {
            s1 = _$3607;
            sh1 = ((s1 * _$3638) / 60 + _$3565) % 24;
            notifyListener();
            s1 = -1;
            repaint();
            return;
        }
        if(s1 == -1)
        {
            s1 = _$3607;
            repaint();
            return;
        }
        if(s1 == _$3607)
        {
            s1 = -1;
            repaint();
            return;
        } else
        {
            s2 = Math.max(s1, _$3607);
            s1 = Math.min(s1, _$3607);
            sh1 = (s1 * _$3638 + _$3565 * 60) / 60;
            sh1 = (sh1 + 24) % 24;
            sh2 = ((s2 * _$3638 + _$3565 * 60) / 60) % 24;
            notifyListener();
            s1 = -1;
            s2 = -1;
            repaint();
            return;
        }
    }

    public void mouseDragged(MouseEvent mouseevent)
    {
        if(s1 == -1)
        {
            return;
        } else
        {
            _$3631 = true;
            mouseMoved(mouseevent);
            return;
        }
    }

    public void mouseMoved(MouseEvent mouseevent)
    {
        int i = pointed(mouseevent.getX(), mouseevent.getY());
        if(i != _$3607)
        {
            _$3607 = i;
            repaint();
        }
    }

    Graphics gOff;
    Image imgOff;
    Dimension ld;
    public static final int AM_PERIOD = 0;
    public static final int PM_PERIOD = 1;
    private int _$3549;
    private int _$3553;
    private int _$3557;
    private int _$3561;
    private int _$3565;
    private int _$3567;
    private Vector _$3569;
    private boolean _$3571;
    private boolean _$3574;
    protected int s1;
    protected int s2;
    protected int gbase;
    private int _$3588;
    private FontMetrics _$3593;
    private int _$3595;
    private int _$3599;
    private int _$3603;
    private int _$3607;
    public double ntip;
    private int _$3619;
    private int _$3622;
    private int _$3625;
    private int _$3628;
    private boolean _$3631;
    private int _$3638;
    private double _$3640;
    private int _$3642;
    private boolean _$3644;
    int sh1;
    int sh2;

    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        TimePicker pick = new TimePicker();
        pick.addTimePickerListener(new TimePickerListener() {

            public void TimeChanged(TimePicker timepicker) {
              //  timepicker.setInterval(1, 5);
            }
        });
        pick.setInterval(0, 24);
        pick.setAMPMMode(false);
        pick.setMonoValue(false);
        f.add(pick);
        f.setVisible(true);
    }
}
