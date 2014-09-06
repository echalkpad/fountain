/**
 * 
 */
package ws.finson.audiosp.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private String deviceName = null;
    private boolean attached = false;
    
    protected final List<HardwareDevice.Parameter> deviceParameters = new ArrayList<>();
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Initialize the common fields of a HardwareDevice using the attributes given in the
     * device configuration element. Attributes and child elements specific to a subclass should be
     * dealt with in the subclass constructor after this constructor has returned.
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
        
        //TODO add test for uniqueness
        
        if ((deviceName == null) || deviceName.isEmpty()) {
            throw new ConfigurationException(
                    "The name of each HardwareDevice must be specified, unique, and not empty.");
        }
   }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#attach()
     */
    @Override
    public void attach() throws IOException {
        attached = true;
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#detach()
     */
    @Override
    public void detach() {
        attached = false;
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#isAttached()
     */
    @Override
    public boolean isAttached() {
        return attached;
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#getDeviceName()
     */
    @Override
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#getParameterList()
     */
    @Override
    public List<Parameter> getParameterList() {
        return new ArrayList<Parameter>(deviceParameters);
    }


}
