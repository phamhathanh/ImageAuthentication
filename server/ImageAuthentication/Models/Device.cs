using System;
using System.Collections.Generic;
using System.Linq;
using System.ComponentModel.DataAnnotations;

namespace ImageAuthentication.Models
{
    public class Device
    {
        public int ID { get; set; }
        [Required]
        public long DeviceID { get; set; }
        public byte[] PasswordHash { get; set; }
    }
}