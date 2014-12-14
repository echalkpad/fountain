package ws.finson.wifix.app;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This ImportCaptureScans class reads a raw data capture text file and creates an XML document
 * containing the same information.
 * 
 * The format of the incoming file is determined by the way the data was acquired, and so this
 * particular class is not very flexible with regard to input changes.
 * 
 * @author Doug Johnson, Nov 2014
 * 
 */
public class ImportCaptureScans implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sourceName = null;
    private LineNumberReader sourceReader = null;

    private final List<DAP_Scan> scans = new ArrayList<>();

    // Patterns to match each of the header lines

    private final Pattern startScan = Pattern.compile("^\\+\\+\\+\\s+(Scan)(?:\\s+(\\w+))*\\s*$");

    public ImportCaptureScans(ApplicationComponent ac, Element cE) throws ConfigurationException,
            IOException {

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
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {
        String srcDir;
        Path srcPath;
        try {
            Nodes dirNodes = tree.getRootElement().query("context/src-dir[1]");
            if (dirNodes.size() == 1) {
                srcDir = ((Element) dirNodes.get(0)).getValue();
            } else {
                srcDir = "";
            }
            srcPath = FileSystems.getDefault().getPath(".", srcDir, sourceName);
            logger.info("Opening file '{}' for reading.", srcPath.toString());
            sourceReader = new LineNumberReader(new InputStreamReader(Files.newInputStream(srcPath,
                    StandardOpenOption.READ), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }

        // read the data file, line by line and build a POJO tree

        String line = null;
        try {
            while ((line = sourceReader.readLine()) != null) {
                Matcher m = startScan.matcher(line);
                if (m.matches()) {
                    String className = "ws.finson.wifix.app." + "Basic" + m.group(1);
                    logger.debug("Scan start: {}", className);
                    Class<DAP_Scan> c = (Class<DAP_Scan>) Class.forName(className);
                    Constructor<DAP_Scan> maker = c.getConstructor(LineNumberReader.class);
                    scans.add(maker.newInstance(sourceReader));
                } else {
                    throw new PipelineOperationException(
                            "Unrecognized input line.  Expecting start of scan '+++ Scan': " + line);
                }
            }
        } catch (IOException | IllegalArgumentException | ReflectiveOperationException
                | SecurityException e) {
            throw new PipelineOperationException(e);
        }
        logger.info("Scan count: {}", scans.size());

        // Walk the POJO tree and build a new scan-sequence branch

        Element scanBranch = new Element("scan-sequence");
        int ordinal = 0;
        for (DAP_Scan currentScan : scans) {
            Element aScanElement = new Element("scan");
            aScanElement.addAttribute(new Attribute("ordinal", Integer.toString(ordinal++)));

            // record the time of this scan

            Element scanValuesElement = new Element("scan-values");
            scanValuesElement.addAttribute(new Attribute("field", "timetag"));
            Element valueElement = new Element("value");
            valueElement.appendChild(currentScan.getValue("timetag"));
            scanValuesElement.appendChild(valueElement);
            aScanElement.appendChild(scanValuesElement);

            // record the acquired data

            Map<String, List<String>> table = currentScan.getValues(new String[] { "SSID", "BSSID",
                    "RSSI", "CHANNEL", "CC" });
            if (table != null) {
                logger.trace("Data table fields: {}", table.keySet().toString());
                for (String key : table.keySet()) {
                    scanValuesElement = new Element("scan-values");
                    scanValuesElement.addAttribute(new Attribute("field", key));
                    for (String value : table.get(key)) {
                        valueElement = new Element("value");
                        valueElement.appendChild(value);
                        scanValuesElement.appendChild(valueElement);
                    }
                    aScanElement.appendChild(scanValuesElement);
                }
            }
            scanBranch.appendChild(aScanElement);
        }
        tree.getRootElement().appendChild(scanBranch);

        // Store some additional context information for downstream processors

        Element contextBranch = tree.getRootElement().getFirstChildElement("context");
        if (contextBranch == null) {
            logger.warn("Input XML Document has no top-level <context> branch.");
        } else {
            Element sourceElement = new Element("source");
            sourceElement.appendChild(sourceName);
            contextBranch.appendChild(sourceElement);

            String datasetName = FilenameUtils.getBaseName(sourceName);
            datasetName = datasetName.replaceFirst("-raw$", "");
            Element sourceDatasetName = new Element("dataset");
            sourceDatasetName.appendChild(datasetName);
            contextBranch.appendChild(sourceDatasetName);
        }
        return tree;
    }
}
