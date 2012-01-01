/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Map;

/**
 *
 * @author Torridity
 */
public class ProxyHelper {

    public static Proxy getProxyFromProperties(Map pProperties) {
        Proxy webProxy = null;
        boolean useProxy = Boolean.parseBoolean((String) pProperties.get("proxySet"));
        if (useProxy) {
            String host = (String) pProperties.get("proxyHost");
            int port = Integer.parseInt((String) pProperties.get("proxyPort"));
            int type = Integer.parseInt((String) pProperties.get("proxyType"));
            final String user = (String) pProperties.get("proxyUser");
            final String password = (String) pProperties.get("proxyPassword");
            InetSocketAddress addr = new InetSocketAddress(host, port);

            switch (type) {
                case 1: {
                    System.out.println("SP");
                    webProxy = new Proxy(Proxy.Type.SOCKS, addr);
                    break;
                }
                default: {
                    System.out.println("HP");
                    webProxy = new Proxy(Proxy.Type.HTTP, addr);
                    break;
                }
            }

            if ((user.length() >= 1) && (password.length() > 1)) {
                System.out.println("A");
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password.toCharArray());
                    }
                });
            }else{
                System.out.println("NOA");
                   Authenticator.setDefault(null);
            }
        } else {
            webProxy = Proxy.NO_PROXY;
        }

        return webProxy;
    }
}
