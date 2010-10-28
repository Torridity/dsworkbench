/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.map.FormManager;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

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
        AbstractForm[] forms = FormManager.getSingleton().getForms().toArray(new AbstractForm[]{});
        for (AbstractForm form : forms) {
            form.renderForm(pG2d);
        }
    }
}
