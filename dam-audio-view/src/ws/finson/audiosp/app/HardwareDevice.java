package ws.finson.audiosp.app;

import java.io.IOException;

/***
 * Identify the methods that a class must implement in order to fill the role of
 * a hardware device.
 * 
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public interface HardwareDevice {
	/**
	 * Open communication with the device.
	 * @throws IOException 
	 */
	void open() throws IOException;
	/**
	 * Close communication with the device.
	 */
	void close();
	/**
	 * Return name of the device.
	 */
	String getName();
	
	// read
	// status
	// write
	// control

}
