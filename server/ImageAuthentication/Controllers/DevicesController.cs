using System;
using System.Data;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Text;
using ImageAuthentication.Models;

namespace ImageAuthentication.Controllers
{
    public class DevicesController : ApiController
    {
        private ImageAuthenticationContext db = new ImageAuthenticationContext();
        private Random random = new Random();

        [Route("api/devices/{deviceID}")]
        [HttpGet]
        public IHttpActionResult GetDeviceSecretInformation(long deviceID)
        {
            if (!DeviceExists(deviceID))
                return NotFound();

            var response = new HttpResponseMessage(HttpStatusCode.Unauthorized);

            var images = db.Images.AsEnumerable();
            var randomImages = images.OrderBy(r => random.Next()).Take(16);
            var base64strings = randomImages.Select(image => image.Base64String);
            var content = base64strings.Aggregate((s1, s2) => s1 + '\n' + s2);
            response.Content = new StringContent(content);

            var header = GetHeader();
            response.Headers.Add("WWW-Authenticate", header);

            return ResponseMessage(response);
        }

        private string GetHeader()
        {
            string realm = "Image Authentication",
                qop = "auth",
                opaque = GetOpaque(),
                nonce = "nonce";
            return $"Digest realm=\"{realm}\",qop=\"{qop}\",nonce=\"{nonce}\",opaque=\"{opaque}\"";
        }

        private string GetOpaque()
        {
            return "opaque";
        }

        [Route("api/devices/{deviceID}/{passwordHashString}")]
        [HttpPost]
        public IHttpActionResult Register(long deviceID, string passwordHashString)
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
            return Created<Device>($"api/devices/{deviceID}", null);
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