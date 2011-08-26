/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.util.map.FormManager;
import java.awt.Graphics2D;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class FormLayerRenderer extends AbstractDirectLayerRenderer {

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (!pSettings.isLayerVisible()) {
            return;
        }
        List<ManageableType> elems = FormManager.getSingleton().getAllElements();
        for (ManageableType t : elems) {
            AbstractForm form = (AbstractForm) t;
            form.renderForm(pG2d);
        }
    }
}
