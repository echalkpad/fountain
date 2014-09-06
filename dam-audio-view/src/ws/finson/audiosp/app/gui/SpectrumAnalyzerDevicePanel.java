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

import ws.finson.audiosp.app.HardwareDevice;
import ws.finson.audiosp.app.SpectrumAnalyzerDevice;
import ws.finson.audiosp.app.UpdateJLabel;

/**
 * This PositionPanel
 * 
 * @author Doug Johnson, 2012
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

        List<HardwareDevice.Parameter> params = sad.getParameterList();

        // Build a data table to display the parameters

        JPanel valueTable = new JPanel();
        valueTable.setLayout(new GridLayout(params.size(), 2));
        valueTable.setOpaque(false);
        ((GridLayout) (valueTable.getLayout())).setHgap(5);

        for (HardwareDevice.Parameter p : params) {
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
        JLabel labelToUpdate = null;
        String propName = evt.getPropertyName();
        if (propName == null) {
            logger.warn("Unexpected null property name in PropertyChangeEvent.");
        } else {
            labelToUpdate = parameterMap.get(propName);
            if (labelToUpdate == null) {
                logger.warn("Changed property '{}' is not being displayed.", propName);
            } else {
                SwingUtilities.invokeLater(new UpdateJLabel(labelToUpdate, evt.getNewValue()
                        .toString()));
            }
        }
    }

}
