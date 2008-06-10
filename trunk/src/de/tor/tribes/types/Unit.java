/*
 * Unit.java
 *
 * Created on 07.10.2007, 15:45:50
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.types;

import java.awt.Image;

/**
 *
 * @author Charon
 */
public class Unit {

    private String name;
    private int speed;
    private int carry;
    private Image image;
    private int woodCost;
    private int mudCost;
    private int ironCost;
    private int farmPlaces;
    private int attack;
    private int def;
    private int defKav;
    private int defBow;
    private int textureID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getCarry() {
        return carry;
    }

    public void setCarry(int carry) {
        this.carry = carry;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getWoodCost() {
        return woodCost;
    }

    public void setWoodCost(int woodCost) {
        this.woodCost = woodCost;
    }

    public int getMudCost() {
        return mudCost;
    }

    public void setMudCost(int mudCost) {
        this.mudCost = mudCost;
    }

    public int getIronCost() {
        return ironCost;
    }

    public void setIronCost(int ironCost) {
        this.ironCost = ironCost;
    }

    public int getFarmPlaces() {
        return farmPlaces;
    }

    public void setFarmPlaces(int farmPlaces) {
        this.farmPlaces = farmPlaces;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public int getDefKav() {
        return defKav;
    }

    public void setDefKav(int defKav) {
        this.defKav = defKav;
    }

    public int getDefBow() {
        return defBow;
    }

    public void setDefBow(int defBow) {
        this.defBow = defBow;
    }
    
     public int getTextureID() {
        return textureID;
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }
}