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
package de.tor.tribes.util.report;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.util.Filter;
import de.tor.tribes.util.farm.FarmManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author extremeCrazyCoder
 */
public class ReportRule implements Filter<FightReport> {
    Logger logger = LogManager.getLogger("ReportRule");
    
    public static final int GREY = 1;
    public static final int BLUE = 2;
    public static final int RED = 4;
    public static final int YELLOW = 8;
    public static final int GREEN = 16;
    
    public enum RuleType {
        AGE,
        ATTACKER_ALLY,
        ATTACKER_TRIBE,
        CATA,
        COLOR,
        CONQUERED,
        DATE,
        DEFENDER_ALLY,
        DEFENDER_TRIBE,
        FAKE,
        FARM,
        OFF,
        WALL,
    }
    
    private RuleType type;
    private Object filterComponent;
    private String targetSet;
    
    public ReportRule(RuleType pType, Object pFilterComponent, String pTargetSet) throws IllegalArgumentException {
        try {
            type = pType;
            filterComponent = pFilterComponent;
            targetSet = pTargetSet;

            //check arguments and throw Exception if illigal
            switch(type) {
            case AGE:
                Long maxAge = (Long) pFilterComponent;
                if(maxAge < 0) throw new IllegalArgumentException("Age < 0");
                break;
            case ATTACKER_ALLY:
                List<Ally> attAllyList = (List<Ally>) pFilterComponent;
                for(int i = attAllyList.size() - 1; i >= 0; i--) {
                    if(attAllyList.get(i) == null) {
                        attAllyList.remove(i);
                        logger.debug("removed entry {}", i);
                    }
                }
                if(attAllyList.isEmpty()) throw new IllegalArgumentException("List empty!");
                break;
            case ATTACKER_TRIBE:
                List<Tribe> attTribeList = (List<Tribe>) pFilterComponent;
                for(int i = attTribeList.size() - 1; i >= 0; i--) {
                    if(attTribeList.get(i) == null) {
                        attTribeList.remove(i);
                        logger.debug("removed entry {}", i);
                    }
                }
                if(attTribeList.isEmpty()) throw new IllegalArgumentException("List empty!");
                break;
            case COLOR:
                if((((Integer) pFilterComponent) & 0x1F) == 0)
                    throw new IllegalArgumentException("No color");
                break;
            case DATE:
                Range<Long> dates = (Range<Long>) pFilterComponent;
                if(dates.getMinimum().equals(dates.getMaximum()))
                    throw new IllegalArgumentException("Empty Range");
                break;
            case DEFENDER_ALLY:
                List<Ally> defAllyList = (List<Ally>) pFilterComponent;
                for(int i = defAllyList.size() - 1; i >= 0; i--) {
                    if(defAllyList.get(i) == null) {
                        defAllyList.remove(i);
                        logger.debug("removed entry {}", i);
                    }
                }
                if(defAllyList.isEmpty()) throw new IllegalArgumentException("List empty!");
                break;
            case DEFENDER_TRIBE:
                List<Tribe> defTribeList = (List<Tribe>) pFilterComponent;
                for(int i = defTribeList.size() - 1; i >= 0; i--) {
                    if(defTribeList.get(i) == null) {
                        defTribeList.remove(i);
                        logger.debug("removed entry {}", i);
                    }
                }
                if(defTribeList.isEmpty()) throw new IllegalArgumentException("List empty!");
                break;
            case CATA:
            case CONQUERED:
            case FAKE:
            case FARM:
            case OFF:
            case WALL:
                filterComponent = null;
                break;
            default:
                throw new IllegalArgumentException("wrong type");
            }
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public ReportRule(Element pElm) throws IllegalArgumentException {
        try {
            type = RuleType.valueOf(pElm.getChildText("type"));
            targetSet = pElm.getChildText("targetSet");
            Element settings = pElm.getChild("settings");

            //check arguments and throw Exception if illigal
            switch(type) {
            case AGE:
                Long maxAge = Long.parseLong(settings.getText());
                filterComponent = maxAge;
                break;
            case ATTACKER_ALLY:
                List<Ally> attAllyList = new ArrayList<>();
                for(Element e: settings.getChildren("ally")) {
                    int id = Integer.parseInt(e.getText());
                    attAllyList.add(DataHolder.getSingleton().getAllies().get(id));
                }
                filterComponent = attAllyList;
                break;
            case ATTACKER_TRIBE:
                List<Tribe> attTribeList = new ArrayList<>();
                for(Element e: settings.getChildren("tribe")) {
                    int id = Integer.parseInt(e.getText());
                    attTribeList.add(DataHolder.getSingleton().getTribes().get(id));
                }
                filterComponent = attTribeList;
                break;
            case COLOR:
                Integer color = Integer.parseInt(settings.getText());
                filterComponent = color;
                break;
            case DATE:
                String dates[] = settings.getText().split("-");
                Long start = Long.parseLong(dates[0]);
                Long end = Long.parseLong(dates[1]);
                Range<Long> dateSpan = Range.between(start, end);
                filterComponent = dateSpan;
                break;
            case DEFENDER_ALLY:
                List<Ally> defAllyList = new ArrayList<>();
                for(Element e: settings.getChildren("ally")) {
                    int id = Integer.parseInt(e.getText());
                    defAllyList.add(DataHolder.getSingleton().getAllies().get(id));
                }
                filterComponent = defAllyList;
                break;
            case DEFENDER_TRIBE:
                List<Tribe> defTribeList = new ArrayList<>();
                for(Element e: settings.getChildren("tribe")) {
                    int id = Integer.parseInt(e.getText());
                    defTribeList.add(DataHolder.getSingleton().getTribes().get(id));
                }
                filterComponent = defTribeList;
                break;
            case CATA:
            case CONQUERED:
            case FAKE:
            case FARM:
            case OFF:
            case WALL:
                filterComponent = null;
                break;
            }
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Element toXml(String name) {
        Element rule = new Element(name);
        rule.addContent(new Element("type").setText(type.name()));
        rule.addContent(new Element("targetSet").setText(targetSet));
        Element settings = new Element("settings");
        
        switch(type) {
        case AGE:
            Long maxAge = (Long) filterComponent;
            settings.setText(maxAge.toString());
            break;
        case ATTACKER_ALLY:
            List<Ally> attAllyList = (List<Ally>) filterComponent;
            for(Ally a: attAllyList) {
                settings.addContent(new Element("ally").setText(Integer.toString(a.getId())));
            }
            break;
        case ATTACKER_TRIBE:
            List<Tribe> attTribeList = (List<Tribe>) filterComponent;
            for(Tribe t: attTribeList) {
                settings.addContent(new Element("tribe").setText(Integer.toString(t.getId())));
            }
            break;
        case COLOR:
            Integer color = (Integer) filterComponent;
            settings.setText(color.toString());
            break;
        case DATE:
            Range<Long> dates = (Range<Long>) filterComponent;
            settings.setText(dates.getMinimum() + "-" + dates.getMaximum());
            break;
        case DEFENDER_ALLY:
            List<Ally> defAllyList = (List<Ally>) filterComponent;
            for(Ally a: defAllyList) {
                settings.addContent(new Element("ally").setText(Integer.toString(a.getId())));
            }
            break;
        case DEFENDER_TRIBE:
            List<Tribe> defTribeList = (List<Tribe>) filterComponent;
            for(Tribe t: defTribeList) {
                settings.addContent(new Element("tribe").setText(Integer.toString(t.getId())));
            }
            break;
        case CATA:
        case CONQUERED:
        case FAKE:
        case FARM:
        case OFF:
        case WALL:
            break;
        default:
            throw new IllegalArgumentException("wrong type");
        }
        
        rule.addContent(settings);
        return rule;
    }

    @Override
    public boolean isValid(FightReport c) {
        if(c == null) return false;
        
        switch(type) {
        case AGE:
            return c.getTimestamp() < System.currentTimeMillis() - (Long) filterComponent;
        case ATTACKER_ALLY:
            Ally a = (c.getAttacker() != null) ? c.getAttacker().getAlly() : NoAlly.getSingleton();
            return ((List<Ally>) filterComponent).contains(a);
        case ATTACKER_TRIBE:
            if(c.getAttacker() == null) return false;
            return ((List<Tribe>) filterComponent).contains(c.getAttacker());
        case CATA:
            return c.wasBuildingDamaged();
        case COLOR:
            int value;
            if (c.areAttackersHidden()) {
                value = GREY;
            } else if (c.isSpyReport()) {
                value = BLUE;
            } else if (c.wasLostEverything()) {
                value = RED;
            } else if (c.wasLostNothing()) {
                value = GREEN;
            } else {
                value = YELLOW;
            }
            return ((((Integer) filterComponent) & value) > 0);
        case CONQUERED:
            return (c.wasConquered() || c.guessType() == Attack.SNOB_TYPE);
        case DATE:
            return ((Range<Long>) filterComponent).contains(c.getTimestamp());
        case DEFENDER_ALLY:
            Ally d = (c.getDefender()!= null) ? c.getDefender().getAlly() : NoAlly.getSingleton();
            return ((List<Ally>) filterComponent).contains(d);
        case DEFENDER_TRIBE:
            if(c.getDefender() == null) return false;
            return ((List<Tribe>) filterComponent).contains(c.getDefender());
        case FAKE:
            return (c.guessType() == Attack.FAKE_TYPE);
        case FARM:
            return FarmManager.getSingleton().getFarmInformation(c.getTargetVillage()) != null;
        case OFF:
            return (c.guessType() == Attack.CLEAN_TYPE);
        case WALL:
            return c.wasWallDamaged();
        default:
            throw new IllegalArgumentException("wrong type");
        }
    }
    
    public String getDescription() {
        switch(type) {
        case AGE:
            return "Filterung nach Alter";
        case ATTACKER_ALLY:
            return "Filterung nach Stamm des Angreifers";
        case ATTACKER_TRIBE:
            return "Filterung nach Angreifer";
        case CATA:
            return "Filtert Berichte mit Gebäudebeschädigung";
        case COLOR:
            return "Filterung nach der Farbe eines Berichts";
        case CONQUERED:
            return "Filtert Berichte mit AGs";
        case DATE:
            return "Filterung nach Datum";
        case DEFENDER_ALLY:
            return "Filterung nach Stamm des Verteidigers";
        case DEFENDER_TRIBE:
            return "Filterung nach Verteidiger";
        case FAKE:
            return "Filtert Fake-Berichte";
        case FARM:
            return "Filterung von Farmberichten";
        case OFF:
            return "Filtert Off-Berichte";
        case WALL:
            return "Filtert Berichte mit Wallbeschädigung";
        default:
            throw new IllegalArgumentException("wrong type");
        }
    }

    public String getStringRepresentation() {
        switch(type) {
        case AGE:
            return "Bericht älter als " + DurationFormatUtils
                    .formatDuration((Long) filterComponent, "dd", false) + " Tag(e)";
        case ATTACKER_ALLY:
            return "Angreifende Stämme " + StringUtils.join((List<Ally>) filterComponent, ", ");
        case ATTACKER_TRIBE:
            return "Angreifer " + StringUtils.join((List<Tribe>) filterComponent, ", ");
        case CATA:
            return "Berichte mit Gebäudebeschädigung";
        case COLOR:
            StringBuilder result = new StringBuilder();
            int color = (Integer) filterComponent;
            result.append("Farben:");
            if ((color & GREY) > 0)
                result.append(" grau");
            
            if ((color & BLUE) > 0)
                result.append(" blau");

            if ((color & GREEN) > 0)
                result.append(" grün");

            if ((color & YELLOW) > 0)
                result.append(" gelb");

            if ((color & RED) > 0)
                result.append(" rot");

            return result.toString();
        case CONQUERED:
            return "AG-Angriffe/Eroberungen";
        case DATE:
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");
            Range<Long> dates = (Range<Long>) filterComponent;
            return "Gesendet zwischen " + df.format(new Date(dates.getMinimum())) +
                    " und " + df.format(new Date(dates.getMaximum()));
        case DEFENDER_ALLY:
            return "Verteidigende Stämme " + StringUtils.join((List<Ally>) filterComponent, ", ");
        case DEFENDER_TRIBE:
            return "Verteidiger " + StringUtils.join((List<Tribe>) filterComponent, ", ");
        case FAKE:
            return "Fakes";
        case FARM:
            return "Farmberichte";
        case OFF:
            return "Off-Berichte";
        case WALL:
            return "Berichte mit Wallbeschädigung";
        default:
            throw new IllegalArgumentException("wrong type");
        }
    }

    public void setTargetSet(String targetSet) {
      this.targetSet = targetSet;
    }

    public String getTargetSet() {
      return targetSet;
    }

    public RuleType getType() {
      return type;
    }

    public Object getComponent() {
      return type;
    }
    
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof ReportRule)) return false;
        
        ReportRule o = (ReportRule) other;
        if(o.getType() != type) return false;
        if(o.getComponent() != null && !o.getComponent().equals(other)) return false;
        
        return o.getTargetSet().equals(targetSet);
    }
}
