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
package de.tor.tribes.util;

import java.util.List;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;

/**
 *
 * @author Torridity
 */
public class PropertyHelper {

  public static void storeTableProperties(JXTable pTable, Configuration pConfig, String pPrefix) {
    List<TableColumn> cols = ((TableColumnModelExt) pTable.getColumnModel()).getColumns(true);

    for (TableColumn c : cols) {
      TableColumnExt col = (TableColumnExt) c;
      String title = col.getTitle();
      pConfig.setProperty(pPrefix + ".table.col." + title + ".width", col.getWidth());
      pConfig.setProperty(pPrefix + ".table.col." + title + ".visible", col.isVisible());
    }
    int sortedCol = pTable.getSortedColumnIndex();
    if (sortedCol < 0) {
      return;
    }
    pConfig.setProperty(pPrefix + ".table.sort.col", sortedCol);
    int sortOrder = 0;
    switch (pTable.getSortOrder(sortedCol)) {
      case ASCENDING:
        sortOrder = 1;
        break;
      case DESCENDING:
        sortOrder = -1;
        break;
      default:
        sortOrder = 0;
    }
    pConfig.setProperty(pPrefix + ".table.sort.order", sortOrder);
    pConfig.setProperty(pPrefix + ".table.horizontal.scroll", pTable.isHorizontalScrollEnabled());
  }

  public static void restoreTableProperties(JXTable pTable, Configuration pConfig, String pPrefix) {
    //set col width
    List<TableColumn> cols = ((TableColumnModelExt) pTable.getColumnModel()).getColumns(true);

    for (TableColumn c : cols) {
      TableColumnExt col = (TableColumnExt) c;
      String title = col.getTitle();
      try {
        col.setPreferredWidth(pConfig.getInteger(pPrefix + ".table.col." + title + ".width", col.getWidth()));
      } catch (ConversionException ignored) {
      }
      try {
        col.setVisible(pConfig.getBoolean(pPrefix + ".table.col." + title + ".visible", true));
      } catch (ConversionException ce) {
        col.setVisible(true);
      }
    }

    SortOrder sortOrder = SortOrder.UNSORTED;
    int iSortOrder = 0;
    try {
      iSortOrder = pConfig.getInteger(pPrefix + ".table.sort.order", 0);
    } catch (ConversionException ignored) {
    }

    switch (iSortOrder) {
      case 1:
        sortOrder = SortOrder.ASCENDING;
        break;
      case -1:
        sortOrder = SortOrder.DESCENDING;
        break;
      default:
        sortOrder = SortOrder.UNSORTED;
    }

    Boolean scroll = false;
    try {
      scroll = pConfig.getBoolean(pPrefix + ".table.horizontal.scroll", false);
    } catch (ConversionException ignored) {
    }

    pTable.setHorizontalScrollEnabled(scroll);

    Integer orderCol = 0;
    try {
      orderCol = pConfig.getInteger(pPrefix + ".table.sort.col", 0);
    } catch (ConversionException ignored) {
    }

    try {
      pTable.setSortOrder(orderCol.intValue(), sortOrder);
    } catch (IndexOutOfBoundsException ignored) {
    }
  }
}
