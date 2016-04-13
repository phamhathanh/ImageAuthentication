using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ImageAuthentication.Models
{
    public class Device
    {
        public short DeviceID { get; set; }
        public int PasswordHash { get; set; }
    }
}