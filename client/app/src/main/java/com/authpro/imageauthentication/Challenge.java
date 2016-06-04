package com.authpro.imageauthentication;

public class Challenge
{
    private final String realm, qop, nonce;

    public Challenge(String realm, String qop, String nonce)
    {
        this.realm = realm;
        this.qop = qop;
        this.nonce = nonce;
    }

    public static Challenge Parse(String header)
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

    public String getRealm()
    {
        return realm;
    }

    public String getQOP()
    {
        return qop;
    }

    public String getNonce()
    {
        return nonce;
    }
}
