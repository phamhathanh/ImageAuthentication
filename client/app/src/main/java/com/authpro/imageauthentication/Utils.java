package com.authpro.imageauthentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils
{
    public static String computeHash(String input, long deviceID)
    {
        input += deviceID;
        byte[] bytesData;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            bytesData = digest.digest(input.getBytes("UTF-8"));
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException exception)
        {
            throw new RuntimeException();
        }

        StringBuilder builder = new StringBuilder();
        for (byte byteData : bytesData)
            builder.append(Integer.toString((byteData & 0xff) + 0x100, 16).substring(1));
        return builder.toString();
    }
}
