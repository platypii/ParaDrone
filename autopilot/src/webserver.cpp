#include <SPIFFS.h>
#include <sys/types.h>
#include <WiFi.h>
#include "paradrone.h"

// Set web server port number to 80
WiFiServer server(80);

boolean web_started = false;

// Incoming HTTP request
String request;
boolean firstLine = true;

// Current time
unsigned long currentTime = millis();
// Previous time
unsigned long previousTime = 0;
// Define timeout time in milliseconds (example: 2000ms = 2s)
const long timeoutTime = 2000;

static void send_header(WiFiClient client, int status, const char *contentType);
static void send_landing_page(WiFiClient client);
static void send_file(WiFiClient client, char *filename);
static void delete_file(WiFiClient client, char *filename);
static void parse_location();

void web_init(const char *ssid, const char *password) {
  if (web_started) {
    Serial.println("Web already started");
    return;
  }

  // Connect to Wi-Fi network with SSID and password
  Serial.print("Connecting to ");
  Serial.print(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  String localIp = WiFi.localIP().toString();
  Serial.println(localIp);

  // Start SPIFFS file system
  if (!SPIFFS.begin(true)) {
    Serial.println("Error mounting SPIFFS");
  }

  // Start web server
  server.begin();

  // Notify app
  bt_send_url(localIp.c_str());

  web_started = true;
}

void web_loop() {
  if (!web_started) return;

  // Listen for incoming clients
  WiFiClient client = server.available();

  if (client) {
    // New client connected
    currentTime = millis();
    previousTime = currentTime;
    String currentLine = "";
    // loop while client is connected
    while (client.connected() && currentTime - previousTime <= timeoutTime) {
      currentTime = millis();
      if (client.available()) {
        char c = client.read();
        // Serial.write(c); // print full request
        request += c;
        if (c == '\n') {
          if (firstLine) {
            Serial.print(request);
            firstLine = false;
          }
          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // Response content
            if (request.startsWith("GET / ")) {
              send_landing_page(client);
            } else if (request.startsWith("GET /log/")) {
              char *filename = strdup(request.substring(8, 23).c_str());
              send_file(client, filename);
            } else if (request.startsWith("DELETE /log/")) {
              char *filename = strdup(request.substring(11, 26).c_str());
              delete_file(client, filename);
            } else if (request.startsWith("GET /msg")) {
              // Parse location from parameters
              parse_location();
            } else {
              send_header(client, 404, NULL);
            }

            client.println();
            break;
          } else {
            currentLine = ""; // EOL
          }
        } else if (c != '\r') {
          currentLine += c;
        }
      }
    }
    // Clear header variable
    request = "";
    firstLine = true;
    // Close the connection
    client.stop();
  }
}

static void send_header(WiFiClient client, int status, const char *contentType) {
  client.printf("HTTP/1.1 %d OK\n", status);
  if (contentType) {
    client.printf("Content-type: %s\n", contentType);
  }
  client.println("Connection: close");
  client.println();
}

static void send_landing_page(WiFiClient client) {
  send_header(client, 200, "text/html");
  client.println("<!DOCTYPE html><html>");
  client.println("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
  client.println("<style>");
  client.println("html { font-family: Helvetica; display: inline-block; margin: 0px auto; text-align: center; }");
  client.println("ul { list-style-type:none; }");
  client.println("</style>");
  client.println("<link rel=\"icon\" type=\"image/png\" href=\"https://paradr.one/favicon.png\">");
  client.println("<title>ParaDrone Device</title>");
  client.println("</head><body>");
  client.println("<h1>ParaDrone Logs</h1>");
  client.printf("<div>%d / %d kb used</div>\n", SPIFFS.usedBytes() >> 10, SPIFFS.totalBytes() >> 10);

  // List files
  File root = SPIFFS.open("/");
  File file = root.openNextFile();
  client.println("<ul>");
  while (file) {
    client.println("<li>");
    client.printf("<a href=\"/log%s\">%s</a> (%d kb)\n", file.name(), file.name() + 1, file.size() >> 10);
    client.printf("<a href onclick=\"return rm('/log%s')\">[x]</a>\n", file.name());
    client.println("</li>");
    file = root.openNextFile();
  }
  client.println("</ul>");
  root.close();

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
  Serial.printf("Serve %s\n", filename);
  // Serve file
  File file = SPIFFS.open(filename);
  if (file) {
    send_header(client, 200, "text/plain");
    uint8_t buf[512];
    int len;
    while ((len = file.read(buf, 512)) > 0) {
      client.write(buf, len);
    }
  } else {
    send_header(client, 404, NULL);
    client.printf("Failed to open file '%s'\n", filename);
  }
  file.close();
}

static void delete_file(WiFiClient client, char *filename) {
  Serial.printf("Delete %s\n", filename);
  if (SPIFFS.remove(filename)) {
    send_header(client, 200, NULL);
  } else {
    send_header(client, 400, NULL);
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
