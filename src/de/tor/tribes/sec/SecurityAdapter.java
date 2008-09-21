/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.sec;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.ssl.OpenSSL;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class SecurityAdapter {

    private static Logger logger = Logger.getLogger(SecurityAdapter.class);

    public static String encryptString(String pData, String pPassword) {
        try {
            return new String(OpenSSL.encrypt("des3", pPassword.toCharArray(), pData.getBytes()));
        } catch (Exception e) {
            logger.error("Unknown error while encrypting string", e);
            return null;
        }
    }

    public static String descryptString(String pData, String pPassword) {
        try {
            // Decrypt
            return new String(OpenSSL.decrypt("des3", pPassword.toCharArray(), pData.getBytes()));
        } catch (Exception e) {
            logger.error("Unknown error while decrypting string", e);
            return null;
        }
    }

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
            for (int i = 0; i < mac.length; i++) {
                result += String.format("%02X", mac[i]);
            }
        } catch (Exception e) {
            logger.error("Failed to get unique ID", e);
            result = null;
        }
        return result;
    }
}