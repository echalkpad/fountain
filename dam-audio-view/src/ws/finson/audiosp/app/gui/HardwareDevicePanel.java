package ws.finson.audiosp.app.gui;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.HardwareDevice;

/**
 * This HardwareDevicePanel displays various bits of information about an arbitrary HardwareDevice.
 * 
 * @author Doug Johnson
 * @since August 2014
 */
@SuppressWarnings("serial")
public class HardwareDevicePanel extends JPanel {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private HardwareDevice theDevice;
    private JLabel deviceNameValue;
    private JLabel deviceClassValue;

    /**
     * Initialize a new HardwareDevicePanel.
     * 
     * @param device
     *            the HardwareDevice that this panel is associated with
     */
    public HardwareDevicePanel(HardwareDevice device) {

        theDevice = device;

        // data table

        JPanel valueTable = new JPanel();
        valueTable.setLayout(new GridLayout(0, 2));
        valueTable.setOpaque(false);
        ((GridLayout) (valueTable.getLayout())).setHgap(5);

        // device name and class

        valueTable.add(new JLabel("Device Name"));
        deviceNameValue = new JLabel(device.getDeviceName());
        valueTable.add(deviceNameValue);

        valueTable.add(new JLabel("Device Class"));
        deviceClassValue = new JLabel(device.getDeviceClass().toString());
        valueTable.add(deviceClassValue);

        this.add(valueTable);

        device.addPropertyChangeListener(new PropertyChangeHandler());
    }

    // ---------------------------

    private class PropertyChangeHandler implements PropertyChangeListener {
        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            logger.trace(evt.toString());
            String propName = evt.getPropertyName();
        }
    }
}
