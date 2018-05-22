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
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Conquer extends ManageableType {

  private Village village = null;
  private long timestamp = 0;
  private Tribe loser = null;
  private Tribe winner = null;

  public int getCurrentAcceptance() {
      long time = timestamp;
    long diff = System.currentTimeMillis() / 1000 - time;
    double risePerHour = DSCalculator.calculateRiseSpeed();
    return 25 + (int) Math.rint((diff / (60 * 60)) * risePerHour);
  }

  /**
   * @return the villageID
   */
  public Village getVillage() {
    return village;
  }

  /**
   * @param pVillage the village to set
   */
  public void setVillage(Village pVillage) {
    this.village = pVillage;
  }

  /**
   * @return the timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return the loser
   */
  public Tribe getLoser() {
    return loser;
  }

  /**
   * @param loser the loser to set
   */
  public void setLoser(Tribe loser) {
    this.loser = loser;
  }

  /**
   * @return the winner
   */
  public Tribe getWinner() {
    return winner;
  }

  /**
   * @param winner the winner to set
   */
  public void setWinner(Tribe winner) {
    this.winner = winner;
  }

  @Override
  public String getElementIdentifier() {
    return "conquer";
  }

  @Override
  public String getElementGroupIdentifier() {
    return "conquers";
  }

  @Override
  public String getGroupNameAttributeIdentifier() {
    return "";
  }

  @Override
  public String toXml() {
    try {
      StringBuilder b = new StringBuilder();
      b.append("<conquer>\n");
        b.append("<villageID>").append(village.getId()).append("</villageID>\n");
        b.append("<timestamp>").append(timestamp).append("</timestamp>\n");
        b.append("<winner>").append(winner.getId()).append("</winner>\n");
        b.append("<loser>").append(loser.getId()).append("</loser>\n");
      b.append("</conquer>");
      return b.toString();
    } catch (Throwable t) {
      //getting xml data failed
    }
    return null;
  }

  @Override
  public void loadFromXml(Element pElement) {
    int villageId = Integer.parseInt(pElement.getChild("villageID").getText());
    int timestamp = Integer.parseInt(pElement.getChild("timestamp").getText());
    int winner = Integer.parseInt(pElement.getChild("winner").getText());
    int loser = Integer.parseInt(pElement.getChild("loser").getText());
    this.village = DataHolder.getSingleton().getVillagesById().get(villageId);
    this.timestamp = (long) timestamp;
    this.loser = DataHolder.getSingleton().getTribes().get(loser);
    this.winner = DataHolder.getSingleton().getTribes().get(winner);
  }
}
