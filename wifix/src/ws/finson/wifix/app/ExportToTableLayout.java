package ws.finson.wifix.app;

import java.io.BufferedOutputStream;
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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This ExportToTableLayout class writes one or more nodesets to a table in a file. Each column
 * comprises one nodeset. The output file can be in text (CSV) or binary (bin).
 * 
 * @author Doug Johnson, Nov 14, 2014
 * 
 */
public class ExportToTableLayout implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<ConfiguredNodeSet> selectors = new ArrayList<>();

    private List<ConfiguredPathname> sinkPathnames = new ArrayList<>();
    private List<BufferedOutputStream> binOut = new ArrayList<>();
    private List<PrintWriter> csvOut = new ArrayList<>();

    /**
     * @param ac
     *            the containing ApplicationComponent
     * @param cE
     *            the Element from the config file that defines this object
     * @throws IOException
     * @throws ConfigurationException
     */
    public ExportToTableLayout(ApplicationComponent ac, Element cE) throws IOException,
            ConfigurationException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                sinkPathnames.add(new ConfiguredPathname(sectionElement));
            } else if ("nodes".equals(sectionElement.getLocalName())) {
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

        Element globalContextElement = in.getRootElement().getFirstChildElement("context");
        for (ConfiguredPathname cpn : sinkPathnames) {
            Path theSinkPath = cpn.getSinkPath(globalContextElement);
            String format = FilenameUtils.getExtension(theSinkPath.toString());
            switch (format) {
            case "csv":
                PrintWriter textWriter;
                try {
                    textWriter = new PrintWriter(Files.newBufferedWriter(theSinkPath,
                            Charset.defaultCharset()));
                } catch (IOException e) {
                    throw new PipelineOperationException(e);
                }
                csvOut.add(textWriter);
                break;
            case "bin":
                BufferedOutputStream binStreamer;
                try {
                    binStreamer = new BufferedOutputStream(new FileOutputStream(cpn.getSinkPath(
                            globalContextElement).toFile()));
                } catch (IOException e) {
                    throw new PipelineOperationException(e);
                }
                binOut.add(binStreamer);
                break;
            default:
                throw new PipelineOperationException("Unrecognized table format: " + format);
            }
        }
        logger.debug("CSV outputs: {}, bin outputs: {}", csvOut.size(), binOut.size());

        // Get the data to print

        List<Nodes> nodesList = new ArrayList<>(selectors.size());
        int rowCount = 0;
        for (int idx = 0; idx < selectors.size(); idx++) {
            Nodes col = selectors.get(idx).getNodeSet(in);
            nodesList.add(col);
            rowCount = Math.max(rowCount, col.size());
            logger.debug("{} column has {} rows.", selectors.get(idx).getLabel(in), col.size());
        }
        int colCount = nodesList.size();

        // Write the CSV file header row

        if (csvOut.size() > 0) {
            StringBuilder buf = new StringBuilder();
            for (int idx = 0; idx < selectors.size(); idx++) {
                if (idx == 0) {
                    buf.append("\"");
                } else {
                    buf.append("\",\"");
                }
                String aLabel =  selectors.get(idx).getLabel(in);
                if (aLabel == null) {
                    buf.append("Field" + Integer.toString(idx));
                } else {
                    buf.append(aLabel);
                }
            }
            buf.append("\"");
            for (PrintWriter sinkWriter : csvOut) {
                sinkWriter.println(buf.toString());
            }
            logger.trace(buf.toString());

            // Write the CSV file value rows

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                buf = new StringBuilder();
                for (int colIndex = 0; colIndex < nodesList.size(); colIndex++) {
                    if (colIndex != 0) {
                        buf.append(",");
                    }
                    Nodes col = nodesList.get(colIndex);
                    if (rowIndex < col.size()) {
                        Node val = col.get(rowIndex);
                        buf.append(val.getValue());
                    }
                }
                for (PrintWriter sinkWriter : csvOut) {
                    sinkWriter.println(buf.toString());
                }
                logger.trace(buf.toString());
            }
        }

        // Write the binary file rows

        if (binOut.size() > 0) {
            int dataValue;

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                byte[] byteBuffer = new byte[colCount];
                int offset = 0;
                for (int colIndex = 0; colIndex < colCount; colIndex++) {
                    Nodes col = nodesList.get(colIndex);
                    if (rowIndex < col.size()) {
                        Node val = col.get(rowIndex);
                        dataValue = Integer.parseInt(val.getValue());
                    } else {
                        dataValue = 0;
                    }
                    byteBuffer[offset++] = (byte) (dataValue & 0xFF);
                }
                for (BufferedOutputStream sinkStreamer : binOut) {
                    try {
                        sinkStreamer.write(byteBuffer);
                        logger.trace("Wrote {} bytes to row {} of binary file.", byteBuffer.length,
                                rowIndex);
                    } catch (IOException e) {
                        throw new PipelineOperationException(e);
                    }
                }
            }
        }

        for (PrintWriter sinkWriter : csvOut) {
            sinkWriter.close();
        }

        for (BufferedOutputStream sinkStreamer : binOut) {
            try {
                sinkStreamer.close();
            } catch (IOException e) {
                throw new PipelineOperationException(e);
            }
        }
        return in;
    }
}
