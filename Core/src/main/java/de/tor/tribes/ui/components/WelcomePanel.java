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
package de.tor.tribes.ui.components;

import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.GlobalOptions;
import java.awt.BorderLayout;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *
 * @author Torridity
 */
public class WelcomePanel extends JXPanel {

    private Hashtable<JXLabel, String> welcomeTooltipMap = new Hashtable<JXLabel, String>();
    private BufferedImage back = null;

    /** Creates new form WelcomePanel */
    public WelcomePanel() {
        initComponents();
        setOpaque(true);
        welcomeTooltipMap.put(jxHelpLabel, "<html> <h2 style='color:#953333; font-weight: bold;'>Integrierte Hilfe</h2> DS Workbench bietet eine umfangreiche Hilfe, die du im Programm jederzeit &uuml;ber <strong>F1</strong> aufrufen kannst. Dabei wird versucht, das passende Hilfethema f&uuml;r die Ansicht, in der du dich gerade befindest, auszuw&auml;hlen. Es schadet aber auch nicht, einfach mal so in der Hilfe zu st&ouml;bern um neue Funktionen zu entdecken. Einsteiger sollten in jedem Fall die ersten drei Kapitel der Wichtigen Grundlagen gelesen haben.</html>");
        welcomeTooltipMap.put(jxCommunityLabel, "<html> <h2 style='color:#953333; font-weight: bold;'>Die DS Workbench Community</h2> Nat&uuml;rlich gibt es neben dir noch eine Vielzahl anderer Spieler, die DS Workbench regelm&auml;&szlig;ig und intensiv benutzen. Einen perfekten Anlaufpunkt f&uuml;r alle Benutzer bietet das DS Workbench Forum, wo man immer jemanden trifft mit dem man Erfahrungen austauschen und wo man Fragen stellen kann.</html>");
        welcomeTooltipMap.put(jxIdeaLabel, "<html> <h2 style='color:#953333; font-weight: bold;'>Verbesserungen und Ideen </h2> Gibt es irgendwas wo du meinst, dass es in DS Workbench fehlt und was anderen Benutzern auch helfen k&ouml;nnte? Hast du eine Idee, wie man DS Workbench verbessern oder die Handhabung vereinfachen k&ouml;nnte? Dann bietet dieser Bereich im DS Workbench Forum die perfekte Anlaufstelle f&uuml;r dich. Trau dich und hilf mit, DS Workbench  zu verbessern. </html>");
        welcomeTooltipMap.put(jxFacebookLabel, "<html> <h2 style='color:#953333; font-weight: bold;'>DS Workbench @ Facebook</h2> Nat&uuml;rlich geh&ouml;rt es heutzutage fast zum guten Ton, bei Facebook in irgendeiner Art und Weise vertreten zu sein. Auch DS Workbench hat eine eigene Facebook Seite, mit deren Hilfe ihr euch jederzeit &uuml;ber aktuelle News oder Geschehnisse im Zusammenhang mit DS Workbench informieren k&ouml;nnt.</html>");
        welcomeTooltipMap.put(jContentLabel, "<html> <h2 style='color:#953333'>Willkommen bei DS Workbench</h2> Wenn du diese Seite siehst, dann hast du DS Workbench erfolgreich installiert und die ersten Schritte ebenso erfolgreich gemeistert. Eigentlich steht nun einer unbeschwerten Angriffsplanung und -durchf&uuml;hrung nichts mehr im Wege. Erlaube mir trotzdem kurz auf einige Dinge hinzuweisen, die dir m&ouml;glicherweise beim <b>Umgang mit DS Workbench helfen</b> oder aber dir die M&ouml;glichkeit geben, einen wichtigen Teil zur <b>Weiterentwicklung und stetigen Verbesserung</b> dieses Programms beizutragen. Fahre einfach mit der Maus &uuml;ber eins der vier Symbole in den Ecken, um hilfreiche und interessante Informationen rund um DS Workbench zu erfahren. Klicke auf ein Symbol, um direkt zum entsprechenden Ziel zu gelangen. Die Eintr&auml;ge findest du sp&auml;ter auch im Hauptmen&uuml; unter 'Sonstiges'. <br> <h3 style='color:#953333'> Nun aber viel Spa&szlig; mit DS Workbench.</h3> </html>");
        try {
            back = ImageIO.read(WelcomePanel.class.getResource("/images/c.gif"));
        } catch (Exception e) {
        }
        if (back != null) {
            setBackgroundPainter(new MattePainter(new TexturePaint(back, new Rectangle2D.Float(0, 0, 200, 20))));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jWelcomePane = new javax.swing.JPanel();
        jContentLabel = new org.jdesktop.swingx.JXLabel();
        jxHelpLabel = new org.jdesktop.swingx.JXLabel();
        jxCommunityLabel = new org.jdesktop.swingx.JXLabel();
        jxIdeaLabel = new org.jdesktop.swingx.JXLabel();
        jxFacebookLabel = new org.jdesktop.swingx.JXLabel();
        jDisableWelcome = new javax.swing.JCheckBox();
        jxCloseLabel = new org.jdesktop.swingx.JXLabel();

        setLayout(new java.awt.BorderLayout());

        jWelcomePane.setOpaque(false);
        jWelcomePane.setLayout(new java.awt.GridBagLayout());

        jContentLabel.setText("<html>\n<h2 style=\"color:#953333\">Willkommen bei DS Workbench </h2>\n<p>Wenn du diese Seite siehst, dann hast du DS Workbench erfolgreich installiert und die ersten Schritte ebenso erfolgreich gemeistert. Eigentlich steht nun einer unbeschwerten Angriffsplanung und -durchf&uuml;hrung nichts mehr im Wege. \nErlaube mir trotzdem kurz auf einige Dinge hinzuweisen, die dir m&ouml;glicherweise beim <b>Umgang mit DS Workbench helfen</b> oder aber dir die M&ouml;glichkeit geben, einen wichtigen Teil zur <b>Weiterentwicklung und stetigen Verbesserung</b> dieses Programms beizutragen. \n Fahre einfach mit der Maus &uuml;ber eins der vier Symbole in den Ecken, um hilfreiche und interessante Informationen rund um DS Workbench zu erfahren. Klicke auf ein Symbol, um direkt zum entsprechenden Ziel zu gelangen. <br>\n<h3 style=\"color:#953333\"> Nun aber viel Spa&szlig; mit DS Workbench.</h3>\n</html>");
        jContentLabel.setMinimumSize(new java.awt.Dimension(300, 300));
        jContentLabel.setPreferredSize(new java.awt.Dimension(300, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jWelcomePane.add(jContentLabel, gridBagConstraints);

        jxHelpLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/128x128/help.png"))); // NOI18N
        jxHelpLabel.setEnabled(false);
        jxHelpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireMouseEnterLinkEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitLinkEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformWelcomeActionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jWelcomePane.add(jxHelpLabel, gridBagConstraints);

        jxCommunityLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/128x128/forum.png"))); // NOI18N
        jxCommunityLabel.setEnabled(false);
        jxCommunityLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireMouseEnterLinkEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitLinkEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformWelcomeActionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        jWelcomePane.add(jxCommunityLabel, gridBagConstraints);

        jxIdeaLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/128x128/idea.png"))); // NOI18N
        jxIdeaLabel.setEnabled(false);
        jxIdeaLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireMouseEnterLinkEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitLinkEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformWelcomeActionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        jWelcomePane.add(jxIdeaLabel, gridBagConstraints);

        jxFacebookLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/128x128/facebook.png"))); // NOI18N
        jxFacebookLabel.setEnabled(false);
        jxFacebookLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireMouseEnterLinkEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitLinkEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformWelcomeActionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        jWelcomePane.add(jxFacebookLabel, gridBagConstraints);

        jDisableWelcome.setText("Willkommensseite beim nächsten Start nicht mehr anzeigen");
        jDisableWelcome.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jWelcomePane.add(jDisableWelcome, gridBagConstraints);

        jxCloseLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        jxCloseLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jxCloseLabel.setText("Danke, ich möchte DS Workbench nun verwenden.");
        jxCloseLabel.setEnabled(false);
        jxCloseLabel.setFont(new java.awt.Font("Tahoma", 0, 14));
        jxCloseLabel.setMaximumSize(new java.awt.Dimension(319, 40));
        jxCloseLabel.setMinimumSize(new java.awt.Dimension(319, 40));
        jxCloseLabel.setPreferredSize(new java.awt.Dimension(319, 40));
        jxCloseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireMouseEnterLinkEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitLinkEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformWelcomeActionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jWelcomePane.add(jxCloseLabel, gridBagConstraints);

        add(jWelcomePane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void fireMouseEnterLinkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMouseEnterLinkEvent
        JXLabel source = ((JXLabel) evt.getSource());
        source.setEnabled(true);
        String text = welcomeTooltipMap.get(source);
        if (text == null) {
            text = welcomeTooltipMap.get(jContentLabel);
        }

        jContentLabel.setText(text);
        repaint();
    }//GEN-LAST:event_fireMouseEnterLinkEvent

    private void fireMouseExitLinkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMouseExitLinkEvent
        ((JXLabel) evt.getSource()).setEnabled(false);
        jContentLabel.setText(welcomeTooltipMap.get(jContentLabel));
        repaint();
    }//GEN-LAST:event_fireMouseExitLinkEvent

    private void firePerformWelcomeActionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_firePerformWelcomeActionEvent
        if (evt.getSource() == jxHelpLabel) {
            GlobalOptions.getHelpBroker().setDisplayed(true);
        } else if (evt.getSource() == jxCommunityLabel) {
            BrowserCommandSender.openPage("https://forum.die-staemme.de/showthread.php?80831-DS-Workbench");
        } else if (evt.getSource() == jxIdeaLabel) {
            BrowserCommandSender.openPage("https://forum.die-staemme.de/showthread.php?80831-DS-Workbench");
        } else if (evt.getSource() == jxFacebookLabel) {
            BrowserCommandSender.openPage("http://www.facebook.com/pages/DS-Workbench/182068775185568");
        } else if (evt.getSource() == jxCloseLabel) {
            //hide welcome page

            GlobalOptions.addProperty("no.welcome", Boolean.toString(jDisableWelcome.isSelected()));
            DSWorkbenchMainFrame.getSingleton().hideWelcomePage();
        }
    }//GEN-LAST:event_firePerformWelcomeActionEvent

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        try {
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //JFrame.setDefaultLookAndFeelDecorated(true);

            // SubstanceLookAndFeel.setSkin(SubstanceLookAndFeel.getAllSkins().get("Twilight").getClassName());
            //  UIManager.put(SubstanceLookAndFeel.FOCUS_KIND, FocusKind.NONE);
        } catch (Exception e) {
        }
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new WelcomePanel(), BorderLayout.CENTER);
        f.setSize(500, 500);
        f.setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXLabel jContentLabel;
    private javax.swing.JCheckBox jDisableWelcome;
    private javax.swing.JPanel jWelcomePane;
    private org.jdesktop.swingx.JXLabel jxCloseLabel;
    private org.jdesktop.swingx.JXLabel jxCommunityLabel;
    private org.jdesktop.swingx.JXLabel jxFacebookLabel;
    private org.jdesktop.swingx.JXLabel jxHelpLabel;
    private org.jdesktop.swingx.JXLabel jxIdeaLabel;
    // End of variables declaration//GEN-END:variables
}
