package ws.finson.wifix.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineSource;
import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * This GetXMLFromDAP class reads a data acquisition program (DAP) data capture text file and
 * creates an XML document containing the same information.
 * 
 * @author Doug Johnson, Nov 2014
 * 
 */
public class GetXMLFromDAP implements PipelineSource<Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sourceName = null;
    private BufferedReader sourceReader = null;

    private final List<DAP_Scan> scans = new ArrayList<>();

    // Patterns to match each of the header lines

    private final Pattern startScan = Pattern.compile("^\\+\\+\\+\\s+(Scan)(?:\\s+(\\w+))*\\s*$");

    public GetXMLFromDAP(ApplicationComponent ac, Element cE) throws ConfigurationException,
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
                    sourceReader = Files.newBufferedReader(
                            FileSystems.getDefault().getPath(".", sourceName),
                            Charset.defaultCharset());
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

        // read the data file, line by line and build a POJO tree

        String line = null;
        try {
            while ((line = sourceReader.readLine()) != null) {
                Matcher m = startScan.matcher(line);
                if (m.matches()) {
                    String className = "ws.finson.wifix.app." + "Basic" + m.group(1);
                    logger.debug("Scan start: {}", className);
                    Class c = Class.forName(className);
                    Constructor<DAP_Scan> maker = c.getConstructor(BufferedReader.class);
                    scans.add(maker.newInstance(sourceReader));
                } else {
                    throw new PipelineSourceException(
                            "Unrecognized input line.  Expecting start of scan '+++ Scan'");
                }
            }
        } catch (IOException | IllegalArgumentException | ReflectiveOperationException
                | SecurityException e) {
            throw new PipelineSourceException(e);
        }
        logger.info("Scan count: {}", scans.size());

        // Walk the POJO tree and build an XML tree

        Element root = new Element("capture");
        Document captureTree = new Document(root);

        Element scanBranch = new Element("scan-sequence");
        for (DAP_Scan currentScan : scans) {
            Element aScanElement = new Element("scan");

            // record the time of this scan

            Element timeTagElement = new Element("timetag");
            timeTagElement.appendChild(currentScan.getValue("timetag"));
            aScanElement.appendChild(timeTagElement);

            // record the acquired data

            Map<String, List<String>> table = currentScan
                    .getValues(new String[] {"SSID", "BSSID", "RSSI","CHANNEL", "CC" });
            if (table != null) {
                logger.debug("Data table fields: {}",table.keySet().toString());
                Element fieldNameElement;
                for (String key : table.keySet()) {
                    fieldNameElement = new Element(key);
                    for (String value : table.get(key)) {
                        Element valueElement = new Element("value");
                        valueElement.appendChild(value);
                        fieldNameElement.appendChild(valueElement);
                    }
                    aScanElement.appendChild(fieldNameElement);
                }
            }
            scanBranch.appendChild(aScanElement);
        }
        root.appendChild(scanBranch);

        return captureTree;

    }
}
