package ws.finson.audiosp.app.gui;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.device.DeviceParameter;
import ws.finson.audiosp.app.device.SpectrumAnalyzerDevice;

/**
 * This SpectrumAnalyzerDevicePanel
 * 
 * @author Doug Johnson, 2014
 */
@SuppressWarnings("serial")
public class SpectrumAnalyzerDevicePanel extends JPanel implements PropertyChangeListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SpectrumAnalyzerDevice sad;

    private Map<String, JLabel> parameterMap = new HashMap<>();

    /**
     * Initialize a new SpectrumAnalyzerDevicePanel.
     * 
     * @param device
     *            the SpectrumAnalyzerDevice that this panel is associated with
     */
    public SpectrumAnalyzerDevicePanel(SpectrumAnalyzerDevice device) {

        sad = device;

        // What are the parameters for this device?

        List<DeviceParameter> params = sad.getParameterDescriptorList();

        // Build a data table to display the parameters

        JPanel valueTable = new JPanel();
        valueTable.setLayout(new GridLayout(params.size(), 2));
        valueTable.setOpaque(false);
        ((GridLayout) (valueTable.getLayout())).setHgap(5);

        for (DeviceParameter p : params) {
            JLabel nameLabel = new JLabel(p.getLabel());
            JLabel valueLabel = new JLabel("-----");
            nameLabel.setLabelFor(valueLabel);
            valueTable.add(nameLabel);
            valueTable.add(valueLabel);
            parameterMap.put(p.getName(), valueLabel);
        }

        this.add(valueTable);

        sad.addPropertyChangeListener(this);
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.trace(evt.toString());
        JLabel labelToUpdate = null;
        String propName = evt.getPropertyName();
        if (propName == null) {
            for (Map.Entry<String,JLabel> me : parameterMap.entrySet()) {
                String newValue = sad.getParameterValue(me.getKey()).toString();
                SwingUtilities.invokeLater(new UpdateJLabel(me.getValue(), newValue));
            }
        } else {
            labelToUpdate = parameterMap.get(propName);
            if (labelToUpdate == null) {
                logger.warn("Changed property '{}' is not currently being displayed.", propName);
            } else {
                SwingUtilities.invokeLater(new UpdateJLabel(labelToUpdate, evt.getNewValue()
                        .toString()));
            }
        }
    }

}
