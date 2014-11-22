package ws.finson.wifix.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * processing. This BuildSensorBranch reads a DAP scan sequence branch and redistributes the data
 * into a sensor sequence branch. Capture scan data is structured scan by scan, whereas sensor data
 * is structured sensor by sensor.
 * 
 * @author Doug Johnson, Nov 18, 2014
 * 
 */
public class BuildParameters implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Element> sensorElementMap = new HashMap<>();
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
                parameterDefinitions.add(ac
                        .getApplication()
                        .getConfig()
                        .getInstanceUsingFactory(BasicParameter.class, sectionElement,
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

        Element sensorSequenceElement = in.getRootElement().getFirstChildElement("sensor-sequence");
        Element scanSequenceElement = in.getRootElement().getFirstChildElement("scan-sequence");

        // How many scans?

        Nodes scanNodes = scanSequenceElement.query("scan");
        logger.debug("scan count: {}", scanNodes.size());

        // Repeat the following steps in order for each requested parameter

        for (ParameterFunction param : parameterDefinitions) {

            // Prepare for single vs multi-value parameter

            Set<String> groupKeys;
            if (param.getKeyFieldName() == null) {
                groupKeys = new HashSet<>(1);
                groupKeys.add("");
            } else {

                // collect all the primary key value nodes in the scan data

                Nodes result = scanSequenceElement.query("scan/scan-values[@field='"
                        + param.getKeyFieldName() + "']/value");
                logger.debug("Total primary key ({}) occurences: {}", param.getKeyFieldName(),
                        result.size());

                // and eliminate the duplicates

                groupKeys = new HashSet<>();
                for (int idx = 0; idx < result.size(); idx++) {
                    String id = result.get(idx).getValue();
                    groupKeys.add(id);
                }
            }
            logger.debug("Unique primary key values: {}", groupKeys.size());

            // Prepare the (key_count) x (scan_count) Map matrix

            Map<String, String[]> matrix = new HashMap<>(groupKeys.size());

            // For each key value, put a "0"-filled value array in the matrix

            for (String aKey : groupKeys) {
                String[] vals = new String[scanNodes.size()];
                Arrays.fill(vals, "0");
                matrix.put(aKey, vals);
            }

            // 3. Re-organize the values from each scan into groups, compute, then store a single
            // value per
            // group into the matrix

            for (int scanIndex = 0; scanIndex < scanNodes.size(); scanIndex++) {

                // Get the data values from the scan

                Nodes sensorValueNodes = scanNodes.get(scanIndex).query(
                        "scan-values[@field='" + param.getXArgument() + "']/value");
                logger.trace("sensor '{}' value count: {}", param.getName(),
                        sensorValueNodes.size());

                // and group them as requested

                Map<String, List<String>> grouper = new HashMap<>(groupKeys.size());
                if (param.getKeyFieldName() == null) { // put all values in the scan in one group
                    List<String> valueList = new ArrayList<>(sensorValueNodes.size());
                    for (int idx = 0; idx < sensorValueNodes.size(); idx++) {
                        valueList.add(sensorValueNodes.get(idx).getValue());
                    }
                    grouper.put("", valueList);
                } else { // put values into several groups by their associated key value
                    Nodes keyValueNodes = scanNodes.get(scanIndex).query(
                            "scan-values[@field='" + param.getKeyFieldName() + "']/value");
                    logger.trace("primary key count: {}", keyValueNodes.size());

                    assert (keyValueNodes.size() == sensorValueNodes.size()) : "Each data value element must have an associated key value element.";

                    for (int idx = 0; idx < sensorValueNodes.size(); idx++) {
                        logger.trace("{} {} {}", idx, keyValueNodes.get(idx).getValue(),
                                sensorValueNodes.get(idx).getValue());
                        List<String> valueList = grouper.get(keyValueNodes.get(idx).getValue());
                        if (valueList == null) {
                            valueList = new ArrayList<>();
                            grouper.put(keyValueNodes.get(idx).getValue(), valueList);
                        }
                        valueList.add(sensorValueNodes.get(idx).getValue());
                    }

                }

                // Calculate the single value that represents each group then
                // store it in the matrix that holds the function result for each group for
                // each scan for this parameter

                for (String grp : grouper.keySet()) {
                    List<String> groupValueList = grouper.get(grp);

                    // ------------
                    // apply the requested function to generate a single value for this group for
                    // this scan
                    
                    String theResultValue;                     
                    String f =  param.getXFunction().toUpperCase();
                   if (f.equals("COUNT")) {
                       theResultValue = Integer.toString(groupValueList.size()); 
                   } else if (f.equals("MAX")) {
                       int mark = Integer.MIN_VALUE;
                       for (String v : groupValueList) {
                           int current = Integer.parseInt(v);
                           mark = Math.max(mark,current);
                       }
                       theResultValue = Integer.toString(mark);
                   } else if (f.equals("MIN")) {
                       int mark = Integer.MAX_VALUE;
                       for (String v : groupValueList) {
                           int current = Integer.parseInt(v);
                           mark = Math.min(mark,current);
                       }
                       theResultValue = Integer.toString(mark);
                   } else if (f.equals("SUM")) {
                       int total = 0;
                       for (String v : groupValueList) {
                           total += Integer.parseInt(v);
                       }
                       theResultValue = Integer.toString(total);
                   } else {
                       theResultValue = param.getXFunction() + "??";
                   }
                   // ------------

                    String[] valueArray = matrix.get(grp);
                    valueArray[scanIndex] = theResultValue;
                }
            }

            // create and attach a twig for this parameter

            Element parameterElement = new Element("parameter");
            parameterElement.addAttribute(new Attribute("name", param.getName()));
            if (param.getKeyFieldName() != null) {
                parameterElement.addAttribute(new Attribute("by", param.getKeyFieldName()));
            }

            for (String key : matrix.keySet()) {
                Element valueContainer = new Element("sensor-values");
                if (!key.isEmpty()) {
                    valueContainer.addAttribute(new Attribute("key", key));
                }
                String[] sensorValues = matrix.get(key);
                for (int scanIndex = 0; scanIndex < sensorValues.length; scanIndex++) {
                    Element ve = new Element("value");
                    ve.appendChild(sensorValues[scanIndex]);
                    valueContainer.appendChild(ve);
                }
                parameterElement.appendChild(valueContainer);
            }

            // attach the new sensor twig to the sensor-sequence branch

            sensorSequenceElement.appendChild(parameterElement);

        }
        return in;
    }
}

//
// // What are the names of the existing sensor measurements?
//
// Element sensorSequenceElement =
// in.getRootElement().getFirstChildElement("sensor-sequence");
// Nodes sensorNodes = sensorSequenceElement.query("sensor");
// if (sensorNodes.size() == 0) {
// logger.warn("The input sensor branch does not contain any sensor elements.  No parameter branch will be constructed.");
// return in;
// }
//
// for (int idx = 0; idx < sensorNodes.size(); idx++) {
// String sensorName = ((Element) sensorNodes.get(idx)).getAttributeValue("name");
// sensorElementMap.put(sensorName, (Element) sensorNodes.get(idx));
// }
//
// if (logger.isDebugEnabled()) {
// String label = ((Element) sensorNodes.get(0)).getAttributeValue("name");
// StringBuilder sbldr = new StringBuilder(label);
// for (int idx = 1; idx < sensorNodes.size(); idx++) {
// label = ((Element) sensorNodes.get(idx)).getAttributeValue("name");
// sbldr.append(", ");
// sbldr.append(label);
// }
// logger.debug("Available sensors: {}", sbldr.toString());
// }
//
// assert sensorElementMap.size() == sensorNodes.size() :
// "Sensor node names must be unique.";

// Evaluate each parameter expression. If there are multiple scan-sequences, do it for
// each of them.

// Elements scanSequenceElements =
// in.getRootElement().getChildElements("scan-sequence");
// for (int idx = 0; idx < scanSequenceElements.size(); idx++) {
// for (BasicParameter param : parameterDefinitions) {
// Element parameterElement = param.twig(scanSequenceElements.get(idx));
// if (parameterElement != null) {
// sensorSequenceElement.appendChild(parameterElement);
// }
// }
// }

