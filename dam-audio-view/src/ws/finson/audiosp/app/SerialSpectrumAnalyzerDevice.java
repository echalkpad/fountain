package ws.finson.audiosp.app;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nu.xom.Attribute;
import nu.xom.Element;
import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import jssc.*;

/**
 * Provide a specific implementation of the SpectrumAnalyzerDevice interface to match a serial
 * device.
 * 
 * @author Doug Johnson
 * @since Aug 16, 2014
 * 
 */
public class SerialSpectrumAnalyzerDevice extends AbstractSpectrumAnalyzerDevice {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String portName;
    private SerialPort theSerialPort;

    /**
     * Initialize the fields specific to a SerialSpectrumAnalyzerDevice using the attributes given
     * in the device configuration element.
     * 
     * @param ac
     *            the ApplicationComponent for which this object is being constructed
     * @param cE
     *            the XML configuration element that is provided for this instance
     * @throws ConfigurationException
     * 
     * 
     * @author Doug Johnson
     * @since Aug 15, 2014
     * 
     */
    public SerialSpectrumAnalyzerDevice(ApplicationComponent ac, Element cE)
            throws ConfigurationException {
        super(ac, cE);

        portName = null;

        int attributeCount = cE.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Attribute a = cE.getAttribute(i);
            switch (a.getLocalName()) {
            case "port":
                portName = a.getValue();
                logger.trace("portName: {}", portName);
                break;
            default:
                break;
            }
        }
        if (portName == null) {
            throw new ConfigurationException(
                    "The name of the serial port must be specified and must not be empty.");
        }
        String[] portNames = SerialPortList.getPortNames();
        for (String aPortName : portNames) {
            logger.trace("Known serial port: {}", aPortName);
        }

    }

    /**
     * @throws IOException
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#open()
     */
    @Override
    public void open() throws IOException {
        theSerialPort = new SerialPort(portName);
        // (SerialPort) portID.open(this.getClass().getName(), 1000);
        // thePort.setSerialPortParams(9600, 8, 1, SerialPort.PARITY_NONE);

        try {
            theSerialPort.openPort();// Open serial port
            theSerialPort.setParams(9600, 8, 1, 0);// Set params.
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#getMagnitudes()
     */
    @Override
    public List<Double> getMagnitudes() {
        // TODO Auto-generated method stub
        return null;
    }

}
