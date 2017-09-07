package de.tor.tribes.util;

public class StringHelper {

    public static int compareByStringRepresentations(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        }

        return compareStrings(o1.toString(), o2.toString());
    }

    /**
     * Compares two strings.
     *
     * @param s1 May not be null
     * @param s2 May not be null
     * @return The relation between the two strings, for meaning ee {@link Comparable#compareTo(Object)}
     */
    private static int compareStrings(String s1, String s2) {
        int n1 = s1.length();
        int n2 = s2.length();

        for (int i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);
            if (c1 != c2) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
                if (c1 != c2) {
                    c1 = Character.toLowerCase(c1);
                    c2 = Character.toLowerCase(c2);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                }
            }
        }

        return n1 - n2;
    }

}
