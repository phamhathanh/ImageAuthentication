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
        private readonly byte[] bytes;

        public SHA256Hash(byte[] bytes)
        {
            if (bytes.Length != 32)
                throw new ArgumentException("Byte array must be exactly 32 bytes.");

            this.bytes = (byte[])bytes.Clone();
        }

        public SHA256Hash(string input)
        {
            var hasher = SHA256.Create();
            var rawBytes = Encoding.Unicode.GetBytes(input);
            this.bytes = hasher.ComputeHash(rawBytes);
            Debug.Assert(bytes.Length == 32);
        }

        public static bool operator ==(SHA256Hash hash1, SHA256Hash hash2)
        {
            for (int i = 0; i < 32; i++)
                if (hash1.bytes[i] != hash2.bytes[i])
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
            int hash = bytes.Length;
            foreach (var item in bytes)
                hash = unchecked(hash * 314159 + item);
            return hash;
        }

        public override string ToString()
        {
            return BitConverter.ToString(bytes);
        }
    }
}