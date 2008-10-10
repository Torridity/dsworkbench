/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.awt.Image;
import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.imageio.ImageIO;
import org.jdom.Element;
import de.tor.tribes.util.xml.JaxenUtils;

/**
 *
 * @author Charon
 */
public class Tag {

    private String sName = null;
    private String sIcon = null;
    private Image mIcon = null;
    private boolean showOnMap = true;

    public static Tag fromXml(Element pElement) throws Exception {
        String name = URLDecoder.decode(pElement.getChild("name").getText(), "UTF-8");
        String iconPath = pElement.getChild("resource").getText();
        if (iconPath != null) {
            iconPath = URLDecoder.decode(iconPath, "UTF-8");
        }
        boolean showOnMap = true;
        try {
            showOnMap = Boolean.parseBoolean(JaxenUtils.getNodeValue(pElement, "extensions/showOnMap"));
        } catch (Exception e) {
        }
        return new Tag(name, iconPath, showOnMap);
    }

    public Tag(String pName, String pIconPath, boolean pShowOnMap) {
        setName(pName);
        setIconPath(sIcon);
        setShowOnMap(pShowOnMap);
    }

    public String getName() {
        return sName;
    }

    public void setName(String pName) {
        this.sName = pName;
    }

    public String getIconPath() {
        return sIcon;
    }

    public void setIconPath(String pIcon) {
        this.sIcon = pIcon;
        loadTagIcon(pIcon);
    }

    private void loadTagIcon(String pFile) {
        try {
            mIcon = ImageIO.read(new File(pFile));
        } catch (Exception e) {
            mIcon = null;
        }
    }

    public Image getTagIcon() {
        return mIcon;
    }

    public void setShowOnMap(boolean pValue) {
        showOnMap = pValue;
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toXml() throws Exception {
        String ret = "<tag>\n";
        ret += "<name>" + URLEncoder.encode(getName(), "UTF-8") + "</name>\n";
        if (getIconPath() != null) {
            ret += "<resource>" + URLEncoder.encode(getIconPath(), "UTF-8") + "</resource>\n";
        } else {
            ret += "<resource/>\n";
        }
        ret += "<extensions>\n";
        ret += "<showOnMap>" + isShowOnMap() + "</showOnMap>\n";
        ret += "</extensions>\n";
        ret += "</tag>\n";
        return ret;
    }
}
