/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.ui.views.DSWorkbenchReTimerFrame;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.ui.views.DSWorkbenchFormFrame;
import de.tor.tribes.ui.views.DSWorkbenchSOSRequestAnalyzer;
import de.tor.tribes.ui.views.DSWorkbenchReportFrame;
import de.tor.tribes.ui.views.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.views.DSWorkbenchSelectionFrame;
import de.tor.tribes.ui.views.DSWorkbenchConquersFrame;
import de.tor.tribes.ui.views.DSWorkbenchChurchFrame;
import de.tor.tribes.ui.views.DSWorkbenchSearchFrame;
import de.tor.tribes.ui.views.DSWorkbenchRankFrame;
import de.tor.tribes.ui.views.DSWorkbenchMerchantDistibutor;
import de.tor.tribes.ui.views.DSWorkbenchStatsFrame;
import de.tor.tribes.ui.views.DSWorkbenchDistanceFrame;
import de.tor.tribes.ui.views.DSWorkbenchTagFrame;
import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar.Separator;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.BasicRibbonUI;

/**
 *
 * @author Torridity
 */
public class RibbonConfigurator {

    public static void addAppIcons(JRibbonFrame frame) {
        RibbonApplicationMenu appmen = new RibbonApplicationMenu();

        frame.setApplicationIcon(getResizableIconFromFile("graphics/big/axe.png"));

        /*RibbonApplicationMenuEntrySecondary exportSubmenu = new RibbonApplicationMenuEntrySecondary(
        new document_save_as(), "Export", null,
        CommandButtonKind.ACTION_ONLY);
        exportSubmenu.setDescriptionText("Exportieren von eigenen Angriffsplänen, Markierungen, Berichten usw.");
        RibbonApplicationMenuEntrySecondary importSubmenu = new RibbonApplicationMenuEntrySecondary(
        new document_save_as(), "Import", null,
        CommandButtonKind.ACTION_ONLY);
        importSubmenu.setDescriptionText("Importieren von Angriffsplänen, Markierungen, Berichten usw.");
         */
        RibbonApplicationMenuEntryPrimary importEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/replace2.png"), "Import", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/replace2.png"), "Export", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        exportEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Export von erstellten Daten <ul><li>Angriffspläne</li> <li>Markierungen</li> <li>Berichte</li> <li>...</li></ul></html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });
        importEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Import von vorher exportierten Daten <ul><li>Angriffspläne</li> <li>Markierungen</li> <li>Berichte</li> <li>...</li></ul></html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(importEntry);
        appmen.addMenuEntry(exportEntry);
        appmen.addMenuSeparator();

        RibbonApplicationMenuEntryPrimary settingsEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/replace2.png"), "Einstellungen", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        settingsEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Einstellungen zu DS Workbench <ul><li>Datenaktualisierung</li> <li>Profile</li> <li>Netzwerkeinstellungen</li> <li>...</li></ul></html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(settingsEntry);
        appmen.addMenuSeparator();
        RibbonApplicationMenuEntryPrimary exitEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/replace2.png"), "Beenden", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        exitEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Beenden von DS Workbench</html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });
        appmen.addMenuEntry(exitEntry);
        appmen.addFooterEntry(new RibbonApplicationMenuEntryFooter(getResizableIconFromFile("graphics/icons/replace2.png"), "Beenden", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        }));


        JCommandButton attackViewButton = factoryButton("Angriffe", "graphics/big/axe.png", "Öffnet die Angriffsübersicht", "Die Angriffsübersicht erlaubt es, geplante Angriffe zu verwalten, zu modifizieren, zu exportieren (z.B. als BB-Codes) und in den Browser zu übertragen. Angriffe müssen vorher durch eins der verfügbaren Angriffswerkzeuge automatisch oder manuell erstellt werden", true);
        attackViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchAttackFrame.getSingleton().setVisible(true);
            }
        });

        JCommandButton markerViewButton = factoryButton("Markierungen", "graphics/icons/mark.png", "Öffnet die Markierungsübersicht", "Die Markierungsübersicht erlaubt es, Spieler und Stammesmarkierungen zu verwalten und zu modifizieren. Markierungen müssen vorher über das entsprechende Kartenwerkzeug erstellt werden", true);
        markerViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMarkerFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton troopsViewButton = factoryButton("Truppen", "graphics/big/troops.png", "Öffnet die Truppenübersicht", "Die Truppenübersicht erlaubt es, vorher aus dem Spiel importierte Truppeninformationen zu verwalten. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        troopsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchTroopsFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton rankViewButton = factoryButton("Ranglisten", "graphics/big/medal.png", "Öffnet die Rangliste", "Zeigt Ranglisten von Spielern und Stämmen mit allen verfügbaren Informationen (Punkte, Dörfer, Kills usw.)", true);
        rankViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchRankFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton formsViewButton = factoryButton("Zeichnungen", "graphics/big/palette2.png", "Öffnet die Zeichnungsübersicht", "Die Zeichnungsübersicht zeigt alle auf der Hauptkarte eingetragenen Zeichnungen an und erlaubt es, diese nachträglich zu verändern.", true);
        formsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchFormFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton churchViewButton = factoryButton("Kirchen", "graphics/big/church1.png", "Öffnet die Kirchenübersicht", "Die Kirchenübersicht zeigt alle in DS Workbench eingetragenen Kirchen an. Diese Ansicht ist nur auf Kirchenwelten verfügbar.", true);
        churchViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    DSWorkbenchChurchFrame.getSingleton().setVisible(true);
                }
            }
        });
        JCommandButton conquerViewButton = factoryButton("Eroberungen", "graphics/big/snob.png", "Öffnet die Eroberungsübersicht", "Die Eroberungsübersicht zeigt alle kürzlich durchgeführten Eroberungen der aktuellen Welt an und erlaubt es, diese nach vielen Kriterien zu filtern. Eroberungen werden bei laufendem DS Workbench regelmäßig vom DS-Server gelesen, die aktuelle Zustimmung von eingelesenen Eroberungen wird entsprechend den Servereinstellungen ausgerechnet.", true);
        conquerViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchConquersFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton notesViewButton = factoryButton("Notizblock", "graphics/big/notebook.png", "Öffnet den Notizblock", "Der Notizblock erlaubt es, Notizen zu einelnen oder mehreren Dörfern zu verwalten. Dabei stehen die gängigen BB-Codes zu Verfügung. Erstellte Notizen tauchen, sofern die Ebene 'Notizen' sichtbar ist, auf der Hauptkarte als Symbole und im Kartenpopup mit dem zugehörigen Notiztext auf. Das Erstellen von Notizen kann entweder über das entsprechende Kartenwerkzeug oder direkt im Notizblock geschehen.", true);
        notesViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchNotepad.getSingleton().setVisible(true);
            }
        });
        JCommandButton tagsViewButton = factoryButton("Gruppen", "graphics/icons/tag.png", "Öffnet die Gruppenübersicht", "Die Gruppenübersicht erlaubt es, vorher aus dem Spiel importierte Gruppen zu verwalten und zu neuen Gruppen zu kombinieren. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        tagsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchTagFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton statsViewButton = factoryButton("Statistiken", "graphics/icons/ally_chart.png", "Öffnet die Statistikübersicht", "Die Statistikübersicht erlaubt es, Statistiken über Spieler und Stämme für beliebige Zeiträume zu führen. Um einen Spieler oder einen Stamm in die Statistiken aufzunehmen, klicke mit der rechten Maustaste auf ein Dorf auf der Hauptkarte und wähle im entsprechenden Untermenü des Kontextmenüs 'Spieler überwachen' oder 'Stamm überwachen'", true);
        statsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchStatsFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton reportsViewButton = factoryButton("Berichte", "graphics/big/report.png", "Öffnet die Berichtdatenbank", "Die Berichtdatenbank erlaubt es, vorher aus dem Spiel importierte Berichte zu verwalten. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        reportsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchReportFrame.getSingleton().setVisible(true);
            }
        });

        frame.getRibbon().setApplicationMenu(appmen);
        frame.getRibbon().configureHelp(getResizableIconFromFile("graphics/big/help2.png"),
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        GlobalOptions.getHelpBroker().setDisplayed(true);
                    }
                });
    }

    public static void addViewTask(JRibbonFrame frame) {
        JRibbonBand infoViewBand = new JRibbonBand("Informationen", getResizableIconFromFile("graphics/big/information.png"));
        JRibbonBand attackViewBand = new JRibbonBand("Angriff", getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand ingameInfoViewBand = new JRibbonBand("Importierte Daten", getResizableIconFromFile("graphics/big/clipboard_next.png"));

        JCommandButton attackViewButton = factoryButton("Angriffe", "graphics/big/axe.png", "Öffnet die Angriffsübersicht", "Die Angriffsübersicht erlaubt es, geplante Angriffe zu verwalten, zu modifizieren, zu exportieren (z.B. als BB-Codes) und in den Browser zu übertragen. Angriffe müssen vorher durch eins der verfügbaren Angriffswerkzeuge automatisch oder manuell erstellt werden", true);
        attackViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchAttackFrame.getSingleton().setVisible(true);
            }
        });

        JCommandButton markerViewButton = factoryButton("Markierungen", "graphics/icons/mark.png", "Öffnet die Markierungsübersicht", "Die Markierungsübersicht erlaubt es, Spieler und Stammesmarkierungen zu verwalten und zu modifizieren. Markierungen müssen vorher über das entsprechende Kartenwerkzeug erstellt werden", true);
        markerViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMarkerFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton troopsViewButton = factoryButton("Truppen", "graphics/big/troops.png", "Öffnet die Truppenübersicht", "Die Truppenübersicht erlaubt es, vorher aus dem Spiel importierte Truppeninformationen zu verwalten. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        troopsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchTroopsFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton rankViewButton = factoryButton("Ranglisten", "graphics/big/medal.png", "Öffnet die Rangliste", "Zeigt Ranglisten von Spielern und Stämmen mit allen verfügbaren Informationen (Punkte, Dörfer, Kills usw.)", true);
        rankViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchRankFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton formsViewButton = factoryButton("Zeichnungen", "graphics/big/palette2.png", "Öffnet die Zeichnungsübersicht", "Die Zeichnungsübersicht zeigt alle auf der Hauptkarte eingetragenen Zeichnungen an und erlaubt es, diese nachträglich zu verändern.", true);
        formsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchFormFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton churchViewButton = factoryButton("Kirchen", "graphics/big/church1.png", "Öffnet die Kirchenübersicht", "Die Kirchenübersicht zeigt alle in DS Workbench eingetragenen Kirchen an. Diese Ansicht ist nur auf Kirchenwelten verfügbar.", true);
        churchViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    DSWorkbenchChurchFrame.getSingleton().setVisible(true);
                }
            }
        });
        JCommandButton conquerViewButton = factoryButton("Eroberungen", "graphics/big/snob.png", "Öffnet die Eroberungsübersicht", "Die Eroberungsübersicht zeigt alle kürzlich durchgeführten Eroberungen der aktuellen Welt an und erlaubt es, diese nach vielen Kriterien zu filtern. Eroberungen werden bei laufendem DS Workbench regelmäßig vom DS-Server gelesen, die aktuelle Zustimmung von eingelesenen Eroberungen wird entsprechend den Servereinstellungen ausgerechnet.", true);
        conquerViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchConquersFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton notesViewButton = factoryButton("Notizblock", "graphics/big/notebook.png", "Öffnet den Notizblock", "Der Notizblock erlaubt es, Notizen zu einelnen oder mehreren Dörfern zu verwalten. Dabei stehen die gängigen BB-Codes zu Verfügung. Erstellte Notizen tauchen, sofern die Ebene 'Notizen' sichtbar ist, auf der Hauptkarte als Symbole und im Kartenpopup mit dem zugehörigen Notiztext auf. Das Erstellen von Notizen kann entweder über das entsprechende Kartenwerkzeug oder direkt im Notizblock geschehen.", true);
        notesViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchNotepad.getSingleton().setVisible(true);
            }
        });
        JCommandButton tagsViewButton = factoryButton("Gruppen", "graphics/icons/tag.png", "Öffnet die Gruppenübersicht", "Die Gruppenübersicht erlaubt es, vorher aus dem Spiel importierte Gruppen zu verwalten und zu neuen Gruppen zu kombinieren. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        tagsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchTagFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton statsViewButton = factoryButton("Statistiken", "graphics/icons/ally_chart.png", "Öffnet die Statistikübersicht", "Die Statistikübersicht erlaubt es, Statistiken über Spieler und Stämme für beliebige Zeiträume zu führen. Um einen Spieler oder einen Stamm in die Statistiken aufzunehmen, klicke mit der rechten Maustaste auf ein Dorf auf der Hauptkarte und wähle im entsprechenden Untermenü des Kontextmenüs 'Spieler überwachen' oder 'Stamm überwachen'", true);
        statsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchStatsFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton reportsViewButton = factoryButton("Berichte", "graphics/big/report.png", "Öffnet die Berichtdatenbank", "Die Berichtdatenbank erlaubt es, vorher aus dem Spiel importierte Berichte zu verwalten. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        reportsViewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchReportFrame.getSingleton().setVisible(true);
            }
        });

        //   attackViewBand.startGroup();
        attackViewBand.addCommandButton(attackViewButton, RibbonElementPriority.TOP);
        attackViewBand.addCommandButton(markerViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(formsViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(churchViewButton, RibbonElementPriority.LOW);

        infoViewBand.addCommandButton(notesViewButton, RibbonElementPriority.TOP);
        infoViewBand.addCommandButton(conquerViewButton, RibbonElementPriority.MEDIUM);
        infoViewBand.addCommandButton(rankViewButton, RibbonElementPriority.MEDIUM);
        infoViewBand.addCommandButton(statsViewButton, RibbonElementPriority.MEDIUM);

        ingameInfoViewBand.addCommandButton(tagsViewButton, RibbonElementPriority.MEDIUM);
        ingameInfoViewBand.addCommandButton(troopsViewButton, RibbonElementPriority.MEDIUM);
        ingameInfoViewBand.addCommandButton(reportsViewButton, RibbonElementPriority.MEDIUM);

        infoViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoViewBand.getControlPanel()),
                new CoreRibbonResizePolicies.Mid2Low(infoViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoViewBand.getControlPanel())));
        attackViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(attackViewBand.getControlPanel()),
                new CoreRibbonResizePolicies.Mid2Low(attackViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(attackViewBand.getControlPanel())));
        ingameInfoViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(ingameInfoViewBand.getControlPanel()),
                new CoreRibbonResizePolicies.Mirror(ingameInfoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.High2Low(ingameInfoViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(ingameInfoViewBand.getControlPanel())));
        RibbonTask task2 = new RibbonTask("Ansicht", attackViewBand, ingameInfoViewBand, infoViewBand);

        //  frame.getRibbon().addTask(task1);
        frame.getRibbon().addTask(task2);
    }

    public static void addGeneralToolsTask(JRibbonFrame frame) {
        JRibbonBand attackToolsBand = new JRibbonBand("Angriff", getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand defendToolsBand = new JRibbonBand("Verteidigung", getResizableIconFromFile("graphics/big/def.png"));
        JRibbonBand infoToolBand = new JRibbonBand("Information", getResizableIconFromFile("graphics/big/information.png"));
        JRibbonBand miscToolsBand = new JRibbonBand("Sonstige", getResizableIconFromFile("graphics/big/box.png"));

        JCommandButton attackPlanerToolButton = factoryButton("Angriffsplaner (automatisch)", "graphics/big/att_auto.png", "Öffnet den automatischen Angriffsplaner", "Der automatische Angriffsplaner erlaubt es, innerhalb kürzester Zeit eine Vielzahl von Angriffen bzw. Truppenbewegungen auf eine beliebige Menge von Zielen zu planen. Er ist dabei auf die grobe Planung unter Verwendung vieler Dörfern ausgelegt, gezielte AG-Angriffe oder Angriffe mit sehr wenigen Dörfern liefern in der Regeln weniger gute oder keine Ergebnisse.", true);
        attackPlanerToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setVisible(true);
            }
        });
        JCommandButton manualAttackPlanerToolButton = factoryButton("Angriffsplaner (manuell)", "graphics/big/att_manual.png", "Öffnet den manuellen Angriffsplaner", "Der manuelle Angriffsplaner erlaubt es, detailliert kleine bis mittlere Mengen an Angriffen zu planen. Er orientiert sich dabei im Wesentlichen an anderen, Online verfügbaren Angriffsplanern.", true);
        manualAttackPlanerToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setVisible(true);
            }
        });
        JCommandButton selectionToolButton = factoryButton("Auswahlübersicht", "graphics/icons/selection.png", "Öffnet die Auswahlübersicht", "Die Auswahlübersicht zeigt in Baum- oder Listenform alle aktuell ausgewählten Dörfer und erlaubt etwa, aus dieser Auswahl Dorflisten zu erstellen. Weiterhin können Dörfer aus der Auswahlübersicht per Drag&Drop beispielsweise in den automatisch Angriffsplaner als Herkunft- oder Zieldörfer eingefügt werden. Die Auswahl von Dörfern erfolgt über das entsprechende Kartenwerkzeug.", true);
        selectionToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchSelectionFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton searchToolButton = factoryButton("Suche", "graphics/big/find.png", "Öffnet die Suche", "Dieses Werkzeug erlaubt die schnelle Suche nach Spielern oder Stämmen", true);
        searchToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchSearchFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton distanceToolButton = factoryButton("Entfernungsberechnung", "graphics/icons/measure.png", "Öffnet die Entfernungsberechnung", "Dieses Werkzeug erlaubt die Berechnung von Entfernungen zwischen allen eigenen Dörfern und einer beliebigen Anzahl anderer Dörfer. Dörfer können einzeln per Drag&Drop von der Hauptkarte in diese Ansicht gezogen werden.", true);
        distanceToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchDistanceFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton astarToolButton = factoryButton("A*Star", "graphics/big/astar.png", "Öffnet den Simulator", "A*Star ermöglicht es, Angriffssimulationen mit Hilfe des im Spiel verwendeten Kampfsystems durchzuführen. Zusätzliche Features erlauben es, die Anzahl von Offs zu bestimmen, die für die Vernichtung einer bestimmten Deff benötigt wird.", true);
        astarToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchSimulatorFrame.getSingleton().setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                DSWorkbenchSimulatorFrame.getSingleton().showIntegratedVersion(GlobalOptions.getSelectedServer());
            }
        });
        JCommandButton sosAnalyzerToolButton = factoryButton("SOS Analyzer", "graphics/big/lifebelt.png", "Öffnet den SOS Analyzer", "Der SOS Analyzer dient der Analyse von SOS Anfragen, die man einfach aus dem Spiel in ein entsprechendes Textfeld kopiert. Dabei werden Ziele, Angreifer und Herkunftsdörfer ausgeschlüsselt, was einen schnellen Überblick über die Situation bietet. Weiterhin können diese Informationen in ein übersichtlicheres Format umformatiert und als BB-Code exportiert werden.", true);
        sosAnalyzerToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchSOSRequestAnalyzer.getSingleton().setVisible(true);
            }
        });
        JCommandButton retimeToolButton = factoryButton("Re-Timer", "graphics/big/retime.png", "Öffnet den Re-Timer", "Der Re-Timer erlaubt es zu Einzelangriffen, die man einfach aus dem Spiel in ein entsprechendes Textfeld kopiert, mögliche re-time Angriffe zu berechnen, welche die angreifenden Truppen bei der Rückkehr in ihr Herkunftsdorf vernichten können. Voraussetzung sind korrekt importierte Truppeninformationen aus dem Spiel (siehe Hilfe) und ein gutes Timing.", true);
        retimeToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
            }
        });
        JCommandButton resourceDistributorToolButton = factoryButton("Rohstoffverteiler", "graphics/big/storage.png", "Öffnet den Rohstoffverteiler", "Der Rohstoffverteiler erlaubt, basierend auf kopierten Informationen aus der Produktionsübersicht, die Rohstoffe in den eingefügten Dörfern auszugleiche oder den Rohstoffbestand in bestimmten Dörfern auf eine gewünschte Menge zu bringen. Er berechnet die dafür notwendigen Transporte, die im Anschluss direkt in den Browser übertragen und von dort abgeschickt werden können.", true);
        resourceDistributorToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMerchantDistibutor.getSingleton().setVisible(true);
            }
        });
        JCommandButton mapshotToolButton = factoryButton("Screenshot erstellen", "graphics/big/camera.png", "Erstellt einen Screenshot der Hauptkarte", "Dieses Werkzeug erlaubt es, einen Screenshot der aktuellen Ansicht der Hauptkarte zu erstellen. Screenshots können im Anschluss auf der Festplatte oder dem DS Workbench Server gespeichert werden, um sie anderen Spielern zugänglich zu machen. Die Speicherung auf dem DS Workbench Server ist allerdings nur bis zu einer bestimmten Bildgröße möglich, die resultierende Bildgröße hängt von der Fenstergröße und dem gewählten Grafikpaket ab.", true);
        mapshotToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().planMapshot();
            }
        });
        JCommandButton runtimeToolButton = factoryButton("Laufzeiten", "graphics/big/speed.png", "Zeigt die Laufzeitübersicht", "Eine einfache Liste, welche die Laufzeiten der auf dieser Welt verfügbaren Einheiten anzeigt.", true);
        runtimeToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                UnitOrderBuilder.showUnitOrder(null, null);
            }
        });
        JCommandButton clockToolButton = factoryButton("Uhr", "graphics/big/clock.png", "Zeigt die Uhr", "Eine Uhr, welche deine aktuelle Systemzeit anzeigt. Sie kann das Timen von Angriffen erleichern, muss aber nicht zwingend mit der Zeit der DS-Server übereinstimmen.", true);
        clockToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ClockFrame.getSingleton().setVisible(true);
            }
        });

        attackToolsBand.addCommandButton(attackPlanerToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(manualAttackPlanerToolButton, RibbonElementPriority.TOP);

        defendToolsBand.addCommandButton(astarToolButton, RibbonElementPriority.MEDIUM);
        defendToolsBand.addCommandButton(sosAnalyzerToolButton, RibbonElementPriority.MEDIUM);
        defendToolsBand.addCommandButton(retimeToolButton, RibbonElementPriority.MEDIUM);

        infoToolBand.addCommandButton(selectionToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(searchToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(distanceToolButton, RibbonElementPriority.LOW);

        miscToolsBand.addCommandButton(resourceDistributorToolButton, RibbonElementPriority.TOP);
        miscToolsBand.addCommandButton(mapshotToolButton, RibbonElementPriority.MEDIUM);
        miscToolsBand.addCommandButton(runtimeToolButton, RibbonElementPriority.MEDIUM);
        miscToolsBand.addCommandButton(clockToolButton, RibbonElementPriority.MEDIUM);

        attackToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(attackToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(attackToolsBand.getControlPanel())));
        defendToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(defendToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(defendToolsBand.getControlPanel())));

        infoToolBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoToolBand.getControlPanel()),
                //new CoreRibbonResizePolicies.Mid2Low(infoToolBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoToolBand.getControlPanel())));

        miscToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(miscToolsBand.getControlPanel()),
                //new CoreRibbonResizePolicies.Mid2Low(miscToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(miscToolsBand.getControlPanel())));

        RibbonTask task1 = new RibbonTask("Allgemeine Werkzeuge", attackToolsBand, defendToolsBand, infoToolBand, miscToolsBand);
        frame.getRibbon().addTask(task1);
    }

    public static void addMapToolsTask(JRibbonFrame frame) {
        JRibbonBand baseToolsBand = new JRibbonBand("Allgemein", getResizableIconFromFile("graphics/cursors/default.png"));
        JRibbonBand attackToolsBand = new JRibbonBand("Angriff", getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand defendToolsBand = new JRibbonBand("Verteidigung", getResizableIconFromFile("graphics/big/def.png"));
        JRibbonBand drawToolsBand = new JRibbonBand("Zeichnen", getResizableIconFromFile("graphics/big/palette2.png"));
        JRibbonBand minimapToolsBand = new JRibbonBand("Minimap", getResizableIconFromFile("graphics/icons/minimap.png"));
        JRibbonBand infoToolBand = new JRibbonBand("Dörfer", getResizableIconFromFile("graphics/icons/map_tools.png"));
//
        JCommandButton noToolButton = factoryButton("Werkzeug abwählen", "graphics/cursors/default.png", "Deaktivierung des aktuellen Karten- und Minimapwerkzeugs", "Deaktiviert das aktuelle Karten- oder Minimapwerkzeug und erlaubt es die Hauptkarte wieder per Drag&Drop zu verschieben", true);
        noToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
            }
        });
        JCommandButton distanceToolButton = factoryButton("Laufzeit bestimmen", "graphics/big/tape_measure1.png", "Bestimmung der Laufzeiten zwischen zwei Dörfern", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken und mit gedrückter linker Maustaste eine Linie zu einem anderen Dorf ziehen. Bei aktiviertem Kartenpopup werden statt der Truppenstärke im Zieldorf die Laufzeiten der einzelnen Einheiten angezeigt.", true);
        distanceToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
            }
        });
        JCommandButton markToolButton = factoryButton("Spieler/Stämme markieren", "graphics/big/brush3.png", "Markiert Spieler/Stämme auf der Hauptkarte", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, den Besitzer und/oder den Stamm des Besitzers eine Farbmarkierung auf der Hauptkarte zuzuweisen", true);
        markToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
            }
        });
        JCommandButton tagToolButton = factoryButton("Gruppe zuweisen", "graphics/icons/tag.png", "Weist einem Dorf eine Gruppe zu", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, um die Gruppenauswahl zu öffnen und dem Dorf eine vorhandene oder eine neue Gruppen zuzuweisen", true);
        tagToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
            }
        });
        JCommandButton noteToolButton = factoryButton("Notiz erstellen", "graphics/big/notebook_add.png", "Erstellt eine neue Notiz", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, um für dieses Dorf eine neue Notiz im Notizblock zu erstellen", true);
        noteToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_NOTE);
            }
        });
        JCommandButton selectToolButton = factoryButton("Dörfer wählen", "graphics/big/selection.png", "Dörfer auf der Hauptkarte auswählen", "Mit gewähltem Werkzeug und gedrückter linker Maustaste ein Auswahlrechteck auf der Hauptkarte aufziehen, um Dörfer in die Auswahlübersicht zu übertragen. Einzeldörfer oder unabhängige Bereiche können mit gedrückter Shift-Taste gewählt werden.", true);
        selectToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
            }
        });
        JCommandButton radarToolButton = factoryButton("Laufzeitradar", "graphics/icons/radar.png", "Zeigt Laufzeitradien um ein Dorf für die verschiedenen Einheiten an", "Mit gewähltem Werkzeug mit der Maus über ein Dorf fahren, um die Laufzeitradien zu sehen. Ein Linksklick auf das Dorf setzt die Laufzeitradien für das aktuelle Dorf fest und erlaubt, die Maus über ein weiteres Dorf zu bewegen. Ein Klick auf ein leeres Kartenfeld löscht die festgestellten Laufzeitradien.", true);
        radarToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_RADAR);
            }
        });
        //
        JCommandButton attackRamToolButton = factoryButton(null, "graphics/big/ram.png", "Erstellt einen Einzelangriff mit Rammenlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackRamToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
            }
        });
        JCommandButton attackAxeToolButton = factoryButton(null, "graphics/big/axe.png", "Erstellt einen Einzelangriff mit Axtlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackAxeToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
            }
        });
        JCommandButton attackSnobToolButton = factoryButton(null, "graphics/big/snob.png", "Erstellt einen Einzelangriff mit AG-Laufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackSnobToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
            }
        });
        JCommandButton attackSpyToolButton = factoryButton(null, "graphics/big/spy.png", "Erstellt einen Einzelangriff mit Späherlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackSpyToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
            }
        });
        JCommandButton attackLightToolButton = factoryButton(null, "graphics/big/light.png", "Erstellt einen Einzelangriff mit LKav-Laufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackLightToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
            }
        });
        JCommandButton attackHeavyToolButton = factoryButton(null, "graphics/big/heavy.png", "Erstellt einen Einzelangriff mit SKav-Laufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackHeavyToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
            }
        });
        JCommandButton attackSwordToolButton = factoryButton(null, "graphics/big/sword.png", "Erstellt einen Einzelangriff mit Schwertlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackSwordToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
            }
        });
//
        JCommandButton supportToolButton = factoryButton("Unterstützungen bestimmen", "graphics/big/support.png", "Öffnet das Unterstützungswerkzeug", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, um das Unterstützungswerkzeug zu öffnen. Darin können Gruppen aus denen Unterstützungen geschickt werden sollen gewählt werden, um anschließend mögliche Unterstützungen zu einem wählbaren, maximalen Zeitpunkt zu bestimmen.", true);
        supportToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SUPPORT);
            }
        });
//
        JCommandButton drawLineToolButton = factoryButton("Linie zeichnen", "graphics/icons/draw_line.png", "Werkzeug zum Zeichnen von Linien auf der Hauptkarte", null, true);
        drawLineToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_LINE);
                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Line.class);

            }
        });
        JCommandButton drawArrowToolButton = factoryButton("Pfeil zeichnen", "graphics/icons/draw_arrow.png", "Werkzeug zum Zeichnen von Pfeilen auf der Hauptkarte", null, true);
        drawArrowToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_ARROW);
                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Arrow.class);
            }
        });
        JCommandButton drawCircleToolButton = factoryButton("Kreis zeichnen", "graphics/icons/draw_circle.png", "Werkzeug zum Zeichnen von Kreisen auf der Hauptkarte", null, true);
        drawCircleToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_CIRCLE);
                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Circle.class);
            }
        });
        JCommandButton drawRectToolButton = factoryButton("Rechteck zeichnen", "graphics/icons/draw_rect.png", "Werkzeug zum Zeichnen von Rechtecken auf der Hauptkarte", null, true);
        drawRectToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_RECT);
                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Rectangle.class);
            }
        });
        JCommandButton drawTextToolButton = factoryButton("Text zeichnen", "graphics/icons/draw_text.png", "Werkzeug zum Zeichnen von Texten auf der Hauptkarte", null, true);
        drawTextToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_TEXT);
                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Text.class);
            }
        });
        JCommandButton drawFreehandToolButton = factoryButton("Freihand zeichnen", "graphics/icons/draw_freeform.png", "Werkzeug zum Erstellen von Freihandzeichnungen auf der Hauptkarte", null, true);
        drawFreehandToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_FREEFORM);
                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.FreeForm.class);
            }
        });
//
        JCommandButton createChurchToolButton = factoryButton(null, "graphics/big/church1.png", "Kirche in einem Dorf auf der Hauptkarte erstellen", "Dieses Werkzeug ist nur auf Kirchenwelten aktiv", true);
        JCommandButton createChurch1ToolButton = factoryButton(null, "graphics/big/church1.png", "Kirche Stufe 1 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um eine Kirche Stufe 1 im Dorf zu platzieren", true);
        createChurch1ToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_1);
                }
            }
        });
        JCommandButton createChurch2ToolButton = factoryButton(null, "graphics/big/church2.png", "Kirche Stufe 2 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um eine Kirche Stufe 2 im Dorf zu platzieren", true);
        createChurch2ToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_2);
                }
            }
        });
        JCommandButton createChurch3ToolButton = factoryButton(null, "graphics/big/church3.png", "Kirche Stufe 3 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um eine Kirche Stufe 3 im Dorf zu platzieren", true);
        createChurch3ToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_3);
                }
            }
        });
//
        JCommandButtonPanel cbp = new JCommandButtonPanel(CommandButtonDisplayState.FIT_TO_ICON);
        cbp.setLayoutKind(JCommandButtonPanel.LayoutKind.ROW_FILL);
        cbp.addButtonGroup("Stufe");
        cbp.addButtonToLastGroup(createChurch1ToolButton);
        cbp.addButtonToLastGroup(createChurch2ToolButton);
        cbp.addButtonToLastGroup(createChurch3ToolButton);
        final JCommandPopupMenu popupMenu = new JCommandPopupMenu(
                cbp, 1, 3);

        createChurchToolButton.setPopupCallback(new PopupPanelCallback() {

            public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                return popupMenu;
            }
        });
        createChurchToolButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
//
        JCommandButton minimapMoveToolButton = factoryButton("Ausschnitt bewegen", "graphics/icons/move.png", "Erlaubt es den sichtbaren Kartenausschnitt auf der Minimap zu verschieben", "Mit gewähltem Werkzeug und gedrückter linker Maustaste den Ausschnitt verschieben", true);
        minimapMoveToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
            }
        });
        JCommandButton minimapZoomToolButton = factoryButton("Minimap vergößern", "graphics/big/view.png", "Vergrößert einen Bereich von 300x300 Dörfer auf der Minimap", "Mit der Maus über die Minimap fahren, der Mauszeiger bildet die Mitte des gezeigten Ausschnitts", true);
        minimapZoomToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
            }
        });
        JCommandButton minimapShotToolButton = factoryButton("Screenshot erstellen", "graphics/big/camera.png", "Erstellt einen Screenshot eines wählbaren Bereiches der Minimap", "Mit gewähltem Werkzeug ein Rechteck auf der Minimap aufziehen, welches den gewünschten Bereich einrahmt", true);
        minimapShotToolButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
            }
        });
//
        baseToolsBand.addCommandButton(noToolButton, RibbonElementPriority.TOP);
//
        attackToolsBand.addCommandButton(attackRamToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(attackSnobToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(attackSpyToolButton, RibbonElementPriority.MEDIUM);
        attackToolsBand.addCommandButton(attackAxeToolButton, RibbonElementPriority.MEDIUM);
        attackToolsBand.addCommandButton(attackLightToolButton, RibbonElementPriority.LOW);
        attackToolsBand.addCommandButton(attackHeavyToolButton, RibbonElementPriority.LOW);
        attackToolsBand.addCommandButton(attackSwordToolButton, RibbonElementPriority.LOW);
//
        drawToolsBand.addCommandButton(drawRectToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawCircleToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawLineToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawArrowToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawFreehandToolButton, RibbonElementPriority.LOW);
        drawToolsBand.addCommandButton(drawTextToolButton, RibbonElementPriority.LOW);
//
        minimapToolsBand.addCommandButton(minimapMoveToolButton, RibbonElementPriority.TOP);
        minimapToolsBand.addCommandButton(minimapZoomToolButton, RibbonElementPriority.LOW);
        minimapToolsBand.addCommandButton(minimapShotToolButton, RibbonElementPriority.LOW);
//
        defendToolsBand.addCommandButton(supportToolButton, RibbonElementPriority.LOW);
//
        infoToolBand.addCommandButton(selectToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(markToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(noteToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(distanceToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(radarToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(tagToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(createChurchToolButton, RibbonElementPriority.LOW);

        baseToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(baseToolsBand.getControlPanel())));
//
        attackToolsBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesRestrictive(attackToolsBand));
//
        drawToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(drawToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(drawToolsBand.getControlPanel())));
        //
        minimapToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(minimapToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(minimapToolsBand.getControlPanel())));
//
        defendToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(defendToolsBand.getControlPanel())));
//
        infoToolBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoToolBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoToolBand.getControlPanel())));
//
        RibbonTask task1 = new RibbonTask("Kartenwerkzeuge", baseToolsBand, attackToolsBand, defendToolsBand, infoToolBand, drawToolsBand, minimapToolsBand);
//
        frame.getRibbon().addTask(task1);
    }

    private static JCommandButton factoryButton(String pLabel, String pIconPath, boolean pShowLabel) {
        return factoryButton(pLabel, pIconPath, null, null, pShowLabel);
    }

    private static JCommandButton factoryButton(String pLabel, String pIconPath, String pTooltipText, String pSecondaryTooltipText, boolean pShowLabel) {
        JCommandButton button = new JCommandButton((pShowLabel) ? pLabel : null, getResizableIconFromFile(pIconPath));
        if (pTooltipText != null) {
            RichTooltip rt = new RichTooltip((pLabel != null) ? pLabel : "Info", pTooltipText);
            if (pSecondaryTooltipText != null) {
                rt.addDescriptionSection(pSecondaryTooltipText);
            }
            try {
                rt.setMainImage(ImageIO.read(new File(pIconPath)));
            } catch (Exception e) {
            }
            button.setActionRichTooltip(rt);
        }
        return button;
    }

    private static ResizableIcon getResizableIconFromResource(String resource) {
        return ImageWrapperResizableIcon.getIcon(DSWorkbenchMainFrame.class.getClassLoader().getResource(resource), new Dimension(18, 18));
    }

    private static ResizableIcon getResizableIconFromFile(String resource) {
        try {
            return ImageWrapperResizableIcon.getIcon(new File(resource).toURI().toURL(), new Dimension(48, 48));
        } catch (Exception e) {
            return new EmptyResizableIcon(18);
        }
    }
}
