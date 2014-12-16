package ws.finson.wifix.app;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This ImportFromTableLayout class reads a csv formatted text file and creates an XML document
 * containing the same information.
 * 
 * The format of the incoming file is standard CSV. The first row is label strings, the second and
 * all following rows are data values. Each data row contains the same number of entries as the
 * label row. Missing values are indicated by two successive delimiters. The expected delimiter is a
 * comma. Leading and trailing spaces are trimmed from labels and values.
 * 
 * @author Doug Johnson, Dec 2014
 * 
 */
public class ImportFromTableLayout implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConfiguredPathname sourcePathName = null;
    private Path srcPath = null;
    private LineNumberReader sourceReader = null;

    private final Pattern csvLabelPattern = Pattern.compile("\\\",\\\"");
    private final Pattern csvValuePattern = Pattern.compile(",");

    public ImportFromTableLayout(ApplicationComponent ac, Element cE)
            throws ConfigurationException, IOException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                sourcePathName = new ConfiguredPathname(sectionElement);
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (sourcePathName == null) {
            throw new ConfigurationException("A source file must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {
        try {
            Element globalContextElement = new Element(tree.getRootElement().getFirstChildElement(
                    "context"));
            srcPath = sourcePathName.getSourcePath(globalContextElement);
            logger.info("Opening file '{}' for reading.", srcPath.toString());
            sourceReader = new LineNumberReader(new InputStreamReader(Files.newInputStream(srcPath,
                    StandardOpenOption.READ), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }

        // The first line of a CSV file defines the column labels and must not be blank.
        // Delimiters are not combined.

        String line = null;
        try {
            line = sourceReader.readLine();
        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }
        if (line == null || line.isEmpty()) {
            return tree;
        }

        String[] label = csvLabelPattern.split(line, -1);
        if (label.length == 0) {
            return tree;
        }
        logger.debug("Column label count: {}", label.length);

        Element parameterBranchElement = new Element("sensor-sequence");

        Element[] col = new Element[label.length];

        for (int idx = 0; idx < label.length; idx++) {
            label[idx] = label[idx].replaceAll("\\\"", "");
            logger.trace("Label {}: {}", idx, label[idx]);
            Element parameterElement = new Element("parameter");
            parameterElement.addAttribute(new Attribute("name", label[idx].trim()));
            parameterBranchElement.appendChild(parameterElement);
            Element valuesElement = new Element("sensor-values");
            parameterElement.appendChild(valuesElement);
            col[idx] = valuesElement;
        }

        // read the data file, line by line and build the XML tree directly

        int recordCount = 0;
        try {
            while ((line = sourceReader.readLine()) != null) {
                String[] val = csvValuePattern.split(line, -1);
                if (val.length != label.length) {
                    throw new PipelineOperationException("Line " + sourceReader.getLineNumber()
                            + ".  Number of data values (" + val.length
                            + ") doesn't match number of labels (" + label.length + ").");
                }
                for (int idx = 0; idx < val.length; idx++) {
                    Element newValueElement = new Element("value");
                    newValueElement.appendChild(val[idx].trim());
                    col[idx].appendChild(newValueElement);
                }
                recordCount++;
            }
            logger.debug("Record count: {}", recordCount);
        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }
        tree.getRootElement().appendChild(parameterBranchElement);

        // Store some additional context information for downstream processors

        Element contextBranch = tree.getRootElement().getFirstChildElement("context");
        if (contextBranch == null) {
            logger.warn("Input XML Document has no top-level <context> branch.");
        } else {
            Element sourceElement = new Element("source");
            sourceElement.appendChild(srcPath.getFileName().toString());
            contextBranch.appendChild(sourceElement);

            String datasetName = FilenameUtils.getBaseName(srcPath.getFileName().toString());
            datasetName = datasetName.replaceFirst("-raw$", "");
            Element sourceDatasetName = new Element("dataset");
            sourceDatasetName.appendChild(datasetName);
            contextBranch.appendChild(sourceDatasetName);
        }
        return tree;
    }
}
