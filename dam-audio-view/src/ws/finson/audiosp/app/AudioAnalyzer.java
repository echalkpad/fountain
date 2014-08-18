package ws.finson.audiosp.app;

import java.io.IOException;

import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.AbstractComponent;
import ws.tuxi.lib.cfg.Application;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * Control an audio FFT device and provide a GUI for it.
 * 
 * @author Doug Johnson
 * @since August 2014
 */
public class AudioAnalyzer extends AbstractComponent {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private SpectrumAnalyzerDevice source;

	/**
	 * Initialize this instance, setting parameters according to the
	 * configuration file.
	 * 
	 * @param app
	 *            the Application for which this is an ApplicationComponent
	 * @param cE
	 *            the XML configuration element that is provided for this
	 *            instance
	 * @throws ConfigurationException
	 */
	public AudioAnalyzer(Application app, Element cE)
			throws ConfigurationException {
		super(app, cE);
		logger.trace("Constructor of {}", this.getClass().getSimpleName());

		// Process each of the configuration sections

		Elements sectionElements = cE.getChildElements();
		for (int idx = 0; idx < sectionElements.size(); idx++) {
			Element sectionElement = sectionElements.get(idx);
			switch (sectionElement.getLocalName()) {
			case "device":
				source = app.getConfig().getInstanceUsingFactory(
						SpectrumAnalyzerDevice.class, sectionElement,
						new Object[] { this, sectionElement });
				break;
			// case "report":
			// reporter =
			// app.getConfig().getInstanceUsingFactory(PathReport.class,
			// sectionElement,
			// new Object[] { this, sectionElement });
			// break;
			default:
				logger.warn("Skipping <{}> element. Element not recognized.",
						sectionElement.getLocalName());
				break;
			}
		}
		if (source == null) {
			throw new ConfigurationException(
					"A device element must be specified.");
		}
		// if (reporter == null) {
		// throw new
		// ConfigurationException("A report element must be specified.");
		// }
	}

	/**
	 * @see ws.tuxi.lib.cfg.ApplicationComponent#preRun()
	 */
	@Override
	public void preRun() throws ConfigurationException {
		logger.info("preRun method in {}",getClass().getSimpleName());
		
		try {
		    source.open();
		} catch (IOException e) {
		    throw new ConfigurationException(e);
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.info("Run method in {}",getClass().getSimpleName());
	}

}
