#include <Wire.h>
#include <MAX30105.h>

MAX30105 particleSensor;

void setup() {
  Serial.begin(115200);
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
    Serial.println("MAX30105 was not found. Please check wiring/power.");
    while (1);
  }
  particleSensor.setup();
  particleSensor.setPulseAmplitudeRed(0x0A); // Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0);  // Turn off Green LED
}

void loop() {
  long irValue = particleSensor.getIR();
  Serial.print("IR Value: ");
  Serial.println(irValue);
  delay(1000);
}

