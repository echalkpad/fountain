/*
 ============================================================================
 Name        : dap.c
 Author      : Doug Johnson
 Version     :
 Copyright   : GPL
 Description : I2C Data Acquisition
 ============================================================================
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#include "LSM303DLHC.h"
#include "LSM303D.h"

#include <cstdlib>
#include <string>
#include <map>
#include <vector>
#include <cstring>
#include "mqtt/client.h"
#include "mqtt/ipersistable.h"

const std::string ADDRESS("tcp://spark:1883");
const std::string CLIENTID("SyncPublisher");
const std::string TOPIC("hello");

const std::string PAYLOAD1("Hello World!");

const char* PAYLOAD2 = "Hi there!";
const char* PAYLOAD3 = "Is anyone listening?";

const int QOS = 1;
const int TIMEOUT = 10000;

/////////////////////////////////////////////////////////////////////////////

class sample_mem_persistence : virtual public mqtt::iclient_persistence
{
	bool open_;
	std::map<std::string, mqtt::ipersistable_ptr> store_;

public:
	sample_mem_persistence() : open_(false) {}

	// "Open" the store
	virtual void open(const std::string& clientId, const std::string& serverURI) {
		std::cout << "[Opening persistence for '" << clientId
			<< "' at '" << serverURI << "']" << std::endl;
		open_ = true;
	}

	// Close the persistent store that was previously opened.
	virtual void close() {
		std::cout << "[Closing persistence store.]" << std::endl;
		open_ = false;
	}

	// Clears persistence, so that it no longer contains any persisted data.
	virtual void clear() {
		std::cout << "[Clearing persistence store.]" << std::endl;
		store_.clear();
	}

	// Returns whether or not data is persisted using the specified key.
	virtual bool contains_key(const std::string &key) {
		return store_.find(key) != store_.end();
	}

	// Gets the specified data out of the persistent store.
	virtual mqtt::ipersistable_ptr get(const std::string& key) const {
		std::cout << "[Searching persistence for key '"
			<< key << "']" << std::endl;
		auto p = store_.find(key);
		if (p == store_.end())
			throw mqtt::persistence_exception();
		std::cout << "[Found persistence data for key '"
			<< key << "']" << std::endl;

		return p->second;
	}
	/**
	 * Returns the keys in this persistent data store.
	 */
	virtual std::vector<std::string> keys() const {
		std::vector<std::string> ks;
		for (const auto& k : store_)
			ks.push_back(k.first);
		return ks;
	}

	// Puts the specified data into the persistent store.
	virtual void put(const std::string& key, mqtt::ipersistable_ptr persistable) {
		std::cout << "[Persisting data with key '"
			<< key << "']" << std::endl;

		store_[key] = persistable;
	}

	// Remove the data for the specified key.
	virtual void remove(const std::string &key) {
		std::cout << "[Persistence removing key '" << key << "']" << std::endl;
		auto p = store_.find(key);
		if (p == store_.end())
			throw mqtt::persistence_exception();
		store_.erase(p);
		std::cout << "[Persistence key removed '" << key << "']" << std::endl;
	}
};

/////////////////////////////////////////////////////////////////////////////

class callback : public virtual mqtt::callback
{
public:
	virtual void connection_lost(const std::string& cause) {
		std::cout << "\nConnection lost" << std::endl;
		if (!cause.empty())
			std::cout << "\tcause: " << cause << std::endl;
	}

	// We're not subscrived to anything, so this should never be called.
	virtual void message_arrived(const std::string& topic, mqtt::message_ptr msg) {
	}

	virtual void delivery_complete(mqtt::idelivery_token_ptr tok) {
		std::cout << "\n\t[Delivery complete for token: "
			<< (tok ? tok->get_message_id() : -1) << "]" << std::endl;
	}
};

// --------------------------------------------------------------------------


int main(int argc, char* argv[]) {
	printf("DAP - I2C Data Acquisition Program\n");
	printf("Stream to MQTT broker.\n");
	
	char brokerName[] = "test.csv";
	printf("Will write messages to %s\n\n",brokerName);
	
	FILE *datafile;
	datafile=fopen(brokerName, "w");
	fprintf(datafile, "device,sample,X,Y,Z\n");

	LSM303DLHC *d1 = new LSM303DLHC(1, 0x19,"LSM303DLHC");
	printf("Device initialization complete: %s at Bus %i, Address %#04x. Handle: %i\n", d1->getDeviceName(),
			d1->getBus(), d1->getDeviceAddress(),d1->getHandle());

	LSM303D *d2 = new LSM303D(1,0x1D,"LSM303D");
	printf("Device initialization complete: %s at Bus %i, Address %#04x. Handle: %i\n", d2->getDeviceName(),
		d2->getBus(), d2->getDeviceAddress(),d2->getHandle());

	sample_mem_persistence persist;
	mqtt::client client(ADDRESS, CLIENTID, &persist);

	callback cb;
	client.set_callback(cb);

	mqtt::connect_options connOpts;
	connOpts.set_keep_alive_interval(20);
	connOpts.set_clean_session(true);

	try {
		std::cout << "Connecting..." << std::flush;
		client.connect(connOpts);
		std::cout << "OK" << std::endl;

		// First use a message pointer.

		std::cout << "Sending message..." << std::flush;
		mqtt::message_ptr pubmsg = std::make_shared<mqtt::message>(PAYLOAD1);
		pubmsg->set_qos(QOS);
		client.publish(TOPIC, pubmsg);
		std::cout << "OK" << std::endl;
	}
	catch (const mqtt::persistence_exception& exc) {
		std::cerr << "Persistence Error: " << exc.what() << " ["
			<< exc.get_reason_code() << "]" << std::endl;
		return 1;
	}
	catch (const mqtt::exception& exc) {
		std::cerr << "Error: " << exc.what() << " ["
			<< exc.get_reason_code() << "]" << std::endl;
		return 1;
	}


	for (int idx = 0; idx < 50; idx++) {

		d1->refreshSensorData();
		d2->refreshSensorData();

		printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
				d1->getDeviceName(),d1->getAccelerationX(),d1->getAccelerationY(),d1->getAccelerationZ(),d1->getTemperature());
		fprintf(datafile,"%s,%i,%f,%f,%f\n",
				d1->getDeviceName(),idx,d1->getAccelerationX(),d1->getAccelerationY(),d1->getAccelerationZ());

		printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
				d2->getDeviceName(),d2->getAccelerationX(),d2->getAccelerationY(),d2->getAccelerationZ(),d2->getTemperature());
		fprintf(datafile,"%s,%i,%f,%f,%f\n",
				d2->getDeviceName(),idx,d2->getAccelerationX(),d2->getAccelerationY(),d2->getAccelerationZ());

		sleep(1);
	}

	return EXIT_SUCCESS;
}

