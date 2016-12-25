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
package de.tor.tribes.ui.windows;

import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.ui.views.DSWorkbenchDoItYourselfAttackPlaner;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.ui.views.DSWorkbenchDistanceFrame;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.ui.views.DSWorkbenchFormFrame;
import de.tor.tribes.ui.views.DSWorkbenchSOSRequestAnalyzer;
import de.tor.tribes.ui.views.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.views.DSWorkbenchReportFrame;
import de.tor.tribes.ui.views.DSWorkbenchConquersFrame;
import de.tor.tribes.ui.views.DSWorkbenchChurchFrame;
import de.tor.tribes.ui.views.DSWorkbenchSelectionFrame;
import de.tor.tribes.ui.views.DSWorkbenchRankFrame;
import de.tor.tribes.ui.views.DSWorkbenchSearchFrame;
import de.tor.tribes.ui.views.DSWorkbenchStatsFrame;
import de.tor.tribes.ui.views.DSWorkbenchTagFrame;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.*;
import de.tor.tribes.ui.components.JOutlookBar;
import de.tor.tribes.ui.components.WelcomePanel;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.ClipboardWatch;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.interfaces.DSWorkbenchFrameListener;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.interfaces.ToolChangeListener;
import de.tor.tribes.util.tag.TagManager;
import java.awt.AWTEvent;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.ui.views.*;
import de.tor.tribes.ui.wiz.red.ResourceDistributorWizard;
import de.tor.tribes.ui.wiz.tap.TacticsPlanerWizard;
import de.tor.tribes.util.*;
import de.tor.tribes.util.interfaces.MapShotListener;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.dist.DistanceManager;
import de.tor.tribes.util.dsreal.DSRealManager;
import de.tor.tribes.util.farm.FarmManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.note.NoteManager;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.roi.ROIManager;
import de.tor.tribes.util.stat.StatManager;
import java.io.File;
import javax.swing.JFileChooser;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.painter.MattePainter;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

/**
 * @TODO add some simple runtime and retime calculator to replace excel
 *
 * @author Charon
 */
public class DSWorkbenchMainFrame extends JRibbonFrame implements
        MapPanelListener,
        ToolChangeListener,
        DSWorkbenchFrameListener,
        MapShotListener {

  private static Logger logger = Logger.getLogger("MainApp");
  private double dCenterX = 500.0;
  private double dCenterY = 500.0;
  private double dZoomFactor = 1.0;
  //  private TribeTribeAttackFrame mTribeTribeAttackFrame = null;
  private AboutDialog mAbout = null;
  private static DSWorkbenchMainFrame SINGLETON = null;
  private boolean initialized = false;
  private boolean putOnline = false;
  private MouseGestures mMouseGestures = new MouseGestures();
  private boolean bWatchClipboard = true;
  private final JFileChooser chooser = new JFileChooser();
  private NotificationHideThread mNotificationHideThread = null;
  private boolean glasspaneVisible = true;

  public static synchronized DSWorkbenchMainFrame getSingleton() {
    if (SINGLETON == null) {
      SINGLETON = new DSWorkbenchMainFrame();
    }
    return SINGLETON;
  }

  /**
   * Creates new form MapFrame
   */
  DSWorkbenchMainFrame() {
    initComponents();
    setAlwaysOnTop(false);
    if (!GlobalOptions.isMinimal()) {
      setTitle("DS Workbench " + Constants.VERSION + Constants.VERSION_ADDITION);
    } else {
      setTitle("DS Workbench Mini " + Constants.VERSION + Constants.VERSION_ADDITION);
    }

    jExportDialog.pack();
    jAddROIDialog.pack();

    JOutlookBar outlookBar = new JOutlookBar();
    outlookBar.addBar("Navigation", jNavigationPanel);
    outlookBar.addBar("Information", jInformationPanel);
    outlookBar.addBar("Karte", jMapPanel);
    outlookBar.addBar("ROI", jROIPanel);
    outlookBar.setVisibleBar(1);
    jSettingsScrollPane.setViewportView(outlookBar);

    mAbout = new AboutDialog(this, true);
    mAbout.pack();
    chooser.setDialogTitle("Speichern unter...");
    chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

      @Override
      public boolean accept(File f) {
          return (f != null) && (f.isDirectory() || f.getName().endsWith(".png"));
      }

      @Override
      public String getDescription() {
        return "PNG Image (*.png)";
      }
    });

    chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

      @Override
      public boolean accept(File f) {
          return (f != null) && (f.isDirectory() || f.getName().endsWith(".jpeg"));
      }

      @Override
      public String getDescription() {
        return "JPEG Image (*.jpeg)";
      }
    });
    // <editor-fold defaultstate="collapsed" desc=" Schedule Backup">
    new Timer("BackupTimer", true).schedule(new BackupTask(), 60 * 10000, 60 * 10000);
        // </editor-fold>

    //give focus to map panel if mouse enters map
    jMapPanelHolder.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent e) {
        jMapPanelHolder.requestFocusInWindow();
      }
    });

    getContentPane().setBackground(Constants.DS_BACK);
    pack();
    capabilityInfoPanel1.addActionListener(MapPanel.getSingleton());

    // <editor-fold defaultstate="collapsed" desc=" Add global KeyListener ">
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

      @Override
      public void eventDispatched(AWTEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED) {
          KeyEvent e = (KeyEvent) event;
          if (DSWorkbenchMainFrame.getSingleton().isActive()) {
            //move shortcuts
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
              scroll(0.0, 2.0);
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
              scroll(0.0, -2.0);
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
              scroll(-2.0, 0.0);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
              scroll(2.0, 0.0);
            } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //shot minimap tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
            } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //attack axe tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
            } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //attack ram tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
            } else if ((e.getKeyCode() == KeyEvent.VK_4) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //attack snob tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
            } else if ((e.getKeyCode() == KeyEvent.VK_5) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //attack sword tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
            } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //attack light tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
            } else if ((e.getKeyCode() == KeyEvent.VK_7) && e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
              //attack heavy tool shortcut
              MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
            } else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isControlDown() && !e.isAltDown()) {
              //search frame shortcut
              DSWorkbenchSearchFrame.getSingleton().setVisible(!DSWorkbenchSearchFrame.getSingleton().isVisible());
            }
          }

          //misc shortcuts
          if ((e.getKeyCode() == KeyEvent.VK_0) && e.isAltDown()) {
            //no tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
          } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //measure tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
          } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //mark tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
          } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //tag tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
          } else if ((e.getKeyCode() == KeyEvent.VK_4) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //attack ingame tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SUPPORT);
          } else if ((e.getKeyCode() == KeyEvent.VK_5) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //attack ingame tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
          } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //attack ingame tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_RADAR);
          } else if ((e.getKeyCode() == KeyEvent.VK_7) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //attack ingame tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_INGAME);
          } else if ((e.getKeyCode() == KeyEvent.VK_8) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            //res ingame tool shortcut
            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SEND_RES_INGAME);
          } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
            //move minimap tool shortcut
            MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
          } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
            //zoom minimap tool shortcut
            MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
          } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
            //shot minimap tool shortcut
            MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
          } else if ((e.getKeyCode() == KeyEvent.VK_T) && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
            //search time shortcut
            ClockFrame.getSingleton().setVisible(!ClockFrame.getSingleton().isVisible());
          } else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isAltDown() && !e.isShiftDown() && !e.isControlDown()) {
            planMapshot();
          } else if (e.getKeyCode() == KeyEvent.VK_F2) {
            DSWorkbenchAttackFrame.getSingleton().setVisible(!DSWorkbenchAttackFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F3) {
            DSWorkbenchMarkerFrame.getSingleton().setVisible(!DSWorkbenchMarkerFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F4) {
            DSWorkbenchTroopsFrame.getSingleton().setVisible(!DSWorkbenchTroopsFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F5) {
            DSWorkbenchRankFrame.getSingleton().setVisible(!DSWorkbenchRankFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F6) {
            DSWorkbenchFormFrame.getSingleton().setVisible(!DSWorkbenchFormFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F7) {
            if (ServerSettings.getSingleton().isChurch()) {
              DSWorkbenchChurchFrame.getSingleton().setVisible(!DSWorkbenchChurchFrame.getSingleton().isVisible());
            }
          } else if (e.getKeyCode() == KeyEvent.VK_F8) {
            DSWorkbenchConquersFrame.getSingleton().setVisible(!DSWorkbenchConquersFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F9) {
            DSWorkbenchNotepad.getSingleton().setVisible(!DSWorkbenchNotepad.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F10) {
            DSWorkbenchTagFrame.getSingleton().setVisible(!DSWorkbenchTagFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F11) {
            DSWorkbenchStatsFrame.getSingleton().setVisible(!DSWorkbenchStatsFrame.getSingleton().isVisible());
          } else if (e.getKeyCode() == KeyEvent.VK_F12) {
            DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
          } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 1
            centerROI(0);
          } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 2
            centerROI(1);
          } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 3
            centerROI(2);
          } else if ((e.getKeyCode() == KeyEvent.VK_4) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 4
            centerROI(3);
          } else if ((e.getKeyCode() == KeyEvent.VK_5) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 5
            centerROI(4);
          } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 6
            centerROI(5);
          } else if ((e.getKeyCode() == KeyEvent.VK_7) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 7
            centerROI(6);
          } else if ((e.getKeyCode() == KeyEvent.VK_8) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 8
            centerROI(7);
          } else if ((e.getKeyCode() == KeyEvent.VK_9) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 9
            centerROI(8);
          } else if ((e.getKeyCode() == KeyEvent.VK_0) && e.isControlDown() && e.isAltDown() && !e.isShiftDown()) {
            //ROI 10
            centerROI(9);
          } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jMapPanelHolder.requestFocusInWindow();
            MapPanel.getSingleton().setSpaceDown(true);
          } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            jMapPanelHolder.requestFocusInWindow();
            MapPanel.getSingleton().setShiftDown(true);
          }
        } else if (event.getID() == KeyEvent.KEY_RELEASED) {
          KeyEvent e = (KeyEvent) event;
          if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            MapPanel.getSingleton().setSpaceDown(false);
          } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            MapPanel.getSingleton().setShiftDown(false);
          }
        }
      }
    }, AWTEvent.KEY_EVENT_MASK);
        // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Load UI Icons ">
    try {
      jOnlineLabel.setIcon(new ImageIcon("./graphics/icons/online.png"));
      jEnableClipboardWatchButton.setIcon(new ImageIcon("./graphics/icons/watch_clipboard.png"));
      jCenterIngameButton.setIcon(new ImageIcon(DSWorkbenchMainFrame.class.getResource("/res/ui/center_ingame.png")));
      jRefreshButton.setIcon(new ImageIcon("./graphics/icons/refresh.png"));
      jCenterCoordinateIngame.setIcon(new ImageIcon("./graphics/icons/center.png"));
    } catch (Exception e) {
      logger.error("Failed to load status icon(s)", e);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Check for desktop support ">
    if (!Desktop.isDesktopSupported()) {
      jCenterIngameButton.setEnabled(false);
      jCenterCoordinateIngame.setEnabled(false);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Restore last map position ">
    try {
      String x = GlobalOptions.getSelectedProfile().getProperty("last.x");
      String y = GlobalOptions.getSelectedProfile().getProperty("last.y");
      dCenterX = Double.parseDouble(x);
      dCenterY = Double.parseDouble(y);
      jCenterX.setText(x);
      jCenterY.setText(y);
    } catch (Exception e) {
      if (ServerSettings.getSingleton().getCoordType() != 2) {
        dCenterX = 250.0;
        dCenterY = 250.0;
        int[] hier = DSCalculator.xyToHierarchical(250, 250);
        jCenterX.setText(Integer.toString(hier[0]));
        jCenterY.setText(Integer.toString(hier[1]));
      } else {
        dCenterX = 500.0;
        dCenterY = 500.0;
        jCenterX.setText("500");
        jCenterY.setText("500");
      }
    }

// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Restore other settings ">
    try {
      String val = GlobalOptions.getProperty("show.map.popup");
      if (val == null) {
        jShowMapPopup.setSelected(true);
        GlobalOptions.addProperty("show.map.popup", Boolean.toString(true));
      } else {
        jShowMapPopup.setSelected(Boolean.parseBoolean(val));
      }
    } catch (Exception e) {
      jShowMapPopup.setSelected(true);
      GlobalOptions.addProperty("show.map.popup", Boolean.toString(true));

    }
    try {
      String val = GlobalOptions.getProperty("show.mouseover.info");
      if (val == null) {
        jShowMouseOverInfo.setSelected(false);
        GlobalOptions.addProperty("show.mouseover.info", Boolean.toString(jShowMouseOverInfo.isSelected()));
      } else {
        jShowMouseOverInfo.setSelected(Boolean.parseBoolean(val));
      }
    } catch (Exception e) {
      jShowMouseOverInfo.setSelected(false);
      GlobalOptions.addProperty("show.mouseover.info", Boolean.toString(jShowMouseOverInfo.isSelected()));
    }

    try {
      String val = GlobalOptions.getProperty("include.support");
      if (val == null) {
        jIncludeSupport.setSelected(false);
        GlobalOptions.addProperty("include.support", Boolean.toString(jIncludeSupport.isSelected()));
      } else {
        jIncludeSupport.setSelected(Boolean.parseBoolean(val));
      }
    } catch (Exception e) {
      jIncludeSupport.setSelected(true);
      GlobalOptions.addProperty("include.support", Boolean.toString(jIncludeSupport.isSelected()));
    }

    try {
      String val = GlobalOptions.getProperty("highlight.tribes.villages");
      if (val == null) {
        jHighlightTribeVillages.setSelected(false);
        GlobalOptions.addProperty("highlight.tribes.villages", Boolean.toString(jHighlightTribeVillages.isSelected()));
      } else {
        jHighlightTribeVillages.setSelected(Boolean.parseBoolean(val));
      }
    } catch (Exception e) {
      jHighlightTribeVillages.setSelected(false);
      GlobalOptions.addProperty("highlight.tribes.villages", Boolean.toString(jHighlightTribeVillages.isSelected()));
    }
    try {
      String val = GlobalOptions.getProperty("show.ruler");
      if (val == null) {
        jShowRuler.setSelected(true);
        GlobalOptions.addProperty("show.ruler", Boolean.toString(true));
      } else {
        jShowRuler.setSelected(Boolean.parseBoolean(val));
      }
    } catch (Exception e) {
      jShowRuler.setSelected(true);
      GlobalOptions.addProperty("show.ruler", Boolean.toString(true));
    }

    try {
      String val = GlobalOptions.getProperty("radar.size");
      int hour = 1;
      int min = 0;
      if (val != null) {
        int r = Integer.parseInt(val);
        hour = r / 60;
        min = r - hour * 60;
      } else {
        throw new Exception();
      }
      jHourField.setText(Integer.toString(hour));
      jMinuteField.setText(Integer.toString(min));
    } catch (Exception e) {
      jHourField.setText("1");
      jMinuteField.setText("0");
      GlobalOptions.addProperty("radar.size", "60");
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Skin Setup">
    DefaultComboBoxModel gpModel = new DefaultComboBoxModel(GlobalOptions.getAvailableSkins());
    jGraphicPacks.setModel(gpModel);
    String skin = GlobalOptions.getProperty("default.skin");
    if (skin != null) {
      if (gpModel.getIndexOf(skin) != -1) {
        jGraphicPacks.setSelectedItem(skin);
      } else {
        jGraphicPacks.setSelectedItem("default");
      }
    } else {
      jGraphicPacks.setSelectedItem("default");
    }
        //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Init A*Star HelpSystem ">
    if (!Constants.DEBUG) {
      GlobalOptions.getHelpBroker().enableHelpKey(DSWorkbenchSimulatorFrame.getSingleton().getRootPane(), "pages.astar", GlobalOptions.getHelpBroker().getHelpSet());
      GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "index", GlobalOptions.getHelpBroker().getHelpSet());
    }
        // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Init mouse gesture listener">
    mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
    mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
    mMouseGestures.start();
// </editor-fold>

    mNotificationHideThread = new NotificationHideThread();
    mNotificationHideThread.start();
    SystrayHelper.installSystrayIcon();
    //update online state
    onlineStateChanged();
    restoreProperties();
  }

  public void storeProperties() {
    if (!GlobalOptions.isMinimal()) {
      GlobalOptions.addProperty("main.size.width", Integer.toString(getWidth()));
      GlobalOptions.addProperty("main.size.height", Integer.toString(getHeight()));
    }
  }

  public final void restoreProperties() {
    if (!GlobalOptions.isMinimal()) {
      try {
        int width = Integer.parseInt(GlobalOptions.getProperty("main.size.width"));
        int height = Integer.parseInt(GlobalOptions.getProperty("main.size.height"));
        int maxHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 50;
        int maxWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 50;
        if (height > maxHeight) {
          height = maxHeight;
        }
        if (width > maxWidth) {
          width = maxWidth;
        }

        setSize(width, height);
      } catch (Exception e) {
      }
    }
  }

  public String[] getCurrentPosition() {
    return new String[]{jCenterX.getText(), jCenterY.getText()};
  }

  /**
   * Update on server change
   */
  public void serverSettingsChangedEvent() {
    try {
      logger.info("Updating server settings");
      // <editor-fold defaultstate="collapsed" desc="Reset user profile specific contents">

      UserProfile profile = GlobalOptions.getSelectedProfile();
      String playerID = profile.toString();
      logger.info(" - using playerID " + playerID);
      profile.restoreProperties();
      jCurrentPlayer.setText(playerID);
      try {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Tribe t = DataHolder.getSingleton().getTribeByName(profile.getTribeName());
        Village[] villages = t.getVillageList();
        Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
        for (Village v : villages) {
          model.addElement(v);
        }
        jCurrentPlayerVillages.setModel(model);
      } catch (Exception e) {
        jCurrentPlayerVillages.setModel(new DefaultComboBoxModel(new Object[]{"-keine Dörfer-"}));
      }
// </editor-fold>
      //update maps
      MapPanel.getSingleton().resetServerDependendSettings();
      MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
      MapPanel.getSingleton().getAttackAddFrame().buildUnitBox();
      //setup views
      DSWorkbenchMarkerFrame.getSingleton().resetView();
      DSWorkbenchMarkerFrame.getSingleton().restoreProperties();
      DSWorkbenchChurchFrame.getSingleton().resetView();
      DSWorkbenchChurchFrame.getSingleton().restoreProperties();
      DSWorkbenchAttackFrame.getSingleton().resetView();
      DSWorkbenchAttackFrame.getSingleton().restoreProperties();
      DSWorkbenchAttackFrame.getSingleton().updateCountdownSettings();
      TacticsPlanerWizard.restoreProperties();
      ResourceDistributorWizard.restoreProperties();
      DSWorkbenchTagFrame.getSingleton().resetView();
      DSWorkbenchTagFrame.getSingleton().restoreProperties();
      DSWorkbenchConquersFrame.getSingleton().resetView();
      DSWorkbenchConquersFrame.getSingleton().restoreProperties();
      //update troops table and troops view
      TroopSetupConfigurationFrame.getSingleton().setup();
      DSWorkbenchTroopsFrame.getSingleton().resetView();
      DSWorkbenchTroopsFrame.getSingleton().restoreProperties();
      DistanceManager.getSingleton().clear();
      StatManager.getSingleton().setup();
      DSWorkbenchDistanceFrame.getSingleton().resetView();
      DSWorkbenchDistanceFrame.getSingleton().restoreProperties();
      DSWorkbenchStatsFrame.getSingleton().resetView();
      DSWorkbenchStatsFrame.getSingleton().restoreProperties();
      DSWorkbenchDoItYourselfAttackPlaner.getSingleton().resetView();
      DSWorkbenchDoItYourselfAttackPlaner.getSingleton().restoreProperties();
      DSWorkbenchReportFrame.getSingleton().resetView();
      DSWorkbenchReportFrame.getSingleton().restoreProperties();
      DSWorkbenchSOSRequestAnalyzer.getSingleton().resetView();
      DSWorkbenchSOSRequestAnalyzer.getSingleton().restoreProperties();
      DSWorkbenchFarmManager.getSingleton().resetView();
      DSWorkbenchFarmManager.getSingleton().restoreProperties();
      BBCodeEditor.getSingleton().reset();
      //update attack planner
      DSWorkbenchSettingsDialog.getSingleton().setupAttackColorTable();
      DSWorkbenchRankFrame.getSingleton().resetView();
      DSWorkbenchRankFrame.getSingleton().restoreProperties();

      if (ServerSettings.getSingleton().getCoordType() != 2) {
        jLabel1.setText("K");
        jLabel2.setText("S");
      } else {
        jLabel1.setText("X");
        jLabel2.setText("Y");
        DSRealManager.getSingleton().checkFilesystem();
      }

      jROIBox.setModel(new DefaultComboBoxModel(ROIManager.getSingleton().getROIs()));
      DSWorkbenchSelectionFrame.getSingleton().resetView();
      DSWorkbenchSelectionFrame.getSingleton().restoreProperties();
      DSWorkbenchNotepad.getSingleton().resetView();
      DSWorkbenchNotepad.getSingleton().restoreProperties();

      if (!GlobalOptions.isOfflineMode() && DSWorkbenchSimulatorFrame.getSingleton().isVisible()) {
        DSWorkbenchSimulatorFrame.getSingleton().showIntegratedVersion(DSWorkbenchSettingsDialog.getSingleton().getWebProxy(), GlobalOptions.getSelectedServer());
      }
      ConquerManager.getSingleton().revalidate(true);
      //relevant for first start
      LayerOrderConfigurationFrame.getSingleton();
      MinimapPanel.getSingleton().redraw(true);
      DSWorkbenchFormFrame.getSingleton().resetView();
      DSWorkbenchFormFrame.getSingleton().restoreProperties();
      FormConfigFrame.getSingleton();
      DSWorkbenchSearchFrame.getSingleton();

      logger.info("Server settings updated");
      if (isVisible() && !DSWorkbenchSettingsDialog.getSingleton().isVisible()) {
        showReminder();
      }
    } catch (Exception e) {
      throw new RuntimeException("Initialization failed", e);
    }
  }

  /**
   * Update UI depending on online state
   */
  public final void onlineStateChanged() {
    jOnlineLabel.setEnabled(!GlobalOptions.isOfflineMode());
    if (GlobalOptions.isOfflineMode()) {
      jOnlineLabel.setToolTipText("Offline");
    } else {
      jOnlineLabel.setToolTipText("Online");
    }
  }

  /**
   * Get current zoom factor
   *
   * @return
   */
  public synchronized double getZoomFactor() {
    return dZoomFactor;
  }

  /**
   * Called at startup
   */
  protected void init() {
    logger.info("Starting initialization");
    /*
     * logger.info(" * Setting up attack planner"); //setup frames mTribeTribeAttackFrame = new TribeTribeAttackFrame();
     * mTribeTribeAttackFrame.pack();
     */
    logger.info(" * Updating server settings");
    //setup everything

    serverSettingsChangedEvent();

    logger.info(" * Setting up maps");
    setupMaps();

    logger.info(" * Setting up views");
    setupFrames();
    fireToolChangedEvent(ImageManager.CURSOR_DEFAULT);
    logger.info("Initialization finished");
    initialized = true;
  }

  public boolean isInitialized() {
    return initialized;
  }

  /*
   * public TribeTribeAttackFrame getAttackPlaner() { return mTribeTribeAttackFrame; }
   */
  /**
   * Setup of all frames
   */
  private void setupFrames() {
    DSWorkbenchAttackFrame.getSingleton().addFrameListener(this);
    DSWorkbenchMarkerFrame.getSingleton().addFrameListener(this);
    DSWorkbenchChurchFrame.getSingleton().addFrameListener(this);
    DSWorkbenchConquersFrame.getSingleton().addFrameListener(this);
    DSWorkbenchNotepad.getSingleton().addFrameListener(this);
    DSWorkbenchTagFrame.getSingleton().addFrameListener(this);
    //  TroopsManagerTableModel.getSingleton().setup();
    DSWorkbenchTroopsFrame.getSingleton().addFrameListener(this);
    DSWorkbenchRankFrame.getSingleton().addFrameListener(this);
    DSWorkbenchFormFrame.getSingleton().addFrameListener(this);
    DSWorkbenchStatsFrame.getSingleton().addFrameListener(this);
    DSWorkbenchReportFrame.getSingleton().addFrameListener(this);
  }

  /**
   * Setup main map and mini map
   */
  private void setupMaps() {
    try {
      dZoomFactor = Double.parseDouble(GlobalOptions.getSelectedProfile().getProperty("zoom"));
      checkZoomRange();
    } catch (Exception e) {
      dZoomFactor = 1.0;
    }
    //build the map panel
    logger.info("Adding MapListener");
    MapPanel.getSingleton().addMapPanelListener(this);
    MapPanel.getSingleton().addToolChangeListener(this);
    MinimapPanel.getSingleton().addToolChangeListener(this);
    logger.info("Adding MapPanel");
    jMapPanelHolder.add(MapPanel.getSingleton(), BorderLayout.CENTER);
    //build the minimap
    logger.info("Adding MinimapPanel");
    jMinimapPanel.add(MinimapPanel.getSingleton());
  }

  public void setZoom(double pZoom) {
    dZoomFactor = pZoom;
    checkZoomRange();
  }

  /*
   * public void setupAndShow() { DockController controller = new DockController(); controller.setRootWindow(this); SplitDockStation
   * splitDockStation = new SplitDockStation();
   *
   * controller.add(splitDockStation); add(splitDockStation); MyDockable miniMap = new MyDockable("Minimap", jMinimapPanel); MyDockable
   * map = new MyDockable("Hauptkarte", jMapPanelHolder); splitDockStation.drop(map); splitDockStation.drop(miniMap, new
   * SplitDockProperty(1, 0, 1, 0.5)); MyDockable settings = new MyDockable("Einstellungen", jSettingsScrollPane);
   * settings.installActions(controller); splitDockStation.drop(settings, new SplitDockProperty(1, 0.5, 1, 0.5)); ScreenDockStation
   * screenDockStation = new ScreenDockStation(controller.getRootWindowProvider()); DefaultScreenDockWindowFactory fac = new
   * DefaultScreenDockWindowFactory(); fac.setKind(DefaultScreenDockWindowFactory.Kind.FRAME); screenDockStation.setWindowFactory(fac);
   * controller.add(screenDockStation); setVisible(true); screenDockStation.setShowing(true); }
   */

  /*
   * public static class MyDockable extends DefaultDockable {
   *
   * public MyDockable(String title, JComponent pPanel) { setTitleText(title); add(pPanel); }
   *
   * public void installActions(DockController pController) { DefaultDockActionSource source = new DefaultDockActionSource(new
   * CloseAction(pController)); setActionOffers(source); } }
   */
  @Override
  public void setVisible(boolean v) {
    logger.info("Setting MainWindow visible");
    if (GlobalOptions.isMinimal()) {
      getContentPane().remove(jPanel4);
      getContentPane().add(infoPanel, BorderLayout.SOUTH);
      setSize(530, 200);
      setPreferredSize(new Dimension(530, 200));
      setMinimumSize(new Dimension(530, 200));
      setMaximumSize(new Dimension(1900, 200));
    }
    super.setVisible(v);
    final boolean vis = v;
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        setupRibbon();

        if (vis) {
          //only if set to visible
          MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);

          double w = (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getCurrentFieldWidth(dZoomFactor);
          double h = (double) MapPanel.getSingleton().getHeight() / (double) GlobalOptions.getSkin().getCurrentFieldHeight(dZoomFactor);

          MinimapPanel.getSingleton().setSelection((int) Math.floor(dCenterX), (int) Math.floor(dCenterY), (int) Math.rint(w), (int) Math.rint(h));
          //start ClipboardWatch
          ClipboardWatch.getSingleton();
          //draw map the first time
          fireRefreshMapEvent(null);
          showReminder();
          if (!GlobalOptions.isMinimal() && !Boolean.parseBoolean(GlobalOptions.getProperty("no.welcome"))) {
            setGlassPane(new WelcomePanel());
            getGlassPane().setVisible(true);
          } else {
            glasspaneVisible = false;
          }
        }
      }
    });

    File runningIndicator = new File(".running");

    if (runningIndicator.exists()) {
      showRestoreOption();
    } else {
      try {
        FileUtils.touch(new File(".running"));
      } catch (IOException ioe) {
        logger.error("Failed to touch file '.running'", ioe);
      }
    }
  }

  private void showRestoreOption() {
    File backupFile = new File(GlobalOptions.getSelectedProfile().getProfileDirectory() + "/backup.xml");
    String lastBackup = null;
    if (backupFile.exists()) {
      lastBackup = new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(new Date(backupFile.lastModified()));
    }

    if (lastBackup != null) {
      if (JOptionPaneHelper.showQuestionConfirmBox(this, "Offenbar wurde DS Workbench nicht korrekt beendet, daher kann es möglicherweise zu Datenverlust gekommen sein.\n"
              + "Für das aktuelle Profil existiert ein Backup (Erstellt: " + lastBackup + "). Möchtest du dieses wiederherstellen?", "Absturz?", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        showInfo("Wiederherstellung läuft, bitte warten...");
        if (performImport(backupFile, "backup").equals("Import beendet.\n")) {
          showSuccess("Wiederherstellung abgeschlossen.");
          JOptionPaneHelper.showInformationBox(this, "Das Backup wurde erfolgreich eingespielt. Wiederhergestellte Pläne und Sets tragen die Erweiterung '_backup'.", "Backup wiederhergestellt");
        } else {
          showInfo("Wiederherstellung abgeschlossen.");
          JOptionPaneHelper.showInformationBox(this, "Bei der Wiederherstellung des Backups gab es Probleme. Möglicherweise sind einige Daten verloren."
                  + " Wiederhergestellte Pläne und Sets tragen die Erweiterung '_backup'.", "Backup wiederhergestellt");
        }
      }
    } else {
      logger.debug("There is no backup. Restoring skipped.");
    }
  }

  public boolean isGlasspaneVisible() {
    return glasspaneVisible;
  }

  public void hideWelcomePage() {
    glasspaneVisible = false;
    getGlassPane().setVisible(false);
  }

  public void showAboutDialog() {
    mAbout.setVisible(true);
  }

  private void setupRibbon() {
    RibbonConfigurator.addGeneralToolsTask(this);
    if (!GlobalOptions.isMinimal()) {
      RibbonConfigurator.addMapToolsTask(this);
    }
    RibbonConfigurator.addViewTask(this);
    RibbonConfigurator.addMiscTask(this);
    RibbonConfigurator.addAppIcons(this);

    try {
      String vis = GlobalOptions.getProperty("ribbon.minimized");
      if (vis != null && Boolean.parseBoolean(vis)) {
        getRibbon().setMinimized(true);
      }
    } catch (Exception e) {
    }
  }

  private void showReminder() {
    if (!GlobalOptions.isMinimal()) {
      showInfo("Weltdaten aktuell? Truppen importiert? Gruppen importiert?");
    }
  }

   public void showInfo(String pMessage) {
    mNotificationHideThread.interrupt();
    infoPanel.setCollapsed(false);
    jXLabel1.setBackgroundPainter(new MattePainter(Color.YELLOW));
    jXLabel1.setIcon(new ImageIcon("./graphics/icons/warning.png"));
    jXLabel1.setForeground(Color.BLACK);
    jXLabel1.setText(pMessage);
  }

  public void showSuccess(String pMessage) {
    mNotificationHideThread.interrupt();
    infoPanel.setCollapsed(false);
    jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
    jXLabel1.setIcon(new ImageIcon(DSWorkbenchMainFrame.class.getResource("/res/checkbox.png")));
    jXLabel1.setForeground(Color.BLACK);
    jXLabel1.setText(pMessage);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jExportDialog = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttackExportTable = new javax.swing.JTable();
        jExportButton = new javax.swing.JButton();
        jCancelExportButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jMarkerSetExportTable = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jReportSetExportTable = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        jNoteSetExportTable = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTroopSetExportTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jExportTags = new javax.swing.JCheckBox();
        jExportForms = new javax.swing.JCheckBox();
        jExportChurches = new javax.swing.JCheckBox();
        jAddROIDialog = new javax.swing.JDialog();
        jLabel7 = new javax.swing.JLabel();
        jROIRegion = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jROITextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jROIPosition = new javax.swing.JComboBox();
        jAddNewROIButton = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jCustomPanel = new javax.swing.JPanel();
        jNavigationPanel = new javax.swing.JPanel();
        jMoveE = new javax.swing.JButton();
        jMoveNE = new javax.swing.JButton();
        jMoveN = new javax.swing.JButton();
        jMoveNW = new javax.swing.JButton();
        jMoveW = new javax.swing.JButton();
        jMoveSW = new javax.swing.JButton();
        jMoveS = new javax.swing.JButton();
        jMoveSE = new javax.swing.JButton();
        jCenterX = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCenterY = new javax.swing.JTextField();
        jRefreshButton = new javax.swing.JButton();
        jMoveE1 = new javax.swing.JButton();
        jZoomInButton = new javax.swing.JButton();
        jZoomOutButton = new javax.swing.JButton();
        jCenterCoordinateIngame = new javax.swing.JButton();
        jMapPanel = new javax.swing.JPanel();
        jShowMapPopup = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jGraphicPacks = new javax.swing.JComboBox();
        jHighlightTribeVillages = new javax.swing.JCheckBox();
        jShowRuler = new javax.swing.JCheckBox();
        jHourField = new javax.swing.JTextField();
        jMinuteField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jShowMouseOverInfo = new javax.swing.JCheckBox();
        jIncludeSupport = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jROIPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jROIBox = new javax.swing.JComboBox();
        jRemoveROIButton = new javax.swing.JButton();
        jAddROIButton = new javax.swing.JButton();
        jInformationPanel = new javax.swing.JPanel();
        jCurrentPlayerVillages = new javax.swing.JComboBox();
        jCurrentPlayer = new javax.swing.JLabel();
        jCenterIngameButton = new javax.swing.JButton();
        jOnlineLabel = new javax.swing.JLabel();
        jCurrentToolLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jEnableClipboardWatchButton = new javax.swing.JButton();
        jXTipOfTheDay1 = new org.jdesktop.swingx.JXTipOfTheDay();
        jPanel4 = new javax.swing.JPanel();
        jStatusPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jMapPanelHolder = new javax.swing.JPanel();
        jMinimapPanel = new javax.swing.JPanel();
        jSettingsScrollPane = new javax.swing.JScrollPane();

        jExportDialog.setTitle("Export");
        jExportDialog.setMinimumSize(new java.awt.Dimension(560, 370));
        jExportDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 100));

        jAttackExportTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Angriffplan", "Exportieren"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jAttackExportTable.setOpaque(false);
        jScrollPane1.setViewportView(jAttackExportTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jScrollPane1, gridBagConstraints);

        jExportButton.setText("Exportieren");
        jExportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jExportButton, gridBagConstraints);

        jCancelExportButton.setText("Abbrechen");
        jCancelExportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jCancelExportButton, gridBagConstraints);

        jScrollPane4.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(100, 100));

        jMarkerSetExportTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Markierungsset", "Exportieren"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jMarkerSetExportTable.setOpaque(false);
        jScrollPane4.setViewportView(jMarkerSetExportTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jScrollPane4, gridBagConstraints);

        jScrollPane5.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane5.setPreferredSize(new java.awt.Dimension(100, 100));

        jReportSetExportTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Berichtsset", "Exportieren"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jReportSetExportTable.setOpaque(false);
        jScrollPane5.setViewportView(jReportSetExportTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jScrollPane5, gridBagConstraints);

        jScrollPane6.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane6.setPreferredSize(new java.awt.Dimension(100, 100));

        jNoteSetExportTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Notizset", "Exportieren"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jNoteSetExportTable.setOpaque(false);
        jScrollPane6.setViewportView(jNoteSetExportTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jScrollPane6, gridBagConstraints);

        jScrollPane7.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane7.setPreferredSize(new java.awt.Dimension(100, 100));

        jTroopSetExportTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Truppeninformationen", "Exportieren"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTroopSetExportTable.setOpaque(false);
        jScrollPane7.setViewportView(jTroopSetExportTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jScrollPane7, gridBagConstraints);

        jPanel5.setPreferredSize(new java.awt.Dimension(100, 100));

        jExportTags.setText("Gruppen exportieren");

        jExportForms.setText("Zeichnungen exportieren");

        jExportChurches.setText("Kirchen exportieren");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jExportForms, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                    .addComponent(jExportTags, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                    .addComponent(jExportChurches, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jExportTags)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jExportForms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jExportChurches)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jExportDialog.getContentPane().add(jPanel5, gridBagConstraints);

        jAddROIDialog.setTitle("ROI hinzufügen");

        jLabel7.setText("Zentrum");

        jROIRegion.setEnabled(false);
        jROIRegion.setMaximumSize(new java.awt.Dimension(120, 20));
        jROIRegion.setMinimumSize(new java.awt.Dimension(120, 20));
        jROIRegion.setPreferredSize(new java.awt.Dimension(120, 20));

        jLabel8.setText("Bezeichnung");

        jROITextField.setToolTipText("Eindeutige Bezeichnung zur Kennzeichnung der ROI");

        jLabel9.setText("Position");

        jROIPosition.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Ende" }));
        jROIPosition.setToolTipText("Position der ROI (Positionen 1-10 können per Shortcut gewählt werden)");

        jAddNewROIButton.setText("Hinzufügen");
        jAddNewROIButton.setToolTipText("ROI hinzufügen");
        jAddNewROIButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddROIDoneEvent(evt);
            }
        });

        jButton5.setText("Abbrechen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddROIDoneEvent(evt);
            }
        });

        javax.swing.GroupLayout jAddROIDialogLayout = new javax.swing.GroupLayout(jAddROIDialog.getContentPane());
        jAddROIDialog.getContentPane().setLayout(jAddROIDialogLayout);
        jAddROIDialogLayout.setHorizontalGroup(
            jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddROIDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jAddROIDialogLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAddNewROIButton))
                    .addGroup(jAddROIDialogLayout.createSequentialGroup()
                        .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jROIRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jROITextField, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                            .addComponent(jROIPosition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jAddROIDialogLayout.setVerticalGroup(
            jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddROIDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jROIRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jROITextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jROIPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAddROIDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAddNewROIButton)
                    .addComponent(jButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jCustomPanelLayout = new javax.swing.GroupLayout(jCustomPanel);
        jCustomPanel.setLayout(jCustomPanelLayout);
        jCustomPanelLayout.setHorizontalGroup(
            jCustomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 572, Short.MAX_VALUE)
        );
        jCustomPanelLayout.setVerticalGroup(
            jCustomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 501, Short.MAX_VALUE)
        );

        jNavigationPanel.setBackground(new java.awt.Color(239, 235, 223));
        jNavigationPanel.setMinimumSize(new java.awt.Dimension(236, 95));

        jMoveE.setBackground(new java.awt.Color(239, 235, 223));
        jMoveE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_e.png"))); // NOI18N
        jMoveE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveNE.setBackground(new java.awt.Color(239, 235, 223));
        jMoveNE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_ne.png"))); // NOI18N
        jMoveNE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveNE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveNE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveNE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveN.setBackground(new java.awt.Color(239, 235, 223));
        jMoveN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_n.png"))); // NOI18N
        jMoveN.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveN.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveN.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveNW.setBackground(new java.awt.Color(239, 235, 223));
        jMoveNW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_nw.png"))); // NOI18N
        jMoveNW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveNW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveNW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveNW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveW.setBackground(new java.awt.Color(239, 235, 223));
        jMoveW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_w.png"))); // NOI18N
        jMoveW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveSW.setBackground(new java.awt.Color(239, 235, 223));
        jMoveSW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_sw.png"))); // NOI18N
        jMoveSW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveSW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveSW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveSW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveS.setBackground(new java.awt.Color(239, 235, 223));
        jMoveS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_s.png"))); // NOI18N
        jMoveS.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveS.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveS.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveSE.setBackground(new java.awt.Color(239, 235, 223));
        jMoveSE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_se.png"))); // NOI18N
        jMoveSE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveSE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveSE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveSE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jCenterX.setText("500");
        jCenterX.setMaximumSize(new java.awt.Dimension(40, 25));
        jCenterX.setMinimumSize(new java.awt.Dimension(40, 25));
        jCenterX.setPreferredSize(new java.awt.Dimension(40, 25));
        jCenterX.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireCheckForVillagePositionEvent(evt);
            }
        });

        jLabel1.setText("X");

        jLabel2.setText("Y");

        jCenterY.setText("500");
        jCenterY.setMaximumSize(new java.awt.Dimension(40, 25));
        jCenterY.setMinimumSize(new java.awt.Dimension(40, 25));
        jCenterY.setPreferredSize(new java.awt.Dimension(40, 25));

        jRefreshButton.setBackground(new java.awt.Color(239, 235, 223));
        jRefreshButton.setToolTipText("Position aktualisieren");
        jRefreshButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jRefreshButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jRefreshButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jRefreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRefreshMapEvent(evt);
            }
        });

        jMoveE1.setBackground(new java.awt.Color(239, 235, 223));
        jMoveE1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jMoveE1.setEnabled(false);
        jMoveE1.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveE1.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveE1.setPreferredSize(new java.awt.Dimension(21, 21));

        jZoomInButton.setBackground(new java.awt.Color(239, 235, 223));
        jZoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/zoom_out.png"))); // NOI18N
        jZoomInButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jZoomInButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jZoomInButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jZoomInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireZoomEvent(evt);
            }
        });

        jZoomOutButton.setBackground(new java.awt.Color(239, 235, 223));
        jZoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/zoom_in.png"))); // NOI18N
        jZoomOutButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jZoomOutButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jZoomOutButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jZoomOutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireZoomEvent(evt);
            }
        });

        jCenterCoordinateIngame.setBackground(new java.awt.Color(239, 235, 223));
        jCenterCoordinateIngame.setToolTipText("Zentrieren (InGame)");
        jCenterCoordinateIngame.setMaximumSize(new java.awt.Dimension(30, 30));
        jCenterCoordinateIngame.setMinimumSize(new java.awt.Dimension(30, 30));
        jCenterCoordinateIngame.setPreferredSize(new java.awt.Dimension(30, 30));
        jCenterCoordinateIngame.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterCurrentPosInGameEvent(evt);
            }
        });

        javax.swing.GroupLayout jNavigationPanelLayout = new javax.swing.GroupLayout(jNavigationPanel);
        jNavigationPanel.setLayout(jNavigationPanelLayout);
        jNavigationPanelLayout.setHorizontalGroup(
            jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                                .addComponent(jMoveNW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMoveN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                                .addComponent(jMoveW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMoveE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jMoveNE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCenterX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCenterY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCenterCoordinateIngame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jNavigationPanelLayout.setVerticalGroup(
            jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMoveNE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveNW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jNavigationPanelLayout.createSequentialGroup()
                            .addComponent(jRefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jCenterCoordinateIngame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(5, 5, 5))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jNavigationPanelLayout.createSequentialGroup()
                            .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMapPanel.setBackground(new java.awt.Color(239, 235, 223));
        jMapPanel.setMinimumSize(new java.awt.Dimension(193, 250));
        jMapPanel.setPreferredSize(new java.awt.Dimension(193, 250));
        jMapPanel.setLayout(new java.awt.GridBagLayout());

        jShowMapPopup.setText("Kartenpopup anzeigen");
        jShowMapPopup.setToolTipText("Zeigt Informationen über das Dorf unter dem Mauszeiger an");
        jShowMapPopup.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jShowMapPopup.setOpaque(false);
        jShowMapPopup.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireShowMapPopupChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jShowMapPopup, gridBagConstraints);

        jLabel5.setText("Laufzeitradius");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jLabel5, gridBagConstraints);

        jLabel12.setText("Grafikpaket");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jLabel12, gridBagConstraints);

        jGraphicPacks.setMaximumSize(new java.awt.Dimension(28, 20));
        jGraphicPacks.setMinimumSize(new java.awt.Dimension(28, 20));
        jGraphicPacks.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireGraphicPackChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jGraphicPacks, gridBagConstraints);

        jHighlightTribeVillages.setText("Spielerdörfer hervorheben");
        jHighlightTribeVillages.setToolTipText("Markiert im Kartenausschnitt alle Dörfer des Spielers, dessen Dorf unter dem Mauszeiger liegt");
        jHighlightTribeVillages.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jHighlightTribeVillages.setOpaque(false);
        jHighlightTribeVillages.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireHighlightTribeVillagesChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jHighlightTribeVillages, gridBagConstraints);

        jShowRuler.setText("Lineal anzeigen");
        jShowRuler.setToolTipText("Zeichnet ein Koordinatenlineal am Kartenrand");
        jShowRuler.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jShowRuler.setOpaque(false);
        jShowRuler.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireShowRulerChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jShowRuler, gridBagConstraints);

        jHourField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jHourField.setText("1");
        jHourField.setMaximumSize(new java.awt.Dimension(24, 25));
        jHourField.setMinimumSize(new java.awt.Dimension(24, 25));
        jHourField.setPreferredSize(new java.awt.Dimension(24, 25));
        jHourField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireRadarValueChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jHourField, gridBagConstraints);

        jMinuteField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jMinuteField.setText("0");
        jMinuteField.setMaximumSize(new java.awt.Dimension(24, 25));
        jMinuteField.setMinimumSize(new java.awt.Dimension(24, 25));
        jMinuteField.setPreferredSize(new java.awt.Dimension(24, 25));
        jMinuteField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireRadarValueChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jMinuteField, gridBagConstraints);

        jLabel11.setText("h");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jLabel11, gridBagConstraints);

        jLabel13.setText("min");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jLabel13, gridBagConstraints);

        jShowMouseOverInfo.setText("MouseOver Infos anzeigen");
        jShowMouseOverInfo.setToolTipText("Zeigt Informationen über das Dorf unter dem Mauszeiger an");
        jShowMouseOverInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jShowMouseOverInfo.setOpaque(false);
        jShowMouseOverInfo.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireShowMouseOverInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jShowMouseOverInfo, gridBagConstraints);

        jIncludeSupport.setText("Unterstützungen einbeziehen");
        jIncludeSupport.setToolTipText(" Unterstützungen bei den MouseOver Infos und bei der Anzeige der Truppendichte mit einbeziehen");
        jIncludeSupport.setOpaque(false);
        jIncludeSupport.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireShowHideSupportsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMapPanel.add(jIncludeSupport, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jMapPanel.add(jLabel3, gridBagConstraints);

        jROIPanel.setBackground(new java.awt.Color(239, 235, 223));
        jROIPanel.setMaximumSize(new java.awt.Dimension(293, 70));
        jROIPanel.setMinimumSize(new java.awt.Dimension(293, 70));
        jROIPanel.setPreferredSize(new java.awt.Dimension(293, 70));

        jLabel6.setText("ROIs");
        jLabel6.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel6.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel6.setPreferredSize(new java.awt.Dimension(40, 25));

        jROIBox.setMinimumSize(new java.awt.Dimension(23, 15));
        jROIBox.setPreferredSize(new java.awt.Dimension(28, 25));
        jROIBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireROISelectedEvent(evt);
            }
        });

        jRemoveROIButton.setBackground(new java.awt.Color(239, 235, 223));
        jRemoveROIButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jRemoveROIButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jRemoveROIButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jRemoveROIButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jRemoveROIButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeROIEvent(evt);
            }
        });

        jAddROIButton.setBackground(new java.awt.Color(239, 235, 223));
        jAddROIButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jAddROIButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jAddROIButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jAddROIButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jAddROIButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeROIEvent(evt);
            }
        });

        javax.swing.GroupLayout jROIPanelLayout = new javax.swing.GroupLayout(jROIPanel);
        jROIPanel.setLayout(jROIPanelLayout);
        jROIPanelLayout.setHorizontalGroup(
            jROIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jROIPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jROIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jROIPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jROIBox, 0, 223, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jROIPanelLayout.createSequentialGroup()
                        .addComponent(jRemoveROIButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAddROIButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jROIPanelLayout.setVerticalGroup(
            jROIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jROIPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jROIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jROIBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jROIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRemoveROIButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAddROIButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jInformationPanel.setBackground(new java.awt.Color(239, 235, 223));

        jCurrentPlayerVillages.setToolTipText("Aktives Dorf als Ausgangspunkt für InGame Aktionen");
        jCurrentPlayerVillages.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                fireCurrentPlayerVillagePopupEvent(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jCurrentPlayer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jCurrentPlayer.setMaximumSize(new java.awt.Dimension(155, 14));
        jCurrentPlayer.setMinimumSize(new java.awt.Dimension(155, 14));
        jCurrentPlayer.setPreferredSize(new java.awt.Dimension(155, 14));

        jCenterIngameButton.setBackground(new java.awt.Color(239, 235, 223));
        jCenterIngameButton.setToolTipText("Zentrieren (InGame)");
        jCenterIngameButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jCenterIngameButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jCenterIngameButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jCenterIngameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterVillageIngameEvent(evt);
            }
        });

        jOnlineLabel.setToolTipText("Online/Offline Modus");
        jOnlineLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jOnlineLabel.setMaximumSize(new java.awt.Dimension(30, 30));
        jOnlineLabel.setMinimumSize(new java.awt.Dimension(30, 30));
        jOnlineLabel.setPreferredSize(new java.awt.Dimension(30, 30));

        jCurrentToolLabel.setToolTipText("Momentan gewähltes Werkzeug");
        jCurrentToolLabel.setAlignmentY(0.0F);
        jCurrentToolLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jCurrentToolLabel.setFocusable(false);
        jCurrentToolLabel.setIconTextGap(0);
        jCurrentToolLabel.setMaximumSize(new java.awt.Dimension(30, 30));
        jCurrentToolLabel.setMinimumSize(new java.awt.Dimension(30, 30));
        jCurrentToolLabel.setPreferredSize(new java.awt.Dimension(30, 30));
        jCurrentToolLabel.setRequestFocusEnabled(false);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/camera.png"))); // NOI18N
        jButton1.setToolTipText("Foto der Hauptkarte erstellen");
        jButton1.setMaximumSize(new java.awt.Dimension(30, 30));
        jButton1.setMinimumSize(new java.awt.Dimension(30, 30));
        jButton1.setPreferredSize(new java.awt.Dimension(30, 30));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateMapShotEvent(evt);
            }
        });

        jEnableClipboardWatchButton.setBackground(new java.awt.Color(239, 235, 223));
        jEnableClipboardWatchButton.setToolTipText("Suche nach Informationen in der Zwischenablage an-/ausschalten");
        jEnableClipboardWatchButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jEnableClipboardWatchButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jEnableClipboardWatchButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jEnableClipboardWatchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeClipboardWatchEvent(evt);
            }
        });

        javax.swing.GroupLayout jInformationPanelLayout = new javax.swing.GroupLayout(jInformationPanel);
        jInformationPanel.setLayout(jInformationPanelLayout);
        jInformationPanelLayout.setHorizontalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCurrentPlayer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                    .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.Alignment.LEADING, 0, 227, Short.MAX_VALUE)
                    .addGroup(jInformationPanelLayout.createSequentialGroup()
                        .addComponent(jCurrentToolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                        .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jEnableClipboardWatchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jInformationPanelLayout.setVerticalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCurrentPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCurrentToolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEnableClipboardWatchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DS Workbench 0.92b");
        setBackground(new java.awt.Color(225, 213, 190));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireDSWorkbenchClosingEvent(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                fireFrameResizedEvent(evt);
            }
        });

        jStatusPanel.setMaximumSize(new java.awt.Dimension(32, 30));
        jStatusPanel.setLayout(new java.awt.BorderLayout());

        capabilityInfoPanel1.setDeletable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        jStatusPanel.add(capabilityInfoPanel1, java.awt.BorderLayout.WEST);

        infoPanel.setAnimated(false);
        infoPanel.setCollapsed(true);
        infoPanel.setDirection(org.jdesktop.swingx.JXCollapsiblePane.Direction.LEFT);
        infoPanel.setInheritAlpha(false);
        infoPanel.setMaximumSize(new java.awt.Dimension(2147483647, 30));

        jXLabel1.setText("jXLabel1");
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideNotificationEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jStatusPanel.add(infoPanel, java.awt.BorderLayout.LINE_END);

        jMapPanelHolder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2));
        jMapPanelHolder.setForeground(new java.awt.Color(240, 240, 240));
        jMapPanelHolder.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jMapPanelHolder.setDoubleBuffered(false);
        jMapPanelHolder.setLayout(new java.awt.BorderLayout());

        jMinimapPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2));
        jMinimapPanel.setDoubleBuffered(false);
        jMinimapPanel.setLayout(new java.awt.BorderLayout());

        jSettingsScrollPane.setFocusTraversalPolicyProvider(true);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jStatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE)
                    .addComponent(jMapPanelHolder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSettingsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSettingsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jMapPanelHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

  /**
   * Update map position
   */
private void fireRefreshMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshMapEvent
  double cx = UIHelper.parseIntFromField(jCenterX, (int) dCenterX);
  double cy = UIHelper.parseIntFromField(jCenterY, (int) dCenterY);
  if (ServerSettings.getSingleton().getCoordType() != 2) {
    int[] hier = DSCalculator.hierarchicalToXy((int) cx, (int) cy, 12);
    if (hier != null) {
      dCenterX = hier[0];
      dCenterY = hier[1];
    }
  } else {
    dCenterX = cx;
    dCenterY = cy;
  }
  double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
  double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  MinimapPanel.getSingleton().setSelection((int) Math.floor(dCenterX), (int) Math.floor(dCenterY), (int) Math.rint(w), (int) Math.rint(h));
  MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY, true);
}//GEN-LAST:event_fireRefreshMapEvent

  /**
   * Update map movement
   */
private void fireMoveMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveMapEvent
  double cx = UIHelper.parseIntFromField(jCenterX, (int) dCenterX);
  double cy = UIHelper.parseIntFromField(jCenterY, (int) dCenterY);

  if (ServerSettings.getSingleton().getCoordType() != 2) {
    int[] hier = DSCalculator.hierarchicalToXy((int) cx, (int) cy, 12);
    if (hier != null) {
      cx = hier[0];
      cy = hier[1];
    }
  }

  if (evt.getSource() == jMoveN) {
    cy -= (double) MapPanel.getSingleton().getHeight() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveNE) {
    cx += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    cy -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveE) {
    cx += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveSE) {
    cx += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    cy += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveS) {
    cy += (double) MapPanel.getSingleton().getHeight() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveSW) {
    cx -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    cy += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveW) {
    cx -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  } else if (evt.getSource() == jMoveNW) {
    cx -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    cy -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  }

  if (ServerSettings.getSingleton().getCoordType() != 2) {
    int[] hier = DSCalculator.xyToHierarchical((int) cx, (int) cy);
    if (hier != null) {
      cx = hier[0];
      cy = hier[1];
    }
  }

  jCenterX.setText(Integer.toString((int) Math.floor(cx)));
  jCenterY.setText(Integer.toString((int) Math.floor(cy)));
  dCenterX = cx;
  dCenterY = cy;
  MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
  double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
  double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
  MinimapPanel.getSingleton().setSelection((int) Math.floor(cx), (int) Math.floor(cy), (int) Math.rint(w), (int) Math.rint(h));
}//GEN-LAST:event_fireMoveMapEvent

  /**
   * React on resize events
   */
private void fireFrameResizedEvent(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_fireFrameResizedEvent
  try {
    MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
  } catch (Exception e) {
    logger.error("Failed to resize map for (" + dCenterX + ", " + dCenterY + ")", e);
  }
}//GEN-LAST:event_fireFrameResizedEvent

  /**
   * Zoom main map
   */
private void fireZoomEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireZoomEvent
  if (evt.getSource() == jZoomInButton) {
    zoomIn();
  } else {
    zoomOut();
  }
}//GEN-LAST:event_fireZoomEvent

  public synchronized void zoomIn() {
    dZoomFactor += 1.0 / 10.0;
    checkZoomRange();

    dZoomFactor = Double.parseDouble(NumberFormat.getInstance().format(dZoomFactor).replaceAll(",", "."));
    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
    int xPos = UIHelper.parseIntFromField(jCenterX, (int) dCenterX);
    int yPos = UIHelper.parseIntFromField(jCenterY, (int) dCenterY);
    if (ServerSettings.getSingleton().getCoordType() != 2) {
      int[] hier = DSCalculator.hierarchicalToXy(xPos, yPos, 12);
      if (hier != null) {
        xPos = hier[0];
        yPos = hier[1];
      }
    }
    MinimapPanel.getSingleton().setSelection(xPos, yPos, (int) Math.rint(w), (int) Math.rint(h));
    MapPanel.getSingleton().updateMapPosition(xPos, yPos, true);
  }

  public synchronized void zoomOut() {
    dZoomFactor -= 1.0 / 10.0;
    checkZoomRange();

    dZoomFactor = Double.parseDouble(NumberFormat.getInstance().format(dZoomFactor).replaceAll(",", "."));
    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
    int xPos = UIHelper.parseIntFromField(jCenterX, (int) dCenterX);
    int yPos = UIHelper.parseIntFromField(jCenterY, (int) dCenterY);

    if (ServerSettings.getSingleton().getCoordType() != 2) {
      int[] hier = DSCalculator.hierarchicalToXy(xPos, yPos, 12);
      if (hier != null) {
        xPos = hier[0];
        yPos = hier[1];
      }
    }

    MinimapPanel.getSingleton().setSelection(xPos, yPos, (int) Math.rint(w), (int) Math.rint(h));
    MapPanel.getSingleton().updateMapPosition(xPos, yPos, true);
  }

  /**
   * Show the toolbar
   */
  /**
   * Center village Ingame
   */
private void fireCenterVillageIngameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterVillageIngameEvent
  if (!jCenterIngameButton.isEnabled()) {
    return;
  }

  Village v = (Village) jCurrentPlayerVillages.getSelectedItem();
  if (v != null) {
    BrowserCommandSender.centerVillage(v);
  }
}//GEN-LAST:event_fireCenterVillageIngameEvent

  /**
   * Center pos Ingame
   */
private void fireCenterCurrentPosInGameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterCurrentPosInGameEvent
  if (!jCenterCoordinateIngame.isEnabled()) {
    return;
  }
  BrowserCommandSender.centerCoordinate(
          UIHelper.parseIntFromField(jCenterX, (int) dCenterX),
          UIHelper.parseIntFromField(jCenterY, (int) dCenterY));
}//GEN-LAST:event_fireCenterCurrentPosInGameEvent

private void fireCurrentPlayerVillagePopupEvent(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_fireCurrentPlayerVillagePopupEvent
  if (jCurrentPlayerVillages.getSelectedIndex() < 0) {
    return;
  }
  centerVillage((Village) jCurrentPlayerVillages.getSelectedItem());
  DSWorkbenchConquersFrame.getSingleton().repaint();
}//GEN-LAST:event_fireCurrentPlayerVillagePopupEvent

private void fireShowMapPopupChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireShowMapPopupChangedEvent
  GlobalOptions.addProperty("show.map.popup", Boolean.toString(jShowMapPopup.isSelected()));
}//GEN-LAST:event_fireShowMapPopupChangedEvent

private void fireCreateMapShotEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateMapShotEvent
  Component parent = this;
  putOnline = false;
  String dir = GlobalOptions.getProperty("screen.dir");
  if (dir == null) {
    dir = ".";
  }

  String type = null;
  chooser.setSelectedFile(new File(dir));
  int ret = chooser.showSaveDialog(parent);
  if (ret == JFileChooser.APPROVE_OPTION) {
    try {
      File f = chooser.getSelectedFile();
      javax.swing.filechooser.FileFilter filter = chooser.getFileFilter();
      if (filter.getDescription().indexOf("jpeg") > 0) {
        type = "jpeg";
      } else if (filter.getDescription().indexOf("png") > 0) {
        type = "png";
      } else {
        type = "png";
      }
      String file = f.getCanonicalPath();
      if (!file.endsWith(type)) {
        file += "." + type;
      }
      File target = new File(file);
      if (target.exists()) {
        //ask if overwrite

        if (JOptionPaneHelper.showQuestionConfirmBox(parent, "Existierende Datei überschreiben?", "Überschreiben", "Nein", "Ja") != JOptionPane.YES_OPTION) {
          return;
        }
      }
      MapPanel.getSingleton().planMapShot(type, target, this);

      GlobalOptions.addProperty("screen.dir", target.getPath());
    } catch (Exception e) {
      logger.error("Failed to write map shot", e);
    }
  }

}//GEN-LAST:event_fireCreateMapShotEvent

private void fireExportEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExportEvent
  if (evt.getSource() == jExportButton) {
    //do export
    logger.debug("Building export data");

    List<String> plansToExport = new LinkedList<String>();
    for (int i = 0; i < jAttackExportTable.getRowCount(); i++) {
      String plan = (String) jAttackExportTable.getValueAt(i, 0);
      Boolean export = (Boolean) jAttackExportTable.getValueAt(i, 1);
      if (export) {
        logger.debug("Selecting attack plan '" + plan + "' to export list");
        plansToExport.add(plan);
      }

    }

    List<String> setsToExport = new LinkedList<String>();
    for (int i = 0; i < jMarkerSetExportTable.getRowCount(); i++) {
      String set = (String) jMarkerSetExportTable.getValueAt(i, 0);
      Boolean export = (Boolean) jMarkerSetExportTable.getValueAt(i, 1);
      if (export) {
        logger.debug("Selecting marker set '" + set + "' to export list");
        setsToExport.add(set);
      }

    }
    List<String> reportsToExport = new LinkedList<String>();
    for (int i = 0; i < jReportSetExportTable.getRowCount(); i++) {
      String set = (String) jReportSetExportTable.getValueAt(i, 0);
      Boolean export = (Boolean) jReportSetExportTable.getValueAt(i, 1);
      if (export) {
        logger.debug("Selecting report set '" + set + "' to export list");
        reportsToExport.add(set);
      }
    }

    List<String> troopSetsToExport = new LinkedList<String>();
    for (int i = 0; i < jTroopSetExportTable.getRowCount(); i++) {
      String set = (String) jTroopSetExportTable.getValueAt(i, 0);
      Boolean export = (Boolean) jTroopSetExportTable.getValueAt(i, 1);
      if (export) {
        logger.debug("Selecting troop set '" + set + "' to export list");
        troopSetsToExport.add(set);
      }
    }
    List<String> noteSetsToExport = new LinkedList<String>();
    for (int i = 0; i < jNoteSetExportTable.getRowCount(); i++) {
      String set = (String) jNoteSetExportTable.getValueAt(i, 0);
      Boolean export = (Boolean) jNoteSetExportTable.getValueAt(i, 1);
      if (export) {
        logger.debug("Selecting note set '" + set + "' to export list");
        noteSetsToExport.add(set);
      }
    }
    boolean needExport = false;
    needExport = !plansToExport.isEmpty();
    needExport |= !setsToExport.isEmpty();
    needExport |= !reportsToExport.isEmpty();
    needExport |= jExportTags.isSelected();
    needExport |= !troopSetsToExport.isEmpty();
    needExport |= jExportForms.isSelected();
    needExport |= !noteSetsToExport.isEmpty();
    needExport |= jExportChurches.isSelected();

    if (!needExport) {
      JOptionPaneHelper.showWarningBox(jExportDialog, "Keine Daten für den Export gewählt", "Export");
      return;

    }
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
      dir = ".";
    }

    JFileChooser chooser = null;
    try {
      chooser = new JFileChooser(dir);
    } catch (Exception e) {
      JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
      return;
    }
    chooser.setDialogTitle("Datei auswählen");
    chooser.setSelectedFile(new File("export.xml"));
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

      @Override
      public boolean accept(File f) {
          return (f != null) && (f.isDirectory() || f.getName().endsWith(".xml"));

      }

      @Override
      public String getDescription() {
        return "*.xml";
      }
    });
    int ret = chooser.showSaveDialog(jExportDialog);
    if (ret == JFileChooser.APPROVE_OPTION) {
      try {
        File f = chooser.getSelectedFile();
        String file = f.getCanonicalPath();
        if (!file.endsWith(".xml")) {
          file += ".xml";
        }

        File target = new File(file);
        if (target.exists()) {
          if (JOptionPaneHelper.showQuestionConfirmBox(jExportDialog, "Bestehende Datei überschreiben?", "Export", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            return;
          }
        }

        String exportString = "<export>\n";
        if (!plansToExport.isEmpty()) {
          exportString += AttackManager.getSingleton().getExportData(plansToExport);
        }
        if (!setsToExport.isEmpty()) {
          exportString += MarkerManager.getSingleton().getExportData(setsToExport);
        }
        if (!reportsToExport.isEmpty()) {
          exportString += ReportManager.getSingleton().getExportData(reportsToExport);
        }
        if (jExportTags.isSelected()) {
          exportString += TagManager.getSingleton().getExportData(null);
        }

        if (!troopSetsToExport.isEmpty()) {
          exportString += TroopsManager.getSingleton().getExportData(troopSetsToExport);
        }

        if (jExportForms.isSelected()) {
          exportString += FormManager.getSingleton().getExportData(null);
        }

        if (!noteSetsToExport.isEmpty()) {
          exportString += NoteManager.getSingleton().getExportData(noteSetsToExport);
        }

        if (jExportChurches.isSelected()) {
          exportString += ChurchManager.getSingleton().getExportData(null);
        }

        exportString += "</export>";
        logger.debug("Writing data to disk");
        FileWriter w = new FileWriter(target);
        w.write(exportString);
        logger.debug("Finalizing writer");
        w.flush();
        w.close();
        logger.debug("Export finished successfully");
        JOptionPaneHelper.showInformationBox(jExportDialog, "Export erfolgreich beendet.", "Export");
      } catch (Exception e) {
        logger.error("Failed to export data", e);
        JOptionPaneHelper.showErrorBox(this, "Export fehlgeschlagen.", "Export");
      }

    } else {
      //cancel pressed
      return;
    }

  }
  jExportDialog.setAlwaysOnTop(false);
  jExportDialog.setVisible(false);
}//GEN-LAST:event_fireExportEvent

private void fireChangeROIEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeROIEvent
  if (evt.getSource() == jAddROIButton) {
    int x = UIHelper.parseIntFromField(jCenterX, (int) dCenterX);
    int y = UIHelper.parseIntFromField(jCenterY, (int) dCenterY);
    jROIRegion.setText("(" + x + "|" + y + ")");
    jROIPosition.setSelectedIndex(jROIPosition.getItemCount() - 1);
    jAddROIDialog.setLocationRelativeTo(this);
    jAddROIDialog.setVisible(true);
  } else {
    try {
      String item = (String) jROIBox.getSelectedItem();
      logger.debug("Removing ROI '" + item + "'");
      ROIManager.getSingleton().removeROI(item);
      jROIBox.removeItem(item);
    } catch (Exception e) {
    }
  }
}//GEN-LAST:event_fireChangeROIEvent

private void fireAddROIDoneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddROIDoneEvent

  if (evt.getSource() == jAddNewROIButton) {
    try {
      int x = UIHelper.parseIntFromField(jCenterX, (int) dCenterX);
      int y = UIHelper.parseIntFromField(jCenterY, (int) dCenterY);
      String value = jROITextField.getText() + " (" + x + "|" + y + ")";
      int pos = Integer.MAX_VALUE;
      try {
        pos = Integer.parseInt((String) jROIPosition.getSelectedItem());
        pos -= 1;
      } catch (Exception ee) {
        //end pos selected
        pos = Integer.MAX_VALUE;
      }

      if (ROIManager.getSingleton().containsROI(value)) {
        JOptionPaneHelper.showWarningBox(this, "ROI '" + value + "' existiert bereits.", "ROI vorhanden");
        return;

      }

      ROIManager.getSingleton().addROI(pos, value);
      jROIBox.setModel(new DefaultComboBoxModel(ROIManager.getSingleton().getROIs()));
    } catch (Exception e) {
      logger.error("Failed to add ROI", e);
    }

  }
  jAddROIDialog.setVisible(false);

}//GEN-LAST:event_fireAddROIDoneEvent

private void fireROISelectedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireROISelectedEvent
  if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
    try {
      String item = (String) jROIBox.getSelectedItem();
      item = item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")"));
      String[] pos = item.trim().split("\\|");
      jCenterX.setText(pos[0]);
      jCenterY.setText(pos[1]);
      fireRefreshMapEvent(null);
    } catch (Exception e) {
    }
  }
}//GEN-LAST:event_fireROISelectedEvent

private void fireDSWorkbenchClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireDSWorkbenchClosingEvent
  logger.debug("Shutting down DSWorkbench");
  try {
    GlobalOptions.addProperty("ribbon.minimized", Boolean.toString(getRibbon().isMinimized()));
    GlobalOptions.getSelectedProfile().updateProperties();
    TacticsPlanerWizard.storeProperties();
    ResourceDistributorWizard.storeProperties();
    GlobalOptions.getSelectedProfile().storeProfileData();
  } catch (Exception e) {
    logger.error("Failed to store profile settings on shutdown");
  }
  dispose();
  System.exit(0);
}//GEN-LAST:event_fireDSWorkbenchClosingEvent

private void fireGraphicPackChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireGraphicPackChangedEvent
  GlobalOptions.addProperty("default.skin", (String) jGraphicPacks.getSelectedItem());
  try {
    GlobalOptions.loadSkin();
  } catch (Exception e) {
    logger.error("Failed to load skin '" + jGraphicPacks.getSelectedItem() + "'", e);
    JOptionPaneHelper.showErrorBox(this, "Fehler beim laden des Grafikpaketes.", "Fehler");
    //load default
    GlobalOptions.addProperty("default.skin", "default");
    try {
      GlobalOptions.loadSkin();
    } catch (Exception ie) {
      logger.error("Failed to load default skin", ie);
    }
  }
  if (isInitialized()) {
    MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
  }
}//GEN-LAST:event_fireGraphicPackChangedEvent

private void fireHighlightTribeVillagesChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireHighlightTribeVillagesChangedEvent
  GlobalOptions.addProperty("highlight.tribes.villages", Boolean.toString(jHighlightTribeVillages.isSelected()));
}//GEN-LAST:event_fireHighlightTribeVillagesChangedEvent
private void fireShowRulerChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireShowRulerChangedEvent
  GlobalOptions.addProperty("show.ruler", Boolean.toString(jShowRuler.isSelected()));
}//GEN-LAST:event_fireShowRulerChangedEvent

private void fireRadarValueChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireRadarValueChangedEvent
  int hours = UIHelper.parseIntFromField(jHourField, 1);
  int minutes = UIHelper.parseIntFromField(jMinuteField);
  GlobalOptions.addProperty("radar.size", Integer.toString(hours * 60 + minutes));
}//GEN-LAST:event_fireRadarValueChangedEvent

private void fireCheckForVillagePositionEvent(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fireCheckForVillagePositionEvent
  List<Village> parsed = PluginManager.getSingleton().executeVillageParser(jCenterX.getText());
  if (!parsed.isEmpty()) {
    if (ServerSettings.getSingleton().getCoordType() != 2) {
      int[] hier = DSCalculator.xyToHierarchical((int) parsed.get(0).getX(), (int) parsed.get(0).getY());
      if (hier != null) {
        jCenterY.setText(Integer.toString(hier[1]));
        jCenterX.setText(Integer.toString(hier[0]));
      }
    } else {
      jCenterY.setText(Short.toString(parsed.get(0).getY()));
      jCenterX.setText(Short.toString(parsed.get(0).getX()));
    }
  }
}//GEN-LAST:event_fireCheckForVillagePositionEvent

private void fireShowMouseOverInfoEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireShowMouseOverInfoEvent
  GlobalOptions.addProperty("show.mouseover.info", Boolean.toString(jShowMouseOverInfo.isSelected()));
}//GEN-LAST:event_fireShowMouseOverInfoEvent

private void fireChangeClipboardWatchEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeClipboardWatchEvent
  if (bWatchClipboard) {
    jEnableClipboardWatchButton.setIcon(new ImageIcon("./graphics/icons/not_watch_clipboard.png"));
    bWatchClipboard = false;
  } else {
    jEnableClipboardWatchButton.setIcon(new ImageIcon("./graphics/icons/watch_clipboard.png"));
    bWatchClipboard = true;
  }
}//GEN-LAST:event_fireChangeClipboardWatchEvent

    private void fireHideNotificationEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideNotificationEvent
      mNotificationHideThread.interrupt();
      infoPanel.setCollapsed(true);
    }//GEN-LAST:event_fireHideNotificationEvent

    private void fireShowHideSupportsEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireShowHideSupportsEvent
      GlobalOptions.addProperty("include.support", Boolean.toString(jIncludeSupport.isSelected()));
      MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TROOP_LAYER);
    }//GEN-LAST:event_fireShowHideSupportsEvent

  public void doExit() {
    fireDSWorkbenchClosingEvent(null);
  }

  public void doExport() {
    //build attack table
    DefaultTableModel model = (DefaultTableModel) jAttackExportTable.getModel();
    int rows = model.getRowCount();
    for (int i = 0; i < rows; i++) {
      model.removeRow(0);
    }
    Iterator<String> plans = AttackManager.getSingleton().getGroupIterator();
    while (plans.hasNext()) {
      String next = plans.next();
      model.addRow(new Object[]{next, Boolean.FALSE});
    }

    //build marker set table
    String[] sets = MarkerManager.getSingleton().getGroups();
    model = (DefaultTableModel) jMarkerSetExportTable.getModel();
    rows = model.getRowCount();
    for (int i = 0; i < rows; i++) {
      model.removeRow(0);
    }

    for (String set : sets) {
      model.addRow(new Object[]{set, Boolean.FALSE});
    }

    //build report set table
    String[] reportSets = ReportManager.getSingleton().getGroups();

    model = (DefaultTableModel) jReportSetExportTable.getModel();
    rows = model.getRowCount();
    for (int i = 0; i < rows; i++) {
      model.removeRow(0);
    }

    for (String set : reportSets) {
      model.addRow(new Object[]{set, Boolean.FALSE});
    }

    //build troop table 
    String[] troopSets = TroopsManager.getSingleton().getGroups();

    model = (DefaultTableModel) jTroopSetExportTable.getModel();
    rows = model.getRowCount();
    for (int i = 0; i < rows; i++) {
      model.removeRow(0);
    }

    for (String set : troopSets) {
      model.addRow(new Object[]{set, Boolean.FALSE});
    }
    //build note table 
    String[] noteSets = NoteManager.getSingleton().getGroups();

    model = (DefaultTableModel) jNoteSetExportTable.getModel();
    rows = model.getRowCount();
    for (int i = 0; i < rows; i++) {
      model.removeRow(0);
    }

    for (String set : noteSets) {
      model.addRow(new Object[]{set, Boolean.FALSE});
    }
    jExportDialog.setAlwaysOnTop(true);
    jExportDialog.setVisible(true);
  }

  public void doImport() {
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
      dir = ".";
    }
    JFileChooser chooser = null;
    try {
      chooser = new JFileChooser(dir);
    } catch (Exception e) {
      JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
      return;
    }
    chooser.setDialogTitle("Datei auswählen");

    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

      @Override
      public boolean accept(File f) {
          return (f != null) && (f.isDirectory() || f.getName().endsWith(".xml"));

      }

      @Override
      public String getDescription() {
        return "*.xml";
      }
    });
    int ret = chooser.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
      try {
        File f = chooser.getSelectedFile();
        String file = f.getCanonicalPath();
        if (!file.endsWith(".xml")) {
          file += ".xml";
        }

        File target = new File(file);

        String extension = JOptionPane.showInputDialog(this, "Welche Kennzeichnung sollen importierte Pläne und Tags erhalten?\n" + "Lass das Eingabefeld leer oder drücke 'Abbrechen', um sie unverändert zu importieren.", "Kennzeichnung festlegen", JOptionPane.INFORMATION_MESSAGE);
        if (extension != null && extension.length() > 0) {
          logger.debug("Using import extension '" + extension + "'");
        } else {
          logger.debug("Using no import extension");
          extension = null;
        }

        if (target.exists()) {
          //do import
          String message = performImport(target, extension);
          JOptionPaneHelper.showInformationBox(this, message, "Import");
        }

        GlobalOptions.addProperty("screen.dir", target.getParent());
      } catch (Exception e) {
        logger.error("Failed to import data", e);
        JOptionPaneHelper.showErrorBox(this, "Import fehlgeschlagen.", "Import");
      }
    }
  }

  private String performImport(File pSource, String pExtension) {
    boolean attackImported = AttackManager.getSingleton().importData(pSource, pExtension);
    boolean markersImported = MarkerManager.getSingleton().importData(pSource, pExtension);
    boolean reportsImported = ReportManager.getSingleton().importData(pSource, pExtension);
    boolean tagImported = TagManager.getSingleton().importData(pSource, pExtension);
    boolean troopsImported = TroopsManager.getSingleton().importData(pSource, null);
    boolean formsImported = FormManager.getSingleton().importData(pSource, pExtension);
    boolean notesImported = NoteManager.getSingleton().importData(pSource, pExtension);

    String message = "Import beendet.\n";
    if (!attackImported) {
      message += "  * Fehler beim Import der Angriffe\n";
    }

    if (!markersImported) {
      message += "  * Fehler beim Import der Markierungen\n";
    }
    if (!reportsImported) {
      message += "  * Fehler beim Import der Berichte\n";
    }
    if (!tagImported) {
      message += "  * Fehler beim Import der Tags\n";
    }

    if (!troopsImported) {
      message += "  * Fehler beim Import der Truppen\n";
    }

    if (!formsImported) {
      message += "  * Fehler beim Import der Formen\n";
    }
    if (!notesImported) {
      message += "  * Fehler beim Import der Notizen\n";
    }
    return message;
  }

  public void planMapshot() {
    Component parent = this;

    putOnline = false;
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
      dir = ".";
    }

    String type = null;
    chooser.setSelectedFile(new File(dir));
    int ret = chooser.showSaveDialog(parent);
    if (ret == JFileChooser.APPROVE_OPTION) {
      try {
        File f = chooser.getSelectedFile();
        javax.swing.filechooser.FileFilter filter = chooser.getFileFilter();
        if (filter.getDescription().indexOf("jpeg") > 0) {
          type = "jpeg";
        } else if (filter.getDescription().indexOf("png") > 0) {
          type = "png";
        } else {
          type = "png";
        }
        String file = f.getCanonicalPath();
        if (!file.endsWith(type)) {
          file += "." + type;
        }
        File target = new File(file);
        if (target.exists()) {
          //ask if overwrite

          if (JOptionPaneHelper.showQuestionConfirmBox(parent, "Existierende Datei überschreiben?", "Überschreiben", "Nein", "Ja") != JOptionPane.YES_OPTION) {
            return;
          }
        }
        MapPanel.getSingleton().planMapShot(type, target, this);

        GlobalOptions.addProperty("screen.dir", target.getPath());
      } catch (Exception e) {
        logger.error("Failed to write map shot", e);
      }
    }
  }

  public boolean isWatchClipboard() {
    return bWatchClipboard;
  }

  /*
   * private void propagateLayerOrder() { DefaultListModel model = ((DefaultListModel) jLayerList.getModel());
   *
   * List<Integer> layerOrder = new LinkedList<Integer>(); for (int i = 0; i < model.size(); i++) { String value = (String) model.get(i);
   * layerOrder.add(Constants.LAYERS.get(value)); } MapPanel.getSingleton().getMapRenderer().setDrawOrder(layerOrder);
   * MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS); }
   */
  private void centerROI(int pId) {
    try {
      String item = (String) jROIBox.getItemAt(pId);
      item = item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")"));
      String[] pos = item.trim().split("\\|");
      jCenterX.setText(pos[0]);
      jCenterY.setText(pos[1]);
      fireRefreshMapEvent(null);
    } catch (Exception e) {
    }
  }

  /**
   * Check if zoom factor is valid and correct if needed
   */
  private void checkZoomRange() {
    if (dZoomFactor <= 0.4) {
      dZoomFactor = 0.4;
      jZoomOutButton.setEnabled(false);
    } else if (dZoomFactor >= 3) {
      dZoomFactor = 3;
      jZoomInButton.setEnabled(false);
    } else {
      jZoomInButton.setEnabled(true);
      jZoomOutButton.setEnabled(true);
    }
  }

  /**
   * Scroll the map
   */
  public void scroll(double pXDir, double pYDir) {
    dCenterX += pXDir;
    dCenterY += pYDir;
    if (ServerSettings.getSingleton().getCoordType() != 2) {
      int[] hier = DSCalculator.xyToHierarchical((int) dCenterX, (int) dCenterY);
      if (hier != null) {
        jCenterX.setText(Integer.toString(hier[0]));
        jCenterY.setText(Integer.toString(hier[1]));
      }
    } else {
      jCenterX.setText(Integer.toString((int) Math.floor(dCenterX)));
      jCenterY.setText(Integer.toString((int) Math.floor(dCenterY)));
    }

    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getBasicFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getBasicFieldHeight() * dZoomFactor;
    MinimapPanel.getSingleton().setSelection((int) Math.floor(dCenterX), (int) Math.floor(dCenterY), (int) Math.rint(w), (int) Math.rint(h));
    MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
  }

  /**
   * Center a village
   */
  public void centerVillage(Village pVillage) {
    if (pVillage == null) {
      return;
    }

    if (ServerSettings.getSingleton().getCoordType() != 2) {
      int[] hier = DSCalculator.xyToHierarchical((int) pVillage.getX(), (int) pVillage.getY());
      if (hier != null) {
        jCenterX.setText(Integer.toString(hier[0]));
        jCenterY.setText(Integer.toString(hier[1]));
      }

    } else {
      jCenterX.setText(Integer.toString(pVillage.getX()));
      jCenterY.setText(Integer.toString(pVillage.getY()));
    }

    fireRefreshMapEvent(null);
  }

  public void centerPosition(int xPos, int yPos) {
    if (ServerSettings.getSingleton().getCoordType() != 2) {
      int[] hier = DSCalculator.xyToHierarchical(xPos, yPos);
      if (hier != null) {
        jCenterX.setText(Integer.toString(hier[0]));
        jCenterY.setText(Integer.toString(hier[1]));
      }

    } else {
      jCenterX.setText(Integer.toString(xPos));
      jCenterY.setText(Integer.toString(yPos));
    }

    fireRefreshMapEvent(null);
  }

  /**
   * Get active user village
   */
  public Village getCurrentUserVillage() {
    try {
      if (jCurrentPlayerVillages.getSelectedIndex() < 0) {
        if (jCurrentPlayerVillages.getItemCount() > 0) {
          jCurrentPlayerVillages.setSelectedIndex(0);
        } else {
          //don't try to get village, list is still empty
          return null;
        }

      }
      return (Village) jCurrentPlayerVillages.getSelectedItem();
    } catch (ClassCastException cce) {
      //if no player was selected yet
      return null;
    } catch (Exception e) {
      logger.warn("Could not get current user village.", e);
      return null;
    }

  }

  public void setCurrentUserVillage(Village pVillage) {
    jCurrentPlayerVillages.setSelectedItem(pVillage);
  }

  protected void hideNotification() {
    infoPanel.setCollapsed(true);
  }

// <editor-fold defaultstate="collapsed" desc=" Listener EventHandlers ">
  @Override
  public void fireToolChangedEvent(int pTool) {
    jCurrentToolLabel.setIcon(ImageManager.getCursorImage(pTool));
  }

  @Override
  public void fireScrollEvent(double pX, double pY) {
    scroll(pX, pY);
  }

  @Override
  public void fireVisibilityChangedEvent(JFrame pSource, boolean v) {
    /*
     * if (pSource == DSWorkbenchAttackFrame.getSingleton()) {
     * jShowAttackFrame.setSelected(DSWorkbenchAttackFrame.getSingleton().isVisible()); } else if (pSource ==
     * DSWorkbenchMarkerFrame.getSingleton()) { jShowMarkerFrame.setSelected(DSWorkbenchMarkerFrame.getSingleton().isVisible()); } else
     * if (pSource == DSWorkbenchTroopsFrame.getSingleton()) {
     * jShowTroopsFrame.setSelected(DSWorkbenchTroopsFrame.getSingleton().isVisible()); } else if (pSource ==
     * DSWorkbenchRankFrame.getSingleton()) { jShowRankFrame.setSelected(DSWorkbenchRankFrame.getSingleton().isVisible()); } else if
     * (pSource == DSWorkbenchFormFrame.getSingleton()) { jShowFormsFrame.setSelected(DSWorkbenchFormFrame.getSingleton().isVisible());
     * } else if (pSource == DSWorkbenchChurchFrame.getSingleton()) {
     * jShowChurchFrame.setSelected(DSWorkbenchChurchFrame.getSingleton().isVisible()); } else if (pSource ==
     * DSWorkbenchConquersFrame.getSingleton()) { jShowConquersFrame.setSelected(DSWorkbenchConquersFrame.getSingleton().isVisible()); }
     * else if (pSource == DSWorkbenchNotepad.getSingleton()) {
     * jShowNotepadFrame.setSelected(DSWorkbenchNotepad.getSingleton().isVisible()); } else if (pSource ==
     * DSWorkbenchTagFrame.getSingleton()) { jShowTagFrame.setSelected(DSWorkbenchTagFrame.getSingleton().isVisible()); } else if
     * (pSource == DSWorkbenchStatsFrame.getSingleton()) {
     * jShowStatsFrame.setSelected(DSWorkbenchStatsFrame.getSingleton().isVisible()); } else if (pSource ==
     * DSWorkbenchReportFrame.getSingleton()) { jShowReportFrame.setSelected(DSWorkbenchReportFrame.getSingleton().isVisible()); }
     */
  }

  public void fireGroupParserEvent(Hashtable<String, List<Village>> pParserResult) {
    TagManager.getSingleton().invalidate();
    String[] groups = pParserResult.keySet().toArray(new String[]{});
    //NotifierFrame.doNotification("DS Workbench hat " + groups.length + ((groups.length == 1) ? " Dorfgruppe " : " Dorfgruppen ") + "in der Zwischenablage gefunden.", NotifierFrame.NOTIFY_INFO);
    showSuccess("DS Workbench hat " + groups.length + ((groups.length == 1) ? " Dorfgruppe " : " Dorfgruppen ") + "in der Zwischenablage gefunden.");
    if(groups.length!=1){ // Data from group import (all groups for given villages)
	    //remove all tags
	    for (String group : groups) {
	      List<Village> villagesForGroup = pParserResult.get(group);
	      if (villagesForGroup != null) {
	        for (Village v : villagesForGroup) {
	          TagManager.getSingleton().removeTags(v);
	        }
	      }
	    }
	
	    for (String group : groups) {
	      //add new groups
	      TagManager.getSingleton().addTagFast(group);
	      //get (added) group
	      Tag t = TagManager.getSingleton().getTagByName(group);
	      //add villages to group
	      List<Village> villagesForGroup = pParserResult.get(group);
	      if (villagesForGroup != null) {
	        //set new tags
	        for (Village v : villagesForGroup) {
	          t.tagVillage(v.getId());
	        }
	      }
	    }
    } else { // data from troops import (all villages for given group) 
    	for (String group : groups) {
	      //add new groups
	      TagManager.getSingleton().addTagFast(group);
	      //get (added) group
	      Tag t = TagManager.getSingleton().getTagByName(group);
	      t.clearTaggedVillages();
	      //add villages to group
	      List<Village> villagesForGroup = pParserResult.get(group);
	      if (villagesForGroup != null) {
	        //set new tags
	        for (Village v : villagesForGroup) {
	          t.tagVillage(v.getId());
	        }
	      }
	    }    	
    }
    TagManager.getSingleton().revalidate(true);
  }

  @Override
  public void fireMapShotDoneEvent() {
    Component parent = this;
    JOptionPaneHelper.showInformationBox(parent, "Kartengrafik erfolgreich gespeichert.", "Information");
    putOnline = false;
  }

  @Override
  public void fireMapShotFailedEvent() {
    JOptionPaneHelper.showErrorBox(this, "Fehler beim Speichern der Kartengrafik.", "Fehler");
  }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Generated Variables">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JButton jAddNewROIButton;
    private javax.swing.JButton jAddROIButton;
    private javax.swing.JDialog jAddROIDialog;
    private javax.swing.JTable jAttackExportTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jCancelExportButton;
    private javax.swing.JButton jCenterCoordinateIngame;
    private javax.swing.JButton jCenterIngameButton;
    private javax.swing.JTextField jCenterX;
    private javax.swing.JTextField jCenterY;
    private javax.swing.JLabel jCurrentPlayer;
    private javax.swing.JComboBox jCurrentPlayerVillages;
    private javax.swing.JLabel jCurrentToolLabel;
    private javax.swing.JPanel jCustomPanel;
    private javax.swing.JButton jEnableClipboardWatchButton;
    private javax.swing.JButton jExportButton;
    private javax.swing.JCheckBox jExportChurches;
    private javax.swing.JDialog jExportDialog;
    private javax.swing.JCheckBox jExportForms;
    private javax.swing.JCheckBox jExportTags;
    private javax.swing.JComboBox jGraphicPacks;
    private javax.swing.JCheckBox jHighlightTribeVillages;
    private javax.swing.JTextField jHourField;
    private javax.swing.JCheckBox jIncludeSupport;
    private javax.swing.JPanel jInformationPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jMapPanel;
    private javax.swing.JPanel jMapPanelHolder;
    private javax.swing.JTable jMarkerSetExportTable;
    private javax.swing.JPanel jMinimapPanel;
    private javax.swing.JTextField jMinuteField;
    private javax.swing.JButton jMoveE;
    private javax.swing.JButton jMoveE1;
    private javax.swing.JButton jMoveN;
    private javax.swing.JButton jMoveNE;
    private javax.swing.JButton jMoveNW;
    private javax.swing.JButton jMoveS;
    private javax.swing.JButton jMoveSE;
    private javax.swing.JButton jMoveSW;
    private javax.swing.JButton jMoveW;
    private javax.swing.JPanel jNavigationPanel;
    private javax.swing.JTable jNoteSetExportTable;
    private javax.swing.JLabel jOnlineLabel;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JComboBox jROIBox;
    private javax.swing.JPanel jROIPanel;
    private javax.swing.JComboBox jROIPosition;
    private javax.swing.JTextField jROIRegion;
    private javax.swing.JTextField jROITextField;
    private javax.swing.JButton jRefreshButton;
    private javax.swing.JButton jRemoveROIButton;
    private javax.swing.JTable jReportSetExportTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jSettingsScrollPane;
    private javax.swing.JCheckBox jShowMapPopup;
    private javax.swing.JCheckBox jShowMouseOverInfo;
    private javax.swing.JCheckBox jShowRuler;
    private javax.swing.JPanel jStatusPanel;
    private javax.swing.JTable jTroopSetExportTable;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXTipOfTheDay jXTipOfTheDay1;
    private javax.swing.JButton jZoomInButton;
    private javax.swing.JButton jZoomOutButton;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}

class NotificationHideThread extends Thread {

  public NotificationHideThread() {
    setName("NotificationHideThread");
    setPriority(MIN_PRIORITY);
    setDaemon(true);
  }

  public void run() {
    boolean interrupted = false;
    while (true) {
      if (!interrupted) {
        DSWorkbenchMainFrame.getSingleton().hideNotification();
      }
      try {
        Thread.sleep(10000);
        interrupted = false;
      } catch (InterruptedException e) {
        interrupted = true;
      }
    }
  }
}

class BackupTask extends TimerTask {

  private static Logger logger = Logger.getLogger("BackupTask");

  @Override
  public void run() {
    try {
      logger.debug("Starting backup");
      String exportString = "<export>\n";
      logger.debug(" - Backing up attacks");
      exportString += AttackManager.getSingleton().getExportData(Arrays.asList(AttackManager.getSingleton().getGroups()));
      logger.debug(" - Backing up markers");
      exportString += MarkerManager.getSingleton().getExportData(Arrays.asList(MarkerManager.getSingleton().getGroups()));
      logger.debug(" - Backing up reports");
      exportString += ReportManager.getSingleton().getExportData(Arrays.asList(ReportManager.getSingleton().getGroups()));
      logger.debug(" - Backing up tags");
      exportString += TagManager.getSingleton().getExportData(null);
      logger.debug(" - Backing up troops");
      exportString += TroopsManager.getSingleton().getExportData(Arrays.asList(TroopsManager.getSingleton().getGroups()));
      logger.debug(" - Backing up forms");
      exportString += FormManager.getSingleton().getExportData(null);
      logger.debug(" - Backing up notes");
      exportString += NoteManager.getSingleton().getExportData(Arrays.asList(NoteManager.getSingleton().getGroups()));
      logger.debug(" - Backing up churches");
      exportString += ChurchManager.getSingleton().getExportData(null);
      logger.debug(" - Backing up farms");
      exportString += FarmManager.getSingleton().getExportData(null);
      exportString += "</export>";
      logger.debug("Writing backup data to disk");
      FileWriter w = new FileWriter(GlobalOptions.getSelectedProfile().getProfileDirectory() + "/backup.xml");
      w.write(exportString);
      w.flush();
      w.close();
      logger.debug("Backup finished successfully");
    } catch (Exception e) {
      logger.error("Failed to create backup", e);
    }

  }
}
