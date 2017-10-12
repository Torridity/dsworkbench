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
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.awt.Toolkit;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


/**
 * Default for all Global settings
 * that are used by almost all components. e.g. WorldData or UI specific objects
 *
 * @author Charon
 * @author extremeCrazyCoder
 */

public class GlobalDefaults {
    private static Logger logger = Logger.getLogger("GlobalDefaults");
    private static boolean INITIALIZED = false;

    private static PropertiesConfiguration GLOBAL_DEFAULTS = null;

    /**
     * Init all managed objects
     *
     * @throws Exception If an Error occurs while initializing the objects
     */
    public static void initialize() {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        logger.debug("Loading defaults");
        loadDefaults();
    }
    
    /**
     * Add a property
     */
    
    public static void loadDefaults() {
        GLOBAL_DEFAULTS = new PropertiesConfiguration();
        GLOBAL_DEFAULTS.addProperty("attack.movement", false);
        GLOBAL_DEFAULTS.addProperty("attack.planer.check.amount", 20000);
        GLOBAL_DEFAULTS.addProperty("attack.planer.enable.check", true);
        GLOBAL_DEFAULTS.addProperty("attack.script.attacks.in.overview", true);
        GLOBAL_DEFAULTS.addProperty("attack.script.attacks.in.place", true);
        GLOBAL_DEFAULTS.addProperty("attack.script.attacks.in.village.info", true);
        GLOBAL_DEFAULTS.addProperty("attack.script.attacks.on.confirm.page", true);
        GLOBAL_DEFAULTS.addProperty("check.updates.on.startup", true);
        GLOBAL_DEFAULTS.addProperty("church.frame.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("clipboard.notification", true);
        GLOBAL_DEFAULTS.addProperty("clock.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("command.sleep.time", 150);
        GLOBAL_DEFAULTS.addProperty("conquers.frame.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("default.browser", "");
        GLOBAL_DEFAULTS.addProperty("default.mark", 0);
        GLOBAL_DEFAULTS.addProperty("default.skin", "default");
        GLOBAL_DEFAULTS.addProperty("delete.farm.reports.on.exit", true);
        GLOBAL_DEFAULTS.addProperty("draw.attacks.by.default", false);
        GLOBAL_DEFAULTS.addProperty("extended.attack.vectors", false);
        GLOBAL_DEFAULTS.addProperty("extended.text.attacks", false);
        GLOBAL_DEFAULTS.addProperty("form.config.frame.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("half.ribbon.size", false);
        GLOBAL_DEFAULTS.addProperty("highlight.tribes.villages", false);
        GLOBAL_DEFAULTS.addProperty("include.support", true);
        GLOBAL_DEFAULTS.addProperty("inform.on.updates", true);
        GLOBAL_DEFAULTS.addProperty("main.size.height", (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 50);
        GLOBAL_DEFAULTS.addProperty("main.size.width", (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 50);
        GLOBAL_DEFAULTS.addProperty("map.marker.transparency", 80);
        GLOBAL_DEFAULTS.addProperty("map.showcontinents", true);
        GLOBAL_DEFAULTS.addProperty("map.zoom.max", 3.0);
        GLOBAL_DEFAULTS.addProperty("map.zoom.min", 0.4);
        GLOBAL_DEFAULTS.addProperty("map.zoom.in.out.factor", 1.06);
        GLOBAL_DEFAULTS.addProperty("mark.villages.on.minimap", true);
        GLOBAL_DEFAULTS.addProperty("max.density.troops", 65000);
        GLOBAL_DEFAULTS.addProperty("max.farm.space", 20000);
        GLOBAL_DEFAULTS.addProperty("max.loss.ratio", 50);
        GLOBAL_DEFAULTS.addProperty("max.sim.rounds", 500);
        GLOBAL_DEFAULTS.addProperty("no.welcome", false);
        GLOBAL_DEFAULTS.addProperty("notify.duration", 1);
        GLOBAL_DEFAULTS.addProperty("obst.server", "");
        GLOBAL_DEFAULTS.addProperty("parser.movement.plan", "imported");
        GLOBAL_DEFAULTS.addProperty("parser.movement.delete.all.on.import", "false");
        GLOBAL_DEFAULTS.addProperty("proxySet", false);
        GLOBAL_DEFAULTS.addProperty("proxyHost", "");
        GLOBAL_DEFAULTS.addProperty("proxyPort", 8080);
        GLOBAL_DEFAULTS.addProperty("proxyType", 0);
        GLOBAL_DEFAULTS.addProperty("proxyUser", "");
        GLOBAL_DEFAULTS.addProperty("proxyPassword", "");
        GLOBAL_DEFAULTS.addProperty("radar.size", 60); //in minutes
        GLOBAL_DEFAULTS.addProperty("red.width", 0);
        GLOBAL_DEFAULTS.addProperty("red.height", 0);
        GLOBAL_DEFAULTS.addProperty("report.server.port", 8080);
        GLOBAL_DEFAULTS.addProperty("ribbon.minimized", false);
        GLOBAL_DEFAULTS.addProperty("screen.dir", ".");
        GLOBAL_DEFAULTS.addProperty("search.frame.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("show.barbarian", true);
        GLOBAL_DEFAULTS.addProperty("show.live.countdown", true);
        GLOBAL_DEFAULTS.addProperty("show.map.popup", true);
        GLOBAL_DEFAULTS.addProperty("show.mouseover.info", false);
        GLOBAL_DEFAULTS.addProperty("show.popup.conquers", true);
        GLOBAL_DEFAULTS.addProperty("show.popup.farm.space", true);
        GLOBAL_DEFAULTS.addProperty("show.popup.ranks", true);
        GLOBAL_DEFAULTS.addProperty("show.sectors", true);
        GLOBAL_DEFAULTS.addProperty("show.ruler", true);
        GLOBAL_DEFAULTS.addProperty("show.church", true);
        GLOBAL_DEFAULTS.addProperty("show.watchtower", true);
        GLOBAL_DEFAULTS.addProperty("show.ruler", true);
        GLOBAL_DEFAULTS.addProperty("sos.mark.all.duplicates.as.fake", "true");
        GLOBAL_DEFAULTS.addProperty("standard.defense.split", "heavy=0/spy=50/sword=500/archer=500/spear=500");
        GLOBAL_DEFAULTS.addProperty("standard.off", "catapult=50/light=3000/ram=300/axe=7000/marcher=500");
        GLOBAL_DEFAULTS.addProperty("stats.frame.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("support.tolerance", 0);
        GLOBAL_DEFAULTS.addProperty("systray.enabled", SystrayHelper.isSystraySupported());
        GLOBAL_DEFAULTS.addProperty("tag.frame.menu.visible", true);
        GLOBAL_DEFAULTS.addProperty("tag.frame.table.visibility", "true;true;true;false");
        GLOBAL_DEFAULTS.addProperty("tap.height", 0);
        GLOBAL_DEFAULTS.addProperty("tap.width", 0);
        GLOBAL_DEFAULTS.addProperty("text.attacks.per.file", "10");
        GLOBAL_DEFAULTS.addProperty("village.order", 0);
        GLOBAL_DEFAULTS.addProperty("watchtower.frame.alwaysOnTop", false);
        GLOBAL_DEFAULTS.addProperty("zip.text.attacks", false);
        
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            GLOBAL_DEFAULTS.addProperty(unit.getName() + ".color", "#ff0000");
        }
        
        GLOBAL_DEFAULTS.addProperty("layer.order",
                "Markierungen;" +
                "Dörfer;" +
                "Dorfsymbole;" +
                "Truppendichte;" +
                "Notizmarkierungen;" +
                "Angriffe;" +
                "Unterstützungen;" +
                "Zeichnungen;" +
                "Kirchenradien;" +
                "Wachturmradien");
      
        //Default templates
        GLOBAL_DEFAULTS.addProperty("attack.template.header", "<Standard>");
        GLOBAL_DEFAULTS.addProperty("attack.template.header.internal", "templates/attack_header.tmpl");
        GLOBAL_DEFAULTS.addProperty("attack.template.block", "<Standard>");
        GLOBAL_DEFAULTS.addProperty("attack.template.block.internal", "templates/attack_block.tmpl");
        GLOBAL_DEFAULTS.addProperty("attack.template.footer", "<Standard>");
        GLOBAL_DEFAULTS.addProperty("attack.template.footer.internal", "templates/attack_footer.tmpl");
        GLOBAL_DEFAULTS.addProperty("report.list.bbexport.template",
                de.tor.tribes.util.bb.ReportListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("attack.bbexport.template",
                de.tor.tribes.util.AttackToBBCodeFormater.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("ally.report.stats.bbexport.template",
                de.tor.tribes.util.bb.AllyReportStatsFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("attack.list.bbexport.template",
                de.tor.tribes.util.bb.AttackListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("def.stats.bbexport.template",
                de.tor.tribes.util.bb.DefStatsFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("form.list.bbexport.template",
                de.tor.tribes.util.bb.FormListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("kills.stats.bbexport.template",
                de.tor.tribes.util.bb.KillStatsFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("marker.list.bbexport.template",
                de.tor.tribes.util.bb.MarkerListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("note.list.bbexport.template",
                de.tor.tribes.util.bb.NoteListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("overall.report.stats.bbexport.template",
                de.tor.tribes.util.bb.OverallReportStatsFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("point.stats.bbexport.template",
                de.tor.tribes.util.bb.PointStatsFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("report.list.bbexport.template",
                de.tor.tribes.util.bb.ReportListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("sos.list.bbexport.template",
                de.tor.tribes.util.bb.SosListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("tag.list.bbexport.template",
                de.tor.tribes.util.bb.TagListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("tribe.report.stats.bbexport.template",
                de.tor.tribes.util.bb.TribeReportStatsFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("troops.list.bbexport.template",
                de.tor.tribes.util.bb.TroopListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("village.list.bbexport.template",
                de.tor.tribes.util.bb.VillageListFormatter.STANDARD_TEMPLATE);
        
        GLOBAL_DEFAULTS.addProperty("winner.loser.stats.bbexport.template",
                de.tor.tribes.util.bb.WinnerLoserStatsFormatter.STANDARD_TEMPLATE);
        
        //TODO set to true after Moral is working
        GLOBAL_DEFAULTS.addProperty("show.popup.moral", ServerSettings.getSingleton()
                .getMoralType() == ServerSettings.POINTBASED_MORAL);
    }
    
    /**
     * Get the value of a property
     */
    public static String getProperty(String pKey) {
        if (pKey == null) {
            return null;
        }
        if(!INITIALIZED) initialize();
        
        Object property = GLOBAL_DEFAULTS.getProperty(pKey);
        if (property != null) {
            return property.toString();
        } else {
            return null;
        }
    }

    public static PropertiesConfiguration getProperties() {
        if(!INITIALIZED) initialize();
        return GLOBAL_DEFAULTS;
    }
    
    public static void reinit() {
        GLOBAL_DEFAULTS.clear();
        loadDefaults();
    }
}
