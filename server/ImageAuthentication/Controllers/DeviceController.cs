using ImageAuthentication.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Http;

namespace ImageAuthentication.Controllers
{
    public class DeviceController : ApiController
    {
        List<Device> devices = new List<Device>();

        public DeviceController() { }

        public DeviceController(List<Device> devices)
        {
            this.devices = devices;
        }

        public IEnumerable<Device> GetAllDevices()
        {
            return devices;
        }

        public async Task<IEnumerable<Device>> GetAllDevicesAsync()
        {
            return await Task.FromResult(GetAllDevices());
        }

        public IHttpActionResult GetDevice(int deviceID)
        {
            var device = devices.FirstOrDefault((d) => d.DeviceID == deviceID);
            if (device == null)
                return NotFound();
            return Ok(device);
        }

        public async Task<IHttpActionResult> GetDeviceAsync(int id)
        {
            return await Task.FromResult(GetDevice(id));
        }
    }
}
