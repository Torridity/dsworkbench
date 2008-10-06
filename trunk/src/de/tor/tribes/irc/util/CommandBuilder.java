/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.irc.util;

/**
 *
 * @author Jejkal
 */
public class CommandBuilder {

    public final static String AWAY_COMMAND = "/away";
    public final static String ME_COMMAND = "/me";
    public final static String PART_COMMAND = "/leave";
    public final static String QUIT_COMMAND = "/quit";
    public final static String WHO_COMMAND = "/who";
    public final static String WHOIS_COMMAND = "/whois";
    public final static String WHOWAS_COMMAND = "/whowas";

    public static boolean isCommand(String pString) {

        if ((pString.toLowerCase().startsWith(ME_COMMAND)) ||
                (pString.toLowerCase().startsWith(ME_COMMAND)) ||
                (pString.toLowerCase().startsWith(PART_COMMAND)) ||
                (pString.toLowerCase().startsWith(QUIT_COMMAND)) ||
                (pString.toLowerCase().startsWith(WHO_COMMAND)) ||
                (pString.toLowerCase().startsWith(WHOIS_COMMAND)) ||
                (pString.toLowerCase().startsWith(WHOWAS_COMMAND))) {
            return true;
        }
        return false;
    }

    public static String parseCommand(String pString) {
        if (pString.startsWith("/")) {
            //command string
            if (pString.toLowerCase().startsWith(ME_COMMAND)) {
            } else if (pString.toLowerCase().startsWith(ME_COMMAND)) {
                String message = pString.substring(4).trim();
                return "ACTION " + message + "";

            }/* else if (pString.toLowerCase().startsWith(ME_COMMAND)) {
            } else if (pString.toLowerCase().startsWith(ME_COMMAND)) {
            } else if (pString.toLowerCase().startsWith(ME_COMMAND)) {
            } else if (pString.toLowerCase().startsWith(ME_COMMAND)) {
            } else if (pString.toLowerCase().startsWith(ME_COMMAND)) {
            }*/ else {
                return "Command not supported";
            }

        } else {
            return pString;
        }
        return "";
    }
}
