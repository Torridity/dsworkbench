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

import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.types.ext.Village;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Torridity
 */
public class PluginManager {

  private static Logger logger = LogManager.getLogger("PluginManager");
  private static PluginManager SINGLETON = null;
  private final File PLUGIN_DIR = new File("./plugins");
  private ClassLoader mClassloader = null;
  private Object mVariableManager = null;

  public static synchronized PluginManager getSingleton() {
    if (SINGLETON == null) {
      SINGLETON = new PluginManager();
    }
    return SINGLETON;
  }

  PluginManager() {
    initializeClassloader();
  }

  private void initializeClassloader() {
   /* List<URL> urls = new LinkedList<URL>();
    int urlCount = 0;
    int fileCount = PLUGIN_DIR.listFiles().length;
    for (File f : PLUGIN_DIR.listFiles()) {
      try {
        urls.add(f.toURI().toURL());
        urlCount++;
      } catch (Exception e) {
        logger.error("Failed to create URL for file '" + f.getPath() + "'");
      }
    }*/

    //create the new classloader
    mClassloader = ClassLoader.getSystemClassLoader();//new URLClassLoader(urls.toArray(new URL[]{}), ClassLoader.getSystemClassLoader());
   /* if (urlCount == fileCount) {
      logger.info("Created classloader for " + urlCount + " plugins");
    } else {
      logger.warn("Created classloader for " + urlCount + "/" + fileCount + " plugins");
    }*/
  }

  /**
   * Execute the village parser plugin
   *
   * @param pData The text that contains village coordinates
   * @return List<Village> Parsed village list
   */
  public List<Village> executeVillageParser(String pData) {
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.VillageParser");
      return ((GenericParserInterface<Village>) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute village parser", e);
    }
    return new LinkedList<>();
  }

  /**
   * Execute the merchant parser plugin
   *
   * @param pData The text that contains merchant infos
   * @return List<VillageMerchantInfo> Parsed list of merchant infos
   */
  public List<VillageMerchantInfo> executeMerchantParser(String pData) {
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.MerchantParser");
      return ((GenericParserInterface<VillageMerchantInfo>) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute merchant parser", e);
    }
    return new LinkedList<>();
  }

  public boolean executeDiplomacyParser(String pData) {
    logger.info("Executing diplomacy parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.DiplomacyParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute diplomacy parser", e);
    }
    return false;
  }

  public boolean executeSupportParser(String pData) {
    logger.info("Executing support parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.SupportParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute support parser", e);
    }
    return false;
  }

  public boolean executeGroupParser(String pData) {
    logger.info("Executing group parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.GroupParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute group parser", e);
    }
    return false;
  }

  public boolean executeNonPAPlaceParser(String pData) {
    logger.info("Executing place parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.NonPAPlaceParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute place parser", e);
    }
    return false;
  }

  public boolean executeReportParser(String pData) {
    logger.info("Executing report parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.ReportParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute report parser", e);
    }
    return false;
  }

  public boolean executeObstReportParser(String pData) {
    logger.info("Executing obst report parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.OBSTServerReportHandler");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute obst report parser", e);
    }
    return false;
  }

  public List<SOSRequest> executeSOSParser(String pData) {
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.SOSParser");
      return ((GenericParserInterface<SOSRequest>) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute sos request parser", e);
    }
    return new LinkedList<>();
  }

  public boolean executeTroopsParser(String pData) {
    logger.info("Executing troops parser");
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.TroopsParser70");
      if (((SilentParserInterface) parser).parse(pData)) {
        //return only of troops could be parsed. otherwise continue with legacy parser
        return true;
      } else {
        logger.info("Troops parser 7.0 returned no result. Trying legacy version....");
      }
    } catch (Exception e) {
      logger.error("Failed to execute troops parser version 7.0", e);
    }

    try {
      Object parser = loadParser("de.tor.tribes.util.parser.TroopsParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute troops parser", e);
    }
    return false;
  }
  
  public boolean executeMovementParser(String pData) {
    try {
      Object parser = loadParser("de.tor.tribes.util.parser.MovementParser");
      return ((SilentParserInterface) parser).parse(pData);
    } catch (Exception e) {
      logger.error("Failed to execute Movemen parser", e);
    }
    return false;
  }

  public String getVariableValue(String pName) {
    if (mVariableManager == null) {
      try {
        Class managerClass = mClassloader.loadClass("de.tor.tribes.util.parser.ParserVariableManager");
        mVariableManager = managerClass.getMethod("getSingleton").invoke(null);
      } catch (Exception e) {
        logger.error("Failed to load parser variable manager", e);
      }
    }
    try {
      return (String) mVariableManager.getClass().getMethod("getProperty", String.class).invoke(mVariableManager, pName);
    } catch (Exception e) {
      logger.error("Failed to execute method getProperty() on parser variable manager", e);
      return "";
    }
  }

  private Object loadParser(String pClazz) throws Exception {
    return mClassloader.loadClass(pClazz).newInstance();
  }
}
