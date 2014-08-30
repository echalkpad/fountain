package ws.finson.audiosp.app;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * This SpectrumAnalyzerGui class implements a simple Swing GUI for managing various
 * aspects of a Spectrum Analyzer.
 * 
 * @author Doug Johnson
 * @since August 2014
 */
@SuppressWarnings("serial")
public class SpectrumAnalyzerGui extends JFrame implements ActionListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final HardwareDevice device;
//	private final Map<JButton, FixtureTask> taskMap;
	

    /**
     * Initialize the fields of this GUI frame using the elements and attributes given
     * in the configuration element.
     * 
     * @param ac
     *            the ApplicationComponent for which this object is being constructed
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     * 
     * 
     * @author Doug Johnson
     * @since August 2014
     * 
     */
    @SuppressWarnings("unchecked")
    public SpectrumAnalyzerGui(HardwareDevice device)
            throws ConfigurationException {
        
        this.device = device;
        
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setName("Audio Analyzer GUI");
				
//		FixtureExecutive exec = (FixtureExecutive) cfg.getApplication().getApplicationComponents(FixtureExecutive.class).get(0);
//		List<FixtureTask> tasks = exec.getTasks();
//
//		device = cfg.getActuators().get(0);
//		taskMap = new HashMap<JButton, FixtureTask>();

		JPanel newContentPane = new JPanel();
		newContentPane.setOpaque(true);
		newContentPane.setLayout(new GridBagLayout());
		newContentPane.setBackground(Color.LIGHT_GRAY);

//		GridBagConstraints rules = new GridBagConstraints();
//
//		rules.gridx = 0;
//		rules.gridy = 0;
//		rules.gridheight = 1;
//		rules.gridwidth = 1;
//		rules.fill = GridBagConstraints.NONE;
//		rules.ipadx = 20;
//		rules.ipady = 0;
//		rules.insets = new Insets(0, 0, 0, 0);
//		rules.anchor = GridBagConstraints.CENTER;
//		rules.weightx = 0;
//		rules.weighty = 0;
//		
//		// Actuator information panel
//		
//		JPanel infoPanel = new InfoPanel(device);
//		infoPanel.setOpaque(false);
//		newContentPane.add(infoPanel,rules);
//
//		// Position panel
//
//		rules.gridx = 1;
//		rules.gridy = 0;
//		JPanel posPanel = new PositionPanel(device);
//		posPanel.setOpaque(false);
//		newContentPane.add(posPanel, rules);
//
//		rules.gridx = 0;
//		rules.gridy = 1;
//
//		// Task buttons
//
//		for (FixtureTask t : tasks) {
//			logger.debug("Task {}", t.toString());
//			JButton b = new JButton(t.getClass().getSimpleName());
//			taskMap.put(b, t);
//			b.addActionListener(this);
//			newContentPane.add(b, rules);
//			rules.gridx++;
//			if (rules.gridx > 2) {
//				rules.gridx = 0;
//				rules.gridy += 1;
//			}
//		}

		setContentPane(newContentPane);
		this.setTitle(this.device.getName());
		pack();
		setVisible(true);

	}

	/**
	 * Start a task thread running.  First disable the task buttons, then start a
	 * task thread, then set up a thread to listen for the task thread to exit.
	 * @param e
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
//		logger.debug("Action event {}", e.getActionCommand());
//		for (JButton b : taskMap.keySet()) {
//			b.setEnabled(false);
//		}
//		ApplicationComponent task = taskMap.get(e.getSource());
//		Thread taskThread = new Thread(task, "Task-"+task.getClass().getSimpleName());
//		logger.debug("New thread to run {} task.", task.getClass()
//				.getSimpleName());
//		taskThread.start();
//
//		Runnable helper = new WaitAndEnable(taskThread);
//		Thread helperThread = new Thread(helper,"WaitAndEnable");
//		helperThread.start();
	}

	// --------------
	
//	/**
//	 * This WaitAndEnable class helps the ExecutiveGui class leave the
//	 * task buttons disabled while one of the tasks executes.  When the
//	 * task thread exits, the thread this object is running on wakes up
//	 * and schedules a Swing task to re-enable the buttons.
//	 * 
//	 * @author Doug Johnson, 2012
//	 */
//	private class WaitAndEnable implements Runnable {
//		private Thread threadToJoin;
//
//		public WaitAndEnable(Thread t) {
//			threadToJoin = t;
//		}
//
//		/**
//		 * 
//		 * @see java.lang.Runnable#run()
//		 */
//		@Override
//		public void run() {
//			logger.debug("Executive's WaitAndEnable run method waiting for {} to exit.",threadToJoin.getName());
//			
//			// Wait for the task thread to exit
//			
//			while (threadToJoin.isAlive()) {
//				try {
//					threadToJoin.join();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			logger.debug("{} has exited.",threadToJoin.getName());
//			
//			// Re-enable the task start buttons
//			
//			javax.swing.SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					for (JButton b : taskMap.keySet()) {
//						b.setEnabled(true);
//					}
//				}
//			});
//
//		}
//	}

}
