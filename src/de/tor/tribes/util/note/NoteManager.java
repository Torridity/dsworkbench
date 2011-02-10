/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.note;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchNotepad;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class NoteManager {

    private static Logger logger = Logger.getLogger("NoteManager");
    private static NoteManager SINGLETON = null;
    private List<Note> notes = null;

    public static synchronized NoteManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoteManager();
        }
        return SINGLETON;
    }

    NoteManager() {
        notes = new LinkedList<Note>();
    }

    public void loadNotesFromFile(String pFile) {

        notes.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        File noteFile = new File(pFile);
        if (noteFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading notes from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(noteFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//notes/note")) {
                    try {
                        Note n = Note.fromXml(e);
                        notes.add(n);
                    } catch (Exception inner) {
                        //ignored, marker invalid
                    }
                }
                logger.debug("Notes successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load notes", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Notes file not found under '" + pFile + "'");
            }
        }
        /*  int cnt = 0;
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
        for (int i = 0; i < 3; i++) {
        Note n = new Note();
        n.addVillage(v);
        int icon = (int) Math.rint(Math.random() * 9);
        n.setMapMarker(icon);
        n.setNoteText("Text" + cnt);
        addNote(n);
        }
        cnt++;

        }*/

        if (notes.size() > 0) {
            //set current note
            DSWorkbenchNotepad.getSingleton().setCurrentNote(notes.get(0));
        }
    }

    public void loadNotesFromDatabase(String pUrl) {
        logger.info("Not implemented yet");
    }

    public void saveNotesToFile(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing notes to '" + pFile + "'");
        }
        try {
            StringBuilder b = new StringBuilder();
            b.append("<notes>\n");
            Note[] aNotes = notes.toArray(new Note[]{});
            for (Note n : aNotes) {
                String xml = n.toXml();
                if (xml != null) {
                    b.append(xml).append("\n");
                }
            }
            b.append("</notes>");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("Notes successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save notes", e);
            }
        }

    }

    public void saveNotesToDatabase() {
        logger.info("Not implemented yet");
    }

    public String getExportData() {
        try {
            logger.debug("Generating notes export data");
            String result = "<notes>\n";

            Note[] aNotes = notes.toArray(new Note[]{});
            for (Note note : aNotes) {
                result += note.toXml();
            }
            result += "</notes>\n";
            logger.debug("Export data generated successfully");
            return result;
        } catch (Exception e) {
            logger.error("Failed to generate notes export data", e);
            return "";
        }
    }

    public boolean importNotes(File pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }

        logger.info("Loading notes");

        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//notes/note")) {
                Note note = Note.fromXml(e);
                if (note != null) {
                    notes.add(note);
                }
            }
            logger.debug("Notes imported successfully");
            DSWorkbenchNotepad.getSingleton().resetView();
            return true;
        } catch (Exception e) {
            logger.error("Failed to import notes", e);
            DSWorkbenchNotepad.getSingleton().resetView();
            return false;
        }
    }

    public Note getNextNote(Note pCurrent) {
        if (notes.isEmpty()) {
            //no element
            return null;
        } else if (pCurrent == null || notes.size() == 1) {
            //only one element
            return notes.get(0);
        } else {
            int id = notes.indexOf(pCurrent);
            if (id + 1 < notes.size()) {
                return notes.get(id + 1);
            } else {
                return notes.get(0);
            }
        }
    }

    public Note getPreviousNote(Note pCurrent) {
        if (notes.isEmpty()) {
            //no element
            return null;
        } else if (pCurrent == null || notes.size() == 1) {
            //only one element
            return notes.get(0);
        } else {
            int id = notes.indexOf(pCurrent);
            if (id - 1 >= 0) {
                return notes.get(id - 1);
            } else {
                return notes.get(notes.size() - 1);
            }
        }
    }

    public Note getFirstNote() {
        //iterator = notes.listIterator();
        if (notes.isEmpty()) {
            return null;
        }
        return notes.get(0);
    }

    public Note getLastNote() {
        if (notes.isEmpty()) {
            return null;
        }
        return notes.get(notes.size() - 1);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void addNote(Note pNote) {
        notes.add(pNote);
    }

    public void removeNote(Note pNote) {
        notes.remove(pNote);
    }

    public Note findNote(Note pStart, String pText) {
        if (pStart == null) {
            if (notes.size() >= 1) {
                pStart = notes.get(0);
            } else {
                //no notes available
                return null;
            }
        }
        Note[] aNotes = notes.toArray(new Note[]{});
        if (aNotes == null) {
            return null;
        }
        for (int i = notes.indexOf(pStart) + 1;;) {
            Note n = null;
            if (i < notes.size()) {
                //search next note
                n = aNotes[i];
                i++;
            } else {
                //last note reached, start from beginning
                i = 0;
                n = aNotes[i];
                i++;
            }

            if (n.getNoteText().toLowerCase().indexOf(pText.toLowerCase()) > -1) {
                //search in note text
                return n;
            }
            //search in village name
            for (Integer id : n.getVillageIds()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                if (v != null) {
                    if (v.toString().toLowerCase().indexOf(pText.toLowerCase()) > -1) {
                        return n;
                    }
                }
            }
            if (i == notes.indexOf(pStart) + 1) {
                break;
            }
        }
        return null;
    }

    public Note getNoteForVillage(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        for (Note n : notes) {
            if (n.getVillageIds().contains(pVillage.getId())) {
                return n;
            }
            /*for (Integer id : n.getVillageIds()) {
            if (id == pVillage.getId()) {
            return n;
            }
            }*/
        }
        return null;
    }

    public List<Note> getNotesForVillage(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        List<Note> noteList = new LinkedList<Note>();
        for (Note n : notes) {
            if (n.getVillageIds().contains(pVillage.getId())) {
                noteList.add(n);
            }
        }
        return noteList;
    }

    public Hashtable<Village, List<Note>> getNotesMap() {

        Hashtable<Village, List<Note>> noteMap = new Hashtable<Village, List<Note>>();

        for (Note n : notes) {
            for (Integer id : n.getVillageIds()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                List<Note> list = noteMap.get(v);
                if (list == null) {
                    list = new ArrayList<Note>();
                    noteMap.put(v, list);
                }
                list.add(n);
            }
        }
        return noteMap;
    }
}
