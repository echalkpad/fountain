package ws.finson.audiosp.app;

import javax.swing.JComponent;

/**
 * This UpdateJComponent class is used to implement JComponent update tasks that
 * are run on the Swing dispatch thread with SwingUtils.invokeLater(Runnable).
 * This class can be extended to implement updates of JComponent subclasses.
 * 
 * @author Doug Johnson, 2012
 */
public class UpdateJComponent implements Runnable {

	private JComponent theComponentToUpdate;
	private Boolean theNewEnable;

	public UpdateJComponent(JComponent c, Boolean e) {
		theComponentToUpdate = c;
		theNewEnable = e;
	}

	public void run() {
		if (theNewEnable != null) {
			theComponentToUpdate.setEnabled(theNewEnable);
		}
	}
}
