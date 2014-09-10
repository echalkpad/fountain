package ws.finson.audiosp.app.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.AbstractComponent;
import ws.tuxi.lib.cfg.Application;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * This application component provides an interface layer between the main thread that reads the
 * configuration file and the Swing thread that actually implements a GUI. A key capability is
 * that processing of the enclosed configuration elements (presumably calling for Swing-based classes)
 * is performed on the Swing Event Dispatch Thread, not the main process thread. The activity is
 * thread safe, only because the main thread blocks and waits until the processing of each
 * subordinate element is done.
 * 
 * @author Doug Johnson
 * @since Aug 27, 2014
 * 
 */
public class SwingStarter extends AbstractComponent {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<DeviceView> views;
    private Element currentElement; // communicate from main thread to Swing Event Dispatch Thread

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

        // Process each of the subordinate configuration sections

        views = new ArrayList<>();
        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            currentElement = sectionElements.get(idx);
            switch (currentElement.getLocalName()) {
            case "view":
                Runnable delegate = new Runnable() {
                    public void run() {
                        try {
                            views.add(getApplication().getConfig().getInstanceUsingFactory(DeviceView.class,
                                    currentElement, new Object[] { SwingStarter.this, currentElement }));
                        } catch (ConfigurationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                try {
                    SwingUtilities.invokeAndWait(delegate);
                } catch (InvocationTargetException | InterruptedException e) {
                    throw new ConfigurationException(e);
                }
                break;
            default:
                logger.warn("Skipping <{}> element. Element not recognized.",
                        currentElement.getLocalName());
                break;
            }
        }
    }

    /**
     * Give the views a chance to finish linking to data sources now that
     * everything has been constructed.
     * 
     * @see ws.tuxi.lib.cfg.AbstractComponent#preRun()
     */
    @Override
    public void preRun() throws ConfigurationException {
        logger.info("preRun method in {}", getClass().getSimpleName());
        Runnable delegate = new Runnable() {
            public void run() {
                for (DeviceView v : views) {
                    try {
                        v.setDataSource();
                    } catch (ConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        try {
            SwingUtilities.invokeAndWait(delegate);
        } catch (InvocationTargetException | InterruptedException e) {
            throw new ConfigurationException(e);
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
        Runnable delegate = new Runnable() {
            public void run() {
                for (DeviceView v : views) {
                    ((JFrame)v).pack();
                    ((JFrame)v).setVisible(true);
                }
            }
        };
        try {
            javax.swing.SwingUtilities.invokeAndWait(delegate);
        } catch (InvocationTargetException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
