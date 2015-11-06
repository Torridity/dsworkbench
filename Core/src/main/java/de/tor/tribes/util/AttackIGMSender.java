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

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Tribe;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class AttackIGMSender {

    public static final int ID_TOO_MANY_IGMS_PER_TRIBE = -1;
    public static final int ID_ERROR_WHILE_SUBMITTING = -2;

    public static SenderResult sendAttackNotifications(List<Attack> pAttacks, String pSubject, String pApiKey) {
        Hashtable<Tribe, List<Attack>> attacks = new Hashtable<Tribe, List<Attack>>();
        for (Attack a : pAttacks) {
            Tribe sender = a.getSource().getTribe();
            if (sender != null) {
                List<Attack> attacksForSender = attacks.get(sender);
                if (attacksForSender == null) {
                    attacksForSender = new LinkedList<Attack>();
                    attacks.put(sender, attacksForSender);
                }
                attacksForSender.add(a);
            }
        }

        Enumeration<Tribe> tribeKeys = attacks.keys();
        String sUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        String messageStart = "[i](Diese IGM wurde automatisch durch DS Workbench generiert)[/i]\n\n";
        while (tribeKeys.hasMoreElements()) {
            Tribe t = tribeKeys.nextElement();
            List<Attack> attacksForTribe = attacks.get(t);
            String message = messageStart;
            List<String> messages = new LinkedList<String>();
            for (Attack a : attacksForTribe) {
                String line = AttackToBBCodeFormater.formatAttack(a, sUrl, false);
                if (message.length() + line.length() > 2000) {
                    messages.add(message);
                    message = messageStart + line;
                } else {
                    message += line;
                }
            }

            if (messages.size() > 8) {
                //9 messages + rest
                String warning = "An Spieler " + t + " müssten mehr als 10 IGMs verschickt werden.\n";
                warning += "Sende entweder mehr Angriffe pro IGM oder teile das Versenden auf mehrere Vorgänge auf.";
                return new SenderResult(ID_ERROR_WHILE_SUBMITTING, warning);
            }

            int cnt = 1;
            if (messages.size() > 0) {
                //send multi messages
                for (String m : messages) {
                    String sub = pSubject + " (Teil " + cnt + "/" + (messages.size() + 1) + ")";
                    if (!IGMSender.sendIGM(t, pApiKey, sub, m)) {
                        //JOptionPaneHelper.showErrorBox(jSendAttacksIGMDialog, "Fehler beim Versenden von IGM " + cnt + " an '" + t + "'", "Fehler");
                        return new SenderResult(ID_ERROR_WHILE_SUBMITTING, "Fehler beim Versenden von IGM " + cnt + " an '" + t + "'");
                    }
                    cnt++;
                }
            }
            String sub = pSubject + " (Teil " + cnt + "/" + (messages.size() + 1) + ")";
            if (!IGMSender.sendIGM(t, pApiKey, sub, message)) {
                return new SenderResult(ID_ERROR_WHILE_SUBMITTING, "Fehler beim Versenden von IGM " + cnt + " an '" + t + "'");
                //JOptionPaneHelper.showErrorBox(jSendAttacksIGMDialog, "Fehler beim Versenden von IGM " + cnt + " an '" + t + "'", "Fehler");
                //return;
            }
        }
        return new SenderResult();
    }

    public static class SenderResult {

        private int iCode = 0;
        private String sMessage = "Nachtichten erfolgreich versandt.";

        public SenderResult() {
        }

        public SenderResult(int pCode, String pMessage) {
            iCode = pCode;
            sMessage = pMessage;
        }

        public int getCode() {
            return iCode;
        }

        public String getMessage() {
            return sMessage;
        }
    }
}
