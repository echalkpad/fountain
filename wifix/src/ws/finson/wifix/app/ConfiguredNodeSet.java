package ws.finson.wifix.app;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * This ConfiguredNodeSet class manages the details of selecting and labeling a node set for use as
 * a column of values in a Table Layout. The configuration is based on the attributes of a nodes
 * element in the configuration file.
 * 
 * 
 * @author Doug Johnson
 * @since Dec 20, 2014
 * 
 */
public class ConfiguredNodeSet {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String theLabel = null;
    private String theLabelPath = null;
    private String theValuePath = null;

    /**
     * @throws ConfigurationException
     * 
     */
    public ConfiguredNodeSet(Element cE) throws ConfigurationException {

        // Process each of the configuration attributes

        int attributeCount = cE.getAttributeCount();
        for (int idx = 0; idx < attributeCount; idx++) {
            Attribute anAttribute = cE.getAttribute(idx);
            String attributeName = anAttribute.getLocalName();
            logger.trace("Attribute '{}'", attributeName);
            if ("type".equals(attributeName)) {
                // ignore
            } else if ("value-path".equals(attributeName)) {
                theValuePath = anAttribute.getValue();
            } else if ("label-path".equals(attributeName)) {
                theLabelPath = anAttribute.getValue();
            } else if ("label".equals(attributeName)) {
                theLabel = anAttribute.getValue();
            } else {
                logger.warn("Skipping '{}'. Attribute not recognized.", attributeName);
            }
        }
        if (theValuePath == null) {
            throw new ConfigurationException(
                    "A valid xPath expression must be given for the value-path.");
        }
    }

    public Nodes getNodeSet(Document tree) {
        return tree.getRootElement().query(theValuePath);
    }

    public String getLabel(Document tree) {

        // Use a live label from the dataset if we can

        String resultLabel = null;
        if (theLabelPath != null) {
            Nodes labelNodes = tree.getRootElement().query(theLabelPath);
            if (labelNodes.size() > 0) {
                resultLabel = labelNodes.get(0).getValue();
            }
        }

        // Otherwise use the provided label if there is one

        if (resultLabel == null && theLabel != null) {
            resultLabel = theLabel;
        }
        return resultLabel;
    }
}
