using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ImageAuthentication.Models
{
    public class Device
    {
        public long DeviceID { get; set; }
        public SHA256Hash PasswordHash { get; set; }
    }
}