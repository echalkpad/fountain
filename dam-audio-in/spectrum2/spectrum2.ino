// Audio Spectrum Display
// Copyright 2013 Tony DiCola (tony@tonydicola.com)

// This code was part of the guide at http://learn.adafruit.com/fft-fun-with-fourier-transforms/

// Aug 2014 DWJ Modified to support two audio channels in, and to remove the NEO pixel display support

#define ARM_MATH_CM4
#include <arm_math.h>

////////////////////////////////////////////////////////////////////////////////
// CONFIGURATION 
////////////////////////////////////////////////////////////////////////////////

int SAMPLE_RATE_HZ = 9000;             // Sample rate of the audio in hertz.
const int FFT_SIZE = 256;              // Size of the FFT.
const int AUDIO_CHANNELS = 2;          // number of microphones connected                                       
const int AUDIO_INPUT_PIN[AUDIO_CHANNELS] = {
  16,17};        // Input ADC pins for audio data.
const int ANALOG_READ_RESOLUTION = 10; // Bits of resolution for the ADC.
const int ANALOG_READ_AVERAGING = 16;  // Number of samples to average with each ADC reading.
const int POWER_LED_PIN = 13;          // Output pin for power LED (pin 13 to use Teensy 3.0's onboard LED).
const int MAX_COMMAND_CHARS = 65;      // Max number of characters in a single command

////////////////////////////////////////////////////////////////////////////////
// INTERNAL STATE
// These shouldn't be modified unless you know what you're doing.
////////////////////////////////////////////////////////////////////////////////

IntervalTimer samplingTimer;
float samples[AUDIO_CHANNELS][FFT_SIZE*2];
float magnitudes[AUDIO_CHANNELS][FFT_SIZE];
int sampleCounter = 0;
char commandBuffer[MAX_COMMAND_CHARS+1];
int commandLength = 0;

////////////////////////////////////////////////////////////////////////////////
// MAIN SKETCH FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

void setup() {
  // Set up serial port.
  Serial.begin(38400);

  // Set up ADC and audio input.
  for (int idx=0; idx<AUDIO_CHANNELS; idx++) {
    pinMode(AUDIO_INPUT_PIN[idx], INPUT);
  }

  analogReadResolution(ANALOG_READ_RESOLUTION);
  analogReadAveraging(ANALOG_READ_AVERAGING);

  // Turn on the power indicator LED.
  pinMode(POWER_LED_PIN, OUTPUT);
  digitalWrite(POWER_LED_PIN, HIGH);

  // Clear the input command buffer
  commandLength = 0;

  // Begin sampling audio
  samplingBegin();
}

void loop() {
  // Calculate FFT if a full sample is available.
  if (samplingIsDone()) {
    // Run FFT on sample data.
    arm_cfft_radix4_instance_f32 fft_inst;
    for (int idx=0; idx<AUDIO_CHANNELS; idx++) {
      arm_cfft_radix4_init_f32(&fft_inst, FFT_SIZE, 0, 1);
      arm_cfft_radix4_f32(&fft_inst, &samples[idx][0]);
      // Calculate magnitude of complex numbers output by the FFT.
      arm_cmplx_mag_f32(&samples[idx][0], &magnitudes[idx][0], FFT_SIZE);
    }
    // Restart audio sampling.
    samplingBegin();
  }

  // Parse any pending commands.
  parserLoop();
}

////////////////////////////////////////////////////////////////////////////////
// SAMPLING FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

void samplingCallback() {
  for (int idx=0; idx<AUDIO_CHANNELS; idx++) {
    // Read from the ADC and store the sample data
    samples[idx][sampleCounter] = (float32_t)analogRead(AUDIO_INPUT_PIN[idx]);
    // Complex FFT functions require a coefficient for the imaginary part of the input.
    // Since we only have real data, set this coefficient to zero.
    samples[idx][sampleCounter+1] = 0.0;
  }

  // Update sample buffer position and stop after the buffer is filled
  sampleCounter += 2;
  if (sampleCounter >= FFT_SIZE*2) {
    samplingTimer.end();
  }
}

void samplingBegin() {
  // Reset sample buffer position and start callback at necessary rate.
  sampleCounter = 0;
  samplingTimer.begin(samplingCallback, 1000000/SAMPLE_RATE_HZ);
}

boolean samplingIsDone() {
  return sampleCounter >= FFT_SIZE*2;
}

////////////////////////////////////////////////////////////////////////////////
// COMMAND PARSING FUNCTIONS
// These functions allow parsing simple commands input on the serial port.
// Commands allow reading and writing variables that control the device.
//
// All commands must end with a semicolon character.
// 
// Example commands are:
// GET SAMPLE_RATE_HZ;
// - Get the sample rate of the device.
// SET SAMPLE_RATE_HZ 400;
// - Set the sample rate of the device to 400 hertz.
// 
////////////////////////////////////////////////////////////////////////////////

void parserLoop() {
  // Process any incoming characters from the serial port
  while (Serial.available() > 0) {
    char c = Serial.read();
    // Add any characters that aren't the end of a command (semicolon) to the input buffer.
    if (c == ';' ) {
      // Parse the command because an end of command token was encountered.
      parseCommand(commandBuffer);
      commandLength = 0;
    } 
    else {
      if (commandLength < MAX_COMMAND_CHARS) {
        c = toupper(c);
        commandBuffer[commandLength++] = c;
      }
    }
  }
}

void parseCommand(char* command) {
  command[commandLength] = '\0';
  if (strcmp(command, "GET MAGNITUDES") == 0) {
    for (int j=0; j<AUDIO_CHANNELS; j++) {
      for (int i = 0; i < FFT_SIZE; i++) {
        Serial.println(magnitudes[0][i]);
      }
    }
  } 
  else if (strcmp(command, "GET SAMPLES") == 0) {
    for (int i = 0; i < FFT_SIZE*2; i+=2) {
      Serial.println(samples[0][i]);
    }
  } 
  else if (strcmp(command, "GET FFT_SIZE") == 0) {
    Serial.println(FFT_SIZE);
  } 
  else if (strcmp(command, "GET SAMPLE_RATE_HZ") == 0) {
    Serial.println(SAMPLE_RATE_HZ);
  } 
  else if (strcmp(command, "GET AUDIO_CHANNELS") == 0) {
    Serial.println(AUDIO_CHANNELS);
  } 
  else {
    Serial.println();
  }
  //  Serial.print("..");
  //  Serial.print(command);
  //  Serial.println("..");

}

