/**
 * 
 */
package ws.finson.audiosp.app;

import java.util.List;

/**
 * @author Doug Johnson
 * @since Aug 15, 2014
 *
 */
public class SerialSpectrumAnalyzer extends AbstractSpectrumAnalyzer {

	public SerialSpectrumAnalyzer(String name, int size, int rate, int channels) {
		super(name, size, rate, channels);
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
