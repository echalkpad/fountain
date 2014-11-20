package ws.finson.wifix.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This BuildParameters class reads a DAP sensor sequence branch and follows instructions to select
 * and process the sensor data to produce a parameter sequence branch. Parameter sequences are
 * structurally similar to sensor sequences, except that parameters may be measured or calculated
 * values, unlike sensors which are considered to be measurement only (although this might get
 * fudged a little bit during data acquisition). Another way to think about the difference is that
 * sensor sequences are captured at run-time, and parameter sequences are generated during post
 * processing.
 *  * This BuildSensorBranch reads a DAP scan sequence branch and redistributes the data into a sensor
 * sequence branch. Capture scan data is structured scan by scan, whereas sensor data is structured
 * sensor by sensor.
 * 
 * @author Doug Johnson, Nov 18, 2014
 * 
 */
public class BuildParameters implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String,Element> sensorElementMap = new HashMap<>();
    private final List<BasicParameter> parameterDefinitions = new ArrayList<>();

    /**
     * @param ac
     *            the containing ApplicationComponent
     * @param cE
     *            the Element from the config file that defines this object
     * @throws IOException
     * @throws ConfigurationException
     */
    public BuildParameters(ApplicationComponent ac, Element cE) throws IOException,
            ConfigurationException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("parameter".equals(sectionElement.getLocalName())) {
                parameterDefinitions.add(ac.getApplication().getConfig().getInstanceUsingFactory(BasicParameter.class, sectionElement,
                        new Object[] { sectionElement }));
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (parameterDefinitions.size() == 0) {
            throw new ConfigurationException("At least one parameter element must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document in) throws PipelineOperationException {
        
        // What are the names of the existing sensor measurements?
        
        Element sensorSequenceElement = in.getRootElement().getFirstChildElement("sensor-sequence");
        Nodes sensorNodes = sensorSequenceElement.query("sensor");
        if (sensorNodes.size() == 0) {
            logger.warn("The input sensor branch does not contain any sensor elements.  No parameter branch will be constructed.");
            return in;
        }

        for (int idx=0; idx<sensorNodes.size(); idx++) {
            String sensorName = ((Element)sensorNodes.get(idx)).getAttributeValue("name");
            sensorElementMap.put(sensorName,(Element)sensorNodes.get(idx));
        }

        if (logger.isDebugEnabled()) {
            String label = ((Element)sensorNodes.get(0)).getAttributeValue("name");
            StringBuilder sbldr = new StringBuilder(label);
            for (int idx=1; idx<sensorNodes.size(); idx++) {
                label = ((Element)sensorNodes.get(idx)).getAttributeValue("name");
                sbldr.append(", ");
                sbldr.append(label);
            }
            logger.debug("Available sensors: {}", sbldr.toString());
        }
        
        assert sensorElementMap.size() == sensorNodes.size() : "Sensor node names must be unique.";

        // Repeat the following steps for each requested parameter expression
        
        ---> call the param function for each scan element

        for (BasicParameter param : parameterDefinitions) {
            String expression = param.expression;
            Element parameterElement = new Element("parameter");
            parameterElement.addAttribute(new Attribute("name",expression));

            // create the new twig 

            Element parameterValuesElement = new Element("parameter-values");
//            for (int scanIndex = 0; scanIndex < sensorValues.length; scanIndex++) {
//                Element ve = new Element("value");
//                ve.appendChild(sensorValues[scanIndex]);
//                parameterValuesElement.appendChild(ve);
//            }
            parameterElement.appendChild(parameterValuesElement);
        

            // attach the new sensor twig to the sensor-sequence branch

            sensorSequenceElement.appendChild(parameterElement);
        }
        return in;
    }
    

}
