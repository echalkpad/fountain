package ws.finson.audiosp.app;

import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.AbstractComponent;
import ws.tuxi.lib.cfg.Application;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * Find classes with the specified annotation(s) on one or more of the instance methods.
 * 
 * @author Doug Johnson, Jul 28, 2013
 * 
 */
public class AudioSp extends AbstractComponent {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private PathSet searcher;
    private PathReport reporter;

    /**
     * Initialize this instance, setting parameters according to the configuration file.
     * 
     * @param app
     *            the Application for which this is an ApplicationComponent
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     */
    public AudioSp(Application app, Element cE) throws ConfigurationException {
        super(app,cE);
        logger.trace("Constructor of {}", this.getClass().getSimpleName());

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            switch (sectionElement.getLocalName()) {
            case "pathset":
                searcher = app.getConfig().getInstanceUsingFactory(PathSet.class, sectionElement,
                        new Object[] { this, sectionElement });
                break;
            case "report":
                reporter = app.getConfig().getInstanceUsingFactory(PathReport.class, sectionElement,
                        new Object[] { this, sectionElement });
                break;
            default:
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
                break;
            }
        }
        if (searcher == null) {
            throw new ConfigurationException("A resourceset element must be specified.");
        }
        if (reporter == null) {
            throw new ConfigurationException("A report element must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.cfg.ApplicationComponent#preRun()
     */
    @Override
    public void preRun() throws ConfigurationException {
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        reporter.printResourceSet(searcher);
    }

}
