package ws.finson.audiosp.app.device;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

/***
 * Identify the methods that a class must implement in order to fill the role of a hardware device.
 * 
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public interface HardwareDevice extends Runnable {

    /**
     * Return name of the device.
     * 
     * @return device name (as provided by the configuration file)
     */
    String getDeviceName();

    /**
     * Return the device class
     * 
     * @return class identifier
     */
    DeviceClassID getDeviceClass();

    /**
     * Get list of device parameter descriptors
     * 
     * @return List of descriptor objects for the parameters that are available for this device
     */
    List<DeviceParameter> getParameterDescriptorList();

    /**
     * Get a particular parameter value from the device driver.
     * 
     * @param s
     *            the String name of the parameter as recognized by the device driver
     * @return the value of the named parameter
     * @throws IOException 
     */
    Object getParameterValue(String s);

    /**
     * Get a particular parameter value from the device driver.
     * 
     * @param p
     *            the DeviceParameter descriptor that identifies the parameter of interest
     * @return the value of the named parameter
     */
    Object getParameterValue(DeviceParameter p);

    /**
     * Set a particular parameter value in the device driver and the device
     * 
     * @param s
     *            the String name of the parameter as recognized by the device driver
     * @param v
     *            the new value of the named parameter
     * @throws IOException 
     */
    void setParameterValue(String s, Object v) throws IOException;

    /**
     * Set a particular parameter value in the device driver and the device
     * 
     * @param p
     *            the DeviceParameter descriptor that identifies the parameter of interest
     * @param v
     *            the new value of the named parameter
     * @throws IOException 
     */
    void setParameterValue(DeviceParameter p, Object v) throws IOException;

    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    /**
     * Add a PropertyChangeListener for a specific property.
     * @param propertyName
     * @param listener
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    /**
     * Remove a PropertyChangeListener for a specific property.
     * @param propertyName
     * @param listener
     */
    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);


    /*
     * possible additions ...
     * 
     * Map<String,String> getProperties() String getProperty(String)
     * 
     * read status write control
     * 
     * Map<String,String> infoMap = new HashMap<String,String>();
     * infoMap.put("identifier",device.getIdentifier()) infoMap.put("title",device.getTitle());
     * 
     * - getDeviceClass getDeviceID getDeviceLabel - getDeviceName getDeviceType getDeviceVersion
     * getLibraryVersion getSerialNumber getServerAddress getServerID getServerPort - isAttached
     * isAttachedToServer
     * 
     * getProtocol setProtocol
     * 
     * class.protocol. SpectrumAnalyzer.DiCola.
     * 
     * getKeys("class.protocol")
     * 
     */

}
