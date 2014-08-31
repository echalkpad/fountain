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
    
    //TODO change open() to connect()
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
	
	// Map<String,String> getProperties()
	// String getProperty(String)
	
	// read
	// status
	// write
	// control
	
	/*     
    Map<String,String> infoMap = new HashMap<String,String>();
    infoMap.put("identifier",device.getIdentifier())
    infoMap.put("title",device.getTitle());
    infoMap.put(x,device., value)
getDeviceClass
getDeviceID
getDeviceLabel
getDeviceName
getDeviceType
getDeviceVersion
getLibraryVersion
getSerialNumber
getServerAddress
getServerID
getServerPort
isAttached
isAttachedToServer
*/


}
