#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>


const char* firebaseHost = "your-project-name.firebaseio.com";
const char* firebaseSecret = "your-firebase-secret";
const char* ssid = "your-wifi-ssid";
const char* password = "your-wifi-password";

const int flamePin = A0;
const int buzzerPin = D5; 
const int relayPin = D6; 

bool flameDetected = false;
unsigned long startTime = 0;

WiFiClientSecure client;

void setup() {
  Serial.begin(115200);
  pinMode(flamePin, INPUT);
  pinMode(buzzerPin, OUTPUT);
  pinMode(relayPin, OUTPUT);

  connectToWiFi();
}

void loop() {
  flameDetected = detectFlame();
  
  if (flameDetected) {
    activateAlarm();
    sendDataToFirebase();
  } else {
    deactivateAlarm();
  }

  delay(1000); 
}

void connectToWiFi() {
  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting...");
  }
  
  Serial.println("Connected to WiFi");
}

bool detectFlame() {
  int sensorValue = analogRead(flamePin);
 
  return sensorValue > 500;
}

void sendDataToFirebase() {
  Serial.println("Flame detected, sending data to Firebase...");

  if (client.connect(firebaseHost, 443)) {
    client.println("POST /your-firebase-database.json?auth=" + String(firebaseSecret));
    client.println("Content-Type: application/json");
    client.println("Host: your-project-name.firebaseio.com");
    client.println("Connection: close");
    client.print("Content-Length: ");
    client.println(20); // Adjust content length based on your data size
    client.println();
    client.println("{\"flame\": true}"); // Send flame status to Firebase
    client.println();
  } else {
    Serial.println("Failed to connect to Firebase");
  }

  delay(1000); 
}

void activateAlarm() {
  digitalWrite(buzzerPin, HIGH);
  digitalWrite(relayPin, HIGH);
  startTime = millis();
}

void deactivateAlarm() {
  if (millis() - startTime >= 10000) { // 10 seconds
    digitalWrite(buzzerPin, LOW);
    digitalWrite(relayPin, LOW);
  }
}
