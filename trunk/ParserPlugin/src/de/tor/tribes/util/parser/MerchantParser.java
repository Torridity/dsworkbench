/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.types.Village;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.util.GenericParserInterface;
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
public class MerchantParser implements GenericParserInterface<VillageMerchantInfo> {

    public List<VillageMerchantInfo> parse(String pProductionString) {
        StringTokenizer lineTok = new StringTokenizer(pProductionString, "\n\r");
        List<VillageMerchantInfo> infos = new LinkedList<VillageMerchantInfo>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            Village v = null;
            StringTokenizer t = new StringTokenizer(line, " \t");
            String merchants = null;
            String farm = null;
            List<Integer> numbers = new LinkedList<Integer>();
            try {
                while (t.hasMoreElements()) {
                    String d = t.nextToken().trim();
                    if (d.trim().matches("[0-9]{1,3}[.][0-9]{1,3}") || d.trim().matches("[0-9]{1,6}")) {
                        numbers.add(Integer.parseInt(d.replaceAll("\\.", "")));
                    } else if (d.trim().matches("[0-9]{1,5}[/][0-9]{1,5}")) {
                        if (merchants == null) {
                            merchants = d;
                        } else {
                            farm = d;
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
                String[] farmInfo = farm.trim().split("/");
                int availMerchants = Integer.parseInt(merchantInfo[0]);
                int overallMerchants = Integer.parseInt(merchantInfo[1]);
                int availFarm = Integer.parseInt(farmInfo[0].replaceAll("\\.", ""));
                int overallFarm = Integer.parseInt(farmInfo[1].replaceAll("\\.", ""));
                VillageMerchantInfo info = null;
                try {
                    info = new VillageMerchantInfo(v, stashCapacity, woodStock, clayStock, ironStock, availMerchants, overallMerchants, availFarm, overallFarm);
                } catch (Exception e) {
                    //to support old data format
                    info = new VillageMerchantInfo(v, stashCapacity, woodStock, clayStock, ironStock, availMerchants, overallMerchants);
                }
                if (info != null) {
                    infos.add(info);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return infos;
    }

    public static void main(String[] args) throws Exception {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        /*  String line = "Cartulia (440|878) K84  	22.09	9.495	109.359 120.800 107.054 	400000	74/74	5758/24000	morgen um 20:06 Uhr";
        String line2 = " Rattennest (-47|35) (439|868) K84  	32.02	10.019	29.295 342.763 762 	400000	110/110	11791/24000";
         */ new MerchantParser().parse(data);
    }

    /*
    Offs: Hier entlang! (434|876) K84  	7.28	10.251	192.154 66.371 154.905 	400000	235/235	5212/20476	am 21.06. um 16:21 Uhr
    Offs: Hier entlang! (436|880) K84  	10.82	10.387	171.896 95.970 175.433 400000	235/235	6312/24000	am 21.06. um 06:50 Uhr
    Rattennest (-1|33) (485|866) K84  	58.55	10.019	71.270 23.198 263.667 	400000	110/110	16981/24000
    Rattennest (-31|45) (455|878) K84  	28.28	10.019	96.649 185.743 222.033 	400000	110/110	18441/24000
    Rattennest (-32|15) (454|848) K84  	37.48	10.019	123.599 79.792 160.859 	400000	110/110	10091/24000
    Rattennest (-33|44) (453|877) K84  	26.17	10.019	134.644 180.000 161.743 400000	110/110	24000/24000
     */
}
