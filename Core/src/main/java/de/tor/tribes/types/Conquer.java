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
import org.jdom2.Element;

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
  public Element toXml(String elementName) {
    Element conquer = new Element(elementName);
    conquer.addContent(new Element("villageID").setText(Integer.toString(village.getId())));
    conquer.addContent(new Element("timestamp").setText(Long.toString(timestamp)));
    conquer.addContent(new Element("winner").setText(Integer.toString(winner.getId())));
    if(loser != null) {
        conquer.addContent(new Element("loser").setText(Integer.toString(loser.getId())));
    }
    return conquer;
  }

  @Override
    public void loadFromXml(Element pElement) {
    int pVillageId = Integer.parseInt(pElement.getChild("villageID").getText());
    long pTimestamp = Long.parseLong(pElement.getChild("timestamp").getText());
    int pWinner = Integer.parseInt(pElement.getChild("winner").getText());
    this.village = DataHolder.getSingleton().getVillagesById().get(pVillageId);
    this.timestamp = pTimestamp;
    try {
        int pLoser = Integer.parseInt(pElement.getChild("loser").getText());
        this.loser = DataHolder.getSingleton().getTribes().get(pLoser);
    } catch(Exception e) {
        this.loser = null;
    }
    this.winner = DataHolder.getSingleton().getTribes().get(pWinner);
  }
}
