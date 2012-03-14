/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Torridity
 */
public class ReportServer {

    public static void main(String[] args) throws Exception {
        ServerSocket so = new ServerSocket(8000);
        System.out.println("A");
        Socket s = so.accept();
        System.out.println("HERE");
        InputStream in = s.getInputStream();
        System.out.println("DONE");
        byte[] data = new byte[1024];
        int read = 0;
        while ((read = in.read(data)) != -1) {
            System.out.println(new String(data));
        }
    }
}
