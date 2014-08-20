package ws.finson.audiosp.app;

/*
 * RXTX binary builds provided as a courtesy of Mfizz Inc. (http://mfizz.com/).
 * Please see http://mfizz.com/oss/rxtx-for-java for more information.
 */
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;

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
    private CommPortIdentifier portID;
    private SerialPort thePort;

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
    @SuppressWarnings("unchecked")
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
        Enumeration<CommPortIdentifier> ports = null;
        ports = CommPortIdentifier.getPortIdentifiers();

        portID = null;
        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portName.equalsIgnoreCase(curPort.getName())) {
                    portID = curPort;
                }
                logger.trace("Known serial port: {}", curPort.getName());
            }
        }
        if (portID == null) {
            throw new ConfigurationException("The serial port '" + portName + "' is not found.");
        }

    }

    /**
     * @throws IOException
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#open()
     */
    @Override
    public void open() throws IOException {
        try {
            thePort = (SerialPort) portID.open(this.getClass().getName(), 1000);
            thePort.setSerialPortParams(38400, 8, 1, SerialPort.PARITY_NONE);
        } catch (PortInUseException e) {
            throw new IOException(e);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException(e);
        }
    }

    /**
     * @see ws.finson.audiosp.app.SpectrumAnalyzerDevice#close()
     */
    @Override
    public void close() {
        thePort.close();
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
