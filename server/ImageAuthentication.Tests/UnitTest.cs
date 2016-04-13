using ImageAuthentication.Controllers;
using ImageAuthentication.Models;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Http.Results;

namespace ImageAuthentication.Tests
{
    [TestClass]
    public class DeviceControllerTest
    {
        [TestMethod]
        public void GetAllDevices_ShouldReturnAllDevices()
        {
            var testDevices = GetTestDevices();
            var controller = new DeviceController(testDevices);

            var result = controller.GetAllDevices() as List<Device>;
            Assert.AreEqual(testDevices.Count, result.Count);
        }

        [TestMethod]
        public void GetDevice_ShouldReturnCorrectDevice()
        {
            var testDevices = GetTestDevices();
            var controller = new DeviceController(testDevices);

            var result = controller.GetDevice(4) as OkNegotiatedContentResult<Device>;
            Assert.IsNotNull(result);
            Assert.AreEqual(testDevices[3].PasswordHash, result.Content.PasswordHash);
        }

        [TestMethod]
        public void GetDevice_ShouldNotFindDevice()
        {
            var controller = new DeviceController(GetTestDevices());

            var result = controller.GetDevice(999);
            Assert.IsInstanceOfType(result, typeof(NotFoundResult));
        }

        private List<Device> GetTestDevices()
        {
            var testDevices = new List<Device>();
            testDevices.Add(new Device { DeviceID = 1, PasswordHash = 1 });
            testDevices.Add(new Device { DeviceID = 2, PasswordHash = 2 });
            testDevices.Add(new Device { DeviceID = 3, PasswordHash = 3 });
            testDevices.Add(new Device { DeviceID = 4, PasswordHash = 4 });

            return testDevices;
        }
    }
}
