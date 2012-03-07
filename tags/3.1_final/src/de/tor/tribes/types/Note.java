/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.bb.VillageListFormatter;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author Jejkal
 */
public class Note extends ManageableType implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%LAST_CHANGE%", "%NOTE_TEXT%", "%VILLAGE_LIST%", "%NOTE_ICON%"};
    private final static String STANDARD_TEMPLATE = "[quote][b]Notiz vom:[/b] %LAST_CHANGE%\n\n[b]Zugeordnete Dörfer:[/b]\n%VILLAGE_LIST%\n\n[b]Notizsymbol:[/b] %NOTE_ICON%\n\n[b]Notiztext:[/b]\n\n%NOTE_TEXT%[/quote]";
    private String sNoteText = null;
    private List<Integer> villageIds = null;
    private long timestamp = -1;
    private int noteSymbol = -1;
    private int mapMarker = 0;

    public Note() {
        villageIds = new LinkedList<Integer>();
        setTimestamp(System.currentTimeMillis());
        sNoteText = "";
    }

    public static String toInternalRepresentation(Note pNote) {
        StringBuilder b = new StringBuilder();
        try {
            b.append(pNote.getNoteSymbol()).append("&").append(URLEncoder.encode(pNote.getNoteText(), "UTF-8")).append("&").append(pNote.getMapMarker()).append("&").append(pNote.getTimestamp()).append("&");
            for (Integer i : pNote.getVillageIds()) {
                b.append(i).append(",");
            }
        } catch (UnsupportedEncodingException usee) {
            return "";
        }

        return b.toString();

    }

    public static Note fromInternalRepresentation(String pLine) {
        Note m = new Note();
        try {

            String[] split = pLine.trim().split("&");
            m.setNoteSymbol(Integer.parseInt(split[0]));
            m.setNoteText(URLDecoder.decode(split[1], "UTF-8"));
            m.setMapMarker(Integer.parseInt(split[2]));
            m.setTimestamp(Long.parseLong(split[3]));
            if (split.length == 5) {
                String[] villages = split[4].split(",");
                if (villages != null) {
                    for (String vs : villages) {
                        try {
                            Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(vs));
                            if (v != null) {
                                m.addVillage(v);
                            }
                        } catch (NumberFormatException nfe1) {
                        }
                    }
                }
            } else {
                //no villages
            }
        } catch (NumberFormatException nfe) {
            m = null;
        } catch (IllegalArgumentException iae) {
            m = null;
        } catch (UnsupportedEncodingException usee) {
            m = null;
        }
        return m;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadFromXml(Element e) {

        setTimestamp(Long.parseLong(e.getChild("timestamp").getText()));
        setMapMarker(Integer.parseInt(e.getChild("mapMarker").getText()));
        setNoteSymbol(Integer.parseInt(e.getChild("noteSymbol").getText()));
        try {
            setNoteText(URLDecoder.decode(e.getChild("text").getText(), "UTF-8"));
        } catch (UnsupportedEncodingException usee) {
            setNoteText("FEHLER");
        }
        for (Element elem : (List<Element>) JaxenUtils.getNodes(e, "villages/village")) {
            getVillageIds().add(Integer.parseInt(elem.getValue()));
        }

    }

    @Override
    public String toXml() {
        try {
            String result = "<note>\n";
            result += "<timestamp>" + getTimestamp() + "</timestamp>\n";
            result += "<mapMarker>" + getMapMarker() + "</mapMarker>\n";
            result += "<noteSymbol>" + getNoteSymbol() + "</noteSymbol>\n";
            result += "<text>" + URLEncoder.encode(getNoteText(), "UTF-8") + "</text>\n";
            result += "<villages>\n";
            for (Integer id : getVillageIds()) {
                result += "<village>" + id + "</village>\n";
            }
            result += "</villages>\n";
            result += "</note>\n";
            return result;
        } catch (UnsupportedEncodingException usee) {
            return null;
        }
    }

    public String toBBCode() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[quote][b]Notiz vom:[/b] ");
        buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(new Date(getTimestamp())));
        buffer.append("\n\n");
        buffer.append("[b]Zugeordnete Dörfer:[/b]\n\n");
        boolean isNext = false;
        for (Integer id : villageIds) {
            if (isNext) {
                buffer.append(", ");
            }
            Village v = DataHolder.getSingleton().getVillagesById().get(id);
            buffer.append(v.toBBCode());
            isNext = true;
        }
        buffer.append("\n\n");
        if (getNoteSymbol() != -1) {
            buffer.append("[b]Notizsymbol:[/b] [img]").append(ImageManager.getNoteImageURLOnServer(getNoteSymbol())).append("[/img]\n\n");
        }
        buffer.append("[b]Notiztext:[/b]\n\n");
        buffer.append(getNoteText());
        buffer.append("[/quote]\n");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return getNoteText();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public final void setTimestamp(long pValue) {
        timestamp = pValue;
    }

    public void setNoteText(String pText) {
        if (sNoteText != null) {
            if (!sNoteText.equals(pText)) {
                sNoteText = pText;
                setTimestamp(System.currentTimeMillis());
            }
        } else {
            sNoteText = pText;
            setTimestamp(System.currentTimeMillis());
        }
    }

    public String getNoteText() {
        if (sNoteText == null || sNoteText.length() < 1) {
            return "-Leere Notiz-";
        } else {
            return sNoteText;
        }
    }

    /**
     * @return the villageIds
     */
    public List<Integer> getVillageIds() {
        return villageIds;
    }

    public boolean addVillage(Village v) {
        if (villageIds.contains(v.getId())) {
            return false;
        }
        villageIds.add(v.getId());
        setTimestamp(System.currentTimeMillis());
        return true;
    }

    public void removeVillage(Village v) {
        villageIds.remove((Integer) v.getId());
        setTimestamp(System.currentTimeMillis());
    }

    /**
     * @param villageIds the villageIds to set
     */
    public void setVillageIds(List<Integer> villageIds) {
        this.villageIds = villageIds;
    }

    /**
     * @return the mapMarker
     */
    public int getMapMarker() {
        return mapMarker;
    }

    /**
     * @param mapMarker the mapMarker to set
     */
    public void setMapMarker(int mapMarker) {
        this.mapMarker = mapMarker;
        setTimestamp(System.currentTimeMillis());
    }

    /**
     * @return the noteSymbol
     */
    public int getNoteSymbol() {
        return noteSymbol;
    }

    /**
     * @param noteSymbol the noteSymbol to set
     */
    public void setNoteSymbol(int noteSymbol) {
        this.noteSymbol = noteSymbol;
        setTimestamp(System.currentTimeMillis());
    }

    @Override
    public String getElementIdentifier() {
        return "note";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "notes";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "name";
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String lastChangeVal = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(new Date(getTimestamp()));
        String villageVal = "";

        List<Village> villages = new LinkedList<Village>();
        for (Integer id : villageIds) {
            Village v = DataHolder.getSingleton().getVillagesById().get(id);
            if (v != null) {
                villages.add(v);
            }
        }

        villageVal = new VillageListFormatter().formatElements(villages, pExtended);

        String noteSymbolVal = "";
        if (getNoteSymbol() != -1) {
            noteSymbolVal = "[img]" + ImageManager.getNoteImageURLOnServer(getNoteSymbol()) + "[/img]";
        } else {
            noteSymbolVal = "-kein Symbol-";
        }
        String noteTextVal = getNoteText();
        return new String[]{lastChangeVal, noteTextVal, villageVal, noteSymbolVal};
    }
}