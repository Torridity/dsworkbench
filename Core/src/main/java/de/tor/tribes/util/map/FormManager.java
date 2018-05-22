/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util.map;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.drawing.AbstractForm;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.xml.JaxenUtils;
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
public class FormManager extends GenericManager<AbstractForm> {

    private static Logger logger = Logger.getLogger("FormManager");
    private static FormManager SINGLETON = null;

    public static synchronized FormManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new FormManager();
        }
        return SINGLETON;
    }

    FormManager() {
        super(false);
    }

    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File formFile = new File(pFile);
        if (formFile.exists()) {
            logger.info("Loading forms from '" + pFile + "'");
            try {
                Document d = JaxenUtils.getDocument(formFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//forms/form")) {
                    AbstractForm form = AbstractForm.fromXml(e);
                    if (form != null) {
                        addManagedElement(form);
                    }
                }
                logger.debug("Forms loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load forms", e);
            }
        } else {
            logger.info("No forms found under '" + pFile + "'");
        }
        revalidate();
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }

        logger.info("Loading forms");
        boolean result = false;
        invalidate();
        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//forms/form")) {
                AbstractForm form = AbstractForm.fromXml(e);
                if (form != null) {
                    if (form.getFormName() == null) {
                        if (pExtension != null) {
                            form.setFormName(pExtension);
                        } else {
                            form.setFormName("");
                        }
                    } else {
                        if (pExtension != null) {
                            form.setFormName(form.getFormName() + "_" + pExtension);
                        }
                    }
                    addManagedElement(form);
                }
            }
            logger.debug("Forms imported successfully");
            //do a pseudo-scroll to update the forms visibility
            MapPanel.getSingleton().fireScrollEvents(0, 0);
            result = true;
        } catch (Exception e) {
            logger.error("Failed to import forms", e);
            //do a pseudo-scroll to update the forms visibility
            MapPanel.getSingleton().fireScrollEvents(0, 0);
        }
        revalidate(true);
        return result;
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        try {
            logger.debug("Generating forms export data");
            String result = "<forms>\n";

            ManageableType[] elements = getAllElements().toArray(new ManageableType[getAllElements().size()]);

            for (ManageableType t : elements) {
                AbstractForm form = (AbstractForm) t;
                result += form.toXml();
            }
            result += "</forms>\n";
            logger.debug("Export data generated successfully");
            return result;
        } catch (Exception e) {
            logger.error("Failed to generate forms export data", e);
            return "";
        }
    }

    @Override
    public void saveElements(String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<forms>\n");

            for (ManageableType type : getAllElements()) {
                AbstractForm form = (AbstractForm) type;
                w.write(form.toXml());
            }
            w.write("</forms>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store forms", e);
        }
    }

    public synchronized void addForm(AbstractForm pForm) {
        addManagedElement(pForm);
    }

    public void removeForm(AbstractForm pForm) {
        removeElement(pForm);
    }

    public void removeForms(List<AbstractForm> pForms) {
        removeElements(pForms);
    }

    public List<AbstractForm> getVisibleForms() {
        List<AbstractForm> visible = new LinkedList<>();
        for (ManageableType t : getAllElements()) {
            AbstractForm f = (AbstractForm) t;
            if (f.isVisibleOnMap()) {
                visible.add(f);
            }
        }
        return visible;
    }
}
