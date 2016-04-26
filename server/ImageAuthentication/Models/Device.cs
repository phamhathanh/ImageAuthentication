using System;
using System.Collections.Generic;
using System.Linq;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ImageAuthentication.Models
{
    public class Device
    {
        public int ID { get; set; }
        [Required]
        [Index("UK_Devices", IsUnique = true)]
        public long DeviceID { get; set; }
        [Required]
        public byte[] PasswordHash { get; set; }
    }
}