package ws.finson.audiosp.app;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * This UpdateJLabel class is used to implement JLabel update tasks that are run
 * on the Swing dispatch thread with SwingUtils.invokeLater(Runnable).  This
 * class extends UpdateJComponent and adds capabilities that are unique to JLabels.
 * 
 * @author Doug Johnson, 2012
 */
public class UpdateJLabel extends UpdateJComponent {

	private JLabel theLabelToUpdate;
	private String theNewText;
	private Icon theNewIcon;
	
	/**
	 * Remember the given parameters so that they can be used later in the run() method.
	 * The label parameter must not be null, but null values for the other parameters are
	 * ignored and will not cause exceptions.
	 * 
	 * @param aLabel the JLabel to be updated
	 * @param s new text for the label
	 * @param k new icon for the label
	 * @param e new enable state for the label
	 */
	public UpdateJLabel(JLabel aLabel, String s, Icon k, Boolean e) {
		super(aLabel,e);
		theLabelToUpdate = aLabel;
		theNewText = s;
		theNewIcon = k;
	}

	public UpdateJLabel(JLabel aLabel, String s) {
		this(aLabel,s,null,null);
	}

	public UpdateJLabel(JLabel aLabel, Icon k) {
		this(aLabel,null,k,null);
	}
	
	public UpdateJLabel(JLabel aLabel, Boolean e) {
		this(aLabel,null,null,e);
	}

	public void run() {
		super.run();
		if (theNewText != null) {
			theLabelToUpdate.setText(theNewText);
		}
		if (theNewIcon != null) {
			theLabelToUpdate.setIcon(theNewIcon);
		}
	}
}
