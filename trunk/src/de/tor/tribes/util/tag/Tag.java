/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.tag;

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Tag {

    private String sName = null;
    private String sIcon = null;
    private Image mIcon = null;

    public static Tag fromXml(Element pElement) {
        return new Tag(pElement.getChild("name").getText(), pElement.getChild("resource").getText());
    }

    public Tag(String pName, String pIconPath) {
        setName(pName);
        setIconPath(pIconPath);
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

    @Override
    public String toString() {
        return getName();
    }

    public String toXml() {
        String ret = "<tag>\n";
        ret += "<name>" + getName() + "</name>\n";
        ret += "<resource>" + getIconPath() + "</resource>\n";
        ret += "</tag>\n";
        return ret;
    }
}
