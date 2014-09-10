package ws.finson.audiosp.app;

import java.beans.PropertyChangeListener;
import java.util.List;

import ws.tuxi.lib.cfg.ConfigurationException;

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
    ClassID getDeviceClass();

    /**
     * Get list of device parameter descriptors
     * 
     * @return List of descriptor objects for the parameters that are available for this device
     */
    List<HardwareDevice.Parameter> getParameterDescriptorList();
    
    /**
     * Get a particular parameter value from the device driver.
     * @param s the String name of the parameter as recognized by the device driver
     * @return the value of the named parameter
     */
    Object getParameterValue(String s);
    
    /**
     * Get a particular parameter value from the device driver.
     * @param p the Parameter descriptor that identifies the parameter of interest
     * @return the value of the named parameter
     */
    Object getParameterValue(HardwareDevice.Parameter p);
    
    /**
     * Set a particular parameter value in the device driver and the device
     * @param s the String name of the parameter as recognized by the device driver
     * @param v the new value of the named parameter
     */
    void setParameterValue(String s, Object v);
    
    /**
     * Set a particular parameter value in the device driver and the device
     * @param p the Parameter descriptor that identifies the parameter of interest
     * @param v the new value of the named parameter
     */
    void setParameterValue(HardwareDevice.Parameter p, Object v);

    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    // ------------------------

    class Parameter {
        private String name;
        private String label;
        private Boolean writable;
        private Class<?> type;
        private Object initialValue;

        /**
         * Note that the class of the initial value object is taken to be the class
         * of the parameter.
         * 
         * @param name String name of the parameter as recognized by the device firmware
         * @param label User friendlier String display label
         * @param writable true if the parameter can be set, false if read only
         * @param value the initial value of the parameter expressed as an Object
         * @throws ConfigurationException 
         */
        public Parameter(String name, String label, Boolean writable, Object value) throws ConfigurationException {
            this.name = name;
            this.label = label;
            this.writable = writable;
            this.initialValue = value;
            try {
                this.type = value.getClass();
            } catch (SecurityException e) {
                throw new ConfigurationException(e);
            }
       }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public Boolean getWritable() {
            return writable;
        }

        public Class<?> getType() {
            return type;
        }
        
        public Object getInitialValue() {
            return initialValue;
        }

    }

    // ------------------------

    /***
     * Identify the top level hardware device classes for which drivers are expected to exist in the
     * Interchangeable Virtual Instrument scheme of things. I'm using the same taxonomy just for general
     * consistency, there is no plan or design that any of this work is IVI compatible.
     * 
     */
    enum ClassID {
        GenericHardwareDevice,
        DigitalMultiMeter,
        Oscilloscope,
        ArbitraryWaveformGenerator,
        PowerSupply,
        Switch,
        PowerMeter,
        SpectrumAnalyzer,
        RFSignalGenerator
    };

    // ------------------------

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
     * Class IVI Driver Digital multimeter (DMM) IviDmm Oscilloscope IviScope Arbitrary
     * waveform/function generator IviFgen DC power supply IviDCPwr Switch IviSwitch Power meter
     * IviPwrMeter Spectrum analyzer IviSpecAn RF signal generator IviRFSigGen
     */

}
