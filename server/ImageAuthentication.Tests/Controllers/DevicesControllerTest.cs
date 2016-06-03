using System;
using System.Data.Entity;
using System.Linq;
using System.Net.Http;
using System.Web.Http;
using System.Web.Mvc;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using ImageAuthentication.Controllers;
using ImageAuthentication.Models;
using Moq;
using System.Linq.Expressions;
using System.Collections;
using System.Collections.Generic;
using System.Web.Http.Results;

namespace ImageAuthentication.Tests.Controllers
{
    [TestClass]
    public class DevicesControllerTest
    {
        [TestMethod]
        public void TestVerify()
        {
            long deviceID = 2152111351939131539;

            var passwordHex = "591501427B5F89136BDE60F418799704C3F5BB0644F8325867A2317CC535D787";
            var passwordHash = Enumerable.Range(0, passwordHex.Length)
                     .Where(x => x % 2 == 0)
                     .Select(x => Convert.ToByte(passwordHex.Substring(x, 2), 16))
                     .ToArray();
            var device = new Device() { ID = 1, DeviceID = deviceID, PasswordHash = passwordHash };
            var devices = new[] { device };
            var mockDevices = new Mock<DbSet<Device>>();
            mockDevices.SetupIQueryable(devices.AsQueryable());

            var mockContext = new Mock<IImageAuthenticationContext>();
            mockContext.Setup(db => db.Devices).Returns(mockDevices.Object);

            var controller = new DevicesController(mockContext.Object);

            controller.Request = new HttpRequestMessage();
            controller.Request.Method = new HttpMethod("GET");
            controller.Request.Headers.Add("Authorization", "iAuth realm=\"My Realm\",nonce=\"18b4735085fd4be3950ea92df9e23bca\",uri=\"api/devices/2152111351939131539\",qop=\"auth\",nc=1,cnonce=\"42c5c842278248d6829d703b1317b877\",response=\"0b2bf95ac132686d7bb88ab84cd6b5fbd962275d0ff861cf37ceb616bc8b40d2\"");

            var result = controller.GetDeviceResource(deviceID);
            Assert.IsInstanceOfType(result, typeof(OkResult));
        }


        [TestMethod]
        public void TestHash()
        {
            var method = "GET";
            var uri = "api/devices/2152111351939131539";
            var nonce = "18b4735085fd4be3950ea92df9e23bca";
            var qop = "auth";
            var nc = 1;
            var cnonce = "42c5c842278248d6829d703b1317b877";
            var password = "d707f47716e28275daeed55a90f201fa5665213d9b21cf09980623200c12b246";
            var response = Utils.ComputeCorrectResponse(method, uri, nonce, qop, nc, cnonce, password);

            var ha1 = password;
            var ha2 = Utils.ComputeHashString($"{method}:{uri}");
            var ha3 = Utils.ComputeHashString($"{ha1}:{nonce}:{nc}:{cnonce}:{qop}:{ha2}");

            Assert.AreEqual("30122166df02f670d87682b661699447690a14f0cbae8d4a94ac73c5fcaa88a4", ha2);
            Assert.AreEqual("0b2bf95ac132686d7bb88ab84cd6b5fbd962275d0ff861cf37ceb616bc8b40d2", ha3);
        }
    }
}
