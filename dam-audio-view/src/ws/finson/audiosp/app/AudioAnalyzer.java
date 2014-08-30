package ws.finson.audiosp.app;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.AbstractComponent;
import ws.tuxi.lib.cfg.Application;
import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * Control an attached audio FFT device.
 * 
 * @author Doug Johnson
 * @since August 2014
 */
public class AudioAnalyzer extends AbstractComponent implements DeviceRack {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<SpectrumAnalyzerDevice> devices = new ArrayList<SpectrumAnalyzerDevice>();

    /**
     * Initialize this instance, setting parameters according to the configuration file.
     * 
     * @param app
     *            the Application for which this is an ApplicationComponent
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     */
    public AudioAnalyzer(Application app, Element cE) throws ConfigurationException {
        super(app, cE);
        logger.trace("Constructor of {}", this.getClass().getSimpleName());

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            switch (sectionElement.getLocalName()) {
            case "device":
                devices.add(app.getConfig().getInstanceUsingFactory(SpectrumAnalyzerDevice.class,
                        sectionElement, new Object[] { this, sectionElement }));
                break;
            default:
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
                break;
            }
        }
        if (devices.isEmpty()) {
            throw new ConfigurationException("At least one device element must be specified for an AudioAnalyzer.");
        }
    }

    /**
     * @see ws.tuxi.lib.cfg.ApplicationComponent#preRun()
     */
    @Override
    public void preRun() throws ConfigurationException {
        logger.info("preRun method in {}", getClass().getSimpleName());
//
//        try {
//            devices.open();
//        } catch (IOException e) {
//            throw new ConfigurationException(e);
//        }
//        logger.info("Attached to spectrum analyzer {} on specified port.", devices.getName());
//        logger.info("FFT Size: {}", devices.getFFTSize());
//        logger.info("sample rate: {}", devices.getSampleRate());
//        logger.info("channels: {}", devices.getChannelCount());
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        logger.info("Run method in {}", getClass().getSimpleName());
//        int passCount = 20;
//        long[] deltas = new long[passCount];
//        for (int pass = 0; pass < passCount; pass++) {
//            try {
//                long startTime = System.currentTimeMillis();
//                List<List<Double>> scan = devices.getMagnitudes();
//                deltas[pass] = System.currentTimeMillis() - startTime;
//                for (int idx = 0; idx < devices.getChannelCount(); idx++) {
//                    List<Double> channelHistogram = scan.get(idx);
//                    System.out.print("Channel " + idx);
//                    for (int bin = 0; bin < devices.getFFTSize(); bin++) {
//                        System.out.print(" " + Double.toString(channelHistogram.get(bin)));
//                    }
//                    System.out.println();
//                }
//            } catch (IOException e) {
//                Throwables.printThrowableChain(e, logger);
//            }
//        }
//        
//        System.out.print("Delta ms per pass: ");
//        for (int idx=0; idx<passCount; idx++) {
//            System.out.print(deltas[idx]+" ");
//        }
//        System.out.println();
//        
//        int threadCount = Thread.activeCount();
//        logger.debug("Thread count: {}", Integer.toString(threadCount));
//        Thread[] threads = new Thread[threadCount * 2];
//        Thread.enumerate(threads);
//        for (int idx = 0; idx < threadCount; idx++) {
//            logger.debug(threads[idx].getName() + ", is daemon: " + threads[idx].isDaemon()
//                    + ", is alive: " + threads[idx].isAlive());
//        }
    }

    /**
     * @see ws.finson.audiosp.app.DeviceRack#getDevices()
     */
    @Override
    public List<HardwareDevice> getDevices() {
        return new ArrayList<HardwareDevice>(devices);
    }

}
