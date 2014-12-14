package ws.finson.wifix.app;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This SendXMLDocumentToSink class writes an XML Document to the specified file using the XOM
 * Serializer class.
 * 
 * @author Doug Johnson, Jun 13, 2013
 * 
 */
public class SendXMLDocumentToSink implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String namePrefix = "";
    private String nameSuffix = "";
    private String dstDir = "";

    private String sinkName = null;
    private Path sinkPath = null;
    private OutputStream sinkStream = null;
    private Serializer formattedWriter = null;

    /**
     * @param ac
     * @param cE
     * @throws ConfigurationException
     * @throws IOException
     */
    public SendXMLDocumentToSink(ApplicationComponent ac, Element cE)
            throws ConfigurationException, IOException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.trace("Begin section element <{}>", sectionElement.getLocalName());
            if ("prefix".equals(sectionElement.getLocalName())) {
                namePrefix = sectionElement.getValue();
            } else if ("suffix".equals(sectionElement.getLocalName())) {
                nameSuffix = sectionElement.getValue();
            } else if ("dst-dir".equals(sectionElement.getLocalName())) {
                dstDir = sectionElement.getValue();
            } else if ("file".equals(sectionElement.getLocalName())) {
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

    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {

        // Do we have or can we build a file name to write to?

        if (sinkName != null) {
            sinkPath = FileSystems.getDefault().getPath(".", sinkName);
        } else {

            Nodes someNodes = tree.getRootElement().query("context[1]/dataset[1]");
            if (someNodes.size() != 1) {
                throw new PipelineOperationException(
                        "Either an implicit dataset name must be available or the explicit name of the sink file must be specified.");
            }
            sinkName = namePrefix + someNodes.get(0).getValue() + nameSuffix + ".xml";

            if (dstDir.isEmpty()) {
                someNodes = tree.getRootElement().query("context[1]/dst-dir[1]");
                if (someNodes.size() == 1) {
                    dstDir = someNodes.get(0).getValue();
                }
            }
            sinkPath = FileSystems.getDefault().getPath(".", dstDir, sinkName);
        }

        try {
            sinkStream = Files.newOutputStream(sinkPath);
            formattedWriter = new Serializer(sinkStream);

            formattedWriter.setIndent(2);
            formattedWriter.setMaxLength(80);
            formattedWriter.write(tree);
            formattedWriter.flush();

        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }
        return tree;
    }

}
