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
package de.tor.tribes.sec;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class SecurityAdapter {

    private static Logger logger = Logger.getLogger("SecurityTools");

    public static String hashStringMD5(String pData) {
        String hashed = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(pData.getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            hashed = hash.toString(16);
        } catch (NoSuchAlgorithmException nsae) {
            // ignore
            logger.warn("Unknown error while hashing string (ignored)", nsae);
        }
        return hashed;
    }

    public static String hashStringSHA1(String pData) {
        String hashed = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("SHA1");
            md5.update(pData.getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            hashed = hash.toString(16);
        } catch (NoSuchAlgorithmException nsae) {
            // ignore
            logger.warn("Unknown error while hashing string (ignored)", nsae);
        }
        return hashed;
    }

    public static String getUniqueID() {
        String result = "";
        try {
            InetAddress address = InetAddress.getLocalHost();

            /*
             * Get NetworkInterface for the current host and then read the 
             * hardware address.
             */
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();

            /*
             * Extract each array of mac address and convert it to hexa with the 
             * following format 08-00-27-DC-4A-9E.
             */
            for (byte aMac : mac) {
                result += String.format("%02X", aMac);
            }
        } catch (Exception e) {
            logger.error("Failed to get unique ID", e);
            result = null;
        }
        return result;
    }
    //SELECT * FROM `users` WHERE SHA1(CONCAT(`name`,`password`))<>'dfe312c89cce6e72e06124a22dea1ffbd2515d6';
  
}
