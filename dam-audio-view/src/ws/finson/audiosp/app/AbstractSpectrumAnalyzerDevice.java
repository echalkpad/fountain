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
public abstract class AbstractSpectrumAnalyzerDevice extends AbstractHardwareDevice implements SpectrumAnalyzerDevice {
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected int FFTSize;
    protected int sampleRate;
    protected int channelCount;

    /**
     * Initialize the common fields of a SpectrumAnalyzerDevice using the attributes given in the
     * device configuration element. Attributes and child elements specific to a subclass should be
     * dealt with in the subclass constructor after this constructor has returned.
     * 
     * @param ac
     *            the ApplicationComponent for which this object is being constructed
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     */
    public AbstractSpectrumAnalyzerDevice(ApplicationComponent ac, Element cE)
            throws ConfigurationException {
        super(ac,cE);
        
        deviceParameters.add(new Parameter("FFT_SIZE","FFT Size",true));
        deviceParameters.add(new Parameter("SAMPLE_RATE_HZ","Sample rate (Hz)",true));
        deviceParameters.add(new Parameter("AUDIO_CHANNEL_COUNT","Channel Count",false));

        this.FFTSize = 256;
        this.sampleRate = 10000;
        this.channelCount = 1;

        int attributeCount = cE.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Attribute a = cE.getAttribute(i);
            switch (a.getLocalName()) {
            case "size":
                try {
                    this.FFTSize = Integer.valueOf(a.getValue());
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Invalid size format: " + a.getValue(), e);
                }
                if (FFTSize < 16 || FFTSize > 1024) {
                    throw new ConfigurationException("Invalid size value: " + FFTSize);
                }
                break;
            case "rate":
                try {
                    this.sampleRate = Integer.valueOf(a.getValue());
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Invalid sample rate format: " + a.getValue(),
                            e);
                }
                if (sampleRate < 1 || sampleRate > 40000) {
                    throw new ConfigurationException("Invalid sample rate value: " + sampleRate);
                }
                break;
            case "channels":
                try {
                    this.channelCount = Integer.valueOf(a.getValue());
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Invalid channel count format: "
                            + a.getValue(), e);
                }
                if (channelCount < 1) {
                    throw new ConfigurationException("Invalid channel count value: " + channelCount);
                }
               break;
            default:
                break;
            }
        }
    }


    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#getFFTSize()
     */
    @Override
    public int getFFTSize() {
        return FFTSize;
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#setFFTSize(int)
     */
    @Override
    public void setFFTSize(int size) {
        throw new UnsupportedOperationException("Cannot set FFT size for device " + getDeviceName());
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#getSampleRate()
     */
    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#setSampleRate(int)
     */
    @Override
    public void setSampleRate(int frequency) {
        throw new UnsupportedOperationException("Cannot set sample rate for device " + getDeviceName());
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#getChannelCount()
     */
    @Override
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#setChannelCount(int)
     */
    @Override
    public void setChannelCount(int count) {
        throw new UnsupportedOperationException("Cannot set channel count for device " + getDeviceName());
    }

    /**
     * @see ws.finson.audiosp.app.HardwareDevice#getDeviceClass()
     */
    @Override
    public HardwareDevice.ClassID getDeviceClass() {
        return HardwareDevice.ClassID.SpectrumAnalyzer;
    }
}
