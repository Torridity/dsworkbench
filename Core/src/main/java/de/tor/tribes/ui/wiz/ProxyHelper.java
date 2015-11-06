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
                    webProxy = new Proxy(Proxy.Type.SOCKS, addr);
                    break;
                }
                default: {
                    webProxy = new Proxy(Proxy.Type.HTTP, addr);
                    break;
                }
            }

            if ((user.length() >= 1) && (password.length() > 1)) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password.toCharArray());
                    }
                });
            }else{
                   Authenticator.setDefault(null);
            }
        } else {
            webProxy = Proxy.NO_PROXY;
        }

        return webProxy;
    }
}
