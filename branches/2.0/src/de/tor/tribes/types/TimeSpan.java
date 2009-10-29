/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Charon
 */
public class TimeSpan {

    private Date atDate = null;
    private Point span = null;

    public TimeSpan(Point pSpan) {
        span = pSpan;
    }

    public TimeSpan(Date pAtDate, Point pSpan) {
        atDate = pAtDate;
        span = pSpan;
    }

    public Date getAtDate() {
        return atDate;
    }

    public Point getSpan() {
        return span;
    }

    public void setSpan(Point pSpan) {
        span = pSpan;
    }

    @Override
    public String toString() {
        String result = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        if (atDate != null) {
            result = "Am " + f.format(atDate) + ", von " + span.x + " Uhr bis " + span.y + " Uhr";
        } else {
            result = "Täglich, von " + span.x + " Uhr bis " + span.y + " Uhr";
        }

        return result;
    }
}
