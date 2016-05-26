namespace ImageAuthentication.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class initial : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.Devices",
                c => new
                    {
                        ID = c.Int(nullable: false, identity: true),
                        DeviceID = c.Long(nullable: false),
                        PasswordHash = c.Binary(nullable: false),
                    })
                .PrimaryKey(t => t.ID)
                .Index(t => t.DeviceID, unique: true, name: "UK_Devices");
            
            CreateTable(
                "dbo.Images",
                c => new
                    {
                        ID = c.Int(nullable: false, identity: true),
                        Base64String = c.String(nullable: false, maxLength: 8000, unicode: false),
                    })
                .PrimaryKey(t => t.ID);
            
        }
        
        public override void Down()
        {
            DropIndex("dbo.Devices", "UK_Devices");
            DropTable("dbo.Images");
            DropTable("dbo.Devices");
        }
    }
}
