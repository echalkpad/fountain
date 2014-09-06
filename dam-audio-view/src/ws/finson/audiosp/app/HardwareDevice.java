package ws.finson.audiosp.app;

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
public interface HardwareDevice {

    /**
     * Open communication between the device driver and the device.
     * 
     * @throws IOException
     */
    void attach() throws IOException;

    /**
     * Close communication with the device.
     */
    void detach();

    /**
     * Get state of driver connection to device
     * 
     * @return true if connection has been established to the device
     */
    boolean isAttached();

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
     * Get list of device parameter names
     * 
     * @return List of parameters that are available for this device
     */
    List<HardwareDevice.Parameter> getParameterList();

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

        /**
         * @param name String name of the parameter as recognized by the device firmware
         * @param label User friendlier String display label
         * @param writable true if the parameter can be set, false if read only
         */
        public Parameter(String name, String label, Boolean writable) {
            super();
            this.name = name;
            this.label = label;
            this.writable = writable;
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

    }
    /***
     * Identify the top level hardware device classes for which drivers are expected to exist in the
     * Interchangeable Virtual Instrument scheme of things. I'm using the same taxonomy just for general
     * consistency, there is no plan or design that any of this work is IVI compatible.
     * 
     */
    enum ClassID {
        DigitalMultiMeter,
        Oscilloscope,
        ArbitraryWaveformGenerator,
        PowerSupply,
        Switch,
        PowerMeter,
        SpectrumAnalyzer,
        RFSignalGenerator
    };


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
