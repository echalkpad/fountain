/**
 * Where does this javadoc text go?
 */
package ws.finson.audiosp.app;

import java.util.List;

/***
 * Identify the methods that a class must implement in order to fill the role of
 * a spectrum analyzer.
 * 
 * Historical note:  These methods are copied directly from the definition that 
 * Tony DiCola (tony@tonydicola.com) used in his sample Python code from the Adafruit 
 * tutorial FFT: Fun with Fourier Transforms.
 * @see <a href="https://learn.adafruit.com/fft-fun-with-fourier-transforms/">https://learn.adafruit.com/fft-fun-with-fourier-transforms/</a>
 * 
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public interface SpectrumAnalyzer {
	/**
	 * Open communication with the device.
	 */
	void open();
	/**
	 * Close communication with the device.
	 */
	void close();
	/**
	 * Return name of the device.
	 */
	String getName();

	/**
	 * Return device's FFT size.
	 */
	int getFFTSize();

	/**
	 * Set the device's FFT size to the specified number of bins.
	 */
	void setFFTSize(int size);

	/**
	 * Return device's sample rate in hertz.
	 */
	int getSampleRate();

	/**
	 * Set the device's sample rate to the specified frequency in hertz.
	 */
	void setSampleRate(int frequency);

	/**
	 * Return number of channels the device is analyzing.
	 */
	int getChannelCount();

	/**
	 * Set the number of channels the caller would like to have analyzed.
	 */
	void setChannelCount(int count);
	
	/**
	 * Return a list of magnitudes from an FFT run on the device. The size of
	 * the returned magnitude list should be the sample as the device's FFT
	 * size.
	 */
	List<Double> getMagnitudes();

}
