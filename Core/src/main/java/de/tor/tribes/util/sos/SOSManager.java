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
package de.tor.tribes.util.sos;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.Defense;
import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.xml.JDomUtils;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class SOSManager extends GenericManager<SOSRequest> {

  private static Logger logger = LogManager.getLogger("SOSManager");
  private static SOSManager SINGLETON = null;

  public static synchronized SOSManager getSingleton() {
    if (SINGLETON == null) {
      SINGLETON = new SOSManager();
    }
    return SINGLETON;
  }

  SOSManager() {
    super(false);
  }


  @Override
    public int importData(Element pElm, String pExtension) {
    if (pElm == null) {
        logger.error("Element argument is 'null'");
        return -1;
    }
    int result = 0;
    invalidate();
    logger.info("Loading SOS information");

    try {
      for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "sosRequests/sosRequest")) {
        SOSRequest s = new SOSRequest();
        s.loadFromXml(e);
        addManagedElement(s);
        result++;
      }
      logger.debug("SOS requests loaded successfully");
    } catch (Exception e) {
      result = result * (-1) - 1;
      logger.error("Failed to load SOS requests", e);
    }
    revalidate(true);
    return result;
  }

  @Override
  public Element getExportData(final List<String> pGroupsToExport) {
    Element sosRequests = new Element("sosRequests");

    logger.debug("Generating SOS information");
    try {
      for (ManageableType t : getAllElements()) {
        sosRequests.addContent(t.toXml("sosRequest"));
      }
      logger.debug("SOS information generated");
    } catch (Exception e) {
      logger.warn("Failed to generate SOS XML", e);
    }
    
    return sosRequests;
  }

  public SOSRequest getRequest(final Tribe pTribe) {
    if (pTribe == null) {
      return null;
    }

    Object result = CollectionUtils.find(getAllElements(), new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return ((SOSRequest) o).getDefender().equals(pTribe);
      }
    });

    return (SOSRequest) result;
  }

  public void addRequest(SOSRequest pRequest) {
    SOSRequest r = getRequest(pRequest.getDefender());
    if (r != null) {
      r.merge(pRequest);
      fireDataChangedEvents();
    } else {
      addManagedElement(pRequest);
    }
  }

  public int getOverallTargetCount() {
    int cnt = 0;
    for (ManageableType t : getAllElements()) {
      SOSRequest r = (SOSRequest) t;
      cnt+= r.getTargets().size();
    }
    return cnt;
  }

  public boolean hasTransferredSupports() {
    for (ManageableType t : getAllElements()) {
      SOSRequest r = (SOSRequest) t;
      for(Village target: r.getTargets()) {
        DefenseInformation info = r.getDefenseInformation(target);
        for (Defense d : info.getSupports()) {
          if (d.isTransferredToBrowser()) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
