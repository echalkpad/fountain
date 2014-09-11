/**
 * 
 */
package ws.finson.audiosp.app.device;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.device.HardwareDevice.ClassID;
import ws.finson.audiosp.app.device.HardwareDevice.Parameter;
import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public abstract class AbstractHardwareDevice implements HardwareDevice {
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReentrantLock parameterLock = new ReentrantLock();

    private String deviceName = null;
    protected HardwareDevice.ClassID deviceClass = HardwareDevice.ClassID.GenericHardwareDevice;

    protected final Map<String, HardwareDevice.Parameter> deviceParameterMap = new HashMap<>();
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
    public HardwareDevice.ClassID getDeviceClass() {
        return deviceClass;
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#getParameterDescriptorList()
     */
    @Override
    public List<Parameter> getParameterDescriptorList() {
        return new ArrayList<Parameter>(deviceParameterMap.values());
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /**
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
     * @see ws.finson.audiosp.app.device.HardwareDevice#getParameterValue(ws.finson.audiosp.app.device.HardwareDevice.Parameter)
     */
    @Override
    public Object getParameterValue(Parameter p) {
        parameterLock.lock();
        try {
            return deviceParameterValues.get(p.getName());
        } finally {
            parameterLock.unlock();
        }
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#setParameterValue(java.lang.String)
     */
    @Override
    public void setParameterValue(String s, Object v) {
        parameterLock.lock();
        try {
            Parameter p = deviceParameterMap.get(s);
            setParameterValue(p, v);
        } finally {
            parameterLock.unlock();
        }
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#setParameterValue(ws.finson.audiosp.app.device.HardwareDevice.Parameter)
     */
    @Override
    public void setParameterValue(Parameter p, Object v) {
        parameterLock.lock();
        try {
            if (p.getWritable()) {
                Object o = p.getType().cast(v);
                deviceParameterValues.put(p.getName(), o);
            }
        } finally {
            parameterLock.unlock();
        }
    }

}
