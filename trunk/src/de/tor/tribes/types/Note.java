/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class Note {

    private String sNoteText = null;
    private List<Integer> villageIDs = null;

    Note() {
        villageIDs = new LinkedList<Integer>();
    }

    public static Note fromXml() {
        Note n = new Note();
        return n;
    }

    public String toXml() {
        String result = "";
        return result;
    }

    public List<Integer> getVillageIDs() {
        return villageIDs;
    }

    public void setNoteText(String pText) {
        sNoteText = pText;
    }

    public String getNoteText() {
        return sNoteText;
    }
}
