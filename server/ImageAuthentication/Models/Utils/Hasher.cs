using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;

namespace ImageAuthentication.Models
{
    public static class Hasher
    {
        public static byte[] ComputeHash(string input)
        {
            var hasher = SHA256.Create();
            var rawBytes = Encoding.Unicode.GetBytes(input);
            return hasher.ComputeHash(rawBytes);
        }
    }
}