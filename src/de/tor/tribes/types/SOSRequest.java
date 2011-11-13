/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.support.SOSFormater;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class SOSRequest implements BBSupport {

    private final String[] VARIABLES = new String[]{"%SOS_ICON%", "%TARGET%", "%ATTACKS%", "%DEFENDERS%", "%WALL_INFO%", "%WALL_LEVEL%", "%FIRST_ATTACK%", "%LAST_ATTACK%", "%SOURCE_LIST%", "%SOURCE_DATE_TYPE_LIST%", "%ATTACK_LIST%", "%SOURCE_DATE_LIST%", "%SOURCE_TYPE_LIST%", "%SUMMARY%"};
    private final static String STANDARD_TEMPLATE = "[quote]%SOS_ICON% %TARGET% (%ATTACKS%)\n[quote]%DEFENDERS%\n%WALL_INFO%[/quote]\n\n%FIRST_ATTACK%\n%SOURCE_DATE_LIST%\n%LAST_ATTACK%\n\n%SUMMARY%[/quote]";

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    public String[] getReplacementsForTarget(Village pTarget, boolean pExtended) {
        String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        //main quote

        //village info size
        String sosImageVal = "[img]" + serverURL + "/graphic/reqdef.png[/img]";
        String targetVal = pTarget.toBBCode();
        String attackCountVal = "[img]" + serverURL + "/graphic/unit/att.png[/img] " + attacks.get(pTarget).getAttacks().size();
        //village details quote

        //add units and wall
        String unitVal = buildUnitInfo(attacks.get(pTarget));
        String wallInfoVal = "[img]" + serverURL + "/graphic/buildings/wall.png[/img] " + buildWallInfo(attacks.get(pTarget));
        String wallLevelVal = Integer.toString(attacks.get(pTarget).getWallLevel());

        //build first-last-attack

        List<TimedAttack> atts = attacks.get(pTarget).getAttacks();

        Collections.sort(atts, SOSRequest.ARRIVE_TIME_COMPARATOR);

        //add first and last attack information
        SimpleDateFormat dateFormat = null;
        if (ServerSettings.getSingleton().isMillisArrival()) {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
        String firstAttackVal = "[img]" + serverURL + "/graphic/map/attack.png[/img] " + dateFormat.format(new Date(atts.get(0).getlArriveTime()));

        //add details for all attacks
        int fakeCount = 0;
        int snobCount = 0;
        String sourceVal = "";
        String sourceDateVal = "";
        String sourceDateTypeVal = "";
        String attackList = "";
        String sourceTypeVal = "";
        List<Attack> thisAttacks = new ArrayList<Attack>();
        for (int i = 0; i < atts.size(); i++) {
            try {
                TimedAttack attack = atts.get(i);
                Attack a = new Attack();
                a.setSource(attack.getSource());
                a.setTarget(pTarget);
                a.setArriveTime(new Date(attack.getlArriveTime()));
                if (attack.isPossibleFake()) {
                    fakeCount++;
                    a.setType(Attack.FAKE_TYPE);
                } else if (attack.isPossibleSnob()) {
                    snobCount++;
                    a.setType(Attack.SNOB_TYPE);
                    a.setUnit(DataHolder.getSingleton().getUnitByPlainName("snob"));
                }
                if (a.getUnit() == null) {
                    a.setUnit(UnknownUnit.getSingleton());
                }
                thisAttacks.add(a);
            } catch (Exception e) {
            }
        }

        attackList = new AttackListFormatter().formatElements(thisAttacks, pExtended);

        for (int i = 0; i < atts.size(); i++) {
            try {
                TimedAttack attack = atts.get(i);
                sourceVal += attack.getSource().toBBCode() + "\n";
                if (attack.isPossibleFake()) {
                    sourceDateTypeVal += attack.getSource().toBBCode() + " " + dateFormat.format(new Date(attack.getlArriveTime())) + " [b](Fake)[/b]" + "\n";
                    sourceDateVal += attack.getSource().toBBCode() + " " + dateFormat.format(new Date(attack.getlArriveTime())) + "\n";
                    sourceTypeVal += attack.getSource().toBBCode() + " [b](Fake)[/b]" + "\n";
                } else if (attack.isPossibleSnob()) {
                    sourceDateTypeVal += attack.getSource().toBBCode() + " " + dateFormat.format(new Date(attack.getlArriveTime())) + " [b](AG)[/b]" + "\n";
                    sourceDateVal += attack.getSource().toBBCode() + " " + dateFormat.format(new Date(attack.getlArriveTime())) + "\n";
                    sourceTypeVal += attack.getSource().toBBCode() + " [b](AG)[/b]" + "\n";
                } else {
                    sourceDateTypeVal += attack.getSource().toBBCode() + " " + dateFormat.format(new Date(attack.getlArriveTime())) + "\n";
                    sourceDateVal += attack.getSource().toBBCode() + " " + dateFormat.format(new Date(attack.getlArriveTime())) + "\n";
                    sourceTypeVal += attack.getSource().toBBCode() + "\n";
                }
            } catch (Exception e) {
            }
        }

        sourceVal = sourceVal.trim();
        sourceTypeVal = sourceTypeVal.trim();
        sourceDateVal = sourceDateVal.trim();
        sourceDateTypeVal = sourceDateTypeVal.trim();
        String lastAttackVal = "[img]" + serverURL + "/graphic/map/return.png[/img] " + dateFormat.format(new Date(atts.get(atts.size() - 1).getlArriveTime()));
        String summaryVal = "[u]Mögliche Fakes:[/u] " + fakeCount + "\n" + "[u]Mögliche AGs:[/u] " + snobCount;

        return new String[]{sosImageVal, targetVal, attackCountVal, unitVal, wallInfoVal, wallLevelVal, firstAttackVal, lastAttackVal, sourceVal, sourceDateTypeVal, attackList, sourceDateVal, sourceTypeVal, summaryVal};
    }

    private String buildUnitInfo(TargetInformation pTargetInfo) {
        StringBuilder buffer = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String defRow = "";
        String offRow = "";

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer amount = pTargetInfo.getTroops().get(unit);
            if (amount != null && amount != 0) {
                if (unit.getPlainName().equals("spear") || unit.getPlainName().equals("sword") || unit.getPlainName().equals("archer") || unit.getPlainName().equals("spy") || unit.getPlainName().equals("heavy") || unit.getPlainName().equals("knight")) {
                    defRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                } else {
                    offRow += unit.toBBCode() + " " + nf.format(amount) + " ";
                }
            }
        }
        if (defRow.length() > 1) {
            buffer.append(defRow.trim()).append("\n");
        }
        if (offRow.length() > 1) {
            buffer.append(offRow.trim()).append("\n");
        }
        return buffer.toString();
    }

    private String buildWallInfo(TargetInformation pTargetInfo) {
        StringBuilder buffer = new StringBuilder();
        double perc = pTargetInfo.getWallLevel() / 20.0;
        int filledFields = (int) Math.rint(perc * 15.0);
        buffer.append("[color=#00FF00]");
        for (int i = 0; i < filledFields; i++) {
            buffer.append("█");
        }
        buffer.append("[/color]");
        if (filledFields < 15) {
            buffer.append("[color=#EEEEEE]");
            for (int i = 0; i < (15 - filledFields); i++) {
                buffer.append("█");
            }
            buffer.append("[/color]");
        }

        buffer.append(" (").append(pTargetInfo.getWallLevel()).append(")");
        return buffer.toString();
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        return getReplacementsForTarget(attacks.keys().nextElement(), pExtended);
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    private Tribe mDefender = null;
    private Hashtable<Village, TargetInformation> attacks = null;

    public SOSRequest() {
        attacks = new Hashtable<Village, TargetInformation>();
    }

    public SOSRequest(Tribe pDefender) {
        setDefender(pDefender);
        attacks = new Hashtable<Village, TargetInformation>();
    }

    public final void setDefender(Tribe pDefender) {
        mDefender = pDefender;
    }

    public Tribe getDefender() {
        return mDefender;
    }

    public void addTarget(Village pTarget) {
        TargetInformation targetInfo = attacks.get(pTarget);
        if (targetInfo == null) {
            targetInfo = new TargetInformation();
            attacks.put(pTarget, targetInfo);
        }
    }

    public TargetInformation getTargetInformation(Village pTarget) {
        return attacks.get(pTarget);
    }

    public Enumeration<Village> getTargets() {
        return attacks.keys();
    }

    public String toBBCode() {
        return toBBCode(true);
    }

    public String toBBCode(boolean pDetailed) {
        StringBuilder buffer = new StringBuilder();
        Enumeration<Village> targets = getTargets();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            TargetInformation targetInfo = getTargetInformation(target);
            buffer.append(SOSFormater.format(target, targetInfo, pDetailed));
            buffer.append("\n\n");
        }
        return buffer.toString();
    }

    public String toBBCode(Village pTarget, boolean pDetailed) {
        StringBuilder buffer = new StringBuilder();
        Village target = pTarget;
        TargetInformation targetInfo = getTargetInformation(target);
        if (targetInfo == null) {
            return "";
        }
        buffer.append(SOSFormater.format(target, targetInfo, pDetailed));
        return buffer.toString();
    }

    public SOSRequest merge(SOSRequest pNewRequest) {
        SOSRequest newRequest = new SOSRequest(getDefender());
        Enumeration<Village> targets = getTargets();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            TargetInformation thisInfo = getTargetInformation(target);
            TargetInformation theOtherInfo = pNewRequest.getTargetInformation(target);
            newRequest.addTarget(target);
            TargetInformation theNewInfo = newRequest.getTargetInformation(target);
            thisInfo.merge(theOtherInfo, theNewInfo);
        }

        Enumeration<Village> newTargets = pNewRequest.getTargets();
        while (newTargets.hasMoreElements()) {
            Village target = newTargets.nextElement();
            TargetInformation theOtherInfo = pNewRequest.getTargetInformation(target);
            TargetInformation thisInfo = getTargetInformation(target);

            newRequest.addTarget(target);
            TargetInformation theNewInfo = newRequest.getTargetInformation(target);
            theOtherInfo.merge(thisInfo, theNewInfo);
        }

        return newRequest;
    }

    @Override
    public String toString() {
        String result = "Verteidiger: " + getDefender() + "\n";
        Enumeration<Village> targets = getTargets();

        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            result += " Ziel: " + target + "\n";
            result += getTargetInformation(target);
            //result += "\n";
        }

        return result;
    }

    public class TargetInformation {

        private List<TimedAttack> attacks = null;
        private int iWallLevel = 20;
        private Hashtable<UnitHolder, Integer> troops = null;
        private int delta = 0;

        public TargetInformation() {
            attacks = new LinkedList<TimedAttack>();
            troops = new Hashtable<UnitHolder, Integer>();
        }

        public int getDelta() {
            return delta;
        }

        public void setDelta(int pDelta) {
            delta = pDelta;
        }

        /**
         * @return the attacks
         */
        public List<TimedAttack> getAttacks() {
            return attacks;
        }

        /**
         * @param attacks the attacks to set
         */
        public void addAttack(Village pSource, Date pArrive) {
            attacks.add(new TimedAttack(pSource, pArrive));
            Collections.sort(attacks, SOSRequest.ARRIVE_TIME_COMPARATOR);
        }

        /**
         * @return the iWallLevel
         */
        public int getWallLevel() {
            return iWallLevel;
        }

        /**
         * @param iWallLevel the iWallLevel to set
         */
        public void setWallLevel(int iWallLevel) {
            this.iWallLevel = iWallLevel;
        }

        /**
         * @return the troops
         */
        public Hashtable<UnitHolder, Integer> getTroops() {
            return troops;
        }

        /**
         * @param troops the troops to set
         */
        public void addTroopInformation(UnitHolder pUnit, Integer pAmount) {
            troops.put(pUnit, pAmount);
        }

        public String getTroopInformationAsHTML() {
            StringBuilder b = new StringBuilder();

            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = troops.get(unit);
                if (amount != null) {
                    b.append("<img src=\"").append(SOSRequest.class.getResource("/res/ui/" + unit.getPlainName() + ".png")).append("\"/>&nbsp;").append(amount).append("\n");
                }
            }

            return b.toString();
        }

        public TargetInformation merge(TargetInformation pInfo, TargetInformation pNewInfo) {
            boolean millis = ServerSettings.getSingleton().isMillisArrival();
            List<TimedAttack> thisAttacks = getAttacks();
            int attCount = thisAttacks.size();
            List<TimedAttack> theOtherAttacks = null;
            if (pInfo != null) {
                theOtherAttacks = pInfo.getAttacks();
            }
            TargetInformation theNewInfo = (pNewInfo == null) ? new TargetInformation() : pNewInfo;
            theNewInfo.setWallLevel(getWallLevel());
            Hashtable<UnitHolder, Integer> theOtherTroopInfo = getTroops();
            Enumeration<UnitHolder> units = theOtherTroopInfo.keys();
            while (units.hasMoreElements()) {
                UnitHolder unit = units.nextElement();
                theNewInfo.addTroopInformation(unit, theOtherTroopInfo.get(unit));
            }

            for (TimedAttack thisAttack : thisAttacks.toArray(new TimedAttack[]{})) {
                theNewInfo.addAttack(thisAttack.getSource(), new Date(thisAttack.getlArriveTime()));
            }

            if (theOtherAttacks != null) {
                for (TimedAttack theOtherAttack : theOtherAttacks) {
                    boolean add = true;
                    if (millis) {//only check if millis are enabled...otherwise we should keep all attacks, as we cannot separate them clearly
                        for (TimedAttack theNewAttack : theNewInfo.getAttacks()) {//go through all attacks and check if one is equal
                            if (theNewAttack.getSource().equals(theOtherAttack.getSource()) && theNewAttack.getlArriveTime().equals(theOtherAttack.getlArriveTime())) {
                                //attack seems to be the same...skip adding
                                add = false;
                                break;
                            }
                        }
                    }

                    if (add) {//attack seems not to exist...add it
                        theNewInfo.addAttack(theOtherAttack.getSource(), new Date(theOtherAttack.getlArriveTime()));
                    }
                }
            }

            theNewInfo.setDelta(theNewInfo.getAttacks().size() - attCount);

            return theNewInfo;
        }

        @Override
        public String toString() {
            String result = " Stufe des Walls: " + getWallLevel() + "\n";
            Enumeration<UnitHolder> units = troops.keys();
            if (troops.isEmpty()) {
                result += " Truppen im Dorf: -Keine Informationen-\n\n";
            } else {
                result += " Truppen im Dorf:\n";
                while (units.hasMoreElements()) {
                    UnitHolder unit = units.nextElement();
                    result += "  " + troops.get(unit) + " " + unit + "\n";
                }
            }
            result += "\n";
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
            for (TimedAttack attack : attacks) {
                result += " * " + attack.getSource() + "(" + format.format(new Date(attack.getlArriveTime())) + ")\n";
            }
            result += "\n";
            return result;
        }
    }
    public static final Comparator<TimedAttack> ARRIVE_TIME_COMPARATOR = new ArriveTimeComparator();

    private static class ArriveTimeComparator implements Comparator<TimedAttack>, java.io.Serializable {

        @Override
        public int compare(TimedAttack s1, TimedAttack s2) {
            return s1.getlArriveTime().compareTo(s2.getlArriveTime());
        }
    }

    public class TimedAttack {

        private Village mSource = null;
        private long lArriveTime = 0;
        private boolean possibleFake = false;
        private boolean possibleSnob = false;

        public TimedAttack(Village pSource, Date pArriveTime) {
            mSource = pSource;
            lArriveTime = pArriveTime.getTime();
        }

        /**
         * @return the mSource
         */
        public Village getSource() {
            return mSource;
        }

        /**
         * @param mSource the mSource to set
         */
        public void setSource(Village mSource) {
            this.mSource = mSource;
        }

        /**
         * @return the lArriveTime
         */
        public Long getlArriveTime() {
            return lArriveTime;
        }

        /**
         * @param lArriveTime the lArriveTime to set
         */
        public void setlArriveTime(long lArriveTime) {
            this.lArriveTime = lArriveTime;
        }

        /**
         * @return the possibleFake
         */
        public boolean isPossibleFake() {
            return possibleFake;
        }

        /**
         * @param possibleFake the possibleFake to set
         */
        public void setPossibleFake(boolean possibleFake) {
            this.possibleFake = possibleFake;
        }

        /**
         * @return the possibleSnob
         */
        public boolean isPossibleSnob() {
            return possibleSnob;
        }

        /**
         * @param possibleSnob the possibleSnob to set
         */
        public void setPossibleSnob(boolean possibleSnob) {
            this.possibleSnob = possibleSnob;
        }
    }
}
