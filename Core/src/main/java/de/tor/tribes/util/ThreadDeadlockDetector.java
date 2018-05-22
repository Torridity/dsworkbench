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

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.log4j.Logger;

public class ThreadDeadlockDetector {
    
    private static Logger logger = Logger.getLogger("ThreadDeadlockDetector");
    private final Timer threadCheck = new Timer("ThreadDeadlockDetector", true);
    private final ThreadMXBean mbean =
            ManagementFactory.getThreadMXBean();
    private final Collection<Listener> listeners =
            new CopyOnWriteArraySet<>();
    /**
     * The number of milliseconds between checking for deadlocks. It may be expensive to check for deadlocks, and it is not critical to know
     * so quickly.
     */
    private static final int DEFAULT_DEADLOCK_CHECK_PERIOD = 10000;
    
    public ThreadDeadlockDetector() {
        this(DEFAULT_DEADLOCK_CHECK_PERIOD);
    }
    
    public ThreadDeadlockDetector(int deadlockCheckPeriod) {
        threadCheck.schedule(new TimerTask() {
            
            @Override
            public void run() {
                checkForDeadlocks();
            }
        }, 10, deadlockCheckPeriod);
    }
    
    private void checkForDeadlocks() {
        logger.debug("Checking for deadlocks");
        long[] ids = findDeadlockedThreads();
        if (ids != null && ids.length > 0) {
            Thread[] threads = new Thread[ids.length];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = findMatchingThread(
                        mbean.getThreadInfo(ids[i]));
            }
            fireDeadlockDetected(threads);
        }
    }
    
    private long[] findDeadlockedThreads() {
        // JDK 1.5 only supports the findMonitorDeadlockedThreads()
        // method, so you need to comment out the following three lines
        if (mbean.isSynchronizerUsageSupported()) {
            return mbean.findDeadlockedThreads();
        } else {
            return mbean.findMonitorDeadlockedThreads();
        }
    }
    
    private void fireDeadlockDetected(Thread[] threads) {
        for (Listener l : listeners) {
            l.deadlockDetected(threads);
        }
    }
    
    private Thread findMatchingThread(ThreadInfo inf) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getId() == inf.getThreadId()) {
                return thread;
            }
        }
        throw new IllegalStateException("Deadlocked Thread not found");
    }
    
    public boolean addListener(Listener l) {
        return listeners.add(l);
    }
    
    public boolean removeListener(Listener l) {
        return listeners.remove(l);
    }

    /**
     * This is called whenever a problem with threads is detected.
     */
    public interface Listener {
        
        void deadlockDetected(Thread[] deadlockedThreads);
    }
    
    public static class DefaultDeadlockListener implements
            ThreadDeadlockDetector.Listener {
        
        private static Logger logger = Logger.getLogger("DefaultDeadlockListener");
        
        @Override
        public void deadlockDetected(Thread[] threads) {
            logger.error("Deadlocked Threads:");
            logger.error("-------------------");
            for (Thread thread : threads) {
                logger.error(thread);
                for (StackTraceElement ste : thread.getStackTrace()) {
                    logger.error("\t" + ste);
                }
            }
        }
    }
}
