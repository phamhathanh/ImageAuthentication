using System;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using System.Web.Http;
using System.Web.Http.Description;
using ImageAuthentication.Models;
using System.Net.Http;

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

        private bool DeviceExists(long deviceID)
        {
            return db.Devices.Count(e => e.DeviceID == deviceID) > 0;
        }

        [Route("api/devices/{deviceID}/{passwordHashString}")]
        [HttpGet]
        public bool VerifyPassword(long deviceID, string passwordHashString)
        {
            var passwordHash = ValidateAndConvert(passwordHashString);

            var hits = from device in db.Devices
                       where device.DeviceID == deviceID
                       select device;

            bool deviceExists = hits.Count() != 0;
            if (!deviceExists)
                throw new HttpResponseException(HttpStatusCode.NotFound);

            if (hits.Count() != 1)
                throw new DataException("Device ID must be unique. Something bad happened.");

            var correctHash = hits.First().PasswordHash;

            for (int i = 0; i < 32; i++)
                if (correctHash[i] != passwordHash[i])
                    return false;

            return true;
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

        [Route("api/devices/{deviceID}/{oldPassword}/{newPassword}")]
        [HttpPut]
        public IHttpActionResult SetPassword(long deviceID, string passwordHashString)
        {
            if (passwordHashString.Length != 64)
                throw new HttpResponseException(HttpStatusCode.BadRequest);

            var passwordHash = Enumerable.Range(0, passwordHashString.Length)
                     .Where(x => x % 2 == 0)
                     .Select(x => Convert.ToByte(passwordHashString.Substring(x, 2), 16))
                     .ToArray();

            var hits = from device in db.Devices
                       where device.DeviceID == deviceID
                       select device;

            if (hits.Count() > 1)
                throw new DataException("Device ID must be unique. Something bad happened.");

            throw new NotImplementedException();

            bool deviceExists = hits.Count() != 0;
            if (!deviceExists)
            {

            }
            else
            {

            }
        }

        // PUT: api/Devices/5
        [ResponseType(typeof(void))]
        public async Task<IHttpActionResult> PutDevice(long id, Device device)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            if (id != device.DeviceID)
            {
                return BadRequest();
            }

            db.Entry(device).State = EntityState.Modified;

            try
            {
                await db.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!DeviceExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return StatusCode(HttpStatusCode.NoContent);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}