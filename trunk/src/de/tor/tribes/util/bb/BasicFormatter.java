/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.bb;

import de.tor.tribes.util.BBFormatterInterface;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.GlobalOptions;
import java.text.NumberFormat;
import java.util.List;

/**
 *
 * @author Torridity
 */
public abstract class BasicFormatter<C extends BBSupport> implements BBFormatterInterface {

    private String sCustomTemplate = null;

    public BasicFormatter() {
        sCustomTemplate = getTemplate();
    }

    @Override
    public final void storeProperty() {
        GlobalOptions.addProperty(getPropertyKey(), sCustomTemplate);
    }

    public abstract String formatElements(List<C> pElements, boolean pExtended);

    @Override
    public final String getTemplate() {
        sCustomTemplate = GlobalOptions.getProperty(getPropertyKey());
        if (sCustomTemplate == null) {
            sCustomTemplate = getStandardTemplate();
        }
        return sCustomTemplate;
    }

    public boolean hasHeaderAndFooter() {
        String template = getTemplate();
        return template.indexOf(LIST_START) >= 0 && template.indexOf(LIST_END) >= 0;
    }

    public String getHeader() {
        String template = getTemplate();
        int listStart = template.indexOf(LIST_START);
        if (listStart < 0) {
            return "";
        }
        return template.substring(0, listStart);
    }

    /**
     * 
     * @return
     */
    public String getLineTemplate() {
        String template = getTemplate();
        if (!hasHeaderAndFooter()) {
            return template;
        }
        int listStart = template.indexOf(LIST_START);
        int listEnd = template.indexOf(LIST_END);
        return template.substring(listStart + LIST_START.length(), listEnd);
    }

    public String getFooter() {
        String template = getTemplate();
        int listEnd = template.indexOf(LIST_END);
        if (listEnd < 0) {
            return "";
        }
        return template.substring(listEnd + LIST_END.length());

    }

    public NumberFormat getNumberFormatter(int pDigits) {
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumFractionDigits(0);
        f.setMinimumFractionDigits(0);
        if (pDigits < 10) {
            f.setMaximumIntegerDigits(1);
            f.setMaximumIntegerDigits(1);
        } else if (pDigits < 100) {
            f.setMaximumIntegerDigits(2);
            f.setMaximumIntegerDigits(2);
        } else {
            f.setMaximumIntegerDigits(3);
            f.setMaximumIntegerDigits(3);
        }
        return f;
    }

    @Override
    public final void setCustomTemplate(String pTemplate) {
        sCustomTemplate = pTemplate;
        storeProperty();
    }
}
