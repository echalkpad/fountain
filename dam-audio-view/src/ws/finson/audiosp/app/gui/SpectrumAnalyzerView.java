package ws.finson.audiosp.app.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.AudioAnalyzer;
import ws.finson.audiosp.app.HardwareDevice;
import ws.finson.audiosp.app.SpectrumAnalyzerDevice;
import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * This SpectrumAnalyzerView class implements a simple Swing GUI for managing various aspects of a
 * Spectrum Analyzer. Note that ALL methods in this class MUST be called from the Swing Event
 * Dispatch Thread. The constructor and setViewSource also assume that any threads that might be
 * changing shared data structures are blocked while these methods execute.
 * 
 * @author Doug Johnson
 * @since August 2014
 */
@SuppressWarnings("serial")
public class SpectrumAnalyzerView extends JFrame implements DeviceView, ActionListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationComponent parent;
    private String sourceName = null;
    private SpectrumAnalyzerDevice device;

    // private final Map<JButton, FixtureTask> taskMap;

    /**
     * Initialize the fields of this GUI frame using the elements and attributes given in the
     * configuration element.
     * 
     * @param ac
     *            the ApplicationComponent for which this object is being constructed
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     * 
     * @author Doug Johnson
     * @since August 2014
     * 
     */
    public SpectrumAnalyzerView(ApplicationComponent ac, Element cE) throws ConfigurationException {
        assert SwingUtilities.isEventDispatchThread() : "DeviceView constructors must run on Swing Dispatch Thread only.";
        parent = ac;

        // Learn what we can from the specified attributes.

        int attributeCount = cE.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Attribute a = cE.getAttribute(i);
            switch (a.getLocalName()) {
            case "type":
                logger.trace("type: {}", a.getValue()); // Has already been used to create this
                                                        // object
                break;
            case "sourceName": // name of our data source
                sourceName = a.getValue();
                logger.trace("sourceName: {}", sourceName);
                break;
            default:
                logger.warn("Skipping <{}> attribute. Attribute not recognized.", a.getLocalName());
                break;
            }
        }

        if (sourceName == null || sourceName.isEmpty()) {
            throw new ConfigurationException(
                    "A sourceName must be specified and it must not be zero-length.");
        }

        // Initialize the top level visual elements of the GUI.

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(getClass().getSimpleName());
        
        // Why new content pane??

        JPanel theContentPane = new JPanel();
        theContentPane.setOpaque(true);
        theContentPane.setLayout(new GridBagLayout());
        theContentPane.setBackground(Color.LIGHT_GRAY);

        setContentPane(theContentPane);
    }

    // FixtureExecutive exec = (FixtureExecutive)
    // cfg.getApplication().getApplicationComponents(FixtureExecutive.class).get(0);
    // List<FixtureTask> tasks = exec.getTasks();
    //
    // device = cfg.getActuators().get(0);
    // taskMap = new HashMap<JButton, FixtureTask>();

    /**
     * @throws ConfigurationException
     * @see ws.finson.audiosp.app.gui.DeviceView#setDataSource()
     */
    @Override
    public void setDataSource() throws ConfigurationException {
        assert SwingUtilities.isEventDispatchThread() : "DeviceView methods must run on Swing Dispatch Thread only.";

        List<AudioAnalyzer> instruments = parent.getApplication().getApplicationComponents(
                AudioAnalyzer.class);

        for (AudioAnalyzer inst : instruments) {
            List<HardwareDevice> devices = inst.getDevices();
            for (HardwareDevice d : devices) {
                if (d.getName().equals(sourceName)) {
                    if (d instanceof SpectrumAnalyzerDevice) {
                        device = (SpectrumAnalyzerDevice) d;
                    } else {
                        throw new ConfigurationException("Device '" + d.getName()
                                + "' is not a SpectrumAnalyzerDevice.");
                    }
                }
            }
        }
        if (device == null) {
            throw new ConfigurationException(
                    "SwingStarter cannot find a SpectrumAnalyzerDevice object in the parent Application configuration.");
        }

        // Create HardwareDevice information panel and add it to the content pane

        GridBagConstraints rules = new GridBagConstraints();

        rules.gridx = 0;
        rules.gridy = 0;
        rules.gridheight = 1;
        rules.gridwidth = 1;
        rules.fill = GridBagConstraints.NONE;
        rules.ipadx = 20;
        rules.ipady = 0;
        rules.insets = new Insets(0, 0, 0, 0);
        rules.anchor = GridBagConstraints.CENTER;
        rules.weightx = 0;
        rules.weighty = 0;

        getContentPane().add(new HardwareDeviceInfoPanel(device), rules);

        // Create SpectrumAnalyzerDevice status panel and add it to the content pane

         rules.gridx = 0;
         rules.gridy = 1;
         getContentPane().add(new SpectrumAnalyzerStatusPanel(device), rules);

        // rules.gridx = 0;
        // rules.gridy = 1;
        //
        // // Task buttons
        //
        // for (FixtureTask t : tasks) {
        // logger.debug("Task {}", t.toString());
        // JButton b = new JButton(t.getClass().getSimpleName());
        // taskMap.put(b, t);
        // b.addActionListener(this);
        // newContentPane.add(b, rules);
        // rules.gridx++;
        // if (rules.gridx > 2) {
        // rules.gridx = 0;
        // rules.gridy += 1;
        // }
        // }
    }

    /**
     * @see ws.finson.audiosp.app.gui.DeviceView#setVisible(java.lang.Boolean)
     */
    @Override
    public void setVisible(Boolean b) {
        assert SwingUtilities.isEventDispatchThread() : "DeviceView methods must run on Swing Dispatch Thread only.";
        pack();
        super.setVisible(b);
    }

    /**
     * Start a task thread running. First disable the task buttons, then start a task thread, then
     * set up a thread to listen for the task thread to exit.
     * 
     * @param e
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert SwingUtilities.isEventDispatchThread() : "DeviceView methods must run on Swing Dispatch Thread only.";
        // logger.debug("Action event {}", e.getActionCommand());
        // for (JButton b : taskMap.keySet()) {
        // b.setEnabled(false);
        // }
        // ApplicationComponent task = taskMap.get(e.getSource());
        // Thread taskThread = new Thread(task, "Task-"+task.getClass().getSimpleName());
        // logger.debug("New thread to run {} task.", task.getClass()
        // .getSimpleName());
        // taskThread.start();
        //
        // Runnable helper = new WaitAndEnable(taskThread);
        // Thread helperThread = new Thread(helper,"WaitAndEnable");
        // helperThread.start();
    }

    // --------------

    // /**
    // * This WaitAndEnable class helps the ExecutiveGui class leave the
    // * task buttons disabled while one of the tasks executes. When the
    // * task thread exits, the thread this object is running on wakes up
    // * and schedules a Swing task to re-enable the buttons.
    // *
    // * @author Doug Johnson, 2012
    // */
    // private class WaitAndEnable implements Runnable {
    // private Thread threadToJoin;
    //
    // public WaitAndEnable(Thread t) {
    // threadToJoin = t;
    // }
    //
    // /**
    // *
    // * @see java.lang.Runnable#run()
    // */
    // @Override
    // public void run() {
    // logger.debug("Executive's WaitAndEnable run method waiting for {} to exit.",threadToJoin.getName());
    //
    // // Wait for the task thread to exit
    //
    // while (threadToJoin.isAlive()) {
    // try {
    // threadToJoin.join();
    // } catch (InterruptedException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    //
    // logger.debug("{} has exited.",threadToJoin.getName());
    //
    // // Re-enable the task start buttons
    //
    // javax.swing.SwingUtilities.invokeLater(new Runnable() {
    // public void run() {
    // for (JButton b : taskMap.keySet()) {
    // b.setEnabled(true);
    // }
    // }
    // });
    //
    // }
    // }

}
