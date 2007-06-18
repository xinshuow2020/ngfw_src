/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc. 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.untangle.node.mail.impl.quarantine;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.untangle.node.util.Pair;
import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


//===============================================
// TODO really weak crypto, but should we even
//      care given the exposure?

/**
 * Class responsible for wrapping/unwrapping
 * Authentication Tokens.  Not based on any
 * strong crypto - just keeping it hard for
 * bad guys to do bad things.
 */
class AuthTokenManager {

    enum DecryptOutcome {
        OK,
        NOT_A_TOKEN,
        MALFORMED_TOKEN
    };


    private final Logger m_logger =
        Logger.getLogger(AuthTokenManager.class);

    private static final String ALG = "Blowfish";
    private static final byte[] INNER_MAGIC = "eks".getBytes();
    private static final byte[] OUTER_MAGIC = "emma".getBytes();

    private SecretKeySpec m_key;



    /**
     * Set the key to be used
     */
    void setKey(byte[] key) {
        try {
            m_key = new SecretKeySpec(key, ALG);
        }
        catch(Exception ex) {
            m_logger.warn("Unable to create key", ex);
        }
    }

    /**
     * Create an authentication token for the given username.  The
     * returned token is a String, but may not be web-safe (i.e. URLEncoding).
     * <br><br>
     * If there is any problem, null us returned
     *
     * @param username the username
     *
     * @return the token
     */
    String createAuthToken(String username) {
        try {
            //Create the encrypted payload
            byte[] usernameBytes = username.getBytes();
            byte[] toEncrypt = new byte[usernameBytes.length + INNER_MAGIC.length];
            System.arraycopy(INNER_MAGIC, 0, toEncrypt, 0, INNER_MAGIC.length);
            System.arraycopy(usernameBytes, 0, toEncrypt, INNER_MAGIC.length, usernameBytes.length);

            //Encrypt
            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.ENCRYPT_MODE, m_key);
            byte[] encrypted = c.doFinal(toEncrypt);

            //Put the known chars at the front, to detect a goofed
            //token
            byte[] toEncode = new byte[encrypted.length + OUTER_MAGIC.length];
            System.arraycopy(OUTER_MAGIC, 0, toEncode, 0, OUTER_MAGIC.length);
            System.arraycopy(encrypted, 0, toEncode, OUTER_MAGIC.length, encrypted.length);

            //Encode
            return new BASE64Encoder().encode(toEncode);
        }
        catch(Exception ex) {
            m_logger.warn("Unable to create token", ex);
            return null;
        }
    }


    /**
     * Attempt to decrypt the auth token.
     *
     * @param token the token
     *
     * @return the outcome, where the String is only valid
     *         if the outcome is "OK"
     */
    Pair<DecryptOutcome, String> decryptAuthToken(String token) {
        try {
            //Decode
            byte[] decodedBytes = new BASE64Decoder().decodeBuffer(token);

            //Check for outer magic
            if(!matches(OUTER_MAGIC, decodedBytes, 0, OUTER_MAGIC.length)) {
                return new Pair<DecryptOutcome, String>(DecryptOutcome.NOT_A_TOKEN);
            }

            //Decrypt
            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.DECRYPT_MODE, m_key);
            byte[] decrypted =  c.doFinal(decodedBytes,
                                          OUTER_MAGIC.length,
                                          decodedBytes.length - OUTER_MAGIC.length);

            if(!matches(INNER_MAGIC, decrypted, 0, INNER_MAGIC.length)) {
                return new Pair<DecryptOutcome, String>(DecryptOutcome.MALFORMED_TOKEN);
            }

            return new Pair<DecryptOutcome, String>(DecryptOutcome.OK,
                                                    new String(decrypted,
                                                               INNER_MAGIC.length,
                                                               decrypted.length - INNER_MAGIC.length));

        }
        catch(Exception ex) {
            m_logger.warn("Unable to decrypt token", ex);
            return new Pair<DecryptOutcome, String>(DecryptOutcome.MALFORMED_TOKEN);
        }
    }


    private boolean matches(byte[] pattern,
                            byte[] inspect,
                            int start,
                            int len) {
        if(inspect == null ||
           len < pattern.length) {
            return false;
        }
        for(int i = 0; i<pattern.length; i++) {
            if(inspect[start + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }

    /*
      public static void main(String[] args) throws Exception {
      byte[] bytes = new byte[4];
      new java.util.Random().nextBytes(bytes);

      AuthTokenManager atm = new AuthTokenManager();
      atm.setKey(bytes);

      String tkn = atm.createAuthToken("bscott@untangle.com");
      System.out.println(tkn);
      Pair<DecryptOutcome, String> p = atm.decryptAuthToken(tkn);

      System.out.println(p.a + ", " + p.b);

      }
    */
}
