// Audio Spectrum Display
// Copyright 2013 Tony DiCola (tony@tonydicola.com)

// This code was part of the guide at http://learn.adafruit.com/fft-fun-with-fourier-transforms/

// Aug 2014 DWJ
// * access two input audio channels
// * switch to pedvide/ADC library for teensy 3.1.  https://github.com/pedvide/ADC
// * remove the NEO pixel display support

#define ARM_MATH_CM4
#include <arm_math.h>

#include <ADC.h>

////////////////////////////////////////////////////////////////////////////////
// CONFIGURATION 
////////////////////////////////////////////////////////////////////////////////

#define AUDIO_CHANNEL_COUNT 2          // number of microphones connected
#define ADC_COUNT 2                    // number of available concurrent ADCs
#define ANALOG_READ_RESOLUTION 12      // Bits of resolution for the ADC.
#define ANALOG_READ_AVERAGING 16       // Number of samples to average with each ADC reading.
#define SAMPLING_SPEED ADC_MED_SPEED
#define CONVERSION_SPEED ADC_MED_SPEED

const int SAMPLE_RATE_HZ = 9000;
const int FFT_SIZE = 256;              // Size of the FFT.
const int AUDIO_INPUT_PIN[AUDIO_CHANNEL_COUNT] = {16,17};        // Input ADC pins for audio data.
const int MAX_COMMAND_CHARS = 65;      // Max number of characters in a single command

////////////////////////////////////////////////////////////////////////////////
// INTERNAL STATE
// These shouldn't be modified unless you know what you're doing.
////////////////////////////////////////////////////////////////////////////////

IntervalTimer samplingTimer;
float samples[AUDIO_CHANNEL_COUNT][FFT_SIZE*2];
float magnitudes[AUDIO_CHANNEL_COUNT][FFT_SIZE];
int sampleCounter = 0;
char commandBuffer[MAX_COMMAND_CHARS+1];
int commandLength = 0;

ADC *adc = new ADC(); // adc object
ADC::Sync_result result;

////////////////////////////////////////////////////////////////////////////////
// MAIN SKETCH FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

void setup() {
  
  // Set up serial port.
  
  Serial.begin(38400);

  // Enable output pin for LED
  
  pinMode(LED_BUILTIN, OUTPUT);

  // Enable input pins for audio
  
  for (int idx=0; idx<AUDIO_CHANNEL_COUNT; idx++) {
    pinMode(AUDIO_INPUT_PIN[idx], INPUT);
  }

  // Set up ADCs
  
  for (int idx=0; idx<ADC_COUNT; idx++) {
    adc->setAveraging(ANALOG_READ_AVERAGING,idx);    // set number of averages
    adc->setResolution(ANALOG_READ_RESOLUTION,idx);  // set bits of resolution
    adc->setConversionSpeed(CONVERSION_SPEED,idx);   // change the conversion speed
    adc->setSamplingSpeed(SAMPLING_SPEED,idx);       // change the sampling speed
  }

  // Turn on the LED.
  
  digitalWrite(LED_BUILTIN, HIGH);

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
    for (int idx=0; idx<AUDIO_CHANNEL_COUNT; idx++) {
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
  result = adc->analogSynchronizedRead(AUDIO_INPUT_PIN[0], AUDIO_INPUT_PIN[1]);
  samples[0][sampleCounter] = result.result_adc0;
  samples[0][sampleCounter+1] = 0.0;

  samples[1][sampleCounter] = result.result_adc1;
  samples[1][sampleCounter+1] = 0.0;

  // Update sample buffer position and stop after the buffer is filled
  sampleCounter += 2;
  if (samplingIsDone()) {
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
    for (int j=0; j<AUDIO_CHANNEL_COUNT; j++) {
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
  else if (strcmp(command, "GET AUDIO_CHANNEL_COUNT") == 0) {
    Serial.println(AUDIO_CHANNEL_COUNT);
  } 
  else if (strcmp(command, "GET ADC_COUNT") == 0) {
    Serial.println(ADC_COUNT);
  } 
  else {
    Serial.println();
  }

}

