/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.Attack;
import de.tor.tribes.types.FightReport;

/**
 *
 * @author Torridity
 */
public class FakeFilter implements ReportFilterInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return (c.guessType() == Attack.FAKE_TYPE);
    }

    @Override
    public String getDescription() {
        return "Filtert Fake-Berichte";
    }

    @Override
    public String getStringRepresentation() {
        return "Fakes";
    }
}