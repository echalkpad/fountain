package ws.finson.wifix.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This BuildAnalysisTree reads a DAP capture tree and outputs a DAP analysis tree. Capture data is
 * structured scan by scan, whereas analysis data is structured sensor by sensor.
 * 
 * @author Doug Johnson, Nov 14, 2014
 * 
 */
public class BuildAnalysisTree implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String primaryKeyName = null;
    private String parameterName = null;

    /**
     * @param ac
     *            the containing ApplicationComponent
     * @param cE
     *            the Element from the config file that defines this object
     * @throws IOException
     * @throws ConfigurationException
     */
    public BuildAnalysisTree(ApplicationComponent ac, Element cE) throws IOException,
            ConfigurationException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("primary-key".equals(sectionElement.getLocalName())) {
                if (primaryKeyName != null) {
                    logger.warn("Ignoring extra <{}> definition, only one is allowed.",
                            sectionElement.getLocalName());
                } else {
                    primaryKeyName = sectionElement.getValue();
                }
            } if ("parameter".equals(sectionElement.getLocalName())) {
                if (parameterName != null) {
                    logger.warn("Ignoring extra <{}> definition, only one is allowed.",
                            sectionElement.getLocalName());
                } else {
                    parameterName = sectionElement.getValue();
                }
                
            } else {
            
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (primaryKeyName == null) {
            throw new ConfigurationException("Name of the primary (unique) key must be specified.");
        }
        if (parameterName == null) {
            throw new ConfigurationException("Name of the parameter must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document in) throws PipelineOperationException {

        Element scanSequenceElement = in.getRootElement().getFirstChildElement("scan-sequence");

        // collect all the primary key occurrences

        Nodes result = scanSequenceElement.query("scan/"+primaryKeyName+"/value");
        logger.debug("Total primary key ({}) occurences: {}", primaryKeyName,result.size());

        // and eliminate the duplicates

        Set<String> keySet = new HashSet<>(result.size());
        for (int idx = 0; idx < result.size(); idx++) {
            String id = result.get(idx).getValue();
            keySet.add(id);
        }
        logger.debug("Unique primary key values: {}", keySet.size());

        // How many scans?

        Nodes scanNodes = scanSequenceElement.query("scan");
        logger.debug("scan count: {}", scanNodes.size());
        
        // For the timetag data, create a 0-filled array

        String[] timeValues = new String[scanNodes.size()];
        Arrays.fill(timeValues, "0");

        // Prepare the (scan_count) x (key_count) Map matrix

        Map<String, String[]> matrix = new HashMap<>(keySet.size());

        // For each primary key value, put a 0-filled sensor value array in the Map

        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String[] vals = new String[scanNodes.size()];
            Arrays.fill(vals, "0");
            matrix.put(iter.next(), vals);
        }

        // Consolidate the values from the scan element groups into the sensor arrays in the map

        for (int scanIndex = 0; scanIndex < scanNodes.size(); scanIndex++) {
            
            // First, store the time tag of this scan
            
            Node timetagElement = (Element) scanNodes.get(scanIndex).query("timetag").get(0);
            timeValues[scanIndex] = timetagElement.getValue();
            logger.trace("Time tag value: {}", timeValues[scanIndex]);
            
            // Next, store data values from the scan into the sensor arrays in the Map 

            Nodes keyValueNodes = scanNodes.get(scanIndex).query(primaryKeyName+"/*");
            Nodes parameterValueNodes = scanNodes.get(scanIndex).query(parameterName+"/*");

            logger.trace("primary key count: {}", keyValueNodes.size());
            logger.trace("parameter value count: {}", parameterValueNodes.size());
            
            assert (keyValueNodes.size() == parameterValueNodes.size()) : "key count and value count must be equal." ;

            for (int sensorIndex = 0; sensorIndex < parameterValueNodes.size(); sensorIndex++) {
                logger.trace("{} {} {}", sensorIndex, keyValueNodes.get(sensorIndex).getValue(),
                        parameterValueNodes.get(sensorIndex).getValue());
                String[] valueArray = matrix.get(keyValueNodes.get(sensorIndex).getValue());
                valueArray[scanIndex] = parameterValueNodes.get(sensorIndex).getValue();
            }
        }

        // write the timetag array to the analysis tree

        Element sensorSequenceElement = new Element("sensor-sequence");

        Element f = new Element("column");
        Attribute m = new Attribute("label","timetag");
        f.addAttribute(m);
        for (int scanIndex=0; scanIndex < timeValues.length; scanIndex++) {
            Element ve = new Element("value");
            ve.appendChild(timeValues[scanIndex]);
            f.appendChild(ve);
        }
        sensorSequenceElement.appendChild(f);

        // write the parameter arrays from the map to the analysis tree
        
        for (String key : matrix.keySet()) {
            f = new Element("column");
            m = new Attribute("label", key.replace(':', '-'));
            f.addAttribute(m);
            String[] parameterValues = matrix.get(key);
            for (int scanIndex = 0; scanIndex < parameterValues.length; scanIndex++) {
                Element ve = new Element("value");
                ve.appendChild(parameterValues[scanIndex]);
                f.appendChild(ve);
            }
            sensorSequenceElement.appendChild(f);
        }
        Element root = new Element("analysis");
        root.appendChild(sensorSequenceElement);

        Document analysisTree = new Document(root);
        return analysisTree;
    }

}
