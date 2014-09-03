package ws.finson.audiosp.app.gui;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.SpectrumAnalyzerDevice;
import ws.finson.audiosp.app.UpdateJLabel;

/**
 * This PositionPanel
 * 
 * @author Doug Johnson, 2012
 */
@SuppressWarnings("serial")
public class SpectrumAnalyzerStatusPanel extends JPanel implements PropertyChangeListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SpectrumAnalyzerDevice sad;
	
	private JLabel sizeValue;
	private JLabel rateValue;
	private JLabel channelValue;
	
	private Map<String,JLabel> propertyMap = new HashMap<>();

	/**
	 * Initialize a new SpectrumAnalyzerStatusPanel.
	 * 
	 * @param device
	 *            the SpectrumAnalyzerDevice that this panel is associated with
	 */
	public SpectrumAnalyzerStatusPanel(SpectrumAnalyzerDevice device) {

		sad = device;
		
		// data table

		JPanel valueTable = new JPanel();
		valueTable.setLayout(new GridLayout(0, 2));
		valueTable.setOpaque(false);
		((GridLayout) (valueTable.getLayout())).setHgap(5);

        // FFT Size

        int val = sad.getFFTSize();
        JLabel sizeLabel = new JLabel("FFT Size");
        sizeValue = new JLabel(Integer.toString(val));
        sizeLabel.setLabelFor(sizeValue);
        valueTable.add(sizeLabel);
        valueTable.add(sizeValue);

        // sample rate

        val = sad.getSampleRate();
        JLabel rateLabel = new JLabel("Sample Rate");
        rateValue = new JLabel(Integer.toString(val));
        rateLabel.setLabelFor(rateValue);
        valueTable.add(rateLabel);
        valueTable.add(rateValue);

        // channel count

        val = sad.getChannelCount();
        JLabel channelLabel = new JLabel("Channel Count");
        channelValue = new JLabel(Integer.toString(val));
        channelLabel.setLabelFor(channelValue);
        valueTable.add(channelLabel);
        valueTable.add(channelValue);

		this.add(valueTable);
        
        propertyMap.put("FFT_SIZE",sizeValue);
        propertyMap.put("SAMPLE_RATE_HZ",rateValue);
        propertyMap.put("AUDIO_CHANNEL_COUNT",channelValue);
		
//		sad.addPropertyChangeListener(this);
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
            labelToUpdate = propertyMap.get(propName);
            if (labelToUpdate == null) {
                logger.warn("Changed property '{}' is not being displayed.",propName);
            } else {
                SwingUtilities.invokeLater(new UpdateJLabel(labelToUpdate, evt.getNewValue().toString()));
            }
        }
    }

}
