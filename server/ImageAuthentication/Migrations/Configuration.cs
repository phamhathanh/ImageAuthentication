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
            AutomaticMigrationsEnabled = true;
        }

        protected override void Seed(ImageAuthenticationContext context)
        {
            context.Devices.AddOrUpdate();
            context.Images.AddOrUpdate();
        }
    }
}
