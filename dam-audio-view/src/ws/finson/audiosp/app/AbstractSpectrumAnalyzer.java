/**
 * 
 */
package ws.finson.audiosp.app;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public abstract class AbstractSpectrumAnalyzer implements SpectrumAnalyzer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String name;
	protected int FFTSize;
	protected int sampleRate;
	protected int channelCount;

	public AbstractSpectrumAnalyzer(ApplicationComponent ac, Element cE) throws ConfigurationException {
		
		this.name = "";
		this.FFTSize = 256;
		this.sampleRate = 10000;
		this.channelCount = 1;
		
		int attributeCount = cE.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			Attribute a = cE.getAttribute(i);
			switch (a.getLocalName()) {
			case "name":
				this.name = a.getValue();
				if (name == null) {
					name = "";
				}
				logger.trace("name: {}",name);
				break;
			case "size":
				try {
					this.FFTSize = Integer.valueOf(a.getValue());
				} catch (NumberFormatException e) {
					throw new ConfigurationException("Invalid size format: "+a.getValue(),e);
				}
				if (FFTSize < 16 || FFTSize > 1024) {
					throw new ConfigurationException("Invalid size value: "+ FFTSize);
				}
				logger.trace("FFT size: {}", FFTSize);
				break;
			case "rate":
				try {
					this.sampleRate = Integer.valueOf(a.getValue());
				} catch (NumberFormatException e) {
					throw new ConfigurationException("Invalid sample rate format: "+a.getValue(),e);
				}
				if (sampleRate < 1 || sampleRate > 40000) {
					throw new ConfigurationException("Invalid sample rate value: "+sampleRate);
				}
				logger.trace("sample rate: {} {}",sampleRate," Hz");
				break;
			case "channels":
				try {
					this.channelCount = Integer.valueOf(a.getValue());
				} catch (NumberFormatException e) {
					throw new ConfigurationException("Invalid channel count format: "+a.getValue(),e);
				}
				if (channelCount < 1) {
					throw new ConfigurationException("Invalid channel count value: "+channelCount);
				}
				logger.trace("channel count: {}",channelCount);
				break;
			default:
				break;
			}
		}
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
		throw new UnsupportedOperationException(
				"Cannot set FFT size for device " + name);
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
		throw new UnsupportedOperationException(
				"Cannot set sample rate for device " + name);
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
		throw new UnsupportedOperationException(
				"Cannot set channel count for device " + name);
	}
}
