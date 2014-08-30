package ws.finson.audiosp.app;

import java.util.List;

/**
 * This interface is implemented by classes that provide access to one or more HardwareDevices, for 
 * example a SpectrumAnalyzerDevice.
 * 
 * @author Doug Johnson
 * @since Aug 28, 2014
 *
 */
public interface DeviceRack {
    /**
     * Get references to all HardwareDevices contained by these slots.
     * @return A list of references to the contained HardwareDevices, if any.  The list may be empty, but it will never be null.
     */
    List<HardwareDevice> getDevices();
    
    /**
     * Get references to all HardwareDevices contained in this slot object that are
     * instances of the given Class<T>, where T is an Interface or a Class.
     * @return A list of references to the qualifying HardwareDevices, if any.  The list may be empty, but it will never be null.
     */
//    <T> List<T> getDevices(Class<T> required);

}
