package com.authpro.imageauthentication;

public class Challenge
{
    private final String realm, nonce, qop;

    public Challenge(String realm, String nonce, String qop)
    {
        this.realm = realm;
        this.nonce = nonce;
        this.qop = qop;
    }

    public String getRealm()
    {
        return realm;
    }

    public String getNonce()
    {
        return nonce;
    }

    public String getQOP()
    {
        return qop;
    }
}
