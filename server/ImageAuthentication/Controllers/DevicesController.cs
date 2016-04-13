using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Http;
using System.Web.Http.Description;
using ImageAuthentication.Models;
using System.Diagnostics;

namespace ImageAuthentication.Controllers
{
    public class DevicesController : ApiController
    {
        private ImageAuthenticationContext db = new ImageAuthenticationContext();

        // GET: api/Devices
        public IQueryable<Device> GetDevices()
        {
            return db.Devices;
        }

        // GET: api/Devices/5
        [ResponseType(typeof(Device))]
        public async Task<IHttpActionResult> GetDevice(long id)
        {
            Device device = await db.Devices.FindAsync(id);
            if (device == null)
            {
                return NotFound();
            }

            return Ok(device);
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

        // POST: api/Devices
        [ResponseType(typeof(Device))]
        public async Task<IHttpActionResult> PostDevice(Device device)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            db.Devices.Add(device);
            await db.SaveChangesAsync();

            return CreatedAtRoute("DefaultApi", new { id = device.DeviceID }, device);
        }

        // DELETE: api/Devices/5
        [ResponseType(typeof(Device))]
        public async Task<IHttpActionResult> DeleteDevice(long id)
        {
            Device device = await db.Devices.FindAsync(id);
            if (device == null)
            {
                return NotFound();
            }

            db.Devices.Remove(device);
            await db.SaveChangesAsync();

            return Ok(device);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool DeviceExists(long deviceID)
        {
            return db.Devices.Count(e => e.DeviceID == deviceID) > 0;
        }

        public bool CheckExistence(long deviceID)
        {
            return DeviceExists(deviceID);
        }

        [Route("/api/devices/{deviceID}/{passwordHashString}")]
        [HttpGet]
        public bool VerifyPassword(long deviceID, string passwordHashString)
        {
            if (passwordHashString.Length != 32)
                throw new FormatException("Hash value is in incorrect format.");

            var passwordHash = Enumerable.Range(0, passwordHashString.Length)
                     .Where(x => x % 2 == 0)
                     .Select(x => Convert.ToByte(passwordHashString.Substring(x, 2), 16))
                     .ToArray();

            var hits = from device in db.Devices
                       where device.DeviceID == deviceID
                       select device;

            bool deviceExists = hits.Count() != 0;
            if (!deviceExists)
                return false;

            if (hits.Count() != 1)
                throw new DataException("Device ID must be unique.");

            var correctHash = hits.First().PasswordHash;

            for (int i = 0; i < 32; i++)
                if (correctHash[i] != passwordHash[i])
                    return false;

            return true;
        }
    }
}