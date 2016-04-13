namespace ImageAuthentication.Migrations
{
    using Models;
    using System;
    using System.Data.Entity;
    using System.Data.Entity.Migrations;
    using System.Linq;

    internal sealed class Configuration : DbMigrationsConfiguration<ImageAuthenticationContext>
    {
        public Configuration()
        {
            AutomaticMigrationsEnabled = false;
        }

        protected override void Seed(ImageAuthenticationContext context)
        {
            context.Devices.AddOrUpdate(x => x.ID,
                new Device()
                {
                    DeviceID = 1,
                    PasswordHash = Hasher.ComputeHash("1")
                },
                new Device()
                {
                    DeviceID = 2,
                    PasswordHash = Hasher.ComputeHash("2")
                }
                );
        }
    }
}
