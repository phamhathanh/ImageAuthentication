using System;
using System.Data;
using System.Linq;
using System.Net;
using System.Web.Http;
using ImageAuthentication.Models;

namespace ImageAuthentication.Controllers
{
    public class DevicesController : ApiController
    {
        private ImageAuthenticationContext db = new ImageAuthenticationContext();

        [Route("api/devices/{deviceID}")]
        [HttpGet]
        public bool CheckExistence(long deviceID)
        {
            return DeviceExists(deviceID);
        }

        [Route("api/devices/{deviceID}/{passwordHashString}")]
        [HttpPost]
        public IHttpActionResult RegisterPassword(long deviceID, string passwordHashString)
        {
            var passwordHash = ValidateAndConvert(passwordHashString);

            bool deviceExists = DeviceExists(deviceID);
            if (deviceExists)
                throw new HttpResponseException(HttpStatusCode.Forbidden);

            var newDevice = new Device()
            {
                DeviceID = deviceID,
                PasswordHash = passwordHash
            };

            db.Devices.Add(newDevice);
            db.SaveChanges();
            return Created<Device>("", null);
            // Not sure how to return 201.
        }

        private bool DeviceExists(long deviceID)
        {
            return db.Devices.Count(e => e.DeviceID == deviceID) > 0;
        }

        [Route("api/devices/{deviceID}/{passwordHashString}")]
        [HttpGet]
        public bool VerifyPassword(long deviceID, string passwordHashString)
        {
            var passwordHash = ValidateAndConvert(passwordHashString);
            var device = GetDevice(deviceID);
            return PasswordIsCorrect(device, passwordHash);
        }

        private byte[] ValidateAndConvert(string hashString)
        {
            if (hashString.Length != 64)
                throw new HttpResponseException(HttpStatusCode.BadRequest);

            return Enumerable.Range(0, hashString.Length)
                     .Where(x => x % 2 == 0)
                     .Select(x => Convert.ToByte(hashString.Substring(x, 2), 16))
                     .ToArray();
        }

        private Device GetDevice(long deviceID)
        {
            var hits = from device in db.Devices
                       where device.DeviceID == deviceID
                       select device;

            bool deviceExists = hits.Count() != 0;
            if (!deviceExists)
                throw new HttpResponseException(HttpStatusCode.NotFound);

            if (hits.Count() != 1)
                throw new DataException("Device ID must be unique. Something bad happened.");

            return hits.First();
        }

        private bool PasswordIsCorrect(Device device, byte[] passwordHash)
        {
            var correctHash = device.PasswordHash;

            for (int i = 0; i < 32; i++)
                if (correctHash[i] != passwordHash[i])
                    return false;

            return true;
        }

        [Route("api/devices/{deviceID}/{oldPassword}/{newPassword}")]
        [HttpPut]
        public IHttpActionResult SetPassword(long deviceID, string oldPassword, string newPassword)
        {
            var oldHash = ValidateAndConvert(oldPassword);
            var newHash = ValidateAndConvert(newPassword);
            var device = GetDevice(deviceID);

            bool oldPasswordIsCorrect = PasswordIsCorrect(device, oldHash);
            if (!oldPasswordIsCorrect)
                throw new HttpResponseException(HttpStatusCode.Unauthorized);

            device.PasswordHash = newHash;
            db.SaveChanges();
            return Ok();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
                db.Dispose();
            base.Dispose(disposing);
        }
    }
}