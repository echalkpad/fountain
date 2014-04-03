# Makefile for the dapMQTT application

PAHO_C_LIB ?= /usr/local/lib
PAHO_C_INC ?= /usr/local/include

all: dapMQTT

CXXFLAGS += -Wall -std=c++0x
#CPPFLAGS += -I.. -I$(PAHO_C_INC)
CPPFLAGS += -I. -I$(PAHO_C_INC)

ifdef DEBUG
  CPPFLAGS += -DDEBUG
  CXXFLAGS += -g -O0
else
  CPPFLAGS += -D_NDEBUG
  CXXFLAGS += -O2
endif

CPPFLAGS += -DDAP_WRITE_CONSOLE=false
CPPFLAGS += -DDAP_WRITE_FILE=false
CPPFLAGS += -DDAP_WRITE_MQTT=true
CPPFLAGS += -DDAP_VERBOSE=true

LDLIBS += -L$(PAHO_C_LIB) -lmqttpp -lpaho-mqtt3a

dapMQTT: dapMQTT.cpp Thermometer.cpp Sensor.cpp Magnetometer.cpp LSM303DLHC.cpp LSM303D.cpp Accelerometer.cpp
	$(CXX) $(CPPFLAGS) $(CXXFLAGS) -o $@ $? $(LDLIBS)

.PHONY: clean
clean:
	rm -f dapMQTT

.PHONY: distclean
distclean: clean


