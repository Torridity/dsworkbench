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
package de.tor.tribes.ui;

import de.tor.tribes.ui.components.LayerOrderPanel;
import de.tor.tribes.util.interfaces.LayerOrderTooltipListener;
import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author Torridity
 */
public class LayerOrderConfigurationFrame extends javax.swing.JFrame implements LayerOrderTooltipListener {

    private static LayerOrderConfigurationFrame SINGLETON = null;
    private HashMap<String, String> tooltips = new HashMap<>();
    private String tooltipLayer = null;
    private String generalInformation = null;
    private LayerOrderPanel panel = null;

    @Override
    public void fireShowTooltipEvent(String pLayer) {
        if (pLayer == null) {
            tooltipLayer = null;
            jTextPane1.setText(generalInformation);
        }
        if (tooltipLayer != null) {
            if (!tooltipLayer.equals(pLayer)) {
                jTextPane1.setText(tooltips.get(pLayer));
                tooltipLayer = pLayer;
            }
        } else {
            if (pLayer != null) {
                jTextPane1.setText(tooltips.get(pLayer));
                tooltipLayer = pLayer;
            } else {
                jTextPane1.setText(generalInformation);
                tooltipLayer = null;
            }
        }
    }

    public static synchronized LayerOrderConfigurationFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new LayerOrderConfigurationFrame();
        }
        return SINGLETON;
    }

    /** Creates new form LayerOrderConfigurationFrame */
    LayerOrderConfigurationFrame() {
        initComponents();
        panel = new LayerOrderPanel(this);
        jPanel1.add(panel, BorderLayout.CENTER);

        buildTooltipMap();
        jTextPane1.setText(generalInformation);
    }

    private void buildTooltipMap() {

        generalInformation = "<html><font font size='-1'>Im oberen Bereich sind alle Ebenen aufgef&uuml;hrt,"
                + " die DS Workbench im Hauptfenster zeichnen kann. Dabei werden <b>dunkelgraue</b> Ebenen gezeichnet, w&auml;hrend <b>hellgraue</b>"
                + " Ebenen ausgeblendet sind und so keinerlei Aufwand (Speicher, CPU) verursachen.<br/>"
                + " Fahre mit der Maus &uuml;ber die Symbole der einzelnen Ebenen, um Hinweise zu den Ebenen zu erhalten. Mit gedr&uuml;ckter"
                + " Maustaste kannst du die Position der einzelnen Ebenen verschieben, wobei eine Position weiter links die Ebene in den Hintergrund"
                + " verschiebt, w&auml;hrend eine Position weiter rechts die Ebene weiter im Vordergrund platziert.<br/>Eine Besonderheit bilden die Ebenen"
                + " 'D&ouml;rfer' und 'Markierungen'. Diese Ebenen sind zum einen miteinander verbunden, zum anderen bestimmen sie, welche Ebenen"
                + " sichtbar und welche unsichtbar sind, da Ebenen links (unterhalb) dieser Ebenen nicht gezeichnet werden.</font></html>";

        URL warnURL = null;
        try {
            warnURL = new File("./graphics/icons/warning.png").toURI().toURL();
        } catch (MalformedURLException ignored) {
        }
        tooltips.put("Markierungen", "<html><font font size='-1'><b>Markierungen</b><br/>Diese Ebene enthält Farbmarkierungen von Spielern und St&auml;mmen."
                + " Sie ist immer sichtbar, kann jedoch entweder unterhalb oder oberhalb der Dorfebene gezeichnet werden, wodurch Markierungen"
                + " deutlicher dargestellt werden k&ouml;nnen.<br/><br/>"
                + "<img src='" + warnURL + "'/>&nbsp;Diese Ebene ist mit der Ebene 'D&ouml;rfer verbunden. Ebenen links (unterhalb) einer dieser beiden Ebenen"
                + " werden nicht gezeichnet.</font></html>");
        tooltips.put("Dörfer", "<html><font font size='-1'><b>D&ouml;rfer</b><br/>Die Dorfebene enth&auml;lt alle Dorfgrafiken. Sie ist immer sichtbar"
                + " und bestimmt, welche anderen Ebenen sichtbar sind. Alle Ebenen die sich links (unterhalb) der Dorfebene befinden,"
                + " werden <u>nicht</u> gezeichnet.<br/><br/>"
                + "<img src='" + warnURL + "'/>&nbsp;Diese Ebene ist mit der Ebene 'Markierungen' verbunden. Ebenen links (unterhalb) einer dieser beiden Ebenen"
                + " werden nicht gezeichnet.</font></html>");

        tooltips.put("Zeichnungen", "<html><font font size='-1'><b>Zeichnungen</b><br/>Diese Ebene enth&auml;lt alle Zeichnungen (Kreise, Rechtecke,"
                + " usw.) die auf der Hauptkarte eingezeichnet sind.<br/><br/>"
                + "<img src='" + warnURL + "'/>&nbsp;Wenn du viele transparente Zeichnungen oder Freihandzeichnungen verwendest, k&ouml;nnte"
                + " das Zeichnen dieser Ebene sehr aufw&auml;ndig sein. Wenn du Performanceprobleme bemerkst, versuch diese Ebene auszublenden.</html>");

        tooltips.put("Dorfsymbole", "<html><font font size='-1'><b>Dorfsymbole</b><br/>Diese Ebene enth&auml;lt Symbole mit deinen einzelne D&ouml;rfer"
                + " markiert sind. Dies sind z.B. Gruppenmarkierungen oder Markierungen von kürzlich geadelten D&ouml;rfern.</html>");
        tooltips.put("Truppendichte", "<html><font font size='-1'><b>Truppendichte</b><br/>Diese Ebene zeigt im unteren Bereich der D&ouml;rfer auf der Karte"
                + " Balken an, welche die Truppenmenge im entsprechenden Dorf repr&auml;sentieren. Diese Balken werden nat&uuml;rlich nur f&uuml;r D&ouml;rfer"
                + " angezeigt, zu denen DS Workbench auch Truppeninformationen besitzt. Diese k&ouml;nnen &uuml;ber den Import von InGame-Informationen oder"
                + " &uuml;ber Berichte eingelesen werden.<br/><br/>"
                + "<img src='" + warnURL + "'/>&nbsp;Sind Truppeninformationen zu vielen D&ouml;rfern vorhanden, so kann das Zeichnen dieser Ebene,"
                + " sehr aufw&auml;ndig sein. Wenn du Performanceprobleme bemerkst, versuch diese Ebene auszublenden.</html>");
        tooltips.put("Notizmarkierungen", "<html><font font size='-1'><b>Notizmarkierungen</b><br/>Diese Ebene stellt Markierungen von Notizen dar."
                + " Sind einem Dorf Notizen zugeordnet und sind f&uuml;r diese Notizen Kartenmarkierungen eingestellt, so werden die auf der Hauptkarte"
                + " angezeigt wenn diese Ebene eingeblendet ist.</html>");
        tooltips.put("Angriffe", "<html><font font size='-1'><b>Angriffe</b><br/>Diese Ebene stellt Angriffe, die sich in Befehlspl&auml;nen befinden,"
                + " auf der Hauptkarte dar, sofern die Angriffe eingezeichnet werden sollen. In den DS Workbench Einstellungen unter 'Befehle' kann man"
                + " weitere Eigenschaften dieser sog. Befehlsvektoren festlegen (z.B. Aktuelle Truppenposition anzeigen oder die Laufrichtung einzeichnen)<br/><br/>"
                + "<img src='" + warnURL + "'/>&nbsp;In der Regel wird empfohlen, diese Ebene auszublenden. Dies dient zum einen der &Uuml;bersichtlichkeit,"
                + " zum anderen ist das Zeichnen dieser Ebene, besonders wenn Truppenposition und Laufrichtung eingezeichnet werden, sehr aufw&auml;ndig.</html>");
        tooltips.put("Unterstützungen", "<html><font font size='-1'><b>Unterst&uuml;tzungen</b><br/>Diese Ebene stellt dar, in welchen D&ouml;rfern Unterst&uuml;tzungen "
                + " stehen und wo diese herkommen. Unterst&uuml;tzungen werden nur eingezeichnet, wenn man in der Truppen&uuml;bersicht in der Tabelle"
                + " 'Unterst&uuml;tzungen' Eintr&auml;ge ausgew&auml;hlt hat.</html>");
        tooltips.put("Kirchenradien", "<html><font font size='-1'><b>Kirchenradien</b><br/>Diese Ebene zeichnet Kirchenradien ein, die in der Kirchen&uuml;bersicht"
                + " aufgef&uuml;hrt sind. Kirchenradien einzelner Spieler werden dabei zu einer Region zusammengefasst. Diese Ebene ist nur auf Welten von Bedeutung,"
                + " auf die Kirche aktiviert ist.<br/><br/>"
                + "<img src='" + warnURL + "'/>&nbsp;Hat man sehr viele Kirchen eingetragen, so wird empfohlen, diese Ebene nur bei Bedarf einzublenden,"
                + " da das Zusammenfassen vieler Kirchenradien sehr aufw&auml;ndig ist und so die Darstellungszeit der Karte stark beeinflusst wird.</html>");
        tooltips.put("Wachturmradien", "<html><font font size='-1'><b>Wachturmradien</b><br/>Diese Ebene zeichnet Wachturmradien ein, die in der Wachturm&uuml;bersicht"
                + " aufgef&uuml;hrt sind. Diese Ebene ist nur auf Welten von Bedeutung, auf der Wachtürme aktiviert sind.<br/><br/>");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButton1 = new javax.swing.JButton();

        setTitle("Zeichenebenen festlegen");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setMaximumSize(new java.awt.Dimension(400, 250));
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 250));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 250));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Informationen"));

        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);

        jButton1.setText("Schließen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCloseFrameEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(331, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireCloseFrameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseFrameEvent
        setVisible(false);
    }//GEN-LAST:event_fireCloseFrameEvent

    public String getLayerOrder() {
        String res = "";
        for (LayerOrderPanel.Layer l : panel.getLayers()) {
            res += l.getName() + ";";
        }
        return res;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
