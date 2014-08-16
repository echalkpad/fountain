/**
 * 
 */
package ws.finson.audiosp.app;

import java.util.List;

import nu.xom.Element;
import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * @author Doug Johnson
 * @since Aug 15, 2014
 *
 */
public class SerialSpectrumAnalyzer extends AbstractSpectrumAnalyzer {

	public SerialSpectrumAnalyzer(ApplicationComponent ac, Element cE) throws ConfigurationException {
        super(ac, cE);
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#open()
	 */
	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#getMagnitudes()
	 */
	@Override
	public List<Double> getMagnitudes() {
		// TODO Auto-generated method stub
		return null;
	}

}
