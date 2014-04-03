/*
 ============================================================================
 Name        : dap.c
 Author      : Doug Johnson
 Version     :
 Copyright   : GPL
 Description : I2C Data Acquisition
 ============================================================================
 */

#include <cstdlib>
#include <cstdio>

#include "LSM303DLHC.h"
#include "LSM303D.h"

#include <iostream>
#include <array>
#include <string>
#include <cstring>
#include <thread>	// For sleep#include <chrono>#include "mqtt/async_client.h"

inline void sleep(int ms) {
	std::this_thread::sleep_for(std::chrono::milliseconds(ms));
}

/////////////////////////////////////////////////////////////////////////////

/**
 * A callback class for use with the main MQTT client.
 */
class callback: public virtual mqtt::callback {
public:
	/**
	 * Virtual destructor
	 */
	virtual ~callback() {
	}

	virtual void connection_lost(const std::string& cause) {
		std::cout << "\nConnection lost" << std::endl;
		if (!cause.empty())
			std::cout << "\tcause: " << cause << std::endl;
	}

	// We're not subscribed to anything, so this should never be called.
	virtual void message_arrived(const std::string& topic,
			mqtt::message_ptr msg) {
	}

	virtual void delivery_complete(mqtt::idelivery_token_ptr tok) {
#ifdef DAP_VERBOSE
		std::cout << "Delivery complete for token: "
				<< (tok ? tok->get_message_id() : -1) << std::endl;
#endif
		}
};

/////////////////////////////////////////////////////////////////////////////

/**
 * A base action listener.
 */
class action_listener: public virtual mqtt::iaction_listener {
protected:
	virtual void on_failure(const mqtt::itoken& tok) {
		std::cout << "\n\tListener: Failure on token: " << tok.get_message_id()
				<< std::endl;
	}

	virtual void on_success(const mqtt::itoken& tok) {
#ifdef DAP_VERBOSE
		std::cout << "\n\tListener: Success on token: " << tok.get_message_id()
				<< std::endl;
#endif
	}
};

/////////////////////////////////////////////////////////////////////////////

/**
 * A derived action listener for publish events.
 */
class delivery_action_listener: public action_listener {
	bool done_;

	virtual void on_failure(const mqtt::itoken& tok) {
		action_listener::on_failure(tok);
		done_ = true;
	}

	virtual void on_success(const mqtt::itoken& tok) {
		action_listener::on_success(tok);
		done_ = true;
	}

public:
	delivery_action_listener() :
			done_(false) {
	}
	bool is_done() const {
		return done_;
	}
};

// --------------------------------------------------------------------------

int main(int argc, char* argv[]) {
	printf("DAP - I2C Data Acquisition Program\n");
	printf("Stream acquired data to various sinks.\n");

	std::array<std::string, 2> ft = { { "false", "true"} };

	const std::string ADDRESS("tcp://spark:1883");
	const std::string CLIENTID("dapMQTT");
	const std::string TOPIC_PREFIX("/ws/guid/raw");

	const int QOS = 0;
	const long TIMEOUT = 10000L;
	const int MESSAGE_BUFFER_SIZE = 1000;

	// Configuration

	bool isVerbose = false;
	bool isWriteFile = false;
	bool isWriteConsole = false;
	bool isWriteMQTT = true;

#ifdef DAP_VERBOSE
	isVerbose = true;
#endif

#ifdef DAP_WRITE_FILE
	isWriteFile = true;
#endif

#ifdef DAP_WRITE_CONSOLE
	isWriteConsole = true;
#endif

#ifdef DAP_WRITE_MQTT
	isWriteMQTT = true;
#endif

	printf("Configuration:\n");
	printf("  Write file    %s\n", ft[isWriteFile].c_str());
	printf("  Write console %s\n", ft[isWriteConsole].c_str());
	printf("  Write MQTT    %s", ft[isWriteMQTT].c_str());
	if (isWriteMQTT) {
		printf(" broker: %s, topic: %s, client ID: %s\n",ADDRESS.c_str(),TOPIC_PREFIX.c_str(),CLIENTID.c_str());
	} else {
		printf("\n");
	}
	printf("  Verbose       %s\n", ft[isVerbose].c_str());

//  Prepare to publish using MQTT

	callback cb;
	mqtt::async_client client(ADDRESS, CLIENTID);
	client.set_callback(cb);
	mqtt::itoken_ptr conntok;

	if (isWriteMQTT) {
		try {
			conntok = client.connect();
			std::cout << "Connecting to " << ADDRESS << " as " << CLIENTID
					<< " ... " << std::flush;
			conntok->wait_for_completion();
			std::cout << "OK" << std::endl;
		} catch (const mqtt::exception& exc) {
			std::cerr << std::endl << "Error (MQTT connect): " << exc.what()
					<< std::endl;
			return 1;
		}
	}

//  Prepare to write to disk storage

	FILE *datafile;
	if (isWriteFile) {
		char fileName[] = "test.csv";
		printf("Will write data samples to %s in CSV format.\n\n", fileName);

		datafile = fopen(fileName, "w");
		fprintf(datafile, "device,sample,X,Y,Z\n");
	}

//  Create sensor interface objects

	LSM303DLHC *d1 = new LSM303DLHC(1, 0x19, "LSM303DLHC");
	printf(
			"Device initialization complete: %s at Bus %i, Address %#04x. Handle: %i\n",
			d1->getDeviceName(), d1->getBus(), d1->getDeviceAddress(),
			d1->getHandle());

	LSM303D *d2 = new LSM303D(1, 0x1D, "LSM303D");
	printf(
			"Device initialization complete: %s at Bus %i, Address %#04x. Handle: %i\n",
			d2->getDeviceName(), d2->getBus(), d2->getDeviceAddress(),
			d2->getHandle());

// Acquire and process data

	for (int idx = 0; idx < 50; idx++) {

		// Acquire

		d1->refreshSensorData();
		d2->refreshSensorData();

		// Write to console

		if (isWriteConsole) {
			printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
					d1->getDeviceName(), d1->getAccelerationX(),
					d1->getAccelerationY(), d1->getAccelerationZ(),
					d1->getTemperature());

			printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
					d2->getDeviceName(), d2->getAccelerationX(),
					d2->getAccelerationY(), d2->getAccelerationZ(),
					d2->getTemperature());
		}

		// Write to disk

		if (isWriteFile) {
			fprintf(datafile, "%s,%i,%f,%f,%f\n", d1->getDeviceName(), idx,
					d1->getAccelerationX(), d1->getAccelerationY(),
					d1->getAccelerationZ());
			fprintf(datafile, "%s,%i,%f,%f,%f\n", d2->getDeviceName(), idx,
					d2->getAccelerationX(), d2->getAccelerationY(),
					d2->getAccelerationZ());
		}

		// Publish with MQTT

		if (isWriteMQTT) {
			char message[MESSAGE_BUFFER_SIZE];

			const char *jsonize =
					"{\"class\":\"ATT\",\"tag\":\"PTNTHTM\",\"device\":%s,\"time\":%d,\"acc_x\":%f,\"acc_y\":%f,\"acc_z\":%f}";
			snprintf(message, MESSAGE_BUFFER_SIZE, jsonize, d2->getDeviceName(),
					time(NULL), d2->getAccelerationX(), d2->getAccelerationY(),
					d2->getAccelerationZ());

			try {
#ifdef DAP_VERBOSE
				std::cout << "Publish to MQTT..." << std::flush;
#endif
				mqtt::message_ptr pubmsg = std::make_shared<mqtt::message>(
						message);
				pubmsg->set_qos(QOS);
				client.publish(TOPIC_PREFIX + '/' + d2->getDeviceName(), pubmsg)->wait_for_completion(
						TIMEOUT);
#ifdef DAP_VERBOSE
				std::cout << "OK" << std::endl;
#endif
			} catch (const mqtt::exception& exc) {
				std::cerr << std::endl << "Error (MQTT publish): " << exc.what()
						<< std::endl;
				return 1;
			}

		}

		sleep(1000);
	}

	// All done.

	if (isWriteFile) {
		std::fclose(datafile);
	}

	if (isWriteMQTT) {
		try {
			std::cout << "Disconnecting..." << std::flush;
			conntok = client.disconnect();
			conntok->wait_for_completion();
			std::cout << "OK" << std::endl;
		} catch (const mqtt::exception& exc) {
			std::cerr << std::endl << "Error (MQTT disconnect): " << exc.what()
					<< std::endl;
			return 1;
		}
	}



	return 0;
}
