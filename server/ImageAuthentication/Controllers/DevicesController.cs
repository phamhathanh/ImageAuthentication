using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using ImageAuthentication.Models;

namespace ImageAuthentication.Controllers
{
    public class DevicesController : ApiController
    {
        private const int NONCE_DURATION = 3;   // Hours.
        private const string REALM = "My Realm",
                                QOP = "auth";

        private IImageAuthenticationContext db = new ImageAuthenticationContext();

        public DevicesController(){ }

        public DevicesController(IImageAuthenticationContext context)
        {
            db = context;
        }

        private Random random = new Random();

        private class NonceInfo
        {
            public DateTime ExpiryTime { get; } = DateTime.Now.AddHours(NONCE_DURATION);
            public int NC { get; set; }
        }
        private static Dictionary<string, NonceInfo> nonces = new Dictionary<string, NonceInfo>();
        // Looks hacky.

        [HttpGet]
        [Route("api/images")]
        public IHttpActionResult GetImages()
        {
            var images = db.Images.AsEnumerable();
            var randomImages = images.OrderBy(r => random.Next()).ToArray();
            if (randomImages.Length != 30)
                throw new DataException("Wrong number of images in database.");
            var base64strings = randomImages.Select(image => image.Base64String);
            var content = base64strings.Aggregate((s1, s2) => s1 + '\n' + s2);

            var response = new HttpResponseMessage()
            {
                StatusCode = HttpStatusCode.OK,
                Content = new StringContent(content)
            };

            return ResponseMessage(response);
        }

        [HttpGet]
        [Route("api/devices/{deviceID}")]
        public IHttpActionResult GetDeviceResource(long deviceID)
        {
            bool deviceExists = db.Devices.Count(e => e.DeviceID == deviceID) > 0;
            if (!deviceExists)
                return NotFound();

            bool authorizationProvided = Request.Headers.Contains("Authorization");
            if (!authorizationProvided)
                return Unauthorized();
            
            return Verify(deviceID);
        }

        private IHttpActionResult Unauthorized()
        {
            var response = new HttpResponseMessage(HttpStatusCode.Unauthorized);

            string nonce = GenerateNonce();

            var header = $"iAuth realm=\"{REALM}\",qop=\"{QOP}\",nonce=\"{nonce}\"";
            response.Headers.Add("WWW-Authenticate", header);

            return ResponseMessage(response);
        }

        private string GenerateNonce()
        {
            if (nonces.Count > 100)
                CleanUp();

            string nonce = Guid.NewGuid().ToString("N");
            /* A GUID is a 128-bit integer (16 bytes) that can be used across all computers and networks wherever
             * a unique identifier is required. Such an identifier has a very low probability of being duplicated.
             */

            nonces.Add(nonce, new NonceInfo());
            return nonce;
        }

        private void CleanUp()
        {
            var allNonces = nonces.Keys.Select(nonce => (string)nonce.Clone()).ToArray();
            foreach (var nonce in allNonces)
            {
                Debug.Assert(nonces.ContainsKey(nonce));
                var info = nonces[nonce];
                if (info.ExpiryTime < DateTime.Now)
                    nonces.Remove(nonce);
            }
        }

        private IHttpActionResult Verify(long deviceID)
        {
            if (!IsAuthorized(deviceID))
                return Unauthorized();
            return Ok();
        }

        private bool IsAuthorized(long deviceID)
        {
            var device = GetDevice(deviceID);
            var authHeader = GetAuthenticationHeader();

            AuthInfo authInfo;
            try
            {
                authInfo = AuthInfo.Parse(authHeader);
            }
            catch (FormatException)
            {
                throw new HttpResponseException(HttpStatusCode.BadRequest);
            }

            var nonce = authInfo.Nonce;
            if (!nonces.ContainsKey(nonce))
                return false;
            var nonceInfo = nonces[nonce];
            if (nonceInfo.ExpiryTime < DateTime.Now)
                return false;

            var nc = authInfo.NC;
            if (nc <= nonceInfo.NC)
                return false;
            nonceInfo.NC = nc;

            var passwordHash = device.PasswordHash.ToHexString();

            var method = Request.Method.ToString();
            var uri = authInfo.URI;

            if ("/" + uri != Request.RequestUri.LocalPath)
                return false;

            var cnonce = authInfo.CNonce;
            var qop = authInfo.QOP;

            return authInfo.Response == Hasher.ComputeCorrectResponse(method, uri, nonce, qop, nc, cnonce, passwordHash);
        }

        private string GetAuthenticationHeader()
        {
            var hits = Request.Headers.GetValues("Authorization");
            if (hits.Count() != 1)
                throw new HttpResponseException(HttpStatusCode.BadRequest);
            return hits.First();
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

        [HttpPut]
        [Route("api/devices/{deviceID}")]
        public IHttpActionResult SetPassword(long deviceID)
        {
            bool authorizationProvided = Request.Headers.Contains("Authorization");
            if (!authorizationProvided)
                return Register(deviceID);
            else
                return ChangePassword(deviceID);
        }

        private IHttpActionResult Register(long deviceID)
        {
            bool deviceExists = db.Devices.Count(e => e.DeviceID == deviceID) > 0;
            if (deviceExists)
                throw new HttpResponseException(HttpStatusCode.Forbidden);

            var passwordHash = GetPasswordHash(deviceID);
            var newDevice = new Device()
            {
                DeviceID = deviceID,
                PasswordHash = passwordHash
            };

            db.Devices.Add(newDevice);
            db.SaveChanges();
            return Created<Device>($"api/devices/{deviceID}", null);
        }

        private byte[] GetPasswordHash(long deviceID)
        {
            var password = Request.Content.ReadAsStringAsync().Result;
            var passwordHash = Hasher.ComputeHash($"{deviceID}:{REALM}:{password}");
            Debug.Assert(passwordHash.ToHexString() == "d707f47716e28275daeed55a90f201fa5665213d9b21cf09980623200c12b246", password);
            return passwordHash;
        }
        
        private IHttpActionResult ChangePassword(long deviceID)
        {
            if (!IsAuthorized(deviceID))
                return Unauthorized();

            var device = GetDevice(deviceID);
            device.PasswordHash = GetPasswordHash(deviceID);
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