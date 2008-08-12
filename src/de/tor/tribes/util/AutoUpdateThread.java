/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.db.DatabaseAdapter;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class AutoUpdateThread extends Thread {

    private static Logger logger = Logger.getLogger(AutoUpdateThread.class);
    protected static final int UPDATE_NEVER = 0;
    protected static final int UPDATE_ON_STARTUP = 1;
    protected static final int UPDATE_HOURLY = 2;
    protected static final int UPDATE_EVERY_2_HOURS = 3;
    protected static final int UPDATE_EVERY_4_HOURS = 4;
    protected static final int UPDATE_EVERY_8_HOURS = 5;
    protected static final int UPDATE_EVERY_12_HOURS = 6;
    protected static final int UPDATE_DAILY = 7;
    private DSWorkbenchMainFrame mParent = null;

    public AutoUpdateThread(DSWorkbenchMainFrame pParent) {
        setDaemon(true);
        setPriority(MIN_PRIORITY);
        mParent = pParent;
    }

    @Override
    public void run() {
        while (true) {
            String prop = GlobalOptions.getProperty("auto.update.interval");
            if (prop != null) {
                int interval = Integer.parseInt(prop);
                if (updateAvailable(interval)) {
                    mParent.updateAvailable();
                }
            }
            try {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            } catch (Exception e) {
                logger.error("Exception while auto update", e);
            }
        }
    }

    private boolean updateAvailable(int pCurrentInterval) {
        if (GlobalOptions.isOfflineMode()) {
            return false;
        }
        int msPerHour = 1000 * 60 * 60;
        switch (pCurrentInterval) {
            case UPDATE_HOURLY:
                return checkTime(msPerHour);
            case UPDATE_EVERY_2_HOURS:
                return checkTime(msPerHour * 2);
            case UPDATE_EVERY_4_HOURS:
                return checkTime(msPerHour * 4);
            case UPDATE_EVERY_8_HOURS:
                return checkTime(msPerHour * 8);
            case UPDATE_EVERY_12_HOURS:
                return checkTime(msPerHour * 12);
            case UPDATE_DAILY:
                return checkTime(msPerHour * 24);
            default: {
                //UPDATE_ON_STARTUP or UPDATE_NEVER
                return false;
            }
        }
    }

    public boolean checkTime(int pTimeSpan) {
        String name = GlobalOptions.getProperty("account.name");
        String password = GlobalOptions.getProperty("account.password");
        if ((name == null) || (password == null)) {
            logger.error("Username or password not set");
            return false;
        }

        switch (DatabaseAdapter.checkUser(name, password)) {
            case DatabaseAdapter.ID_CONNECTION_FAILED: {
                logger.error("Connection to database failed");
                return false;
            }
            case DatabaseAdapter.ID_USER_NOT_EXIST: {
                logger.error("Account " + name + " does not exist");
                return false;
            }
            case DatabaseAdapter.ID_WRONG_PASSWORD: {
                logger.error("Wrong password for account " + name);
                return false;
            }
        }
        //account check ok, check update
       /* long delta = DatabaseAdapter.getTimeSinceLastUpdate(name, GlobalOptions.getSelectedServer());

        if (delta == 0) {
            //check failed, do not allow update
            return false;
        } else if (delta < pTimeSpan) {
            //update not yet allowed
            return false;
        } else {
            //update possible
            return true;
        }*/
        return false;
    }
}
