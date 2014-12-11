package ws.finson.wifix.app;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
import ws.tuxi.lib.pipeline.PipelineSource;
import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * This ImportFromTableLayout class reads a csv formatted text file and creates an XML document
 * containing the same information.
 * 
 * The format of the incoming file is standard CSV. The first row is label strings, the second and
 * all following rows are data values. Each data row contains the same number of entries as the
 * label row. Missing values are indicated by two successive delimiters. 
 * The expected delimiter is a comma.  Leading and trailing spaces are trimmed from labels and values.
 * 
 * @author Doug Johnson, Dec 2014
 * 
 */
public class ImportFromTableLayout implements PipelineSource<Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sourceName = null;
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
                if (sourceName != null) {
                    logger.warn("Ignoring extra <{}> definition, only one is allowed.",
                            sectionElement.getLocalName());
                } else {
                    sourceName = sectionElement.getValue();

                    sourceReader = new LineNumberReader(new InputStreamReader(Files.newInputStream(
                            FileSystems.getDefault().getPath(".", sourceName),
                            StandardOpenOption.READ), StandardCharsets.UTF_8));
                }
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (sourceName == null) {
            throw new ConfigurationException("Name of the source file must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineSource#readPipelineSource()
     */
    @Override
    public Document readPipelineSource() throws PipelineSourceException {

        // Create the skeleton XML document

        Element root = new Element("session");
        Element contextBranch = new Element("context");
        root.appendChild(contextBranch);

        Element sourceElement = new Element("source");
        sourceElement.appendChild(sourceName);
        contextBranch.appendChild(sourceElement);

        Element sourceBaseName = new Element("basename");
        sourceBaseName.appendChild(FilenameUtils.getBaseName(sourceName));
        contextBranch.appendChild(sourceBaseName);

        // The first line of a CSV file defines the column labels and must not be blank.
        // Delimiters are not combined.

        String line = null;
        try {
            line = sourceReader.readLine();
        } catch (IOException e) {
            throw new PipelineSourceException(e);
        }
        if (line == null || line.isEmpty()) {
            return new Document(root);
        }

        String[] label = csvLabelPattern.split(line, -1);
        if (label.length == 0) {
            return new Document(root);
        }
        logger.debug("Column label count: {}", label.length);

        Element parameterBranchElement = new Element("sensor-sequence");
        root.appendChild(parameterBranchElement);

        Element[] col = new Element[label.length];

        for (int idx = 0; idx < label.length; idx++) {
            label[idx] = label[idx].replaceAll("\\\"","");
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
                    throw new PipelineSourceException("Line " + sourceReader.getLineNumber()
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
            logger.debug("Record count: {}",recordCount);
        } catch (IOException e) {
            throw new PipelineSourceException(e);
        }

        return new Document(root);

    }
}
