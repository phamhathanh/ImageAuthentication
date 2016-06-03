using System;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

namespace ImageAuthentication.Models
{
    public static class Hasher
    {
        public static string ComputeCorrectResponse(string method, string uri, string nonce, string qop, int nc, string cnonce, string passwordHash)
        {
            var ha1 = passwordHash;
            var ha2 = ComputeHash($"{method}:{uri}").ToHexString();
            var ha3 = ComputeHash($"{ha1}:{nonce}:{nc}:{cnonce}:{qop}:{ha2}").ToHexString();
            return ha3;
        }

        public static byte[] ComputeHash(string text)
        {
            SHA256Managed hasher = new SHA256Managed();
            var bytes = Encoding.UTF8.GetBytes(text);
            var hash = hasher.ComputeHash(bytes, 0, bytes.Length);
            return hash;
        }

        public static byte[] ToByteArray(this string hexString)
        {
            return Enumerable.Range(0, hexString.Length)
                    .Where(x => x % 2 == 0)
                    .Select(x => Convert.ToByte(hexString.Substring(x, 2), 16))
                    .ToArray();
        }

        public static string ToHexString(this byte[] bytes) => BitConverter.ToString(bytes).Replace("-", "").ToLowerInvariant();
    }
}