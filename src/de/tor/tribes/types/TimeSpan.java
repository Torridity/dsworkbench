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

    private Tribe validFor = null;
    private Date atDate = null;
    private Point span = null;

    public TimeSpan(Point pSpan) {
        span = pSpan;
        validFor = null;
    }

    public TimeSpan(Date pAtDate, Point pSpan) {
        atDate = pAtDate;
        span = pSpan;
        validFor = null;
    }

    public TimeSpan(Point pSpan, Tribe pTribe) {
        span = pSpan;
        validFor = pTribe;
    }

    public TimeSpan(Date pAtDate, Point pSpan, Tribe pTribe) {
        atDate = pAtDate;
        span = pSpan;
        validFor = pTribe;
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

    public Tribe isValidFor() {
        return validFor;
    }

    @Override
    public String toString() {
        String result = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        if (atDate != null) {
            result = "Am " + f.format(atDate) + ", von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        } else {
            result = "Täglich, von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        }

        return result;
    }
}
