package ws.finson.wifix.app;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This NormalizeTableValues class reads all the entries in a table, calculates the min and max,
 * then scales all the values to fit a given range and type and rewrites every value in the table.
 * 
 * @author Doug Johnson, Dec 24, 2014
 * 
 */
public class NormalizeTableValues implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<ConfiguredNodeSet> selectors = new ArrayList<>();

    /**
     * @param ac
     *            the containing ApplicationComponent
     * @param cE
     *            the Element from the config file that defines this object
     * @throws IOException
     * @throws ConfigurationException
     */
    public NormalizeTableValues(ApplicationComponent ac, Element cE) throws IOException,
            ConfigurationException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("nodes".equals(sectionElement.getLocalName())) {
                selectors.add(new ConfiguredNodeSet(sectionElement));
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
    }

    /**
     * 
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document in) throws PipelineOperationException {

        Nodes tables = in.query("//table");
        for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
            Node aTable = tables.get(tableIndex);
            double minValue = Double.MAX_VALUE;
            double maxValue = Double.MIN_VALUE;
            Nodes columns = aTable.query("col");

            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Node column = columns.get(colIndex);
                Nodes values = column.query("value");
                for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                    double currentValue = Double.parseDouble(values.get(rowIndex).getValue());
                    minValue = Math.min(currentValue, minValue);
                    maxValue = Math.max(currentValue, maxValue);
                }
            }
            
            double slope = (255.0 - 0) / (maxValue - minValue);

            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Node column = columns.get(colIndex);
                Nodes values = column.query("value");
                for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                    Element valueElement = (Element) values.get(rowIndex);
                    double currentValue = Double.parseDouble(valueElement.getValue());
                    double newValue = (currentValue - minValue) * slope;
                    valueElement.removeChildren();
                    String newStringValue = Integer.toString((int)newValue);
                    valueElement.appendChild(newStringValue);
                }
            }
        }

        return in;
    }
}
