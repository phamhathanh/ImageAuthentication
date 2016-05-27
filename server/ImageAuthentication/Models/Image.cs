using System;
using System.Collections.Generic;
using System.Linq;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ImageAuthentication.Models
{
    public class Image
    {
        public int ID { get; set; }
        [Required]
        [Column(TypeName = "VARCHAR(MAX)")]
        public string Base64String { get; set; }
    }
}