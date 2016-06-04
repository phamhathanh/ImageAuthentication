package com.authpro.imageauthentication;

import android.content.Context;
import android.provider.Settings;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils
{
    public static String deviceURI(Context context)
    {
        String rawDeviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(rawDeviceID, 16);
        return "api/devices/" + deviceID;
    }

    public static String computeHash(String text)
    {
        byte[] bytesData;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            bytesData = digest.digest(text.getBytes("UTF-8"));
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
