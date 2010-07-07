/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.xml.JaxenUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class ReportSet {

    private String name = null;
    private List<FightReport> reports = null;

    public ReportSet(String pSetName) {
        setName(pSetName);
        reports = new LinkedList<FightReport>();
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getName() {
        return name;
    }

    public static ReportSet fromXml(Element e) throws Exception {
        String name = URLDecoder.decode(e.getAttributeValue("name"), "UTF-8");
        ReportSet set = new ReportSet(name);
        for (Element m : (List<Element>) JaxenUtils.getNodes(e, "reports/report")) {
            try {
                FightReport report = new FightReport(m);
                set.addReport(report);
            } catch (Exception inner) {
                //ignored, report invalid
            }
        }
        return set;
    }

    public String toXml() throws Exception {
        String result = "";

        result += "<reportSet name=\"" + URLEncoder.encode(getName(), "UTF-8") + "\">\n";
        result += "<reports>\n";
        for (FightReport r : getReports()) {
            String xml = r.toXml();
            if (xml != null) {
                result += xml + "\n";
            }
        }
        result += "</reports>\n";
        result += "</reportSet>\n";
        return result;
    }

    public FightReport[] getReports() {
        return reports.toArray(new FightReport[]{});
    }

    public synchronized void addReport(FightReport pReport) {
        reports.add(pReport);
    }

    public synchronized void removeReport(FightReport pReport) {
        reports.remove(pReport);
    }

    public String toString() {
        String res = "Name: " + getName() + "\n";
        res += "Reports: " + getReports().length + "\n";
        return res;
    }
}
