using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;

namespace ImageAuthentication.Models
{
    internal class AuthInfo
    {
        public string Realm { get; }
        public string Nonce { get; }
        public string URI { get; }
        public string QOP { get; }
        public int NC { get; }
        public string CNonce { get; }
        public string Response { get; }

        public AuthInfo(string realm, string nonce, string uri, string qop, int nc, string cnonce, string response)
        {
            Realm = realm;
            Nonce = nonce;
            URI = uri;
            QOP = qop;
            NC = nc;
            CNonce = cnonce;
            Response = response;
        }

        public static AuthInfo Parse(string authHeader)
        {
            if (authHeader == null || !authHeader.StartsWith("iAuth"))
                throw new FormatException();

            var paramsString = authHeader.Substring("iAuth ".Length);
            var rawParams = paramsString.Split(',');

            var realm = GetStringParam("realm", rawParams[0]);
            var nonce = GetStringParam("nonce", rawParams[1]);
            var uri = GetStringParam("uri", rawParams[2]);
            var qop = GetStringParam("qop", rawParams[3]);
            var nc = GetUnsignedParam("nc", rawParams[4]);
            var cnonce = GetStringParam("cnonce", rawParams[5]);
            var response = GetStringParam("response", rawParams[6]);

            return new AuthInfo(realm, nonce, uri, qop, nc, cnonce, response);
        }

        private static string GetStringParam(string paramName, string rawParam)
        {
            string quoted = ExtractParam(paramName, rawParam);
            return quoted.Substring(1, quoted.Length - 2);
        }

        private static string ExtractParam(string paramName, string rawParam)
        {
            if (rawParam == null || !rawParam.StartsWith(paramName))
                throw new FormatException();
            return rawParam.Substring(paramName.Length + 1);
        }

        private static int GetUnsignedParam(string paramName, string rawParam)
        {
            string unparsed = ExtractParam(paramName, rawParam);
            int output;
            bool isValid = int.TryParse(unparsed, out output);
            if (!isValid || output <= 0)
                throw new FormatException();
            return output;
        }
    }
}