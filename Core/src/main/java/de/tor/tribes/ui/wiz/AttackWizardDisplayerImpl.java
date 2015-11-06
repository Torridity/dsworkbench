/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz;

import de.tor.tribes.ui.panels.TAPAttackInfoPanel;
import java.awt.Color;
import javax.swing.JPanel;
import org.netbeans.api.wizard.displayer.WizardDisplayerImpl;

/**
 *
 * @author Torridity
 */
public class AttackWizardDisplayerImpl extends WizardDisplayerImpl {

    public AttackWizardDisplayerImpl() {
        super(TAPAttackInfoPanel.getSingleton());
    }
}
