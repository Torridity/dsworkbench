/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Jejkal
 */
public class DiffMaker {

    public DiffMaker() {
    }

    public void getDiffLines() {
        try {
            BufferedReader older = new BufferedReader(new FileReader(new File("village_new.txt")));
            BufferedReader newer = new BufferedReader(new FileReader(new File("village_newer.txt")));
            String oldLine = "";
            String newLine = "";
            int equal = 0;
            int lines = 0;
            int tokens = 0;
            int equalTok = 0;
            int ds = 0;
            int completeNew = 0;
            String result = "";
            String changed = "";
            StringBuffer sb = new StringBuffer();
            while (true) {
                oldLine = older.readLine();
                newLine = newer.readLine();
                if (oldLine != null) {
                    if (newLine.equals(oldLine)) {
                        sb.append("\n");
                    } else {
                        StringTokenizer oldToken = new StringTokenizer(oldLine, ",");
                        StringTokenizer newToken = new StringTokenizer(newLine, ",");

                        for (int i = 0; i < 7; i++) {
                            String nT = newToken.nextToken();
                            String oT = oldToken.nextToken();
                            if (oT.equals(nT)) {
                                equalTok++;
                                sb.append(",");
                            } else {
                                sb.append(nT);
                                sb.append(",");
                            }
                        }
                        tokens += 7;
                        sb.append("\n");
                    }
                } else {
                    if (newLine == null) {
                        break;
                    } else {
                        completeNew++;
                        sb.append(newLine);
                        sb.append("\n");
                    }
                }
                lines++;
            }
            System.out.println("Lines " + lines);
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(new File("diff.txt")));
            out.write(sb.toString().getBytes());
            out.finish();


           
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File("diff.txt")))));
            String line = "";
            cnt = 0;
            while ((line = br.readLine()) != null) {
                //if (line.length() > 0) {
                //System.out.println(line);
                cnt++;
            // }
            }
            System.out.println(cnt);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    int cnt = 0;

    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        new DiffMaker().getDiffLines();
        System.out.println("Dur " + (System.currentTimeMillis() - s));
    }
}
