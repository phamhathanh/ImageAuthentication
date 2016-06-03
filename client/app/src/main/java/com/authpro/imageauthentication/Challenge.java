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
