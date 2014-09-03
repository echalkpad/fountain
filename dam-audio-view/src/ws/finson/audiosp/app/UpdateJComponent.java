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

	/**
	 * Remember the parameter values so that the run method can make the
	 * desired changes.
	 * @param c The JComponent to change
	 * @param e Boolean enable/disable.  Ignored if null.
	 */
	public UpdateJComponent(JComponent c, Boolean e) {
		theComponentToUpdate = c;
		theNewEnable = e;
	}

	/**
	 * This run() method is executed on the Swing dispatch thread and makes the
	 * requested changes.  Note that the class that wants to make changes must
	 * use SwingUtils.invokeLater(theNewUpdaterObject) to actually start the update
	 * process.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (theNewEnable != null) {
			theComponentToUpdate.setEnabled(theNewEnable);
		}
	}
}
