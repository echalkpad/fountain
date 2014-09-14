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

    private final DeviceParameter[] ini = {
            new DeviceParameter("FFT_SIZE", "FFT Size", true, Integer.class,true),
            new DeviceParameter("SAMPLE_RATE_HZ", "Sample rate (Hz)", true, Integer.class,true),
            new DeviceParameter("AUDIO_CHANNEL_COUNT", "Channel Count", true, Integer.class,true) };

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
        deviceClass = DeviceClassID.SpectrumAnalyzer;
        
        // Save the parameter descriptors

        for (int idx = 0; idx < ini.length; idx++) {
            deviceParameterMap.put(ini[idx].getName(), ini[idx]);
        }
        
        // Save any initial values provided in the setup

        int attributeCount = cE.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Attribute a = cE.getAttribute(i);
            if (deviceParameterMap.containsKey(a.getLocalName())) {
                String s = a.getValue();
                try {
                    DeviceParameter p = deviceParameterMap.get(a);
                    Constructor<?> c;
                    if (p.getWritable()) {
                        c = p.getType().getConstructor(String.class);
                        deviceParameterValues.put(s, c.newInstance(s));
                    } else {
                        throw new ReadOnlyParameterException("'"+p.getName()+"' is read-only and cannot be set.");
                    }
                } catch (SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | ReadOnlyParameterException e) {
                    throw new ConfigurationException(e);
                }
            }
        }
    }

    /**
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#getFFTSize()
     */
    @Override
    public Integer getFFTSize() {
        return (Integer) getParameterValue("FFT_SIZE");
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
