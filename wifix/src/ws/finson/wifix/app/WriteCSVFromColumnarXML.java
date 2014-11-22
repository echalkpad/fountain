package ws.finson.wifix.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
 * This WriteCSVFromColumnarXML code writes one or more nodesets to a CSV file. Each column
 * comprises one nodeset.
 * 
 * @author Doug Johnson, Nov 14, 2014
 * 
 */
public class WriteCSVFromColumnarXML implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sinkName = null;
    private List<String> label = new ArrayList<>();
    private List<String> xpath = new ArrayList<>();

    /**
     * @param ac
     *            the containing ApplicationComponent
     * @param cE
     *            the Element from the config file that defines this object
     * @throws IOException
     * @throws ConfigurationException
     */
    public WriteCSVFromColumnarXML(ApplicationComponent ac, Element cE) throws IOException,
            ConfigurationException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                if (sinkName != null) {
                    logger.warn("Ignoring extra <{}> definition, only one is allowed.",
                            sectionElement.getLocalName());
                } else {
                    sinkName = sectionElement.getValue();
                }
            } else if ("nodes".equals(sectionElement.getLocalName())) {
                String a = sectionElement.getAttributeValue("label");
                String x = sectionElement.getAttributeValue("xpath");
                if (a == null || x == null) {
                    throw new ConfigurationException(
                            "A nodes element must have both a label and an xpath attribute.");
                }
                label.add(a);
                xpath.add(x);
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (sinkName == null) {
            throw new ConfigurationException("Name of the sink file must be specified.");
        }
    }

    /**
     * 
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document in) throws PipelineOperationException {

        // Get the data to print

        List<Nodes> nodesList = new ArrayList<>(label.size());
        int rowCount = 0;
        for (int idx = 0; idx < label.size(); idx++) {
            Nodes col = in.getRootElement().query(xpath.get(idx));
            nodesList.add(col);
            rowCount = Math.max(rowCount, col.size());
            logger.debug("{} column has {} rows.",label.get(idx), col.size());
        }

        // Create the file to write it to

        PrintWriter sinkWriter;
        try {
            sinkWriter = new PrintWriter(Files.newBufferedWriter(
                    FileSystems.getDefault().getPath(".", sinkName + ".csv"),
                    Charset.defaultCharset()));
        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }

        // Write the CSV file header row

        StringBuilder buf = new StringBuilder();
        for (int idx = 0; idx < label.size(); idx++) {
            if (idx != 0) {
                buf.append(",");
            }
            if (label.get(idx) == null || label.get(idx).isEmpty()) {
                buf.append("Field" + Integer.toString(idx));
            } else {
                buf.append(label.get(idx));
            }
        }
        sinkWriter.println(buf.toString());
        logger.trace(buf.toString());

        // Write the CSV file value rows

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            buf = new StringBuilder();
            for (int colIndex = 0; colIndex < nodesList.size(); colIndex++) {
                if (colIndex != 0) {
                    buf.append(",");
                }
                Nodes col =  nodesList.get(colIndex);
                if (rowIndex < col.size() ) {
                    Node val = col.get(rowIndex);
                    buf.append(val.getValue());
                }
            }
            sinkWriter.println(buf.toString());
            logger.trace(buf.toString());
       }

        sinkWriter.close();
        return in;
    }
}

// Element sensorSequenceElement = in.getRootElement().getFirstChildElement("sensor-sequence");
//
// // Get the timetag-values nodes for this sensor sequence
//
// Node timetagValuesNode = sensorSequenceElement.query(
// "sensor[@name='timetag']/sensor-values").get(0);
// int rowCount = timetagValuesNode.getChildCount();
//
// // Get the sensor and parameter nodes in the sensor-sequence
//
// Nodes sensorBranches = sensorSequenceElement.query("sensor | parameter");
// logger.debug("Sensor and parameter count: {}", sensorBranches.size());
//
// // Create a separate CSV file for each sensor or parameter
//
// for (int branchIndex = 0; branchIndex < sensorBranches.size(); branchIndex++) {
//
// String branchName = ((Element) sensorBranches.get(branchIndex))
// .getAttributeValue("name");
//
// if ("timetag".equals(branchName)) {
// continue;
// }
//
// logger.debug("Create CSV file for {}", branchName);
//
// PrintWriter sinkWriter;
// try {
// sinkWriter = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault()
// .getPath(".", sinkName + "-" + branchName + ".csv"), Charset
// .defaultCharset()));
// } catch (IOException e) {
// throw new PipelineOperationException(e);
// }
//
// Nodes sensorValuesNodes = sensorBranches.get(branchIndex).query("sensor-values");
//
// // Write the CSV file header row
//
// StringBuilder buf = new StringBuilder("timetag");
// for (int idx = 0; idx < sensorValuesNodes.size(); idx++) {
// Element col = (Element) sensorValuesNodes.get(idx);
// Attribute labelAttribute = col.getAttribute("key");
// if (labelAttribute == null) {
// buf.append(", " + Integer.toString(idx));
// } else {
// buf.append(", " + labelAttribute.getValue());
// }
// }
// sinkWriter.println(buf.toString());
// logger.trace(buf.toString());
//
// // Write the CSV file value rows
//
// for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
// buf = new StringBuilder(timetagValuesNode.getChild(rowIndex).getValue());
// for (int colIndex = 0; colIndex < sensorValuesNodes.size(); colIndex++) {
// Element col = (Element) sensorValuesNodes.get(colIndex);
// Element val = (Element) col.getChild(rowIndex);
// buf.append(", " + val.getValue());
// }
// sinkWriter.println(buf.toString());
// }
//
// sinkWriter.close();
// }
