/*
 * AttackAddFrame.java
 *
 * Created on 17. Juni 2008, 14:13
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.attack.AttackManager;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author  Jejkal
 */
public class AttackAddFrame extends javax.swing.JFrame {

    private Village mSource;
    private Village mTarget;
    private final NumberFormat nf = NumberFormat.getInstance();
    private boolean skipValidation = false;

    /** Creates new form AttackAddFrame */
    public AttackAddFrame() {
        initComponents();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        frameControlPanel1.setupPanel(this, true, false);
        getContentPane().setBackground(Constants.DS_BACK);

        ((DateEditor) jTimeSpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((DateEditor) jTimeSpinner.getEditor()).getFormat().applyPattern("dd.MM.yy HH:mm:ss");
        jTimeSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (skipValidation) {
                    return;
                }
                if (!validateTime()) {
                    ((DateEditor) jTimeSpinner.getEditor()).getTextField().setForeground(Color.RED);
                } else {
                    ((DateEditor) jTimeSpinner.getEditor()).getTextField().setForeground(Color.BLACK);
                }
            }
        });
        buildUnitBox();
    }

    /**Check if the currently selected unit can arrive at the target village at the selected time.
     * Returns result depending on the time mode (arrive or send) 
     */
    private boolean validateTime() {
        long sendMillis = ((Date) jTimeSpinner.getValue()).getTime();
        //check time depending selected unit
        double speed = ((UnitHolder) jUnitBox.getSelectedItem()).getSpeed();
        double minTime = DSCalculator.calculateMoveTimeInMinutes(mSource, mTarget, speed);
        long moveTime = (long) minTime * 60000;
        return (sendMillis > System.currentTimeMillis() + moveTime);
    }

    private void buildUnitBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
        }
        jUnitBox.setModel(model);
    }

    public Date getTime() {
        return (Date) jTimeSpinner.getValue();
    }

    public UnitHolder getSelectedUnit() {
        return (UnitHolder) jUnitBox.getSelectedItem();
    }

    public void setupAttack(Village pSource, Village pTarget, int pInitialUnit, Date pInititalTime) {
        if ((pSource == null) || (pTarget == null)) {
            return;
        }
        if (pSource.equals(pTarget)) {
            return;
        }
        if (pSource.getTribe() == null) {
            //empty villages cannot attack
            return;
        }
        skipValidation = true;
        int initialUnit = (pInitialUnit >= 0) ? pInitialUnit : 0;
        if (initialUnit > jUnitBox.getItemCount() - 1) {
            initialUnit = -1;
        }
        jUnitBox.setSelectedIndex(initialUnit);

        if (pInititalTime != null) {
            jTimeSpinner.setValue(pInititalTime);
        } else {
            double dur = DSCalculator.calculateMoveTimeInMinutes(pSource, pTarget, ((UnitHolder) jUnitBox.getSelectedItem()).getSpeed());
            dur = dur * 60000;
            jTimeSpinner.setValue(new Date(System.currentTimeMillis() + (long) dur + 60000));
            ((DateEditor) jTimeSpinner.getEditor()).getTextField().setForeground(Color.BLACK);
        }
        mSource = pSource;
        mTarget = pTarget;
        jSourceVillage.setText(pSource.getTribe() + " (" + pSource + ")");
        if (pTarget.getTribe() != null) {
            jTargetVillage.setText(pTarget.getTribe() + " (" + pTarget + ")");
        } else {
            jTargetVillage.setText("Barbarendorf" + " (" + pTarget.getX() + "|" + pTarget.getY() + ")");
        }
        double d = DSCalculator.calculateDistance(mSource, mTarget);

        jDistance.setText(nf.format(d));
        setVisible(true);
        skipValidation = false;
    }

    public void setupAttack(Village pSource, Village pTarget, int pInitialUnit) {
        setupAttack(pSource, pTarget, pInitialUnit, null);
    }

    public void setupAttack(Village pSource, Village pTarget) {
        setupAttack(pSource, pTarget, -1);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jDistance = new javax.swing.JLabel();
        jSourceVillage = new javax.swing.JLabel();
        jSourceLabel = new javax.swing.JLabel();
        jTargetLabel = new javax.swing.JLabel();
        jUnitLabel = new javax.swing.JLabel();
        jUnitBox = new javax.swing.JComboBox();
        jTimeSpinner = new javax.swing.JSpinner();
        jDistanceLabel = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JLabel();
        jArriveTimeLabel = new javax.swing.JLabel();
        frameControlPanel1 = new de.tor.tribes.ui.FrameControlPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("AttackAddFrame.title")); // NOI18N
        setUndecorated(true);

        jOKButton.setBackground(new java.awt.Color(239, 235, 223));
        jOKButton.setText(bundle.getString("AttackAddFrame.jOKButton.text")); // NOI18N
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });

        jCancelButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelButton.setText(bundle.getString("AttackAddFrame.jCancelButton.text")); // NOI18N
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelAddAttackEvent(evt);
            }
        });

        jDistance.setBackground(new java.awt.Color(239, 235, 223));
        jDistance.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jDistance.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jDistance.setOpaque(true);

        jSourceVillage.setBackground(new java.awt.Color(239, 235, 223));
        jSourceVillage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jSourceVillage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSourceVillage.setOpaque(true);

        jSourceLabel.setText(bundle.getString("AttackAddFrame.jSourceLabel.text")); // NOI18N
        jSourceLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jTargetLabel.setText(bundle.getString("AttackAddFrame.jTargetLabel.text")); // NOI18N

        jUnitLabel.setText(bundle.getString("AttackAddFrame.jUnitLabel.text")); // NOI18N
        jUnitLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        jUnitLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        jUnitLabel.setPreferredSize(new java.awt.Dimension(120, 14));

        jUnitBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Speerträger (18)", "Adelsgeschlecht (35)", "Berittener Bogenschütze (11 Felder/min)" }));
        jUnitBox.setSelectedIndex(2);
        jUnitBox.setMinimumSize(new java.awt.Dimension(300, 18));
        jUnitBox.setPreferredSize(new java.awt.Dimension(300, 20));
        jUnitBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUnitChangedEvent(evt);
            }
        });

        jTimeSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));

        jDistanceLabel.setText(bundle.getString("AttackAddFrame.jDistanceLabel.text")); // NOI18N

        jTargetVillage.setBackground(new java.awt.Color(239, 235, 223));
        jTargetVillage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTargetVillage.setAutoscrolls(true);
        jTargetVillage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTargetVillage.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        jTargetVillage.setMinimumSize(new java.awt.Dimension(39, 20));
        jTargetVillage.setOpaque(true);
        jTargetVillage.setPreferredSize(new java.awt.Dimension(39, 20));

        jArriveTimeLabel.setText(bundle.getString("AttackAddFrame.jArriveTimeLabel.text")); // NOI18N
        jArriveTimeLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        jArriveTimeLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        jArriveTimeLabel.setPreferredSize(new java.awt.Dimension(120, 14));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTargetLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(jSourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(jDistanceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(jUnitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jArriveTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTimeSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(jSourceVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(jDistance, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(jUnitBox, 0, 306, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton)))
                .addContainerGap())
            .addComponent(frameControlPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(frameControlPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSourceVillage, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTargetVillage, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTargetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDistanceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUnitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jArriveTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOKButton)
                    .addComponent(jCancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireUnitChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUnitChangedEvent
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        UnitHolder u = (UnitHolder) evt.getItem();
        long arriveTime = (long) (System.currentTimeMillis() + DSCalculator.calculateMoveTimeInSeconds(mSource, mTarget, u.getSpeed()) * 1000 + 1000);
        if (((Date) jTimeSpinner.getValue()).getTime() < arriveTime) {
            //only set new arrive time if unit could not arrive at the current time
            jTimeSpinner.setValue(new Date(arriveTime));
        }
    }
}//GEN-LAST:event_fireUnitChangedEvent

private void fireCancelAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelAddAttackEvent
    setVisible(false);
}//GEN-LAST:event_fireCancelAddAttackEvent

private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
    if (!validateTime()) {
        ((DateEditor) jTimeSpinner.getEditor()).getTextField().setForeground(Color.RED);
        return;
    }

    AttackManager.getSingleton().addAttack(mSource, mTarget, getSelectedUnit(), getTime());
    setVisible(false);
}//GEN-LAST:event_fireAddAttackEvent

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            GlobalOptions.initialize();
            GlobalOptions.setSelectedServer("de26");

            DataHolder.getSingleton().loadData(false);
        } catch (Exception e) {
        }
        Village source = DataHolder.getSingleton().getVillages()[452][467];
        Village target = DataHolder.getSingleton().getVillages()[449][466];
        /* for (int i = 0; i < 1000; i++) {
        for (int j = 0; j < 1000; j++) {
        Village v = DataHolder.getSingleton().getVillages()[i][j];
        if (v != null) {
        if (source == null) {
        source = v;
        } else if (target == null) {
        target = v;
        } else {
        break;
        }
        }
        }
        
        }*/

        new AttackAddFrame().setupAttack(source, target);

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.FrameControlPanel frameControlPanel1;
    private javax.swing.JLabel jArriveTimeLabel;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JLabel jDistance;
    private javax.swing.JLabel jDistanceLabel;
    private javax.swing.JButton jOKButton;
    private javax.swing.JLabel jSourceLabel;
    private javax.swing.JLabel jSourceVillage;
    private javax.swing.JLabel jTargetLabel;
    private javax.swing.JLabel jTargetVillage;
    private javax.swing.JSpinner jTimeSpinner;
    private javax.swing.JComboBox jUnitBox;
    private javax.swing.JLabel jUnitLabel;
    // End of variables declaration//GEN-END:variables
}