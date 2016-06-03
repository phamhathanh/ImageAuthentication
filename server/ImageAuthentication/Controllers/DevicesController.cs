using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using ImageAuthentication.Models;
using static ImageAuthentication.Models.Utils;

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
        private Dictionary<string, NonceInfo> nonces = new Dictionary<string, NonceInfo>();

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
                if (info.ExpiryTime > DateTime.Now)
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
            var authInfo = GetAuthenticationInfo();

            var nonce = authInfo.Nonce;
            if (!nonces.ContainsKey(nonce))
                return false;
            var nonceInfo = nonces[nonce];
            if (nonceInfo.ExpiryTime > DateTime.Now)
                return false;
            var nc = authInfo.NC;
            if (nc <= nonceInfo.NC)
                return false;
            nonceInfo.NC = nc;

            var realm = authInfo.Realm;
            var password = device.PasswordHash;
            var ha1 = ComputeHashString($"{deviceID}:{realm}:{password}");

            var method = Request.Method.ToString();
            var uri = authInfo.URI;
            
            if (uri != Request.RequestUri.LocalPath)
                return false;
            var ha2 = ComputeHashString($"{method}:{uri}");

            var cnonce = authInfo.CNonce;
            var qop = authInfo.QOP;
            var ha3 = ComputeHashString($"{ha1}:{nonce}:{nc}:{cnonce}:{qop}:{ha2}");

            return authInfo.Response == ha3;
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

        private AuthInfo GetAuthenticationInfo()
        {
            var hits = Request.Headers.GetValues("Authorization");
            bool exists = hits.Count() != 0;
            if (!exists)
                throw new HttpResponseException(HttpStatusCode.NotFound);
            if (hits.Count() != 1)
                throw new HttpResponseException(HttpStatusCode.BadRequest);

            string authHeader = hits.First();
            if (authHeader == null || !authHeader.StartsWith("iAuth"))
                throw new HttpResponseException(HttpStatusCode.BadRequest);

            var paramsString = authHeader.Substring("iAuth ".Length);
            var rawParams = paramsString.Split(',');

            var realm = GetStringParam("realm", rawParams[0]);
            var nonce = GetStringParam("nonce", rawParams[1]);
            var uri = GetStringParam("uri", rawParams[2]);
            var qop = GetStringParam("qop", rawParams[3]);
            var nc = GetUnsignedParam("nc", rawParams[4]);
            var cnonce = GetStringParam("cnonce", rawParams[5]);
            var response = GetStringParam("response", rawParams[6]);

            return new AuthInfo(realm, nonce, uri, qop, nc, cnonce, response);
        }

        private string GetStringParam(string paramName, string rawParam)
        {
            string quoted = ExtractParam(paramName, rawParam);
            return quoted.Substring(1, quoted.Length - 2);
        }

        private string ExtractParam(string paramName, string rawParam)
        {
            if (rawParam == null || !rawParam.StartsWith(paramName))
                throw new HttpResponseException(HttpStatusCode.BadRequest);
            return rawParam.Substring(paramName.Length + 1);
        }

        private int GetUnsignedParam(string paramName, string rawParam)
        {
            string unparsed = ExtractParam(paramName, rawParam);
            int output;
            bool isValid = int.TryParse(unparsed, out output);
            if (!isValid || output <= 0)
                throw new HttpResponseException(HttpStatusCode.BadRequest);
            return output;
        }

        [Route("api/devices/{deviceID}")]
        [HttpPut]
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

            var password = Request.Content.ReadAsStringAsync().Result;
            var passwordHash = ComputeHash($"{deviceID}:{REALM}:{password}");
            Debug.Assert(ComputeHashString($"{deviceID}:{REALM}:{password}") == "d707f47716e28275daeed55a90f201fa5665213d9b21cf09980623200c12b246", password);
            var newDevice = new Device()
            {
                DeviceID = deviceID,
                PasswordHash = passwordHash
            };

            db.Devices.Add(newDevice);
            db.SaveChanges();
            return Created<Device>($"api/devices/{deviceID}", null);
        }

        private byte[] GetPasswordHash()
        {
            var password = Request.Content.ToString();
            return ComputeHash(password);
        }
        
        private IHttpActionResult ChangePassword(long deviceID)
        {
            if (!IsAuthorized(deviceID))
                return Unauthorized();

            var device = GetDevice(deviceID);
            device.PasswordHash = GetPasswordHash();
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

    internal class AuthInfo
    {
        public string Realm { get; }
        public string Nonce { get; }
        public string URI { get; }
        public string QOP { get; }
        public int NC { get; }
        public string CNonce { get; }
        public string Response { get; }

        public AuthInfo(string realm, string nonce, string uri, string qop, int nc, string cnonce, string response)
        {
            Realm = realm;
            Nonce = nonce;
            URI = uri;
            QOP = qop;
            NC = nc;
            CNonce = cnonce;
            Response = response;
        }
    }
}