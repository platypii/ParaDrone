#include <SPIFFS.h>
#include <sys/types.h>
#include <WiFi.h>
#include "paradrone.h"

// Set web server port number to 80
WiFiServer server(80);

bool web_started = false;

// Incoming HTTP request
String request;
bool first_line = true;

// Current time
unsigned long current = millis();
// Previous time
unsigned long previous = 0;
// Define timeout in milliseconds (example: 2000ms = 2s)
const long timeout = 2000;

static void send_header(WiFiClient client, int status, const char *content_type = NULL, bool cors = false);
static void send_landing_page(WiFiClient client);
static void send_file(WiFiClient client, char *filename);
static void delete_file(WiFiClient client, char *filename);
static void parse_location();
static void parse_lz();

/**
 * Connect to wifi, and start a web server
 */
void web_init(const char *ssid, const char *password) {
  if (web_started) {
    Serial.printf("%.1fs web already started\n", millis() * 1e-3);

    // Notify app
    String local_ip = WiFi.localIP().toString();
    bt_send_url(local_ip.c_str());

    return;
  }

  // Stop GPS
  Serial2.end();

  // Connect to Wi-Fi network with SSID and password
  Serial.printf("%.1fs connecting to ", millis() * 1e-3);
  Serial.print(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print('.');
  }
  String local_ip = WiFi.localIP().toString();
  Serial.println(local_ip);

  // Start SPIFFS file system
  if (!SPIFFS.begin(true)) {
    Serial.printf("%.1fs error mounting spiffs\n", millis() * 1e-3);
  }

  // Start web server
  server.begin();

  // Notify app
  bt_send_url(local_ip.c_str());

  web_started = true;
}

void web_loop() {
  if (!web_started) return;

  // Listen for incoming clients
  WiFiClient client = server.available();

  if (client) {
    // New client connected
    current = millis();
    previous = current;
    String line = "";
    // loop while client is connected
    while (client.connected() && current - previous <= timeout) {
      current = millis();
      if (client.available()) {
        char c = client.read();
        // Serial.write(c); // print full request
        request += c;
        if (c == '\n') {
          if (first_line) {
            Serial.print(request);
            first_line = false;
          }
          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (line.length() == 0) {
            // Response content
            if (request.startsWith("GET / ")) {
              send_landing_page(client);
            } else if (request.startsWith("GET /log/")) {
              const int space = request.indexOf(' ', 8);
              char *filename = strdup(request.substring(8, space).c_str());
              send_file(client, filename);
            } else if (request.startsWith("DELETE /log/")) {
              const int space = request.indexOf(' ', 11);
              char *filename = strdup(request.substring(11, space).c_str());
              delete_file(client, filename);
            } else if (request.startsWith("GET /msg")) {
              // Parse location from parameters
              parse_location();
              send_header(client, 200, NULL, true);
            } else if (request.startsWith("GET /lz")) {
              // Parse lz from parameters
              parse_lz();
              send_header(client, 200, NULL, true);
            } else {
              send_header(client, 404);
            }

            client.println();
            break;
          } else {
            line = ""; // EOL
          }
        } else if (c != '\r') {
          line += c;
        }
      }
    }
    // Clear header variable
    request = "";
    first_line = true;
    // Close the connection
    client.stop();
  }
}

/**
 * Send http header to a client
 * @param client the http client
 * @param status http status code
 * @param content_type mime content type (eg- text/html)
 * @param cors allow cross origin requests (for browser automation)
 */
static void send_header(WiFiClient client, int status, const char *content_type, bool cors) {
  client.printf("HTTP/1.1 %d OK\n", status);
  if (content_type) {
    client.printf("Content-type: %s\n", content_type);
  }
  if (cors) {
    client.printf("Access-Control-Allow-Origin: *\n");
  }
  client.println("Connection: close");
  client.println();
}

/**
 * Send landing page as an http response
 * @param client the http client
 */
static void send_landing_page(WiFiClient client) {
  const uint64_t used = SPIFFS.usedBytes();
  send_header(client, 200, "text/html");
  client.println("<!DOCTYPE html><html>");
  client.println("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
  client.println("<style>");
  client.println("html { font-family: Helvetica; }");
  client.println("h1 { text-align: center; }");
  client.println("a { text-decoration: none; }");
  client.println("a:first-child { margin-right: 8px; }");
  client.println("ul { list-style-type: none; padding: 0; }");
  client.println("li { display: flex; margin-bottom: 4px; }");
  client.println(".dots { flex: 1; border-bottom: 1px dotted #999; }");
  client.println("</style>");
  client.println("<link rel=\"icon\" type=\"image/png\" href=\"https://paradr.one/favicon.png\">");
  client.println("<title>ParaDrone Device</title>");
  client.println("</head><body>");
  client.println("<h1>ParaDrone Logs</h1>");

  // List files
  File root = SPIFFS.open("/");
  File file = root.openNextFile();
  client.println("<ul>");
  if (!file) {
    client.println("<li><em>no logs</em></li>");
  }
  while (file) {
    client.println("<li>");
    client.printf("<a href=\"/log%s\">%s</a>", file.path(), file.path() + 1);
    if (file.size() < 1024) {
      client.printf(" %d b\n", file.size());
    } else {
      client.printf(" %d kb\n", file.size() >> 10);
    }
    client.println("<span class=dots></span>");
    client.printf("<a href onclick=\"return rm('/log%s')\">[X]</a>\n", file.path());
    client.println("</li>");
    file = root.openNextFile();
  }
  client.println("</ul>");
  root.close();
  client.printf("<div>Total %ld bytes</div>\n", used);

  client.println("<script>");
  client.println("function rm(url){");
  client.println("const xhr = new XMLHttpRequest()");
  client.println("xhr.open('DELETE', url)");
  client.println("xhr.onload = () => {if(xhr.readyState == 4 && xhr.status == 200) location.reload()}");
  client.println("xhr.send()");
  client.println("return false}");
  client.println("</script></body></html>");
}

static void send_file(WiFiClient client, char *filename) {
  Serial.printf("%.1fs serve %s\n", millis() * 1e-3, filename);
  File file = SPIFFS.open(filename);
  if (file) {
    send_header(client, 200, "text/plain");
    uint8_t buf[512];
    int len;
    while ((len = file.read(buf, 512)) > 0) {
      client.write(buf, len);
    }
  } else {
    send_header(client, 404);
    client.printf("Failed to open file '%s'\n", filename);
  }
  file.close();
}

static void delete_file(WiFiClient client, char *filename) {
  Serial.printf("%.1fs delete file '%s'\n", millis() * 1e-3, filename);
  if (SPIFFS.remove(filename)) {
    send_header(client, 200);
  } else {
    send_header(client, 400);
    client.printf("Failed to delete file '%s'\n", filename);
  }
}

/**
 * Parse double from url parameter
 */
static double get_param(String params, String key) {
  const int index = params.indexOf(key) + key.length() + 1;
  return atof(params.substring(index).c_str());
}

/**
 * Parse location info from request parameters
 */
static void parse_location() {
  const int url_start = request.indexOf(' ', 0) + 6;
  const int url_end = request.indexOf(' ', url_start);
  String params = request.substring(url_start, url_end);
  const double lat = get_param(params, "lat");
  const double lng = get_param(params, "lng");
  const double alt = get_param(params, "alt");
  const double vN = get_param(params, "vN");
  const double vE = get_param(params, "vE");
  const double climb = get_param(params, "climb");
  // Update location
  GeoPointV *loc = new GeoPointV {
    .millis = millis(),
    .lat = lat,
    .lng = lng,
    .alt = alt,
    .vN = vN,
    .vE = vE,
    .climb = climb
  };
  update_location(loc);
}

/**
 * Parse landing zone from request parameters
 */
static void parse_lz() {
  const int url_start = request.indexOf(' ', 0) + 5;
  const int url_end = request.indexOf(' ', url_start);
  String params = request.substring(url_start, url_end);
  const double lat = get_param(params, "lat");
  const double lng = get_param(params, "lng");
  const double alt = get_param(params, "alt");
  const double dir = get_param(params, "dir");
  // Set lz
  LandingZoneMessage *lz = new LandingZoneMessage {
    .msg_type = 'Z',
    .lat = (int) (lat * 1e6), // microdegrees
    .lng = (int) (lng * 1e6), // microdegrees
    .alt = (short) (alt * 10), // decimeters
    .landing_direction = (short) (dir * 1e3) // milliradians
  };
  set_landing_zone(lz);
}
