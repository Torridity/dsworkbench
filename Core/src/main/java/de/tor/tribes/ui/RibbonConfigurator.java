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

import de.tor.tribes.ui.windows.BBCodeEditor;
import de.tor.tribes.ui.windows.FormConfigFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.ClockFrame;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.ui.views.*;
import de.tor.tribes.ui.wiz.red.ResourceDistributorWizard;
import de.tor.tribes.ui.wiz.tap.TacticsPlanerWizard;
import de.tor.tribes.util.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
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

/**
 * @author Torridity
 * @author extremeCrazyCoder
 */
public class RibbonConfigurator {
    private static Logger logger = Logger.getLogger("RibbonConfigurator");

    public static void addAppIcons(JRibbonFrame frame) {
        RibbonApplicationMenu appmen = new RibbonApplicationMenu();

        // <editor-fold defaultstate="collapsed" desc="Main Menue">
        frame.setApplicationIcon(getResizableIconFromFile("graphics/big/axe.png"));

        RibbonApplicationMenuEntryPrimary importEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/24x24/load.png"), "Import", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doImport();
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/24x24/save.png"), "Export", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doExport();
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        exportEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Export von erstellten Daten <ul><li>Angriffspläne</li> <li>Markierungen</li> <li>Berichte</li> <li>...</li></ul></html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });
        importEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
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

        RibbonApplicationMenuEntryPrimary bbEditorEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/bbeditor.png"), "BB-Template Editor", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BBCodeEditor.getSingleton().setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        bbEditorEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>BB-Template Editor zur eigenen Gestaltung aller Templates f&uuml;r den BB-Code Export</html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(bbEditorEntry);

        RibbonApplicationMenuEntryPrimary standardAttackFrame = new RibbonApplicationMenuEntryPrimary(getResizableIconFromResource("/res/ui/troop_info_add.png"), "Standardangriffe", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TroopSetupConfigurationFrame.getSingleton().setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        standardAttackFrame.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Standardangriffe, z.B. f&uuml;r das &Uuml;bertragen von Angriffen in den Browser, festlegen</html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(standardAttackFrame);

        if (!GlobalOptions.isMinimal()) {
            RibbonApplicationMenuEntryPrimary layerEditor = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/24x24/layer_settings.gif"), "Ebeneneinstellungen", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LayerOrderConfigurationFrame.getSingleton().setAlwaysOnTop(true);
                    LayerOrderConfigurationFrame.getSingleton().setVisible(true);
                }
            }, JCommandButton.CommandButtonKind.ACTION_ONLY);

            layerEditor.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
                @Override
                public void menuEntryActivated(JPanel targetPanel) {
                    targetPanel.removeAll();
                    targetPanel.setLayout(new BorderLayout());
                    targetPanel.add(new JLabel("<html>Mit den Ebneneinstellungen kann bestimmt werden, welche Elemente auf der Hauptkarte sichtbar sind. Dies kann z.B. hilfreich sein, wenn "
                            + "DS Workbench langsam reagiert. In diesem Fall wird empfohlen, nicht ben&ouml;tigte Ebenen auszublenden.</html>"), BorderLayout.CENTER);
                    targetPanel.revalidate();
                }
            });

            appmen.addMenuEntry(layerEditor);
        }

        RibbonApplicationMenuEntryPrimary settingsEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/settings.png"), "Einstellungen", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalOptions.storeViewStates();
                DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        settingsEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Einstellungen zu DS Workbench <ul><li>Datenaktualisierung</li> <li>Profile</li> <li>Netzwerkeinstellungen</li> <li>...</li></ul></html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(settingsEntry);

        appmen.addMenuSeparator();
        RibbonApplicationMenuEntryPrimary exitEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/logout.png"), "Beenden", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doExit();

            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        exitEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel("<html>Beenden von DS Workbench</html>"), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });
        appmen.addMenuEntry(exitEntry);
        appmen.addFooterEntry(new RibbonApplicationMenuEntryFooter(getResizableIconFromFile("graphics/icons/logout.png"), "Beenden", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doExit();
            }
        }));

        frame.getRibbon().setApplicationMenu(appmen);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Help button">
        frame.getRibbon().configureHelp(getResizableIconFromFile("graphics/big/help2.png"),
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Constants.DEBUG) {
                    GlobalOptions.getHelpBroker().setDisplayed(true);
                }
            }
        });
        // </editor-fold>
    }

    public static void addGeneralToolsTask(JRibbonFrame frame) {
        JRibbonBand attackToolsBand = new JRibbonBand("Angriff", getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand defendToolsBand = new JRibbonBand("Verteidigung", getResizableIconFromFile("graphics/big/def.png"));
        JRibbonBand infoToolBand = new JRibbonBand("Information", getResizableIconFromFile("graphics/big/information.png"));
        JRibbonBand miscToolsBand = new JRibbonBand("Sonstige", getResizableIconFromFile("graphics/big/box.png"));

        // <editor-fold defaultstate="collapsed" desc="attackToolsBand setup">
        JCommandButton attackPlanerToolButton = factoryButton("Taktikplaner", "graphics/big/att_auto.png", "Öffnet den Taktikplaner",
                "Der Taktikplaner erlaubt es, innerhalb kürzester Zeit eine Vielzahl von Angriffen oder Verteidungen auf eine beliebige Menge von Zielen zu planen. "
                + "Er ist dabei auf die grobe Planung unter Verwendung vieler Dörfern ausgelegt, gezielte AG-Angriffe oder Angriffe mit sehr wenigen Dörfern liefern "
                + "in der Regeln weniger gute oder keine Ergebnisse.", true);
        attackPlanerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        TacticsPlanerWizard.show();
                    }
                });
            }
        });
        JCommandButton manualAttackPlanerToolButton = factoryButton("Angriffsplaner (manuell)", "graphics/big/att_manual.png", "Öffnet den manuellen Angriffsplaner", "Der manuelle Angriffsplaner erlaubt es, detailliert kleine bis mittlere Mengen an Angriffen zu planen. Er orientiert sich dabei im Wesentlichen an anderen, Online verfügbaren Angriffsplanern.", true);
        manualAttackPlanerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setVisible(true);
                        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().requestFocus();
                    }
                });
            }
        });
        attackToolsBand.addCommandButton(attackPlanerToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(manualAttackPlanerToolButton, RibbonElementPriority.TOP);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="defendToolsBand setup">
        JCommandButton astarToolButton = factoryButton("A*Star", "graphics/big/astar.png", "Öffnet den Simulator", "A*Star ermöglicht es, Angriffssimulationen mit Hilfe des im Spiel verwendeten Kampfsystems durchzuführen. Zusätzliche Features erlauben es, die Anzahl von Offs zu bestimmen, die für die Vernichtung einer bestimmten Deff benötigt wird.", true);
        astarToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (!GlobalOptions.isOfflineMode()) {
                                DSWorkbenchSimulatorFrame.getSingleton().setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                                DSWorkbenchSimulatorFrame.getSingleton().showIntegratedVersion(DSWorkbenchSettingsDialog.getSingleton().getWebProxy(), GlobalOptions.getSelectedServer());
                                DSWorkbenchSimulatorFrame.getSingleton().requestFocus();
                            } else {
                                JOptionPaneHelper.showInformationBox(DSWorkbenchMainFrame.getSingleton(), "A*Star ist im Offline-Modus leider nicht verfügbar.", "Information");
                            }
                        } catch (Exception e) {
                            logger.warn("Error while showing Simulator", e);
                        }
                    }
                });

            }
        });
        JCommandButton sosAnalyzerToolButton = factoryButton("SOS Analyzer", "graphics/big/lifebelt.png", "Öffnet den SOS Analyzer", "Der SOS Analyzer dient der Analyse von SOS Anfragen, die man einfach aus dem Spiel in ein entsprechendes Textfeld kopiert. Dabei werden Ziele, Angreifer und Herkunftsdörfer ausgeschlüsselt, was einen schnellen Überblick über die Situation bietet. Weiterhin können diese Informationen in ein übersichtlicheres Format umformatiert und als BB-Code exportiert werden.", true);
        sosAnalyzerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSOSRequestAnalyzer.getSingleton().setVisible(true);
                        DSWorkbenchSOSRequestAnalyzer.getSingleton().requestFocus();
                    }
                });
            }
        });
        /*
         * JCommandButton retimeToolButton = factoryButton("Re-Timer", "graphics/big/retime.png", "Öffnet den Re-Timer", "Der Re-Timer
         * erlaubt es zu Einzelangriffen, die man einfach aus dem Spiel in ein entsprechendes Textfeld kopiert, mögliche re-time Angriffe zu
         * berechnen, welche die angreifenden Truppen bei der Rückkehr in ihr Herkunftsdorf vernichten können. Voraussetzung sind korrekt
         * importierte Truppeninformationen aus dem Spiel (siehe Hilfe) und ein gutes Timing.", true);
         * retimeToolButton.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { SwingUtilities.invokeLater(new Runnable() {
         *
         * @Override public void run() { DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
         * DSWorkbenchReTimerFrame.getSingleton().requestFocus(); } }); } });
         */
        defendToolsBand.addCommandButton(astarToolButton, RibbonElementPriority.MEDIUM);
        defendToolsBand.addCommandButton(sosAnalyzerToolButton, RibbonElementPriority.MEDIUM);
        // defendToolsBand.addCommandButton(retimeToolButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="infoToolBand setup">
        JCommandButton selectionToolButton = factoryButton("Auswahlübersicht", "graphics/icons/selection.png", "Öffnet die Auswahlübersicht", "Die Auswahlübersicht zeigt in Baum- oder Listenform alle aktuell ausgewählten Dörfer und erlaubt etwa, aus dieser Auswahl Dorflisten zu erstellen. Weiterhin können Dörfer aus der Auswahlübersicht per Drag&Drop beispielsweise in den automatisch Angriffsplaner als Herkunft- oder Zieldörfer eingefügt werden. Die Auswahl von Dörfern erfolgt über das entsprechende Kartenwerkzeug.", true);
        selectionToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSelectionFrame.getSingleton().setVisible(true);
                        DSWorkbenchSelectionFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton searchToolButton = factoryButton("Suche", "graphics/big/find.png", "Öffnet die Suche", "Dieses Werkzeug erlaubt die schnelle Suche nach Spielern oder Stämmen", true);
        searchToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSearchFrame.getSingleton().setVisible(true);
                        DSWorkbenchSearchFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton distanceToolButton = factoryButton("Entfernungsberechnung", "graphics/icons/measure.png", "Öffnet die Entfernungsberechnung", "Dieses Werkzeug erlaubt die Berechnung von Entfernungen zwischen allen eigenen Dörfern und einer beliebigen Anzahl anderer Dörfer. Dörfer können einzeln per Drag&Drop von der Hauptkarte in diese Ansicht gezogen werden.", true);
        distanceToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchDistanceFrame.getSingleton().setVisible(true);
                        DSWorkbenchDistanceFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        infoToolBand.addCommandButton(selectionToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(searchToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(distanceToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="miscToolsBand setup">
        JCommandButton resourceDistributorToolButton = factoryButton("Rohstoffverteiler", "graphics/big/resource_distrib.png", "Öffnet den Rohstoffverteiler", "Der Rohstoffverteiler erlaubt, basierend auf kopierten Informationen aus der Produktionsübersicht, die Rohstoffe in den eingefügten Dörfern auszugleiche oder den Rohstoffbestand in bestimmten Dörfern auf eine gewünschte Menge zu bringen. Er berechnet die dafür notwendigen Transporte, die im Anschluss direkt in den Browser übertragen und von dort abgeschickt werden können.", true);
        resourceDistributorToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ResourceDistributorWizard.show();
                    }
                });
            }
        });

        JCommandButton farmManagerButton = factoryButton("Farmmanager", "graphics/big/farm_tool.png", "Öffnet den Farmmanager", "Der Farmmanager erlaubt das einfache Farmen von Dörfern. Farmen können auf verschiedene Wege gesucht werden, der Farmmanager kümmert sich dann um deren Verwaltung. Zum eigentlichen Farmen muss man nur noch die entsprechenden Tabs im Browser öffnen lassen und den Farmangriff abschicken. Nach dem Farmlauf liest man den entsprechenden Bericht ein und verfügt sofort wieder über aktuelle Informationen.", true);
        farmManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchFarmManager.getSingleton().setVisible(true);
                        DSWorkbenchFarmManager.getSingleton().requestFocus();
                    }
                });
            }
        });

        JCommandButton mapshotToolButton = factoryButton("Screenshot erstellen", "graphics/big/camera.png", "Erstellt einen Screenshot der Hauptkarte", "Dieses Werkzeug erlaubt es, einen Screenshot der aktuellen Ansicht der Hauptkarte zu erstellen. Screenshots können im Anschluss auf der Festplatte oder dem DS Workbench Server gespeichert werden, um sie anderen Spielern zugänglich zu machen. Die Speicherung auf dem DS Workbench Server ist allerdings nur bis zu einer bestimmten Bildgröße möglich, die resultierende Bildgröße hängt von der Fenstergröße und dem gewählten Grafikpaket ab.", true);
        mapshotToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().planMapshot();
            }
        });
        JCommandButton runtimeToolButton = factoryButton("Laufzeiten", "graphics/big/speed.png", "Zeigt die Laufzeitübersicht", "Eine einfache Liste, welche die Laufzeiten der auf dieser Welt verfügbaren Einheiten anzeigt.", true);
        runtimeToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        UnitOrderBuilder.showUnitOrder(null, null);
                    }
                });

            }
        });
        JCommandButton clockToolButton = factoryButton("Uhr", "graphics/big/clock.png", "Zeigt die Uhr", "Eine Uhr, welche deine aktuelle Systemzeit anzeigt. Sie kann das Timen von Angriffen erleichern, muss aber nicht zwingend mit der Zeit der DS-Server übereinstimmen.", true);
        clockToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ClockFrame.getSingleton().setVisible(true);
                        ClockFrame.getSingleton().requestFocus();
                    }
                });

            }
        });
        miscToolsBand.addCommandButton(resourceDistributorToolButton, RibbonElementPriority.TOP);
        miscToolsBand.addCommandButton(farmManagerButton, RibbonElementPriority.TOP);
        miscToolsBand.addCommandButton(mapshotToolButton, RibbonElementPriority.MEDIUM);
        miscToolsBand.addCommandButton(runtimeToolButton, RibbonElementPriority.MEDIUM);
        miscToolsBand.addCommandButton(clockToolButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>

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
        JRibbonBand infoToolBand = new JRibbonBand("Dörfer", getResizableIconFromFile("graphics/icons/map_tools.png"));
        JRibbonBand drawToolsBand = new JRibbonBand("Zeichnen", getResizableIconFromFile("graphics/big/palette2.png"));
        JRibbonBand minimapToolsBand = new JRibbonBand("Minimap", getResizableIconFromFile("graphics/icons/minimap.png"));

        // <editor-fold defaultstate="collapsed" desc="baseToolsBand setup">
        JCommandButton noToolButton = factoryButton("Werkzeug abwählen", "graphics/cursors/default.png", "Deaktivierung des aktuellen Karten- und Minimapwerkzeugs", "Deaktiviert das aktuelle Karten- oder Minimapwerkzeug und erlaubt es die Hauptkarte wieder per Drag&Drop zu verschieben", true);
        noToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
            }
        });
        baseToolsBand.addCommandButton(noToolButton, RibbonElementPriority.TOP);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="attackToolsBand setup">
        JCommandButton attackRamToolButton = factoryButton(null, "graphics/big/ram.png", "Erstellt einen Einzelangriff mit Rammenlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackRamToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
            }
        });
        JCommandButton attackSnobToolButton = factoryButton(null, "graphics/big/snob.png", "Erstellt einen Einzelangriff mit AG-Laufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackSnobToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
            }
        });
        JCommandButton attackSpyToolButton = factoryButton(null, "graphics/big/spy.png", "Erstellt einen Einzelangriff mit Späherlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackSpyToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
            }
        });
        JCommandButton attackAxeToolButton = factoryButton(null, "graphics/big/axe.png", "Erstellt einen Einzelangriff mit Axtlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackAxeToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
            }
        });
        JCommandButton attackLightToolButton = factoryButton(null, "graphics/big/light.png", "Erstellt einen Einzelangriff mit LKav-Laufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackLightToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
            }
        });
        JCommandButton attackHeavyToolButton = factoryButton(null, "graphics/big/heavy.png", "Erstellt einen Einzelangriff mit SKav-Laufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackHeavyToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
            }
        });
        JCommandButton attackSwordToolButton = factoryButton(null, "graphics/big/sword.png", "Erstellt einen Einzelangriff mit Schwertlaufzeit", "Mit gewähltem Werkzeug auf das Herkunftsdorf klicken und mit gedrückter linker Maustaste eine Linie zum Zieldorf ziehen", true);
        attackSwordToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
            }
        });
        attackToolsBand.addCommandButton(attackRamToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(attackSnobToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(attackSpyToolButton, RibbonElementPriority.MEDIUM);
        attackToolsBand.addCommandButton(attackAxeToolButton, RibbonElementPriority.MEDIUM);
        attackToolsBand.addCommandButton(attackLightToolButton, RibbonElementPriority.LOW);
        attackToolsBand.addCommandButton(attackHeavyToolButton, RibbonElementPriority.LOW);
        attackToolsBand.addCommandButton(attackSwordToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="defendToolsBand setup">
        JCommandButton supportToolButton = factoryButton("Unterstützungen bestimmen", "graphics/big/support.png", "Öffnet das Unterstützungswerkzeug", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, um das Unterstützungswerkzeug zu öffnen. Darin können Gruppen aus denen Unterstützungen geschickt werden sollen gewählt werden, um anschließend mögliche Unterstützungen zu einem wählbaren, maximalen Zeitpunkt zu bestimmen.", true);
        supportToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SUPPORT);
            }
        });
        defendToolsBand.addCommandButton(supportToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="infoToolBand setup">
        JCommandButton selectToolButton = factoryButton("Dörfer wählen", "graphics/big/selection.png", "Dörfer auf der Hauptkarte auswählen", "Mit gewähltem Werkzeug und gedrückter linker Maustaste ein Auswahlrechteck auf der Hauptkarte aufziehen, um Dörfer in die Auswahlübersicht zu übertragen. Einzeldörfer oder unabhängige Bereiche können mit gedrückter Shift-Taste gewählt werden.", true);
        selectToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
            }
        });
        JCommandButton markToolButton = factoryButton("Spieler/Stämme markieren", "graphics/big/brush3.png", "Markiert Spieler/Stämme auf der Hauptkarte", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, den Besitzer und/oder den Stamm des Besitzers eine Farbmarkierung auf der Hauptkarte zuzuweisen", true);
        markToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
            }
        });
        JCommandButton noteToolButton = factoryButton("Notiz erstellen", "graphics/big/notebook_add.png", "Erstellt eine neue Notiz", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, um für dieses Dorf eine neue Notiz im Notizblock zu erstellen", true);
        noteToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_NOTE);
            }
        });
        JCommandButton distanceToolButton = factoryButton("Laufzeit bestimmen", "graphics/big/tape_measure1.png", "Bestimmung der Laufzeiten zwischen zwei Dörfern", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken und mit gedrückter linker Maustaste eine Linie zu einem anderen Dorf ziehen. Bei aktiviertem Kartenpopup werden statt der Truppenstärke im Zieldorf die Laufzeiten der einzelnen Einheiten angezeigt.", true);
        distanceToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
            }
        });
        JCommandButton radarToolButton = factoryButton("Laufzeitradar", "graphics/icons/radar.png", "Zeigt Laufzeitradien um ein Dorf für die verschiedenen Einheiten an", "Mit gewähltem Werkzeug mit der Maus über ein Dorf fahren, um die Laufzeitradien zu sehen. Ein Linksklick auf das Dorf setzt die Laufzeitradien für das aktuelle Dorf fest und erlaubt, die Maus über ein weiteres Dorf zu bewegen. Ein Klick auf ein leeres Kartenfeld löscht die festgestellten Laufzeitradien.", true);
        radarToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_RADAR);
            }
        });
        JCommandButton tagToolButton = factoryButton("Gruppe zuweisen", "graphics/icons/tag.png", "Weist einem Dorf eine Gruppe zu", "Mit gewähltem Werkzeug auf ein Dorf auf der Hauptkarte klicken, um die Gruppenauswahl zu öffnen und dem Dorf eine vorhandene oder eine neue Gruppen zuzuweisen", true);
        tagToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
            }
        });

        // <editor-fold defaultstate="collapsed" desc="church button setup">
        JCommandButton createChurchToolButton = factoryButton(null, "graphics/big/Church1.png", "Kirche in einem Dorf auf der Hauptkarte erstellen", "Dieses Werkzeug ist nur auf Kirchenwelten aktiv", true);
        JCommandButton createChurch1ToolButton = factoryButton(null, "graphics/big/Church1.png", "Kirche Stufe 1 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um eine Kirche Stufe 1 im Dorf zu platzieren", true);
        createChurch1ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_1);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Kirchenwelten verfügbar");
                }
            }
        });
        JCommandButton createChurch2ToolButton = factoryButton(null, "graphics/big/Church2.png", "Kirche Stufe 2 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um eine Kirche Stufe 2 im Dorf zu platzieren", true);
        createChurch2ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_2);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Kirchenwelten verfügbar");
                }
            }
        });
        JCommandButton createChurch3ToolButton = factoryButton(null, "graphics/big/Church3.png", "Kirche Stufe 3 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um eine Kirche Stufe 3 im Dorf zu platzieren", true);
        createChurch3ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_3);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Kirchenwelten verfügbar");
                }
            }
        });
        JCommandButton removeChurchToolButton = factoryButton(null, "graphics/big/NoChurch.png", "Kirche löschen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um die dortige Kirche zu löschen", true);
        removeChurchToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_REMOVE_CHURCH);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Kirchenwelten verfügbar");
                }
            }
        });

        JCommandButtonPanel cbpChurch = new JCommandButtonPanel(CommandButtonDisplayState.FIT_TO_ICON);
        cbpChurch.setLayoutKind(JCommandButtonPanel.LayoutKind.ROW_FILL);
        cbpChurch.addButtonGroup("Stufe");
        cbpChurch.addButtonToLastGroup(createChurch1ToolButton);
        cbpChurch.addButtonToLastGroup(createChurch2ToolButton);
        cbpChurch.addButtonToLastGroup(createChurch3ToolButton);
        cbpChurch.addButtonToLastGroup(removeChurchToolButton);
        final JCommandPopupMenu popupMenuChurch = new JCommandPopupMenu(
                cbpChurch, 1, 4);

        createChurchToolButton.setPopupCallback(new PopupPanelCallback() {
            @Override
            public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                return popupMenuChurch;
            }
        });
        createChurchToolButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="watchtower button setup">
        JCommandButton createWatchtowerToolButton = factoryButton(null, "graphics/big/Watchtower1.png", "Wachturm in einem Dorf auf der Hauptkarte erstellen", "Dieses Werkzeug ist nur auf Wachturmwelten aktiv", true);
        JCommandButton createWatchtower1ToolButton = factoryButton(null, "graphics/big/Watchtower1.png", "Wachturm Stufe 1 erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um einen Wachturm Stufe 1 im Dorf zu platzieren", true);
        createWatchtower1ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_WATCHTOWER_1);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Wachturmwelten verfügbar");
                }
            }
        });
        JCommandButton createWatchtowerInToolButton = factoryButton(null, "graphics/big/WatchtowerIn.png", "Wachturm erstellen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um einen Wachturm im Dorf zu platzieren. Die Stufe wird über ein Eingabefeld festgelegt", true);
        createWatchtowerInToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_WATCHTOWER_INPUT);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Wachturmwelten verfügbar");
                }
            }
        });
        JCommandButton removeWatchtowerToolButton = factoryButton(null, "graphics/big/NoWatchtower.png", "Wachturm löschen", "Mit gewähltem Werkzeug auf ein Dorf der Hauptkarte klicken, um den dortigen Wachturm zu löschen", true);
        removeWatchtowerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_REMOVE_WATCHTOWER);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo("Dieses Werkzeug ist nur auf Wachturmwelten verfügbar");
                }
            }
        });

        JCommandButtonPanel wbp = new JCommandButtonPanel(CommandButtonDisplayState.FIT_TO_ICON);
        wbp.setLayoutKind(JCommandButtonPanel.LayoutKind.ROW_FILL);
        wbp.addButtonGroup("Stufe");
        wbp.addButtonToLastGroup(createWatchtower1ToolButton);
        wbp.addButtonToLastGroup(createWatchtowerInToolButton);
        wbp.addButtonToLastGroup(removeWatchtowerToolButton);
        final JCommandPopupMenu watchtoerPopupMenu = new JCommandPopupMenu(
                wbp, 1, 3);

        createWatchtowerToolButton.setPopupCallback(new PopupPanelCallback() {
            @Override
            public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                return watchtoerPopupMenu;
            }
        });
        createWatchtowerToolButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        // </editor-fold>

        infoToolBand.addCommandButton(selectToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(markToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(noteToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(distanceToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(radarToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(tagToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(createChurchToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(createWatchtowerToolButton, RibbonElementPriority.LOW);

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="drawToolsBand setup">
        JCommandButton drawRectToolButton = factoryButton("Rechteck zeichnen", "graphics/icons/draw_rect.png", "Werkzeug zum Zeichnen von Rechtecken auf der Hauptkarte", null, true);
        drawRectToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_RECT);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Rectangle.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawCircleToolButton = factoryButton("Kreis zeichnen", "graphics/icons/draw_circle.png", "Werkzeug zum Zeichnen von Kreisen auf der Hauptkarte", null, true);
        drawCircleToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_CIRCLE);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Circle.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawLineToolButton = factoryButton("Linie zeichnen", "graphics/icons/draw_line.png", "Werkzeug zum Zeichnen von Linien auf der Hauptkarte", null, true);
        drawLineToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_LINE);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Line.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawArrowToolButton = factoryButton("Pfeil zeichnen", "graphics/icons/draw_arrow.png", "Werkzeug zum Zeichnen von Pfeilen auf der Hauptkarte", null, true);
        drawArrowToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_ARROW);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Arrow.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawFreehandToolButton = factoryButton("Freihand zeichnen", "graphics/icons/draw_freeform.png", "Werkzeug zum Erstellen von Freihandzeichnungen auf der Hauptkarte", null, true);
        drawFreehandToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_FREEFORM);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.FreeForm.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawTextToolButton = factoryButton("Text zeichnen", "graphics/icons/draw_text.png", "Werkzeug zum Zeichnen von Texten auf der Hauptkarte", null, true);
        drawTextToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_TEXT);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Text.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        drawToolsBand.addCommandButton(drawRectToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawCircleToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawLineToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawArrowToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawFreehandToolButton, RibbonElementPriority.LOW);
        drawToolsBand.addCommandButton(drawTextToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="minimapToolsBand setup">
        JCommandButton minimapMoveToolButton = factoryButton("Ausschnitt bewegen", "graphics/icons/move.png", "Erlaubt es den sichtbaren Kartenausschnitt auf der Minimap zu verschieben", "Mit gewähltem Werkzeug und gedrückter linker Maustaste den Ausschnitt verschieben", true);
        minimapMoveToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
            }
        });
        JCommandButton minimapZoomToolButton = factoryButton("Minimap vergößern", "graphics/big/view.png", "Vergrößert einen Bereich von 300x300 Dörfer auf der Minimap", "Mit der Maus über die Minimap fahren, der Mauszeiger bildet die Mitte des gezeigten Ausschnitts", true);
        minimapZoomToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
            }
        });
        JCommandButton minimapShotToolButton = factoryButton("Screenshot erstellen", "graphics/big/camera.png", "Erstellt einen Screenshot eines wählbaren Bereiches der Minimap", "Mit gewähltem Werkzeug ein Rechteck auf der Minimap aufziehen, welches den gewünschten Bereich einrahmt", true);
        minimapShotToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
            }
        });
        minimapToolsBand.addCommandButton(minimapMoveToolButton, RibbonElementPriority.TOP);
        minimapToolsBand.addCommandButton(minimapZoomToolButton, RibbonElementPriority.LOW);
        minimapToolsBand.addCommandButton(minimapShotToolButton, RibbonElementPriority.LOW);
        // </editor-fold>

        baseToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(baseToolsBand.getControlPanel())));

        attackToolsBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesRestrictive(attackToolsBand));

        drawToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(drawToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(drawToolsBand.getControlPanel())));

        minimapToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(minimapToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(minimapToolsBand.getControlPanel())));

        defendToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(defendToolsBand.getControlPanel())));

        infoToolBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoToolBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoToolBand.getControlPanel())));

        RibbonTask task1 = new RibbonTask("Kartenwerkzeuge", baseToolsBand, attackToolsBand, defendToolsBand, infoToolBand, drawToolsBand, minimapToolsBand);
        frame.getRibbon().addTask(task1);
    }

    public static void addViewTask(JRibbonFrame frame) {
        JRibbonBand attackViewBand = new JRibbonBand("Angriff", getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand ingameInfoViewBand = new JRibbonBand("Importierte Daten", getResizableIconFromFile("graphics/big/clipboard_next.png"));
        JRibbonBand infoViewBand = new JRibbonBand("Informationen", getResizableIconFromFile("graphics/big/information.png"));

        // <editor-fold defaultstate="collapsed" desc="attackViewBand setup">
        JCommandButton attackViewButton = factoryButton("Befehle", "graphics/big/axe_sword.png", "Öffnet die Befehlsübersicht",
                "Die Befehlssübersicht erlaubt es, geplante Angriffe und Verteidigungen zu verwalten, zu modifizieren, zu exportieren (z.B. als BB-Codes) "
                + "und in den Browser zu übertragen. Befehle müssen vorher durch eins der verfügbaren Werkzeuge automatisch oder manuell erstellt werden", true);
        attackViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchAttackFrame.getSingleton().setVisible(true);
                        DSWorkbenchAttackFrame.getSingleton().requestFocus();
                    }
                });
            }
        });

        JCommandButton markerViewButton = factoryButton("Markierungen", "graphics/icons/mark.png", "Öffnet die Markierungsübersicht", "Die Markierungsübersicht erlaubt es, Spieler und Stammesmarkierungen zu verwalten und zu modifizieren. Markierungen müssen vorher über das entsprechende Kartenwerkzeug erstellt werden", true);
        markerViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchMarkerFrame.getSingleton().setVisible(true);
                        DSWorkbenchMarkerFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton formsViewButton = factoryButton("Zeichnungen", "graphics/big/palette2.png", "Öffnet die Zeichnungsübersicht", "Die Zeichnungsübersicht zeigt alle auf der Hauptkarte eingetragenen Zeichnungen an und erlaubt es, diese nachträglich zu verändern.", true);
        formsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchFormFrame.getSingleton().setVisible(true);
                        DSWorkbenchFormFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton churchViewButton = factoryButton("Kirchen", "graphics/big/Church1.png", "Öffnet die Kirchenübersicht", "Die Kirchenübersicht zeigt alle in DS Workbench eingetragenen Kirchen an. Diese Ansicht ist nur auf Kirchenwelten verfügbar.", true);
        churchViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            DSWorkbenchChurchFrame.getSingleton().setVisible(true);
                            DSWorkbenchChurchFrame.getSingleton().requestFocus();
                        }
                    });
                } else {
                    JOptionPaneHelper.showInformationBox(DSWorkbenchMainFrame.getSingleton(), "Kirchen sind auf dieser Welt nicht aktiv.", "Information");
                }
            }
        });
        JCommandButton watchtowerViewButton = factoryButton("Wachtürme", "graphics/big/Watchtower1.png", "Öffnet die Wachturmübersicht", "Die Wachturmübersicht zeigt alle in DS Workbench eingetragenen Wachtürme an. Diese Ansicht ist nur auf Wachturmwelten verfügbar.", true);
        watchtowerViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            DSWorkbenchWatchtowerFrame.getSingleton().setVisible(true);
                            DSWorkbenchWatchtowerFrame.getSingleton().requestFocus();
                        }
                    });
                } else {
                    JOptionPaneHelper.showInformationBox(DSWorkbenchMainFrame.getSingleton(), "Wachtürme sind auf dieser Welt nicht aktiv.", "Information");
                }
            }
        });
        //   attackViewBand.startGroup();
        attackViewBand.addCommandButton(attackViewButton, RibbonElementPriority.TOP);
        attackViewBand.addCommandButton(markerViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(formsViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(churchViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(watchtowerViewButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="ingameInfoViewBand setup">
        JCommandButton tagsViewButton = factoryButton("Gruppen", "graphics/icons/tag.png", "Öffnet die Gruppenübersicht", "Die Gruppenübersicht erlaubt es, vorher aus dem Spiel importierte Gruppen zu verwalten und zu neuen Gruppen zu kombinieren. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        tagsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchTagFrame.getSingleton().setVisible(true);
                        DSWorkbenchTagFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton troopsViewButton = factoryButton("Truppen", "graphics/big/troops.png", "Öffnet die Truppenübersicht", "Die Truppenübersicht erlaubt es, vorher aus dem Spiel importierte Truppeninformationen zu verwalten. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        troopsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchTroopsFrame.getSingleton().setVisible(true);
                        DSWorkbenchTroopsFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton reportsViewButton = factoryButton("Berichte", "graphics/big/report.png", "Öffnet die Berichtdatenbank", "Die Berichtdatenbank erlaubt es, vorher aus dem Spiel importierte Berichte zu verwalten. Weitere Informationen findest du in der Hilfe (F1) im Abschnitt 'Import von Spielinformationen'.", true);
        reportsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchReportFrame.getSingleton().setVisible(true);
                        DSWorkbenchReportFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        ingameInfoViewBand.addCommandButton(tagsViewButton, RibbonElementPriority.MEDIUM);
        ingameInfoViewBand.addCommandButton(troopsViewButton, RibbonElementPriority.MEDIUM);
        ingameInfoViewBand.addCommandButton(reportsViewButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="infoViewBand setup">
        JCommandButton notesViewButton = factoryButton("Notizblock", "graphics/big/notebook.png", "Öffnet den Notizblock", "Der Notizblock erlaubt es, Notizen zu einelnen oder mehreren Dörfern zu verwalten. Dabei stehen die gängigen BB-Codes zu Verfügung. Erstellte Notizen tauchen, sofern die Ebene 'Notizen' sichtbar ist, auf der Hauptkarte als Symbole und im Kartenpopup mit dem zugehörigen Notiztext auf. Das Erstellen von Notizen kann entweder über das entsprechende Kartenwerkzeug oder direkt im Notizblock geschehen.", true);
        notesViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchNotepad.getSingleton().setVisible(true);
                        DSWorkbenchNotepad.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton conquerViewButton = factoryButton("Eroberungen", "graphics/big/snob.png", "Öffnet die Eroberungsübersicht", "Die Eroberungsübersicht zeigt alle kürzlich durchgeführten Eroberungen der aktuellen Welt an und erlaubt es, diese nach vielen Kriterien zu filtern. Eroberungen werden bei laufendem DS Workbench regelmäßig vom DS-Server gelesen, die aktuelle Zustimmung von eingelesenen Eroberungen wird entsprechend den Servereinstellungen ausgerechnet.", true);
        conquerViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchConquersFrame.getSingleton().setVisible(true);
                        DSWorkbenchConquersFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton rankViewButton = factoryButton("Ranglisten", "graphics/big/medal.png", "Öffnet die Rangliste", "Zeigt Ranglisten von Spielern und Stämmen mit allen verfügbaren Informationen (Punkte, Dörfer, Kills usw.)", true);
        rankViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchRankFrame.getSingleton().setVisible(true);
                        DSWorkbenchRankFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton statsViewButton = factoryButton("Statistiken", "graphics/icons/ally_chart.png", "Öffnet die Statistikübersicht", "Die Statistikübersicht erlaubt es, Statistiken über Spieler und Stämme für beliebige Zeiträume zu führen. Um einen Spieler oder einen Stamm in die Statistiken aufzunehmen, klicke mit der rechten Maustaste auf ein Dorf auf der Hauptkarte und wähle im entsprechenden Untermenü des Kontextmenüs 'Spieler überwachen' oder 'Stamm überwachen'", true);
        statsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchStatsFrame.getSingleton().setVisible(true);
                        DSWorkbenchStatsFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        infoViewBand.addCommandButton(notesViewButton, RibbonElementPriority.TOP);
        infoViewBand.addCommandButton(conquerViewButton, RibbonElementPriority.MEDIUM);
        infoViewBand.addCommandButton(rankViewButton, RibbonElementPriority.MEDIUM);
        infoViewBand.addCommandButton(statsViewButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>

        attackViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(attackViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.Mid2Low(attackViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(attackViewBand.getControlPanel())));
        ingameInfoViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(ingameInfoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.Mirror(ingameInfoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.High2Low(ingameInfoViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(ingameInfoViewBand.getControlPanel())));
        infoViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.Mid2Low(infoViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoViewBand.getControlPanel())));
        RibbonTask task2 = new RibbonTask("Ansicht", attackViewBand, ingameInfoViewBand, infoViewBand);

        //  frame.getRibbon().addTask(task1);
        frame.getRibbon().addTask(task2);
    }

    public static void addMiscTask(JRibbonFrame frame) {
        JRibbonBand miscBand = new JRibbonBand("Sonstiges", getResizableIconFromResource("/res/128x128/help.png"));

        // <editor-fold defaultstate="collapsed" desc="miscBand setup">
        JCommandButton helpButton = factoryButton("Hilfe", "/res/128x128/help.png", "Die integrierte Hilfe", "DS Workbench bietet eine sehr ausführliche Hilfe, die alle Funktionen detailiert beschreibt und so Einblicke in bisher vielleicht unentdeckte Features offenbart. Sie ist jederzeit über F1, diesen Button oder den Hilfebutton in der rechten oberen Ecke des Hauptfensters zu erreichen.", true);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalOptions.getHelpBroker().setDisplayed(true);
            }
        });
        JCommandButton facebookButton = factoryButton("Facebook", "/res/128x128/facebook.png", "DS Workbench @ Facebook", "Wie viele andere Personen und Produkte verfügt DS Workbench über eine eigene Facebook Seite. Wenn dir DS Workbench gefällt hast du so die Möglichkeit, jederzeit über neue Entwicklungen auf dem Laufenden zu sein.", true);
        facebookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserInterface.openPage("http://www.facebook.com/pages/DS-Workbench/182068775185568");
            }
        });
        JCommandButton donateButton = factoryButton("Spenden", "/res/ui/paypal.gif", "Für DS Workbench spenden", "Natürlich ist DS Workbench komplett kostenlos und wird das auch weiterhin bleiben. Dennoch sind kleine Spenden als Dank für die Arbeit die seit mehreren Jahren in DS Workbench fließt immer gern gesehen. Für alle Spenden möchte ich mich an dieser Stelle herzlich bedanken.", true);
        donateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserInterface.openPage("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=4434173");
            }
        });
        JCommandButton aboutButton = factoryButton("About", "/res/ui/about.png", "Über DS Workbench", "Informationen über die Version von DS Workbench die ihr gerade verwendest.", true);
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().showAboutDialog();
            }
        });
        miscBand.addCommandButton(helpButton, RibbonElementPriority.TOP);
        miscBand.addCommandButton(facebookButton, RibbonElementPriority.TOP);
        miscBand.addCommandButton(donateButton, RibbonElementPriority.TOP);
        miscBand.addCommandButton(aboutButton, RibbonElementPriority.TOP);
        // </editor-fold>

        miscBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(miscBand.getControlPanel()),
                new IconRibbonBandResizePolicy(miscBand.getControlPanel())
        ));
        RibbonTask task1 = new RibbonTask("Sonstiges", miscBand);
        frame.getRibbon().addTask(task1);
    }

    private static JCommandButton factoryButton(String pLabel, String pIconPath, String pTooltipText, String pSecondaryTooltipText, boolean pShowLabel) {
        JCommandButton button = null;

        if (!new File(pIconPath).exists()) {
            button = new JCommandButton((pShowLabel) ? pLabel : null, getResizableIconFromResource(pIconPath));
        } else {
            button = new JCommandButton((pShowLabel) ? pLabel : null, getResizableIconFromFile(pIconPath));
        }

        if (pTooltipText != null) {
            RichTooltip rt = new RichTooltip((pLabel != null) ? pLabel : "Info", pTooltipText);
            if (pSecondaryTooltipText != null) {
                rt.addDescriptionSection(pSecondaryTooltipText);
            }
            if (new File(pIconPath).exists()) {
                try {
                    rt.setMainImage(ImageIO.read(new File(pIconPath)));
                } catch (Exception ignored) {
                }
            } else {
                try {
                    rt.setMainImage(ImageIO.read(RibbonConfigurator.class.getResource(pIconPath)));
                } catch (Exception ignored) {
                }
            }
            button.setActionRichTooltip(rt);
        }
        return button;
    }

    private static ResizableIcon getResizableIconFromResource(String resource) {
        try {
            return ImageWrapperResizableIcon.getIcon(DSWorkbenchMainFrame.class.getResource(resource), new Dimension(48, 48));
        } catch (Exception e) {
            return new EmptyResizableIcon(18);
        }
    }

    private static ResizableIcon getResizableIconFromFile(String resource) {
        try {
            return ImageWrapperResizableIcon.getIcon(new File(resource).toURI().toURL(), new Dimension(48, 48));
        } catch (Exception e) {
            return new EmptyResizableIcon(18);
        }
    }
}
