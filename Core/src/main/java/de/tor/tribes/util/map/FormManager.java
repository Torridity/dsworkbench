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
import de.tor.tribes.util.xml.JDomUtils;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class FormManager extends GenericManager<AbstractForm> {

    private static Logger logger = LogManager.getLogger("FormManager");
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
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        logger.info("Loading forms");
        invalidate();
        try {
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "forms/form")) {
                AbstractForm form = AbstractForm.fromXml(e);
                if (form != null) {
                    if (pExtension != null) {
                        if (form.getFormName() == null) {
                                form.setFormName(pExtension);
                        } else {
                                form.setFormName(form.getFormName() + "_" + pExtension);
                        }
                    }
                    addManagedElement(form);
                    result++;
                }
            }
            logger.debug("Forms imported successfully");
            //do a pseudo-scroll to update the forms visibility
            MapPanel.getSingleton().fireScrollEvents(0, 0);
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to import forms", e);
            //do a pseudo-scroll to update the forms visibility
            MapPanel.getSingleton().fireScrollEvents(0, 0);
        }
        revalidate(true);
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element forms = new Element("plans");
        try {
            logger.debug("Generating forms data");
            for (ManageableType t : getAllElements()) {
                forms.addContent(t.toXml("form"));
            }
            logger.debug("Export data generated successfully");
        } catch (Exception e) {
            logger.error("Failed to generate forms export data", e);
        }
        return forms;
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
