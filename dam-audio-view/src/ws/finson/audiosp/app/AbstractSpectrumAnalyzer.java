/**
 * 
 */
package ws.finson.audiosp.app;


/**
 * @author Doug Johnson
 * @since August 2014
 *
 */
public abstract class AbstractSpectrumAnalyzer implements SpectrumAnalyzer {
	private String name;
	protected int FFTSize;
	protected int sampleRate;
	protected int channelCount;
	
	public AbstractSpectrumAnalyzer(String name, int size, int rate, int channels) {
		this.name = name;
		this.FFTSize = size;
		this.sampleRate = rate;
		this.channelCount = channels;
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#getFFTSize()
	 */
	@Override
	public int getFFTSize() {
		return FFTSize;
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#setFFTSize(int)
	 */
	@Override
	public void setFFTSize(int size) {
		throw new UnsupportedOperationException("Cannot set FFT size for device "+name);
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#getSampleRate()
	 */
	@Override
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#setSampleRate(int)
	 */
	@Override
	public void setSampleRate(int frequency) {
		throw new UnsupportedOperationException("Cannot set sample rate for device "+name);
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#getChannelCount()
	 */
	@Override
	public int getChannelCount() {
		return channelCount;
	}

	/**
	 * @see ws.finson.audiosp.app.SpectrumAnalyzer#setChannelCount(int)
	 */
	@Override
	public void setChannelCount(int count) {
		throw new UnsupportedOperationException("Cannot set channel count for device "+name);
	}
}
