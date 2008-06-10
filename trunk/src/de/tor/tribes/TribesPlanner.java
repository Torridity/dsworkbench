/*
 * TribesPlanner.java
 *
 * Created on 25. Juli 2007, 16:12
 */

package de.tor.tribes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author  Charon
 */
public class TribesPlanner extends javax.swing.JFrame {
    
    private final String DATE_FORMAT = "dd/MM/yyyy - HH:mm:ss";
    private SimpleDateFormat mFormatter = null;
    private Properties mSpeeds = null;
    private MaskFormatter formatter;
    private Hashtable<String, String> mUnitPropertyMappings = null;
    
    /** Creates new form TribesPlanner */
    public TribesPlanner() throws Exception{
        //init application
        initComponents();
        mFormatter = new SimpleDateFormat(DATE_FORMAT);
        
        //load speeds settings
        mSpeeds = new Properties();
        InputStream iStream = null;
        if(new File("MovementSpeeds.properties").exists()){
            try{
                iStream = new FileInputStream("MovementSpeeds.properties");
            }catch(FileNotFoundException fnfe){
                showError("Einheitengeschwindigkeiten nicht gefunden oder fehlerhaft.");
                System.exit(1);
            }
        }else{
            iStream = TribesPlanner.class.getResourceAsStream("MovementSpeeds.properties");
        }
        
        try{
            mSpeeds.load(iStream);
        }catch(Exception e){
            showError("Einheitengeschwindigkeiten nicht gefunden oder fehlerhaft.");
            System.exit(1);
        }
        //done loading speed settings
        
        //build speed <> unit name mappings
        buildMappings();
        
        //setup attacker table
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
            
        },
                new String [] {
            "Name", "X", "Y", "Einheit", "Zeitversatz"
        }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        
        try{
            formatter = new MaskFormatter("## Stunden, ## Minuten, ## Sekunden");
            formatter.setPlaceholderCharacter('0');
            formatter.install(jFormattedTextField1);
        }catch(Exception e){
            throw e;
        }

        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(3);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(3);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(80);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(30);
        
    }
    
    private void buildMappings(){
        mUnitPropertyMappings = new Hashtable<String, String>();
        DefaultComboBoxModel unitModel = new DefaultComboBoxModel();
        String unit = "Speerträger " + "(" + mSpeeds.getProperty("SPEAR") + ")";
        unitModel.addElement("Speerträger " + "(" + mSpeeds.getProperty("SPEAR") + ")");
        mUnitPropertyMappings.put(unit, "SPEAR");
        unit = "Schwertkämpfer " + "(" + mSpeeds.getProperty("SWORD") + ")";
        unitModel.addElement("Schwertkämpfer " + "(" + mSpeeds.getProperty("SWORD") + ")");
        mUnitPropertyMappings.put(unit, "SWORD");
        unit = "Axtkämpfer " + "(" + mSpeeds.getProperty("AXE") + ")";
        unitModel.addElement("Axtkämpfer " + "(" + mSpeeds.getProperty("AXE") + ")");
        mUnitPropertyMappings.put(unit, "AXE");
        unit = "Bogenschütze " + "(" + mSpeeds.getProperty("BOW") + ")";
        unitModel.addElement("Bogenschütze " + "(" + mSpeeds.getProperty("BOW") + ")");
        mUnitPropertyMappings.put(unit, "BOW");
        unit = "Späher " + "(" + mSpeeds.getProperty("SPY") + ")";
        unitModel.addElement("Späher " + "(" + mSpeeds.getProperty("SPY") + ")");
        mUnitPropertyMappings.put(unit, "SPY");
        unit = "Leichte Kavallerie " + "(" + mSpeeds.getProperty("LIGHT") + ")";
        unitModel.addElement("Leichte Kavallerie " + "(" + mSpeeds.getProperty("LIGHT") + ")");
        mUnitPropertyMappings.put(unit, "LIGHT");
        unit = "Berittener Bogenschütze " + "(" + mSpeeds.getProperty("BOW2") + ")";
        unitModel.addElement("Berittener Bogenschütze " + "(" + mSpeeds.getProperty("BOW2") + ")");
        mUnitPropertyMappings.put(unit, "BOW2");
        unit = "Schwere Kavallerie " + "(" + mSpeeds.getProperty("HEAVY") + ")";
        unitModel.addElement("Schwere Kavallerie " + "(" + mSpeeds.getProperty("HEAVY") + ")");
        mUnitPropertyMappings.put(unit, "HEAVY");
        unit = "Rammbock " + "(" + mSpeeds.getProperty("RAM") + ")";
        unitModel.addElement("Rammbock " + "(" + mSpeeds.getProperty("RAM") + ")");
        mUnitPropertyMappings.put(unit, "RAM");
        unit = "Katapult " + "(" + mSpeeds.getProperty("CATA") + ")";
        unitModel.addElement("Katapult " + "(" + mSpeeds.getProperty("CATA") + ")");
        mUnitPropertyMappings.put(unit, "CATA");
        unit = "Paladin " + "(" + mSpeeds.getProperty("PALA") + ")";
        unitModel.addElement("Paladin " + "(" + mSpeeds.getProperty("PALA") + ")");
        mUnitPropertyMappings.put(unit, "PALA");
        unit = "Adelsgeschlecht " + "(" + mSpeeds.getProperty("NOBLE") + ")";
        unitModel.addElement("Adelsgeschlecht " + "(" + mSpeeds.getProperty("NOBLE") + ")");
        mUnitPropertyMappings.put(unit, "NOBLE");
        jUnitSelection.setModel(unitModel);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTargetX = new javax.swing.JTextField();
        jTargetY = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jSourceName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSourceX = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jSourceY = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jUnitSelection = new javax.swing.JComboBox();
        jAddAttacker = new javax.swing.JButton();
        jRemoveAttacker = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jRemoveAttacker1 = new javax.swing.JButton();
        jRemoveAttacker2 = new javax.swing.JButton();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jAttackName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jArrivalDate = new javax.swing.JSpinner();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(25, 25));

        jButton2.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton2.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton2.setPreferredSize(new java.awt.Dimension(25, 25));

        jButton3.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(25, 25));

        jButton4.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton4.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton4.setPreferredSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(368, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Angriffsziel"));

        jLabel1.setText("Koordinaten");

        jTargetX.setText("619");
        jTargetX.setToolTipText("X-Koordinate");
        jTargetX.setMaximumSize(new java.awt.Dimension(40, 20));
        jTargetX.setMinimumSize(new java.awt.Dimension(40, 20));
        jTargetX.setPreferredSize(new java.awt.Dimension(40, 20));

        jTargetY.setText("291");
        jTargetY.setToolTipText("Y-Koordinate");
        jTargetY.setMaximumSize(new java.awt.Dimension(40, 20));
        jTargetY.setMinimumSize(new java.awt.Dimension(40, 20));
        jTargetY.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel2.setText("|");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jTargetX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(jLabel2)
                .add(1, 1, 1)
                .add(jTargetY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(272, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTargetX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(jTargetY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Angreifer"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "X-Koordinate", "Y-Koordinate", "Langsamste Einheit"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel3.setText("Name");

        jLabel4.setText("Koordinaten");

        jSourceX.setText("620");
        jSourceX.setToolTipText("X-Koordinate");
        jSourceX.setMaximumSize(new java.awt.Dimension(40, 20));
        jSourceX.setMinimumSize(new java.awt.Dimension(40, 20));
        jSourceX.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel5.setText("|");

        jSourceY.setText("294");
        jSourceY.setToolTipText("Y-Koordinate");
        jSourceY.setMaximumSize(new java.awt.Dimension(40, 20));
        jSourceY.setMinimumSize(new java.awt.Dimension(40, 20));
        jSourceY.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel6.setText("Langsamste Einheit");

        jUnitSelection.setMaximumSize(new java.awt.Dimension(150, 20));
        jUnitSelection.setMinimumSize(new java.awt.Dimension(150, 20));
        jUnitSelection.setPreferredSize(new java.awt.Dimension(150, 20));

        jAddAttacker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jAddAttacker.setToolTipText("Angreifer hinzuf\u00fcgen");
        jAddAttacker.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jAddAttacker.setMaximumSize(new java.awt.Dimension(25, 25));
        jAddAttacker.setMinimumSize(new java.awt.Dimension(25, 25));
        jAddAttacker.setPreferredSize(new java.awt.Dimension(25, 25));
        jAddAttacker.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackerEvent(evt);
            }
        });

        jRemoveAttacker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jRemoveAttacker.setToolTipText("Angreifer entfernen");
        jRemoveAttacker.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jRemoveAttacker.setMaximumSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker.setMinimumSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker.setPreferredSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackerEvent(evt);
            }
        });

        jLabel7.setText("Zeitversatz");

        jRemoveAttacker1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/up.gif"))); // NOI18N
        jRemoveAttacker1.setToolTipText("Angriff nach oben");
        jRemoveAttacker1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jRemoveAttacker1.setMaximumSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker1.setMinimumSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker1.setPreferredSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveAttackUpEvent(evt);
            }
        });

        jRemoveAttacker2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/down.gif"))); // NOI18N
        jRemoveAttacker2.setToolTipText("Angriff nach unten");
        jRemoveAttacker2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jRemoveAttacker2.setMaximumSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker2.setMinimumSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker2.setPreferredSize(new java.awt.Dimension(25, 25));
        jRemoveAttacker2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveAttackDownEvent(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel6)
                            .add(jLabel3)
                            .add(jLabel4)
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jSourceX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(1, 1, 1)
                                .add(jLabel5)
                                .add(1, 1, 1)
                                .add(jSourceY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 166, Short.MAX_VALUE))
                            .add(jUnitSelection, 0, 252, Short.MAX_VALUE)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jFormattedTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED))
                            .add(jSourceName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                        .add(10, 10, 10)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jRemoveAttacker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jRemoveAttacker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jAddAttacker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jRemoveAttacker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jSourceName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jSourceX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel5)
                                    .add(jSourceY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel4))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(jUnitSelection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(jFormattedTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jRemoveAttacker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jAddAttacker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jRemoveAttacker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jRemoveAttacker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Angriffseinstellungen"));

        jLabel10.setText("Name");

        jAttackName.setToolTipText("Name des Angriffs");

        jLabel11.setText("Ankunft");

        jArrivalDate.setModel(new javax.swing.SpinnerDateModel());
        jArrivalDate.setToolTipText("Ankunftszeit");
        jArrivalDate.setMaximumSize(new java.awt.Dimension(40, 20));
        jArrivalDate.setMinimumSize(new java.awt.Dimension(40, 20));
        jArrivalDate.setPreferredSize(new java.awt.Dimension(40, 20));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel11)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jAttackName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .add(jArrivalDate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jAttackName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jArrivalDate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton6.setText("Berechnen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateEvent(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(373, Short.MAX_VALUE)
                .add(jButton6)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton6)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
private void fireCalculateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateEvent
    //general attack settings
    String name = jAttackName.getText();
    
    Date arrivalDate = (Date)jArrivalDate.getValue();
    
    if(System.currentTimeMillis() > arrivalDate.getTime()){
        showError("Ankunftszeit in Vergangenheit.");
        return;
    }
    
    //done general attack settings
    
    //target settings
    int xTarget = 0;
    int yTarget = 0;
    
    try{
        xTarget = Integer.parseInt(jTargetX.getText());
        yTarget = Integer.parseInt(jTargetY.getText());
    } catch(NumberFormatException nfe){
        showError("Angriffsziel Koordinaten ungültig.");
        return;
    }
    
    //done target settings
    
    //attackers
    DefaultTableModel model = ((DefaultTableModel)jTable1.getModel());
    int attackers = model.getRowCount();
    if(attackers <= 0){
        showError("Keine Angreifer festgelegt.");
        return;
    }
    
    //calculate attacks
    for(int i=0;i<attackers;i++){
        int x = 0;
        int y = 0;
        int speed = 0;
        String sDelay = "";
        String attacker = "<unbekannt>";
        try{
            attacker = (String)model.getValueAt(i, 0);
            x = (Integer)model.getValueAt(i, 1);
            y = (Integer)model.getValueAt(i, 2);
            String unit = (String)model.getValueAt(i, 3);
            sDelay = (String)model.getValueAt(i, 4);
            speed = Integer.parseInt((String)mSpeeds.get(mUnitPropertyMappings.get(unit)));
        }catch(Exception e){
            e.printStackTrace();
            showError("Angreifer " + attacker + " ungültig.");
            return;
        }
        
        int delayHour = Integer.parseInt(sDelay.split(":")[0]);
        int delayMin = Integer.parseInt(sDelay.split(":")[1]);
        int delaySec = Integer.parseInt(sDelay.split(":")[2]);
        long delay = delaySec*1000;
        delay += delayMin * 60000;
        delay += delayHour * 3600000;
        long attackerArrive = arrivalDate.getTime() + delay;
        double dist = Math.sqrt(Math.pow(Math.abs(xTarget - x), 2) + Math.pow(Math.abs(yTarget - y), 2));
        
        double duration = dist * speed; //minutes
        int durInt = (int)duration;
        int hours = 0;
        
        int minutes = durInt;
        if(minutes > 60){
            hours = minutes % 60;
            minutes -= hours * 60;
        }
        
        duration -= minutes;
        int seconds = (int)(duration * 60);
        
        durInt *=  60000;
        System.out.println(hours + ":" + minutes + ":" + seconds);
        attackerArrive -= (int)durInt;
        System.out.println(mFormatter.format(new Date(attackerArrive)));
    }
    
    //done attackers
    
}//GEN-LAST:event_fireCalculateEvent

    private void fireMoveAttackDownEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveAttackDownEvent
        // TODO add your handling code here:
}//GEN-LAST:event_fireMoveAttackDownEvent
    
private void fireMoveAttackUpEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveAttackUpEvent
    // TODO add your handling code here:
}//GEN-LAST:event_fireMoveAttackUpEvent

private void fireRemoveAttackerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackerEvent
    int row = jTable1.getSelectedRow();
    if(row == -1){
        showError("Keine Angriff ausgewählt.");
        return;
    }
    ((DefaultTableModel)jTable1.getModel()).removeRow(row);
}//GEN-LAST:event_fireRemoveAttackerEvent

private void fireAddAttackerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackerEvent
    String name = jSourceName.getText();
    int xPos = 0;
    int yPos = 0;
    try{
        xPos = Integer.parseInt(jSourceX.getText());
        yPos = Integer.parseInt(jSourceY.getText());
    }catch(NumberFormatException e){
        showError("Angreifer Koordinaten ungültig.");
        return;
    }
    
    String unit = (String)jUnitSelection.getSelectedItem();
    if(unit == null){
        unit = (String)jUnitSelection.getItemAt(0);
    }
    
    String delay = jFormattedTextField1.getText();
    //"## Stunden, ## Minuten, ## Sekunden"
    delay = delay.replaceAll(" Stunden, ", ":");
    delay = delay.replaceAll(" Minuten, ", ":");
    delay = delay.replaceAll(" Sekunden", "");
    
    ((DefaultTableModel)jTable1.getModel()).addRow(new Object[]{name, xPos, yPos, unit, delay});
}//GEN-LAST:event_fireAddAttackerEvent

private void showError(String pMessage){
    JOptionPane.showMessageDialog(this, pMessage, "Fehler", JOptionPane.ERROR_MESSAGE);
}

private void showInfo(String pMessage){
    JOptionPane.showMessageDialog(this, pMessage, "Information", JOptionPane.INFORMATION_MESSAGE);
}

/**
 * @param args the command line arguments
 */
public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            try{
                new TribesPlanner().setVisible(true);
            }catch(Exception e){
                StringWriter swe = new StringWriter();
                swe.write("Schwerer Anwendungsfehler.\n");
                e.printStackTrace(new PrintWriter(swe));
                JOptionPane.showMessageDialog(null, swe.toString(), "Fehler", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    });
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddAttacker;
    private javax.swing.JSpinner jArrivalDate;
    private javax.swing.JTextField jAttackName;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton jRemoveAttacker;
    private javax.swing.JButton jRemoveAttacker1;
    private javax.swing.JButton jRemoveAttacker2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jSourceName;
    private javax.swing.JTextField jSourceX;
    private javax.swing.JTextField jSourceY;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTargetX;
    private javax.swing.JTextField jTargetY;
    private javax.swing.JComboBox jUnitSelection;
    // End of variables declaration//GEN-END:variables
    
}
