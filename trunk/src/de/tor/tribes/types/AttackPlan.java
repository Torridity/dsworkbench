/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class AttackPlan {

    private String name = null;
    private List<Attack> attacks = null;

    public AttackPlan(String pName) {
        setName(pName);
        attacks = new LinkedList<Attack>();
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getName() {
        return name;
    }

    public Attack[] getAttacks() {
        return attacks.toArray(new Attack[]{});
    }

    public void addAttack(Attack pAttacks) {
        attacks.add(pAttacks);
    }

    public void removeAttacks(List<Attack> pAttacks) {
        for (Attack attack : pAttacks) {
            removeAttack(attack);
        }
    }

    public void removeAttack(Attack pAttack) {
        attacks.remove(pAttack);
    }

    public void removeAttack(int pId) {
        removeAttacks(new int[]{pId});
    }

    public void removeAttacks(int[] pIds) {
        Attack[] aAttacks = getAttacks();
        List<Attack> toRemove = new LinkedList<Attack>();
        for (int id : pIds) {
            toRemove.add(aAttacks[id]);
        }
        for (Attack a : toRemove) {
            removeAttack(a);
        }
    }

    public void removeAll() {
        attacks.clear();
    }

    public String toXML() {
        StringBuffer b = new StringBuffer();


        return b.toString();
    }
}
