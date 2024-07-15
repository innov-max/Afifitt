#include <Wire.h>
#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include "Adafruit_GFX.h"
#include "OakOLED.h"
#include "MAX30100_PulseOximeter.h"

#define REPORTING_PERIOD_MS 1000
#define FIREBASE_REPORTING_PERIOD_MS 5000 // Report to Firebase every 5 seconds

OakOLED oled;

char ssid[] = "strathmore";                  // Your WiFi credentials
char pass[] = "5trathm0re";

PulseOximeter pox;
float BPM = 0;
float SpO2 = 0;
uint32_t tsLastReport = 0;
uint32_t tsLastFirebaseReport = 0;

FirebaseData firebaseData; // Global FirebaseData object

#define FIREBASE_HOST "afifit-4328c-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "AIzaSyCo-Adyp0GX2-FBXP_zWa2lpOLyGaUOdx0"

const unsigned char bitmap[] PROGMEM =
{
    // Bitmap data here
};

void onBeatDetected()
{
    Serial.println("Beat Detected!");
    oled.drawBitmap(60, 20, bitmap, 28, 28, 1);
    oled.display();
}

void setup()
{
    Serial.begin(115200);
    oled.begin();
    oled.clearDisplay();
    oled.setTextSize(1);
    oled.setTextColor(1);
    oled.setCursor(0, 0);

    oled.println("Initializing pulse oximeter..");
    oled.display();

    pinMode(16, OUTPUT);

    Serial.print("Initializing Pulse Oximeter..");

    if (!pox.begin())
    {
        Serial.println("FAILED");
        oled.clearDisplay();
        oled.setTextSize(1);
        oled.setTextColor(1);
        oled.setCursor(0, 0);
        oled.println("FAILED");
        oled.display();
        for (;;);
    }
    else
    {
        oled.clearDisplay();
        oled.setTextSize(1);
        oled.setTextColor(1);
        oled.setCursor(0, 0);
        oled.println("SUCCESS");
        oled.display();
        Serial.println("SUCCESS");
        pox.setOnBeatDetectedCallback(onBeatDetected);
    }

    // Initialize WiFi
    WiFi.begin(ssid, pass);
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(1000);
        Serial.println("Connecting to WiFi...");
    }
    Serial.println("Connected to WiFi");

    // Initialize Firebase
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
    Firebase.reconnectWiFi(true); // Ensure WiFi reconnects on disconnection

    // Set the default current for the IR LED (optional)
    pox.setIRLedCurrent(MAX30100_LED_CURR_7_6MA);
}

void loop()
{
    pox.update();

    BPM = pox.getHeartRate();
    SpO2 = pox.getSpO2();

    // Debugging to check if the sensor values are being read
    Serial.print("Debug - Heart rate: ");
    Serial.print(BPM);
    Serial.print(" bpm / SpO2: ");
    Serial.println(SpO2);

    if (millis() - tsLastReport > REPORTING_PERIOD_MS)
    {
        tsLastReport = millis(); // Reset report timer

        Serial.print("Heart rate:");
        Serial.print(BPM);
        Serial.print(" bpm / SpO2:");
        Serial.print(SpO2);
        Serial.println(" %");

        oled.clearDisplay();
        oled.setTextSize(1);
        oled.setTextColor(1);
        oled.setCursor(0, 16);
        oled.println(BPM);

        oled.setTextSize(1);
        oled.setTextColor(1);
        oled.setCursor(0, 0);
        oled.println("Heart BPM");

        oled.setTextSize(1);
        oled.setTextColor(1);
        oled.setCursor(0, 30);
        oled.println("SpO2");

        oled.setTextSize(1);
        oled.setTextColor(1);
        oled.setCursor(0, 45);
        oled.println(SpO2);
        oled.display();
    }

    // If it's time to report to Firebase
    if (millis() - tsLastFirebaseReport > FIREBASE_REPORTING_PERIOD_MS)
    {
        tsLastFirebaseReport = millis(); // Reset Firebase report timer

        // Log data to Firebase
        if (Firebase.pushFloat(firebaseData, "/bpm", BPM))
        {
            Serial.println("BPM logged successfully");
        }
        else
        {
            Serial.print("Failed to log BPM: ");
            Serial.println(firebaseData.errorReason());
        }

        if (Firebase.pushFloat(firebaseData, "/spo2", SpO2))
        {
            Serial.println("SpO2 logged successfully");
        }
        else
        {
            Serial.print("Failed to log SpO2: ");
            Serial.println(firebaseData.errorReason());
        }
    }

    // Avoid blocking operations
    delay(1000); // Short delay to prevent WDT reset
}
