package ws.finson.wifix.app;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This ImportCSVToTable class reads one or more csv formatted text files and creates an XML
 * document containing the same information in my XML "table" format.
 * 
 * The format of the incoming file is standard RFC 4180 CSV. The first row is label strings, the
 * second and all following rows are data values. Each data row contains the same number of entries
 * as the label row. The default delimiter is a comma, but this can be configured. Delimiters are
 * not combined, thus missing values can be indicated by two successive delimiters. Leading and
 * trailing spaces are trimmed from labels and values.
 * 
 * @author Doug Johnson, Dec 2014
 * 
 */
public class ImportCSVToTable implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<ConfiguredPathname> sourcePathnames = new ArrayList<>();
    private String delimiter = ",";
    private InputStreamReader sourceReader = null;

    public ImportCSVToTable(ApplicationComponent ac, Element cE) throws ConfigurationException,
            IOException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                sourcePathnames.add(new ConfiguredPathname(sectionElement));
            } else if ("delimiter".equals(sectionElement.getLocalName())) {
                delimiter = sectionElement.getValue();
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (sourcePathnames.isEmpty()) {
            throw new ConfigurationException("At least one source file must be specified.");
        }
        if (delimiter.length() != 1) {
            throw new ConfigurationException("Field delimiter must be a single character.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {
        Path srcPath;
        Element globalContextElement = tree.getRootElement().getFirstChildElement("context");
        if (globalContextElement == null) {
            logger.warn("Input XML Document has no top-level <context> branch.");
        }
        for (ConfiguredPathname sourcePathname : sourcePathnames) {
            CSVParser parser;
            CSVFormat sourceFormat = CSVFormat.RFC4180.withDelimiter(delimiter.charAt(0))
                    .withHeader();
            try {
                srcPath = sourcePathname.getSourcePath(globalContextElement);
                logger.info("Opening file '{}' for CSV import.", srcPath.toString());
                sourceReader = new InputStreamReader(Files.newInputStream(srcPath,
                        StandardOpenOption.READ), StandardCharsets.UTF_8);
                parser = new CSVParser(sourceReader, sourceFormat);
            } catch (IOException e) {
                throw new PipelineOperationException(e);
            }

            Map<String, Integer> labelMap = parser.getHeaderMap();
            logger.debug("Column label count: {}", labelMap.size());

            Element tableElement = new Element("table");

            Element[] col = new Element[labelMap.size()];
            int columnIndex = 0;
            for (String label : labelMap.keySet()) {
                logger.trace("Label {}: {}", labelMap.get(label), label);
                Element columnElement = new Element("col");
                columnElement.addAttribute(new Attribute("name", label));
                tableElement.appendChild(columnElement);
                col[columnIndex++] = columnElement;
            }

            // read the data file, line by line and build the XML table directly

            int recordCount = 0;
            Iterator<CSVRecord> iter = parser.iterator();
            try {
                while (iter.hasNext()) {
                    CSVRecord rec = iter.next();
                    if (!rec.isConsistent()) {
                        throw new PipelineOperationException("Line "
                                + parser.getCurrentLineNumber() + ".  Number of data values ("
                                + rec.size() + ") doesn't match number of labels ("
                                + labelMap.size() + ").");
                    }
                    for (int idx = 0; idx < rec.size(); idx++) {
                        Element newValueElement = new Element("value");
                        newValueElement.appendChild(rec.get(idx));
                        col[idx].appendChild(newValueElement);
                    }
                    recordCount++;
                }
                logger.debug("Record count: {}", recordCount);
            } catch (Exception e) {
                throw new PipelineOperationException(e);
            }
            tree.getRootElement().appendChild(tableElement);

            // Store some additional context information for downstream processors

            if (globalContextElement != null) {
                Element sourceElement = new Element("source");
                sourceElement.appendChild(srcPath.getFileName().toString());
                globalContextElement.appendChild(sourceElement);

                String datasetName = FilenameUtils.getBaseName(srcPath.getFileName().toString());
                datasetName = datasetName.replaceFirst("-raw$", "");
                Element sourceDatasetName = new Element("base");
                sourceDatasetName.appendChild(datasetName);
                globalContextElement.appendChild(sourceDatasetName);
            }
            try {
                parser.close();
            } catch (IOException e) {
                throw new PipelineOperationException(e);
            }

        }
        return tree;
    }
}
