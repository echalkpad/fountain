package ws.finson.audiosp.app.device;

/*
 * RXTX binary builds provided as a courtesy of Mfizz Inc. (http://mfizz.com/).
 * Please see http://mfizz.com/oss/rxtx-for-java for more information.
 */
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
    private BufferedReader deviceReader;
    private BufferedWriter deviceWriter;

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

        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
        if (!ports.hasMoreElements()) {
            throw new ConfigurationException(
                    "No comm ports found by CommPortIdentifier.getPortIdentifiers().");
        }
        portID = null;
        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
            logger.trace("Known comm port: {}", curPort.getName());
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portName.equalsIgnoreCase(curPort.getName())) {
                    portID = curPort;
                }
            }
        }
        if (portID == null) {
            throw new ConfigurationException("The serial port '" + portName + "' is not found.");
        }

        // Connect to the device

        try {
            thePort = (SerialPort) portID.open(this.getClass().getName(), 1000);
            thePort.setSerialPortParams(38400, 8, 1, SerialPort.PARITY_NONE);
            deviceReader = new BufferedReader(new InputStreamReader(thePort.getInputStream()));
            deviceWriter = new BufferedWriter(new OutputStreamWriter(thePort.getOutputStream()));
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * 
     * @see ws.finson.audiosp.app.device.AbstractHardwareDevice#readDeviceParameterValue(java.lang.String)
     */
    @Override
    protected Object readDeviceParameterValue(String pname) throws IOException {
        if (deviceParameterMap.containsKey(pname)) {
            deviceWriter.write("GET " + pname + ";");
            deviceWriter.flush();
            while (!deviceReader.ready()) {
            }

            String response = deviceReader.readLine();
            if (response == null) {
                throw new EOFException(
                        "Unexpected null response (EOF) while reading pname from the device on "
                                + thePort.getName() + ".");
            }
            try {
                DeviceParameter p = deviceParameterMap.get(pname);
                deviceParameterValues.put(pname, p.getType().getConstructor(String.class)
                        .newInstance(response));
            } catch (SecurityException | IllegalArgumentException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IOException(e);
            }
        }
        return deviceParameterValues.get(pname);
    }

    /**
     * @see ws.finson.audiosp.app.device.AbstractHardwareDevice#writeDeviceParameterValue(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    protected void writeDeviceParameterValue(String pname, Object pvalue) throws IOException {
        throw new IOException("Cannot write parameter values to device " + getDeviceName());
    }

    /**
     * @throws IOException
     * @see ws.finson.audiosp.app.device.SpectrumAnalyzerDevice#getMagnitudes()
     */
    @Override
    public List<List<Double>> getMagnitudes() throws IOException {
        String response;
        int channelCount = (Integer) getParameterValue("AUDIO_CHANNEL_COUNT");
        int FFTSize = (Integer) getParameterValue("FFT_SIZE");

        List<List<Double>> result = new ArrayList<List<Double>>(channelCount);

        deviceWriter.write("GET MAGNITUDES;");
        deviceWriter.flush();

        for (int cn = 0; cn < channelCount; cn++) {
            result.add(new ArrayList<Double>(FFTSize));
            for (int bin = 0; bin < FFTSize; bin++) {
                // long startTime = System.currentTimeMillis();
                while (!deviceReader.ready()) {

                }
                // long deltaT = System.currentTimeMillis() - startTime;
                // if (deltaT > 0) {
                // System.out.println("Not ready delta ms: "+Long.toString(deltaT));
                // }

                response = deviceReader.readLine();
                if (response == null) {
                    throw new EOFException(
                            "Unexpected null response (EOF) while reading MAGNITUDES from the device on "
                                    + thePort.getName() + ".");
                }
                try {
                    result.get(cn).add(Double.parseDouble(response));
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
            }
        }
        return result;
    }

}
