/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *@TODO (DIFF) Template based BB attack export
 * @author Charon
 */
public class AttackToBBCodeFormater {

    public static String formatAttack(Attack pAttack, String pServerURL, boolean pExtended) {
        //  StringBuffer buffer = new StringBuffer();
        String sendtime = null;
        String arrivetime = null;
        String template = GlobalOptions.getProperty("attack.bbexport.template");
        if (template == null) {
            template = "%TYPE% von %ATTACKER% aus %SOURCE% mit %UNIT% auf %DEFENDER% in %TARGET% startet am [color=red]%SEND%[/color] und kommt am [color=green]%ARRIVE%[/color] an";
        }

        Date aTime = pAttack.getArriveTime();
        Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(pAttack.getSource(), pAttack.getTarget(), pAttack.getUnit().getSpeed()) * 1000));
        if (pExtended) {
            //sendtime = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(sTime);
            //arrivetime = new SimpleDateFormat("'[color=green]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(aTime);
            sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(sTime);
            arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(aTime);
        } else {
            sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(sTime);
            arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(aTime);
        }

        switch (pAttack.getType()) {
            case Attack.CLEAN_TYPE: {
                // buffer.append("Angriff (Clean-Off) ");
                template = template.replaceAll("%TYPE%", "Angriff (Clean-Off)");
                break;
            }
            case Attack.FAKE_TYPE: {
                //buffer.append("Angriff (Fake) ");
                template = template.replaceAll("%TYPE%", "Angriff (Fake)");
                break;
            }
            case Attack.SNOB_TYPE: {
                // buffer.append("Angriff (AG) ");
                template = template.replaceAll("%TYPE%", "Angriff (AG)");
                break;
            }
            case Attack.SUPPORT_TYPE: {
                //buffer.append("Unterstützung ");
                template = template.replaceAll("%TYPE%", "Unterstützung");
                break;
            }
            default: {
                // buffer.append("Angriff ");
                template = template.replaceAll("%TYPE%", "Angriff");
            }
        }

        //  if (Boolean.parseBoolean(GlobalOptions.getProperty("export.tribe.names"))) {
        //buffer.append(" von ");
        if (pAttack.getSource().getTribe() != null) {
            //buffer.append(pAttack.getSource().getTribe().toBBCode());
            template = template.replaceAll("%ATTACKER%", pAttack.getSource().getTribe().toBBCode());
        } else {
            //buffer.append("Barbaren");
            template = template.replaceAll("%ATTACKER%", "Barbaren");
        }
        //  }
        // buffer.append(" aus ");
        //  buffer.append(pAttack.getSource().toBBCode());
        template = template.replaceAll("%SOURCE%", pAttack.getSource().toBBCode());
        //  if (Boolean.parseBoolean(GlobalOptions.getProperty("export.units"))) {
        // buffer.append(" mit ");
        if (pExtended) {
            // buffer.append("[img]" + pServerURL + "/graphic/unit/unit_" + pAttack.getUnit().getPlainName() + ".png[/img]");
            template = template.replaceAll("%UNIT%", "[img]" + pServerURL + "/graphic/unit/unit_" + pAttack.getUnit().getPlainName() + ".png[/img]");
        } else {
            //buffer.append(pAttack.getUnit().getName());
            template = template.replaceAll("%UNIT%", pAttack.getUnit().getName());
        }
        // }
        // buffer.append(" auf ");

        //  if (Boolean.parseBoolean(GlobalOptions.getProperty("export.tribe.names"))) {
        if (pAttack.getTarget().getTribe() != null) {
            //  buffer.append(pAttack.getTarget().getTribe().toBBCode());
            template = template.replaceAll("%DEFENDER%", pAttack.getTarget().getTribe().toBBCode());
        } else {
            //  buffer.append("Barbaren");
            template = template.replaceAll("%DEFENDER%", "Barbaren");
        }
        //buffer.append(" in ");
        // }

        //buffer.append(pAttack.getTarget().toBBCode());
        template = template.replaceAll("%TARGET%", pAttack.getTarget().toBBCode());
        // buffer.append(" startet am ");
        //buffer.append(sendtime);
        template = template.replaceAll("%SEND%", sendtime);
        //if (Boolean.parseBoolean(GlobalOptions.getProperty("export.arrive.time"))) {
        //buffer.append(" und kommt am ");
        //buffer.append(arrivetime);
        template = template.replaceAll("%ARRIVE%", arrivetime);
        //buffer.append(" an\n");
        template += "\n";
        // } else {
        //     buffer.append("\n");
        // }
        // return buffer.toString();
        return template;
    }

    public static String formatAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pSendTime, int pType, String pServerURL, boolean pExtended) {
        String sendtime = null;
        String arrivetime = null;
        String template = GlobalOptions.getProperty("attack.bbexport.template");
        if (template == null) {
            template = "%TYPE% von %ATTACKER% aus %SOURCE% mit %UNIT% auf %DEFENDER% in %TARGET% startet am [color=red]%SEND%[/color] und kommt am [color=green]%ARRIVE%[/color] an";
        }

        Date aTime = new Date(pSendTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(pSource, pTarget, pUnit.getSpeed()) * 1000));

        if (pExtended) {
            //sendtime = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(sTime);
            //arrivetime = new SimpleDateFormat("'[color=green]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(aTime);
            sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(pSendTime);
            arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size]'").format(aTime);
        } else {
            sendtime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(pSendTime);
            arrivetime = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(aTime);
        }

        switch (pType) {
            case Attack.CLEAN_TYPE: {
                // buffer.append("Angriff (Clean-Off) ");
                template = template.replaceAll("%TYPE%", "Angriff (Clean-Off)");
                break;
            }
            case Attack.FAKE_TYPE: {
                //buffer.append("Angriff (Fake) ");
                template = template.replaceAll("%TYPE%", "Angriff (Fake)");
                break;
            }
            case Attack.SNOB_TYPE: {
                // buffer.append("Angriff (AG) ");
                template = template.replaceAll("%TYPE%", "Angriff (AG)");
                break;
            }
            case Attack.SUPPORT_TYPE: {
                //buffer.append("Unterstützung ");
                template = template.replaceAll("%TYPE%", "Unterstützung");
                break;
            }
            default: {
                // buffer.append("Angriff ");
                template = template.replaceAll("%TYPE%", "Angriff");
            }
        }

        //  if (Boolean.parseBoolean(GlobalOptions.getProperty("export.tribe.names"))) {
        //buffer.append(" von ");
        if (pSource.getTribe() != null) {
            //buffer.append(pAttack.getSource().getTribe().toBBCode());
            template = template.replaceAll("%ATTACKER%", pSource.getTribe().toBBCode());
        } else {
            //buffer.append("Barbaren");
            template = template.replaceAll("%ATTACKER%", "Barbaren");
        }
        //  }
        // buffer.append(" aus ");
        //  buffer.append(pAttack.getSource().toBBCode());
        template = template.replaceAll("%SOURCE%", pSource.toBBCode());
        //  if (Boolean.parseBoolean(GlobalOptions.getProperty("export.units"))) {
        // buffer.append(" mit ");
        if (pExtended) {
            // buffer.append("[img]" + pServerURL + "/graphic/unit/unit_" + pAttack.getUnit().getPlainName() + ".png[/img]");
            template = template.replaceAll("%UNIT%", "[img]" + pServerURL + "/graphic/unit/unit_" + pUnit.getPlainName() + ".png[/img]");
        } else {
            //buffer.append(pAttack.getUnit().getName());
            template = template.replaceAll("%UNIT%", pUnit.getName());
        }
        // }
        // buffer.append(" auf ");

        //  if (Boolean.parseBoolean(GlobalOptions.getProperty("export.tribe.names"))) {
        if (pTarget.getTribe() != null) {
            //  buffer.append(pAttack.getTarget().getTribe().toBBCode());
            template = template.replaceAll("%DEFENDER%", pTarget.getTribe().toBBCode());
        } else {
            //  buffer.append("Barbaren");
            template = template.replaceAll("%DEFENDER%", "Barbaren");
        }
        //buffer.append(" in ");
        // }

        //buffer.append(pAttack.getTarget().toBBCode());
        template = template.replaceAll("%TARGET%", pTarget.toBBCode());
        // buffer.append(" startet am ");
        //buffer.append(sendtime);
        template = template.replaceAll("%SEND%", sendtime);
        //if (Boolean.parseBoolean(GlobalOptions.getProperty("export.arrive.time"))) {
        //buffer.append(" und kommt am ");
        //buffer.append(arrivetime);
        template = template.replaceAll("%ARRIVE%", arrivetime);
        //buffer.append(" an\n");
        template += "\n";
        // } else {
        //     buffer.append("\n");
        // }
        // return buffer.toString();
        return template;
    }
}
