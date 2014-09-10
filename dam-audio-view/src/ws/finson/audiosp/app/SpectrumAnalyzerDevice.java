package ws.finson.audiosp.app;

import java.io.IOException;
import java.util.List;

/***
 * Identify the methods that a class must implement in order to fill the role of
 * a spectrum analyzer device.
 * 
 * Historical note:  Most of these method signatures were originally copied directly from the 
 * definition that Tony DiCola (tony@tonydicola.com) used in his sample Python code from the Adafruit 
 * tutorial FFT: Fun with Fourier Transforms.
 * @see <a href="https://learn.adafruit.com/fft-fun-with-fourier-transforms/">https://learn.adafruit.com/fft-fun-with-fourier-transforms/</a>
 * 
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public interface SpectrumAnalyzerDevice extends HardwareDevice {
	/**
	 * Return device's FFT size.
	 * @return  Number of entries in the FFTs being performed.
	 */
	Integer getFFTSize();

	/**
	 * Set the device's FFT size to the specified number of bins.
	 */
	void setFFTSize(int size);

	/**
	 * Return device's sample rate in hertz.
	 */
	Integer getSampleRate();

	/**
	 * Set the device's sample rate to the specified frequency in hertz.
	 */
	void setSampleRate(int frequency);

	/**
	 * Return number of channels the device is analyzing.
	 */
	Integer getChannelCount();

	/**
	 * Set the number of channels the caller would like to have analyzed.
	 */
	void setChannelCount(int count);
	
	/**
	 * Return a list of magnitudes from an FFT run on the device. The size of
	 * the returned magnitude list should be the same as the device's FFT
	 * size.
	 * @throws IOException 
	 */
	List<List<Double>> getMagnitudes() throws IOException;

}
