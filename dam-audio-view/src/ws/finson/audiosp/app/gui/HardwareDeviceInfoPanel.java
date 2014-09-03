package ws.finson.audiosp.app.gui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.HardwareDevice;

/**
 * This InfoPanel displays various bits of information about a
 * Spectrum Analyzer device.
 * 
 * @author Doug Johnson
 * @since August 2014
 */
@SuppressWarnings("serial")
public class HardwareDeviceInfoPanel extends JPanel  {
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Initialize a new InfoPanel.
	 * 
	 * @param device
	 *            the HardwareDevice that this panel is associated with
	 */
	public HardwareDeviceInfoPanel(HardwareDevice device) {

		// data table

		JPanel valueTable = new JPanel();
		valueTable.setLayout(new GridLayout(0, 2));
		valueTable.setOpaque(false);
		((GridLayout) (valueTable.getLayout())).setHgap(5);

		// identifier label and value

		JLabel label = new JLabel("Device Name");
		JLabel value = new JLabel(device.getName());
		
		valueTable.add(label);
		valueTable.add(value);

		this.add(valueTable);
	}
}
