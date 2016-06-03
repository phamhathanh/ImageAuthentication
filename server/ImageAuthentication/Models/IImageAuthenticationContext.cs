using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;
using System.Web;

namespace ImageAuthentication.Models
{
    public interface IImageAuthenticationContext : IDisposable
    {
        DbSet<Device> Devices { get; }
        DbSet<Image> Images { get; }
        int SaveChanges();
    }
}
