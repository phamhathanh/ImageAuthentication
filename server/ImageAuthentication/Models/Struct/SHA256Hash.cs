using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

namespace ImageAuthentication.Models
{
    public struct SHA256Hash
    {
        private readonly long part0, part1, part2, part3;

        public SHA256Hash(byte[] bytes)
        {
            if (bytes.Length != 32)
                throw new ArgumentException("Byte array must be exactly 32 bytes.");

            part0 = BitConverter.ToInt64(bytes, 0);
            part1 = BitConverter.ToInt64(bytes, 8);
            part2 = BitConverter.ToInt64(bytes, 16);
            part3 = BitConverter.ToInt64(bytes, 24);
        }

        public SHA256Hash(string input)
        {
            var hasher = SHA256.Create();
            var rawBytes = Encoding.Unicode.GetBytes(input);
            var bytes = hasher.ComputeHash(rawBytes);
            Debug.Assert(bytes.Length == 32);

            part0 = BitConverter.ToInt64(bytes, 0);
            part1 = BitConverter.ToInt64(bytes, 8);
            part2 = BitConverter.ToInt64(bytes, 16);
            part3 = BitConverter.ToInt64(bytes, 24);
        }

        public static bool operator ==(SHA256Hash hash1, SHA256Hash hash2)
        {
            if (hash1.part0 != hash2.part0)
                return false;
            if (hash1.part1 != hash2.part1)
                return false;
            if (hash1.part2 != hash2.part2)
                return false;
            if (hash1.part3 != hash2.part3)
                return false;

            return true;
        }

        public static bool operator !=(SHA256Hash hash1, SHA256Hash hash2)
        {
            return !(hash1 == hash2);
        }

        public override bool Equals(object other)
        {
            return other is SHA256Hash && (SHA256Hash)other == this;
        }

        public override int GetHashCode()
        {
            int hash = 17;
            hash = hash * 29 + part0.GetHashCode();
            hash = hash * 29 + part1.GetHashCode();
            hash = hash * 29 + part2.GetHashCode();
            hash = hash * 29 + part3.GetHashCode();
            return hash;
        }

        public override string ToString()
        {
            return part0.ToString("X8") + part1.ToString("X8") 
                + part2.ToString("X8") + part3.ToString("X8");
        }
    }
}