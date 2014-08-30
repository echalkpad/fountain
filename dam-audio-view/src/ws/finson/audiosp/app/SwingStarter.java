package ws.finson.audiosp.app;

import java.util.List;

import javax.swing.JFrame;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.AbstractComponent;
import ws.tuxi.lib.cfg.Application;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.cfg.Throwables;

/**
 * This application component provides an interface layer between the main thread that reads the
 * configuration file and the Swing thread that actually implements a GUI.
 * 
 * @author Doug Johnson
 * @since Aug 27, 2014
 * 
 */
public class SwingStarter extends AbstractComponent {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Application parent;
    private SpectrumAnalyzerDevice device;
    private String sourceName;
    private String viewClassName;
    private Class<JFrame> viewClass;

    /**
     * Initialize this instance, setting parameters according to the configuration file.
     * 
     * @param app
     *            the Application for which this is an ApplicationComponent
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     */
    public SwingStarter(Application app, Element cE) throws ConfigurationException {
        super(app, cE);
        logger.trace("Constructor of {}", this.getClass().getSimpleName());

        // Remember the parent Application object so that we can ask it questions later.

        this.parent = app;

        // Process each of the configuration sections

        sourceName = null;
        viewClass = null;
        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            switch (sectionElement.getLocalName()) {
            case "view":
                Attribute a = sectionElement.getAttribute("sourceName");
                if (a == null || a.getValue().length() == 0) {
                    throw new ConfigurationException(
                            "The 'sourceName' attribute and value must be specified for each view element.");
                }
                sourceName = a.getValue();

                a = sectionElement.getAttribute("type");
                if (a == null || a.getValue().length() == 0) {
                    throw new ConfigurationException(
                            "The 'type' attribute and a classsname must be specified for each view element.");
                }
                viewClassName = a.getValue();
                break;

            default:
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
                break;
            }
        }
        if (sourceName == null) {
            throw new ConfigurationException(
                    "A 'view' element with a 'sourceName' attribute and value must be specified.");
        }
        if (viewClassName == null) {
            throw new ConfigurationException(
                    "A 'view' element with a 'type' attribute and value must be specified.");
        }
    }
    /**
     * Find the SpectrumAnalyzerDevice for which we will provide the GUI and remember it.
     * 
     * @see ws.tuxi.lib.cfg.AbstractComponent#preRun()
     */
    @Override
    public void preRun() throws ConfigurationException {
        List<DeviceRack> racks = parent.getApplicationComponents(DeviceRack.class);
        device = null;
        for (DeviceRack r : racks) {
            List<HardwareDevice> devices = r.getDevices();
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
    }

    /**
     * This run() method is invoked when application loading is done and the program is ready to go.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
       logger.info("Run method in {}", getClass().getSimpleName());
       javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createAndShowGUI();
                } catch (ConfigurationException e) {
                    Throwables.printThrowableChain(e, logger, Level.ERROR);
                }
            }
        });
    }
    
    //FIXME The classpath is gone by the time we make the getInstance call below.

    /**
     * Create the GUI and show it. For thread safety, this method must be invoked from the Swing
     * event-dispatching thread.
     * 
     * @throws ConfigurationException
     */
    private void createAndShowGUI() throws ConfigurationException {
        Element tempElement = new Element("view");
        Attribute a = new Attribute("type",viewClassName);
        tempElement.addAttribute(a);
        parent.getConfig().getInstanceUsingFactory(JFrame.class, tempElement,
                new Object[] { device });
    }

}
