/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.map;

import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Line;
import de.tor.tribes.ui.DSWorkbenchFormFrame;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class FormManager {

    private static Logger logger = Logger.getLogger("FormManager");
    private static FormManager SINGLETON = null;
    private List<AbstractForm> forms = null;

    public static synchronized FormManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new FormManager();
        }
        return SINGLETON;
    }

    FormManager() {
        forms = new LinkedList<AbstractForm>();
    }

    public void loadFormsFromDatabase(String pUrl) {
    }

    public void loadFormsFromFile(String pFile) {
        forms.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        File formFile = new File(pFile);
        if (formFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.info("Loading forms from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(formFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//forms/form")) {
                    AbstractForm form = AbstractForm.fromXml(e);
                    if (form != null) {
                        forms.add(form);
                    }
                }
                logger.debug("Forms loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load forms", e);
            }
        } else {
            logger.info("No forms found under '" + pFile + "'");
        }
    }

    public void saveFormsToDatabase(String pUrl) {
    }

    public void saveFormsToFile(String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<forms>\n");

            for (AbstractForm form : forms) {
                w.write(form.toXml());
            }
            w.write("</forms>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store forms", e);
        }
    }

    /**
     * @return the forms
     */
    public synchronized List<AbstractForm> getForms() {
        return forms;
    }

    public synchronized void addForm(AbstractForm pForm) {
        if (pForm != null) {
            forms.add(pForm);
            DSWorkbenchFormFrame.getSingleton().updateFormList();
        }
    }

    public synchronized void removeForm(AbstractForm pForm) {
        forms.remove(pForm);
        DSWorkbenchFormFrame.getSingleton().updateFormList();
    }

    public synchronized List<AbstractForm> getVisibleForms() {
        List<AbstractForm> visible = new LinkedList<AbstractForm>();
        for (AbstractForm f : forms) {
            if (f.isVisibleOnMap()) {
                visible.add(f);
            }
        }
        return visible;
    }

    public static void main(String[] args) {
        Line l = new Line();
        l.setXPos(0);
        l.setYPos(0);
        l.setXPosEnd(10);
        l.setYPosEnd(10);
        l.setDrawColor(Color.RED);
        l.setStrokeWidth(1.0f);
        FormManager.getSingleton().addForm(l);
        FormManager.getSingleton().saveFormsToFile("forms.xml");
        FormManager.getSingleton().loadFormsFromFile("forms.xml");
    }
}
