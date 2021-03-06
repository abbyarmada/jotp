package org.amdelamar.jotp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.amdelamar.jotp.exception.BadOperationException;
import org.amdelamar.jotp.type.HOTP;
import org.amdelamar.jotp.type.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

public class OTP {

    public enum Type {
        HOTP, TOTP
    }

    public final static int BYTES = 20; // 160 bit

    /**
     * Generate a random string using the characters provided, with the specified length.
     * 
     * @param characters
     * @param length
     *            default 20
     * @return secure random string
     */
    public static String random(String characters, int length) {
        if (length < 1)
            length = BYTES;
        java.security.SecureRandom random = new java.security.SecureRandom();
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    /**
     * Generate a random string in Base32, with the specified length.
     * 
     * @param length
     *            default 20
     * @return secure random string
     */
    public static String randomBase32(int length) {
        if (length < 1)
            length = BYTES;
        byte[] bytes = new byte[length];
        java.security.SecureRandom random = new java.security.SecureRandom();
        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    }

    /**
     * Generate a random string in Hexadecimal, with the specified length.
     * 
     * @param length
     *            default 20
     * @return secure random string
     */
    public static String randomHex(int length) {
        if (length < 1)
            length = BYTES;
        byte[] bytes = new byte[length];
        java.security.SecureRandom random = new java.security.SecureRandom();
        random.nextBytes(bytes);

        return Hex.encodeHexString(bytes);
    }

    /**
     * Create a one-time-password with the given key, base, digits, and OTP.Type.
     * 
     * @param key
     *            The secret. Shhhhhh!
     * @param base
     *            The offset. (e.g. TOTP base is time from UTC rounded to the half-second while HOTP
     *            is a counter)
     * @param digits
     *            The length of the code (e.g. 6 for 123006)
     * @param type
     *            Type.TOTP or Type.HOTP
     * @return code
     * @throws BadOperationException
     * @see https://tools.ietf.org/html/rfc4226
     * @see https://tools.ietf.org/html/rfc6238
     */
    public static String create(String key, String base, int digits, Type type) throws BadOperationException {

        if (type == Type.HOTP) {
            HOTP hotp = new HOTP();
            return hotp.create(key, base, digits);
        } else if (type == Type.TOTP) {
            TOTP totp = new TOTP();
            return totp.create(key, base, digits);
        } else {
            // Type not recognized
            throw new BadOperationException("OTP Type not recognized.");
        }
    }

    /**
     * Create a one-time-password with the given key, base, and digits.
     * 
     * @param key
     *            The secret. Shhhhhh!
     * @param base
     *            The offset. (HOTP is a counter incremented by each use)
     * @param digits
     *            The length of the code (e.g. 6 for 123006)
     * @return code
     * @throws BadOperationException
     * @see https://tools.ietf.org/html/rfc4226
     */
    public static String createHOTP(String key, String base, int digits) {
        HOTP hotp = new HOTP();
        return hotp.create(key, base, digits);
    }

    /**
     * Create a one-time-password with the given key, base, and digits.
     * 
     * @param key
     *            The secret. Shhhhhh!
     * @param base
     *            The offset. (TOTP base is time from UTC rounded to the half-second)
     * @param digits
     *            The length of the code (e.g. 6 for 123006)
     * @return code
     * @throws BadOperationException
     * @see https://tools.ietf.org/html/rfc6238
     */
    public static String createTOTP(String key, String base, int digits) {
        TOTP totp = new TOTP();
        return totp.create(key, base, digits);
    }

    /**
     * Returns true if the code is valid for the Hmac-based OTP of the secret.
     * 
     * @param secret
     *            Shhhhh.
     * @param code
     * @param digits
     *            Length of code (e.g. 6 for 123006)
     * @return true if valid
     * @see https://tools.ietf.org/html/rfc4226
     */
    public static boolean verifyHOTP(String secret, String code, int digits) {
        try {
            // get base time in Hex
            long t = (long) Math.floor(Math.round(((double) System.currentTimeMillis()) / 1000.0) / 30l);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(t);
            dos.close();
            byte[] longBytes = baos.toByteArray();
            String base = Hex.encodeHexString(longBytes);

            // convert Base32 secret to Hex
            byte[] bytes = new Base32().decode(secret);
            String key = Hex.encodeHexString(bytes);

            String ncode = createHOTP(key, base, digits);

            // compare OTP codes
            if (code.equals(ncode))
                return true;
            else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if the code is valid for the Time-based OTP of the secret.
     * 
     * @param secret
     *            Shhhhh.
     * @param code
     * @param digits
     *            Length of code (e.g. 6 for 123006)
     * @return true if valid
     * @see https://tools.ietf.org/html/rfc6238
     */
    public static boolean verifyTOTP(String secret, String code, int digits) {
        try {
            // get base time in Hex
            long t = (long) Math.floor(Math.round(((double) System.currentTimeMillis()) / 1000.0) / 30l);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(t);
            dos.close();
            byte[] longBytes = baos.toByteArray();
            String base = Hex.encodeHexString(longBytes);

            // convert Base32 secret to Hex
            byte[] bytes = new Base32().decode(secret);
            String key = Hex.encodeHexString(bytes);

            String ncode = createTOTP(key, base, digits);

            // compare OTP codes
            if (code.equals(ncode))
                return true;
            else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
