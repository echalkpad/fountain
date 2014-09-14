/**
 * 
 */
package ws.finson.audiosp.app.device;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import nu.xom.Attribute;
import nu.xom.Element;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.cfg.Throwables;

/**
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public abstract class AbstractHardwareDevice implements HardwareDevice {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReentrantLock parameterLock = new ReentrantLock();

    private String deviceName = null;
    protected DeviceClassID deviceClass = DeviceClassID.GenericHardwareDevice;

    protected final Map<String, DeviceParameter> deviceParameterMap = new HashMap<>();
    protected final Map<String, Object> deviceParameterValues = new HashMap<>();

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Initialize the common fields of a HardwareDevice using the attributes given in the device
     * configuration element. Attributes and child elements specific to a subclass should be dealt
     * with in the subclass constructor after this constructor has returned.
     * 
     * @param ac
     *            the ApplicationComponent for which this object is being constructed
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     */
    public AbstractHardwareDevice(ApplicationComponent ac, Element cE)
            throws ConfigurationException {

        int attributeCount = cE.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Attribute a = cE.getAttribute(i);
            switch (a.getLocalName()) {
            case "name":
                this.deviceName = a.getValue();
                break;
            default:
                break;
            }
        }

        // TODO add test for uniqueness

        if ((deviceName == null) || deviceName.isEmpty()) {
            throw new ConfigurationException(
                    "The name of each HardwareDevice must be specified, unique, and not empty.");
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        logger.debug("Run method in {}", this.getClass().getSimpleName());

        // Initialize each parameter value, either cache -> device or
        // device -> cache.

       parameterLock.lock();
        try {
            try {
                for (String s : deviceParameterMap.keySet()) {
                    logger.trace("Initializing parameter '{}'.", s);
                    if (deviceParameterValues.containsKey(s)) {
                        writeDeviceParameterValue(s, deviceParameterValues.get(s));
                    } else {
                        readDeviceParameterValue(s);
                    }
                    logger.trace("DeviceParameter '{}' has value '{}'.", s,
                            deviceParameterValues.get(s));
                }
            } catch (IOException e) {
                Throwables.printThrowableChain(e, logger, Level.WARN);
                return;
            }
        } finally {
            parameterLock.unlock();
        }

        // tell everybody who signed up that there is a fresh set of parameter values available

        pcs.firePropertyChange(new PropertyChangeEvent(this, null, null, null));

        // Wait for task requests and process them upon arrival

        // TODO implement task processing
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#getDeviceName()
     */
    @Override
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#getDeviceClass()
     */
    @Override
    public DeviceClassID getDeviceClass() {
        return deviceClass;
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#getParameterDescriptorList()
     */
    @Override
    public List<DeviceParameter> getParameterDescriptorList() {
        return new ArrayList<DeviceParameter>(deviceParameterMap.values());
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @throws IOException
     * @see ws.finson.audiosp.app.device.HardwareDevice#getParameterValue(java.lang.String)
     */
    @Override
    public Object getParameterValue(String s) {
        parameterLock.lock();
        try {
            return deviceParameterValues.get(s);
        } finally {
            parameterLock.unlock();
        }
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#getParameterValue(ws.finson.audiosp.app.device.HardwareDevice.DeviceParameter)
     */
    @Override
    public Object getParameterValue(DeviceParameter p) {
        parameterLock.lock();
        try {
            return deviceParameterValues.get(p.getName());
        } finally {
            parameterLock.unlock();
        }
    }

    /**
     * @throws IOException
     * @see ws.finson.audiosp.app.device.HardwareDevice#setParameterValue(java.lang.String)
     */
    @Override
    public void setParameterValue(String s, Object v) throws IOException {
        setParameterValue(deviceParameterMap.get(s), v);
    }

    /**
     * @throws IOException
     * @see ws.finson.audiosp.app.device.HardwareDevice#setParameterValue(ws.finson.audiosp.app.device.HardwareDevice.DeviceParameter,
     *      java.lang.Object)
     */
    @Override
    public void setParameterValue(DeviceParameter p, Object v) throws IOException {
        parameterLock.lock();
        try {
            if (p.getWritable()) {
                Object o = p.getType().cast(v);
                writeDeviceParameterValue(p.getName(), o);
            }
        } finally {
            parameterLock.unlock();
        }
    }

    /**
     * Read the named parameter value from the actual device, then store it with the cached values
     * in deviceParameterValues and also return it directly to the caller. Must be implemented by a
     * subclass that actually talks to the device.
     * 
     * @param pname
     * @return the requested parameter value
     * @throws IOException
     */
    abstract protected Object readDeviceParameterValue(String pname) throws IOException;

    /**
     * Write the given value out to the actual device and store it with the cached values in
     * deviceParameterValues. Must be implemented by a subclass that actually talks to the device.
     * 
     * @param pname
     * @param pvalue
     * @throws IOException
     */
    abstract protected void writeDeviceParameterValue(String pname, Object pvalue)
            throws IOException;

}
