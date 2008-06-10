/*
 * FileFormatException.java
 * 
 * Created on 09.10.2007, 17:56:10
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.io;

/**
 *
 * @author Charon
 */
public class FileFormatException extends Exception{
    
    public FileFormatException(){
        super();
    }

    public FileFormatException(String message){
        super(message);
    }
    
}
