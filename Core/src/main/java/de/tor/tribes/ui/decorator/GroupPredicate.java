/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.decorator;

import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Component;
import java.util.List;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

/**
 *
 * @author Torridity
 */
public class GroupPredicate implements HighlightPredicate {

    public static final int ALL = -1;
    private int highlightColumn;
    private int testColumn;
    private boolean relation;
    private List<Tag> groups;
    private String troopGroup = null;
    /**
     * Instantiates a Predicate with the given Pattern and testColumn index
     * (in model coordinates) highlighting all columns.
     *  A column index of -1 is interpreted
     * as "all". 
     * 
     * @param pattern the Pattern to test the cell value against
     * @param testColumn the column index in model coordinates
     *   of the cell which contains the value to test against the pattern 
     */
    public GroupPredicate(List<Tag> groups, int testColumn, boolean pRelation, String pTroopGroup) {
        this(groups, testColumn, ALL, pRelation, pTroopGroup);
    }

    /**
     * Instantiates a Predicate with the given Pattern and test-/decorate
     * column index in model coordinates. A column index of -1 is interpreted
     * as "all". 
     * 
     * 
     * @param pattern the Pattern to test the cell value against
     * @param testColumn the column index in model coordinates 
     *   of the cell which contains the value
     *   to test against the pattern 
     * @param decorateColumn the column index in model coordinates
     *   of the cell which should be 
     *   decorated if the test against the value succeeds.
     */
    public GroupPredicate(List<Tag> groups, int testColumn, int decorateColumn, boolean pRelation, String pTroopGroup) {
        this.groups = groups;
        this.testColumn = testColumn;
        this.highlightColumn = decorateColumn;
        this.relation = pRelation;
        this.troopGroup = pTroopGroup;
    }

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        return isHighlightCandidate(adapter) && test(adapter);
    }

    /**
     * Test the value. This is called only if the 
     * pre-check returned true, because accessing the 
     * value might be potentially costly
     * @param adapter
     * @return
     */
    private boolean test(ComponentAdapter adapter) {
        // single test column
        if (testColumn >= 0) {
            return testColumn(adapter, testColumn);
        }
        // test all
        for (int column = 0; column < adapter.getColumnCount(); column++) {
            boolean result = testColumn(adapter, column);
            if (result) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param adapter
     * @param testColumn
     * @return
     */
    private boolean testColumn(ComponentAdapter adapter, int testColumn) {
        if (!adapter.isTestable(testColumn)) {
            return false;
        }

        int rowToTest = adapter.convertRowIndexToModel(adapter.row);
        VillageTroopsHolder h = (VillageTroopsHolder)TroopsManager.getSingleton().getAllElements(troopGroup).get(rowToTest);
        
        boolean result = false;
        if (h != null) {

            Village v = h.getVillage();
            //true == 
            //false ||

            if (relation) {
                //and connection
                boolean failure = false;
                for (Tag t : groups) {
                    if (!t.tagsVillage(v.getId())) {
                        failure = true;
                        break;
                    }
                }
                if (!failure) {
                    result = true;
                }
            } else {
                //or connection
                for (Tag t : groups) {
                    if (t.tagsVillage(v.getId())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * A quick pre-check.
     * @param adapter
     * 
     * @return
     */
    private boolean isHighlightCandidate(ComponentAdapter adapter) {
        return (groups != null)
                && ((highlightColumn < 0)
                || (highlightColumn == adapter.convertColumnIndexToModel(adapter.column)));
    }

    /**
     * 
     * @return returns the column index to decorate (in model coordinates)
     */
    public int getHighlightColumn() {
        return highlightColumn;
    }
}
