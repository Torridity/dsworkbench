/*
 * WorldDataHolder.java
 *
 * Created on 07.10.2007, 15:45:09
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.io;

import de.tor.tribes.types.Unit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class WorldDataHolder {

    private List<Unit> mUnits = null;

    public  void loadUnits() throws FileNotFoundException, FileFormatException {
        BufferedReader reader = new BufferedReader(new FileReader("units.txt"));
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("%")) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    if (tokenizer.countTokens() < 12) {
                        throw new FileFormatException("units.txt hat ein ungültiges Format.");
                    }

                    //%Name, Wood, Mud, Iron, People, Attack, Def, DefKav, Defow, Speed, Carry
                    try {
                        Unit u = new Unit();
                        u.setName(tokenizer.nextToken());
                        u.setWoodCost(Integer.parseInt(tokenizer.nextToken()));
                        u.setMudCost(Integer.parseInt(tokenizer.nextToken()));
                        u.setIronCost(Integer.parseInt(tokenizer.nextToken()));
                        u.setFarmPlaces(Integer.parseInt(tokenizer.nextToken()));
                        u.setAttack(Integer.parseInt(tokenizer.nextToken()));
                        u.setDef(Integer.parseInt(tokenizer.nextToken()));
                        u.setDefKav(Integer.parseInt(tokenizer.nextToken()));
                        u.setDefBow(Integer.parseInt(tokenizer.nextToken()));
                        u.setSpeed(Integer.parseInt(tokenizer.nextToken()));
                        u.setCarry(Integer.parseInt(tokenizer.nextToken()));
                        u.setTextureID(Integer.parseInt(tokenizer.nextToken()));
                        mUnits.add(u);
                    } catch (Exception e) {
                        throw new FileFormatException("units.txt hat ein ungültiges Format. (" + e.getMessage() + ")");
                    }
                }
            }
        } catch (Exception e) {
            throw new FileFormatException("units.txt hat ein ungültiges Format. (" + e.getMessage() + ")");
        }
    }
    
    public List<Unit> getUnits(){
        return mUnits;
    }
    
    public Unit getUnitByName(String name){
        if(name == null) return null;
        for(Unit u : mUnits){
            if(u.getName().equals(name)) return u;
        }
        return null;
    }
}