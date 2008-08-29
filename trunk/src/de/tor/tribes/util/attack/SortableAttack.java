/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.attack;

import java.util.Arrays;

/**
 *
 * @author Jejkal
 */
public class SortableAttack implements Comparable {

    private String name = null;
    private int id = 0;
    private int cBy = 0;

    public SortableAttack(String pName, int pId) {
        name = pName;
        id = pId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        try {
            SortableAttack s2 = (SortableAttack) o;
            if (cBy == 0) {
                return new Integer(getId()).compareTo(s2.getId());
            } else {
                return getName().compareTo(s2.getName());
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public String toString() {
        return name + " " + id;
    }

    public static void main(String[] args) {
        SortableAttack[] a = new SortableAttack[]{new SortableAttack("test1", 3), new SortableAttack("test2", 2), new SortableAttack("test3", 1)};
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
        Arrays.sort(a);
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }
}
