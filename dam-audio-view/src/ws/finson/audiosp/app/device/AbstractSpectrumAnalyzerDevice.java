/**
 * 
 */
package ws.finson.audiosp.app.device;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.finson.audiosp.app.device.HardwareDevice.ClassID;
import ws.finson.audiosp.app.device.HardwareDevice.Parameter;
import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * @author Doug Johnson
 * @since August 2014
 * 
 */
public abstract class AbstractSpectrumAnalyzerDevice extends AbstractHardwareDevice implements
        SpectrumAnalyzerDevice {
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Parameter[] ini = {
            new Parameter("FFT_SIZE", "FFT Size", true, new Integer(256)),
            new Parameter("SAMPLE_RATE_HZ", "Sample rate (Hz)", true, new Integer(10000)),
            new Parameter("AUDIO_CHANNEL_COUNT", "Channel Count", true, new Integer(1)) };

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
        super(ac, cE);
        deviceClass = HardwareDevice.ClassID.SpectrumAnalyzer;

        for (int idx = 0; idx < ini.length; idx++) {
            deviceParameterMap.put(ini[idx].getName(), ini[idx]);
            deviceParameterValues.put(ini[idx].getName(), ini[idx].getInitialValue());
        }

        int attributeCount = cE.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Attribute a = cE.getAttribute(i);
            if (deviceParameterMap.containsKey(a)) {
                String s = a.getValue();
                try {
                    Parameter p = deviceParameterMap.get(a);
                    Constructor<?> c;
                    if (p != null) {
                        c = p.getType().getConstructor(String.class);
                        deviceParameterValues.put(s, c.newInstance(s));
                    }
                } catch (SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException e) {
                    throw new ConfigurationException(e);
                }
            }
        }

        // int attributeCount = cE.getAttributeCount();
        // for (int i = 0; i < attributeCount; i++) {
        // int temp;
        // Attribute a = cE.getAttribute(i);
        // switch (a.getLocalName()) {
        // case "size":
        // try {
        // temp = Integer.valueOf(a.getValue());
        // } catch (NumberFormatException e) {
        // throw new ConfigurationException("Invalid size format: " + a.getValue(), e);
        // }
        // if (temp < 16 || temp > 1024) {
        // throw new ConfigurationException("Invalid size value: " + temp);
        // }
        //
        // break;
        // case "rate":
        // try {
        // this.sampleRate = Integer.valueOf(a.getValue());
        // } catch (NumberFormatException e) {
        // throw new ConfigurationException("Invalid sample rate format: " + a.getValue(),
        // e);
        // }
        // if (sampleRate < 1 || sampleRate > 40000) {
        // throw new ConfigurationException("Invalid sample rate value: " + sampleRate);
        // }
        // break;
        // case "channels":
        // try {
        // this.channelCount = Integer.valueOf(a.getValue());
        // } catch (NumberFormatException e) {
        // throw new ConfigurationException("Invalid channel count format: "
        // + a.getValue(), e);
        // }
        // if (channelCount < 1) {
        // throw new ConfigurationException("Invalid channel count value: " + channelCount);
        // }
        // break;
        // default:
        // break;
        // }
        // }
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#getFFTSize()
     */
    @Override
    public Integer getFFTSize() {
        return (Integer) getParameterValue("FFT_SIZE");
    }

    /**
     * @see ws.finson.audiosp.app.device.HardwareDevice#getDeviceClass()
     */
    @Override
    public HardwareDevice.ClassID getDeviceClass() {
        return HardwareDevice.ClassID.SpectrumAnalyzer;
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#getSampleRate()
     */
    @Override
    public Integer getSampleRate() {
        return (Integer) getParameterValue("SAMPLE_RATE_HZ");
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#getChannelCount()
     */
    @Override
    public Integer getChannelCount() {
        return (Integer) getParameterValue("AUDIO_CHANNEL_COUNT");
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#setFFTSize(int)
     */
    @Override
    public void setFFTSize(int size) {
        throw new UnsupportedOperationException("Cannot set FFT size for device " + getDeviceName());
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#setSampleRate(int)
     */
    @Override
    public void setSampleRate(int frequency) {
        throw new UnsupportedOperationException("Cannot set sample rate for device "
                + getDeviceName());
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#setChannelCount(int)
     */
    @Override
    public void setChannelCount(int count) {
        throw new UnsupportedOperationException("Cannot set channel count for device "
                + getDeviceName());
    }
}
