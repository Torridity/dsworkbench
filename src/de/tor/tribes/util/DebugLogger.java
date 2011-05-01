/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class DebugLogger {

    public static void initialize() {
        ConsoleAppender a = new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n"));
        Logger.getRootLogger().setLevel(Level.DEBUG);
        Logger.getRootLogger().addAppender(a);
    }
}
