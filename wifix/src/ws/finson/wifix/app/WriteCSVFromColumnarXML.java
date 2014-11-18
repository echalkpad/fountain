package ws.finson.wifix.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;

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
 * This WriteCSVFromColumnarXML reads a DAP analysis tree and outputs a CSV file.
 * 
 * @author Doug Johnson, Nov 14, 2014
 * 
 */
public class WriteCSVFromColumnarXML implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sinkName = null;
    private String sinkNameRoot = null;

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
     * This code copies any "values" Elements out of the XML file and writes them to a CSV file. The
     * column header is the values' Element "label" attribute. The column contents are the values of
     * each values child element.
     * 
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document in) throws PipelineOperationException {

        // Get all the sensor parameter nodes

        Nodes parameterBranches = in.getRootElement().query("sensor-sequence/sensor");
        logger.debug("Sensor parameter count: {}", parameterBranches.size());

        // Create a separate CSV file for each sensor parameter

        for (int parameterIndex = 0; parameterIndex < parameterBranches.size(); parameterIndex++) {

            String parameterName = ((Element) parameterBranches.get(parameterIndex))
                    .getAttributeValue("label");

            PrintWriter sinkWriter;
            try {
                sinkWriter = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault()
                        .getPath(".", sinkName + "-" + parameterName + ".csv"), Charset
                        .defaultCharset()));
            } catch (IOException e) {
                throw new PipelineOperationException(e);
            }

            // Get the timetag-values and sensor-values nodes for this sensor parameter

            Node timetagValuesNode = parameterBranches.get(parameterIndex).query("timetag-values")
                    .get(0);
            int rowCount = timetagValuesNode.getChildCount();
            Nodes sensorValuesNodes = parameterBranches.get(parameterIndex).query("sensor-values");

            // Write the CSV file header row

            StringBuilder buf = new StringBuilder("timetag");
            for (int idx = 0; idx < sensorValuesNodes.size(); idx++) {
                Element col = (Element) sensorValuesNodes.get(idx);
                Attribute labelAttribute = col.getAttribute("label");
                buf.append(", " + labelAttribute.getValue());
            }
            sinkWriter.println(buf.toString());
            logger.trace(buf.toString());

            // Write the CSV file value rows

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                buf = new StringBuilder(timetagValuesNode.getChild(rowIndex).getValue());
                for (int colIndex = 0; colIndex < sensorValuesNodes.size(); colIndex++) {
                    Element col = (Element) sensorValuesNodes.get(colIndex);
                    Element val = (Element) col.getChild(rowIndex);
                    buf.append(", " + val.getValue());
                }
                sinkWriter.println(buf.toString());
            }

            sinkWriter.close();
        }
        return in;

    }
}
