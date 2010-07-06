/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Jejkal
 */
public class MerchantParser {

    public static List<VillageMerchantInfo> parse(String pProductionString) {
        StringTokenizer lineTok = new StringTokenizer(pProductionString, "\n\r");
        List<VillageMerchantInfo> infos = new LinkedList<VillageMerchantInfo>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            // List<Village> villages = VillageParser.parse(line);
            //Village v = villages.get(villages.size() - 1);
            Village v = null;
            StringTokenizer t = new StringTokenizer(line, " \t");
            String merchants = null;
            List<Integer> numbers = new LinkedList<Integer>();
            try {
                while (t.hasMoreElements()) {
                    String d = t.nextToken().trim();
                    if (d.trim().matches("[0-9]{1,3}[.][0-9]{1,3}") || d.trim().matches("[0-9]{1,6}")) {
                        numbers.add(Integer.parseInt(d.replaceAll("\\.", "")));
                    } else if (d.trim().matches("[0-9]{1,5}[/][0-9]{1,5}")) {
                        if (merchants == null) {
                            merchants = d;
                        }
                    } else if (d.trim().matches("[(][0-9]{1,3}[|][0-9]{1,3}[)]")) {
                        String[] split = d.trim().replaceAll("\\(", "").replaceAll("\\)", "").split("\\|");
                        int x = Integer.parseInt(split[0]);
                        int y = Integer.parseInt(split[1]);
                        v = new Village();
                        v.setX((short) x);
                        v.setY((short) y);
                    }

                }

                int woodStock = numbers.get(numbers.size() - 4);
                int clayStock = numbers.get(numbers.size() - 3);
                int ironStock = numbers.get(numbers.size() - 2);
                int stashCapacity = numbers.get(numbers.size() - 1);
                String[] merchantInfo = merchants.trim().split("/");
                int availMerchants = Integer.parseInt(merchantInfo[0]);
                int overallMerchants = Integer.parseInt(merchantInfo[1]);
                VillageMerchantInfo info = new VillageMerchantInfo(v, stashCapacity, woodStock, clayStock, ironStock, availMerchants, overallMerchants);
                infos.add(info);
            } catch (Exception e) {
                //  e.printStackTrace();
            }
        }
        return infos;
    }

    public static void main(String[] args) throws Exception {
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        /*  String line = "Cartulia (440|878) K84  	22.09	9.495	109.359 120.800 107.054 	400000	74/74	5758/24000	morgen um 20:06 Uhr";
        String line2 = " Rattennest (-47|35) (439|868) K84  	32.02	10.019	29.295 342.763 762 	400000	110/110	11791/24000";
         */ MerchantParser.parse(data);
    }

    /*
    Offs: Hier entlang! (434|876) K84  	7.28	10.251	192.154 66.371 154.905 	400000	235/235	5212/20476	am 21.06. um 16:21 Uhr
    Offs: Hier entlang! (436|880) K84  	10.82	10.387	171.896 95.970 175.433 400000	235/235	6312/24000	am 21.06. um 06:50 Uhr
    Rattennest (-1|33) (485|866) K84  	58.55	10.019	71.270 23.198 263.667 	400000	110/110	16981/24000
    Rattennest (-31|45) (455|878) K84  	28.28	10.019	96.649 185.743 222.033 	400000	110/110	18441/24000
    Rattennest (-32|15) (454|848) K84  	37.48	10.019	123.599 79.792 160.859 	400000	110/110	10091/24000
    Rattennest (-33|44) (453|877) K84  	26.17	10.019	134.644 180.000 161.743 400000	110/110	17641/24000
     */
    public static class VillageMerchantInfo implements Cloneable {

        /**
         * @return the direction
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * @param direction the direction to set
         */
        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public enum Direction {

            INCOMING, OUTGOING, BOTH
        }
        private Village village = null;
        private int stashCapacity = 0;
        private int woodStock = 0;
        private int clayStock = 0;
        private int ironStock = 0;
        private int overallMerchants = 0;
        private int availableMerchants = 0;
        private Direction direction = Direction.BOTH;

        public VillageMerchantInfo(Village pVillage, int pStashCapacity, int pWoodStock, int pClayStock, int pIronStock, int pAvailMerchants, int pMaxMerchants) {
            setVillage(pVillage);
            setWoodStock(pWoodStock);
            setClayStock(pClayStock);
            setStashCapacity(pStashCapacity);
            setIronStock(pIronStock);
            setAvailableMerchants(pAvailMerchants);
            setOverallMerchants(pMaxMerchants);
        }

        /**
         * @return the village
         */
        public Village getVillage() {
            return village;
        }

        /**
         * @param village the village to set
         */
        public void setVillage(Village village) {
            this.village = DataHolder.getSingleton().getVillages()[village.getX()][village.getY()];
        }

        /**
         * @return the stashCapacity
         */
        public int getStashCapacity() {
            return stashCapacity;
        }

        /**
         * @param stashCapacity the stashCapacity to set
         */
        public void setStashCapacity(int stashCapacity) {
            this.stashCapacity = stashCapacity;
        }

        /**
         * @return the woodStock
         */
        public int getWoodStock() {
            return woodStock;
        }

        /**
         * @param woodStock the woodStock to set
         */
        public void setWoodStock(int woodStock) {
            this.woodStock = woodStock;
        }

        /**
         * @return the clayStock
         */
        public int getClayStock() {
            return clayStock;
        }

        /**
         * @param clayStock the clayStock to set
         */
        public void setClayStock(int clayStock) {
            this.clayStock = clayStock;
        }

        /**
         * @return the ironStock
         */
        public int getIronStock() {
            return ironStock;
        }

        /**
         * @param ironStock the ironStock to set
         */
        public void setIronStock(int ironStock) {
            this.ironStock = ironStock;
        }

        /**
         * @return the overallMerchants
         */
        public int getOverallMerchants() {
            return overallMerchants;
        }

        /**
         * @param overallMerchants the overallMerchants to set
         */
        public void setOverallMerchants(int overallMerchants) {
            this.overallMerchants = overallMerchants;
        }

        /**
         * @return the availableMerchants
         */
        public int getAvailableMerchants() {
            return availableMerchants;
        }

        /**
         * @param availableMerchants the availableMerchants to set
         */
        public void setAvailableMerchants(int availableMerchants) {
            this.availableMerchants = availableMerchants;
        }

        public VillageMerchantInfo clone() {
            VillageMerchantInfo info = new VillageMerchantInfo(village, stashCapacity, woodStock, clayStock, ironStock, availableMerchants, overallMerchants);
            info.setDirection(getDirection());
            return info;
        }

        public String toString() {
            String res = getVillage() + " ";
            res += getVillage() + ": " + getWoodStock() + ", " + getClayStock() + ", " + getIronStock() + " (" + getStashCapacity() + ") " + getAvailableMerchants() + "/" + getOverallMerchants();
            return res;
        }
    }
}


