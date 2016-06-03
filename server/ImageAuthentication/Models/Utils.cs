using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;

namespace ImageAuthentication.Models
{
    public static class Utils
    {
        public static string ComputeCorrectResponse(string method, string uri, string nonce, string qop, int nc, string cnonce, string passwordHash)
        {
            var ha1 = passwordHash;
            var ha2 = ComputeHashString($"{method}:{uri}");
            var ha3 = ComputeHashString($"{ha1}:{nonce}:{nc}:{cnonce}:{qop}:{ha2}");
            return ha3;
        }

        public static string ComputeHashString(string text)
        {
            var hash = ComputeHash(text);
            return BitConverter.ToString(hash).Replace("-", string.Empty).ToLowerInvariant();
        }

        public static byte[] ComputeHash(string text)
        {
            SHA256Managed hasher = new SHA256Managed();
            var bytes = Encoding.UTF8.GetBytes(text);
            var hash = hasher.ComputeHash(bytes, 0, bytes.Length);
            return hash;
        }
    }
}