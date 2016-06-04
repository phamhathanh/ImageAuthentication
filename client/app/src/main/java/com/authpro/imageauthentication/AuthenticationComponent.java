package com.authpro.imageauthentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class AuthenticationComponent implements ICallbackable<HttpResult>
{
    private Challenge challenge;
    private int nc = 0;
    private final long deviceID;
    private final String method;

    public AuthenticationComponent(long deviceID, HttpMethod method)
    {
        this.deviceID = deviceID;
        this.method = method.name();

        String url = Config.API_URL + "api/devices/" + deviceID;
        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    @Override
    public void callback(HttpResult result)
    {
        switch (result.getStatus())
        {
            case UNAUTHORIZED:
                String header = result.getHeader("WWW-Authenticate");
                this.challenge = Challenge.Parse(header);
                break;
            default:
                // Fail silently.
        }
    }

    public String buildAuthorizationHeader(String password)
    {
        String realm = challenge.getRealm();
        String nonce = challenge.getNonce();
        String qop = challenge.getQOP();
        String cNonce = UUID.randomUUID().toString().replaceAll("-", "");
        String path = "api/devices/" + deviceID;

        nc++;

        String response = computeResponse(method, path, realm, nonce, qop, nc, cNonce, deviceID, password);
        String header = "iAuth "+"realm=\""+realm+"\",nonce=\""+nonce+"\",uri=\""+path
                +"\",qop=\""+qop+"\",nc="+nc+",cnonce=\""+cNonce+"\",response=\""+response+"\"";
        return header;
    }

    private String computeResponse(String method, String path, String realm, String nonce,
                            String qop, int nc, String cNonce, long deviceID, String password)
    {
        String ha1 = computeHash(deviceID+":"+realm+":"+password);
        String ha2 = computeHash(method+":"+path);
        String ha3 = computeHash(ha1+":"+nonce+":"+nc+":"+cNonce+":"+qop+":"+ha2);
        return ha3;
    }

    private String computeHash(String text)
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
