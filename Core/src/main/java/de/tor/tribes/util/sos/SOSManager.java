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
import de.tor.tribes.util.xml.JaxenUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class SOSManager extends GenericManager<SOSRequest> {

  private static Logger logger = Logger.getLogger("SOSManager");
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
  public void loadElements(String pFile) {
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return;
    }
    invalidate();
    initialize();
    File sosFile = new File(pFile);
    if (sosFile.exists()) {
      logger.info("Loading SOS information from '" + pFile + "'");

      try {
        Document d = JaxenUtils.getDocument(sosFile);
        for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//sosRequests/sosRequest")) {
          SOSRequest s = new SOSRequest();
          s.loadFromXml(e);
          addManagedElement(s);
        }
        logger.debug("SOS requests loaded successfully");
      } catch (Exception e) {
        logger.error("Failed to load SOS requests", e);
      }
    } else {
      logger.info("No SOS information found under '" + pFile + "'");
    }
    revalidate();
  }

  @Override
  public void saveElements(String pFile) {
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Writing SOS information to '" + pFile + "'");
    }

    try {
      StringBuilder b = new StringBuilder();
      b.append("<sosRequests>\n");
      for (ManageableType t : getAllElements()) {
        SOSRequest s = (SOSRequest) t;
        if (s != null) {
          String xml = s.toXml();
          if (xml != null) {
            b.append(xml).append("\n");
          }
        }
      }
      b.append("</sosRequests>");
      FileWriter w = new FileWriter(pFile);
      w.write(b.toString());
      w.flush();
      w.close();
      logger.debug("SOS information successfully saved");
    } catch (Throwable t) {
      if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
        //this should only happen at the first start
        logger.info("Ignoring error, server directory does not exists yet");
      } else {
        logger.error("Failed to save SOS information", t);
      }
      //try to delete errornous file
      new File(pFile).delete();
    }

  }

  @Override
  public String getExportData(List<String> pGroupsToExport) {
    return null;
  }

  @Override
  public boolean importData(File pFile, String pExtension) {
    return false;
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
      Enumeration<Village> targets = r.getTargets();
      while (targets.hasMoreElements()) {
        targets.nextElement();
        cnt++;
      }
    }
    return cnt;
  }

  public boolean hasTransferredSupports() {
    for (ManageableType t : getAllElements()) {
      SOSRequest r = (SOSRequest) t;
      Enumeration<Village> targets = r.getTargets();
      while (targets.hasMoreElements()) {
        DefenseInformation info = r.getDefenseInformation(targets.nextElement());
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
