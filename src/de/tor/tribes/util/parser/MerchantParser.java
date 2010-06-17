/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.types.Village;
import de.tor.tribes.util.algo.MerchantDestination;
import de.tor.tribes.util.algo.MerchantDistributor;
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

    public static boolean parse(String pProductionString) {
        StringTokenizer lineTok = new StringTokenizer(pProductionString, "\n\r");
        List<VillageMerchantInfo> infos = new LinkedList<VillageMerchantInfo>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            // List<Village> villages = VillageParser.parse(line);
            //Village v = villages.get(villages.size() - 1);
            StringTokenizer t = new StringTokenizer(line, " \t");
            String merchants = null;
            List<Integer> numbers = new LinkedList<Integer>();
            try {
                while (t.hasMoreElements()) {
                    String d = t.nextToken().trim();

                    if (d.trim().matches("[0-9]{1,3}[.][0-9]{1,3}") || d.trim().matches("[0-9]{1,6}")) {
                        numbers.add(Integer.parseInt(d.replaceAll("\\.", "")));
                    }
                    if (d.trim().matches("[0-9]{1,5}[/][0-9]{1,5}")) {
                        if (merchants == null) {
                            merchants = d;
                        }
                    }

                }

                int woodStock = numbers.get(numbers.size() - 4);
                int clayStock = numbers.get(numbers.size() - 3);
                int ironStock = numbers.get(numbers.size() - 2);
                int stashCapacity = numbers.get(numbers.size() - 1);
                String[] merchantInfo = merchants.trim().split("/");
                int availMerchants = Integer.parseInt(merchantInfo[0]);
                int overallMerchants = Integer.parseInt(merchantInfo[1]);
                VillageMerchantInfo info = new VillageMerchantInfo(null, stashCapacity, woodStock, clayStock, ironStock, availMerchants, overallMerchants);
                infos.add(info);
            } catch (Exception e) {
            }
        }

        

        return true;
    }

    public static void main(String[] args) throws Exception {
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        /* String line = "Cartulia (440|878) K84  	22.09	9.495	109.359 120.800 107.054 	400000	74/74	5758/24000	morgen um 20:06 Uhr";
        String line2 = " Rattennest (-47|35) (439|868) K84  	32.02	10.019	29.295 342.763 762 	400000	110/110	11791/24000";*/
        MerchantParser.parse(data);
    }

    public static class VillageMerchantInfo {

        private Village village = null;
        private int stashCapacity = 0;
        private int woodStock = 0;
        private int clayStock = 0;
        private int ironStock = 0;
        private int overallMerchants = 0;
        private int availableMerchants = 0;

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
            this.village = village;
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

        public String toString() {
            String res = getVillage() + " ";
            res += getWoodStock() + ", " + getClayStock() + ", " + getIronStock() + " (" + getStashCapacity() + ") " + getAvailableMerchants() + "/" + getOverallMerchants();
            return res;
        }
    }
}


