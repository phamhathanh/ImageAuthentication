package com.authpro.imageauthentication;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Utils
{
    public static String deviceURI(Context context)
    {
        String rawDeviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(rawDeviceID, 16);
        return "api/devices/" + deviceID;
    }

    public static String computeHash(String input, long deviceID)
    {
        return "";
        // Prevent breaking.
    }

    public static String computeHeader(String method, String path, Challenge challenge, int nc, long deviceID, String password)
    {
        String realm = challenge.getRealm();
        String nonce = challenge.getNonce();
        String qop = challenge.getQOP();
        String cNonce = generateCNonce();

        String response = computeResponse(method, path, realm, nonce, qop, nc, cNonce, deviceID, password);

        String header = "iAuth "+"realm=\""+realm+"\",nonce=\""+nonce+"\",uri=\""+path+"\",qop=\""+qop+"\",nc="+nc+",cnonce=\""+cNonce+"\",response=\""+response+"\"";
        return header;
    }

    private static String computeResponse(String method, String path, String realm, String nonce, String qop, int nc, String cNonce, long deviceID, String password)
    {
        String ha1 = computeHash(deviceID+":"+realm+":"+password);
        String ha2 = computeHash(method+":"+path);
        String ha3 = computeHash(ha1+":"+nonce+":"+nc+":"+cNonce+":"+qop+":"+ha2);
        return ha3;
    }

    private static String generateCNonce()
    {
        return UUID.randomUUID().toString().replaceAll("-", "");
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

    public static Challenge ParseChallenge(String header)
    {
        if (header == null || !header.startsWith("iAuth"))
            throw new RuntimeException("Bad header.");

        String paramsString = header.substring("iAuth ".length());
        String[] rawParams = paramsString.split(",");

        String realm = getStringParam("realm", rawParams[0]);
        String qop = getStringParam("qop", rawParams[1]);
        String nonce = getStringParam("nonce", rawParams[2]);

        return new Challenge(realm, qop, nonce);
    }

    private static String getStringParam(String paramName, String rawParam)
    {
        if (rawParam == null || !rawParam.startsWith(paramName))
            throw new RuntimeException("Bad header.");
        String quoted = rawParam.substring(paramName.length() + 1);
        return quoted.substring(1, quoted.length() - 1);
    }
}
