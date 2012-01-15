/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.util.List;

/**
 *
 * @author Jejkal
 */
public interface GenericParserInterface<T> {

    public List<T> parse(String pData);
}
