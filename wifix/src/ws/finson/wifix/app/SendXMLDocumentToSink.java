package ws.finson.wifix.app;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
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

    private String sinkName = null;
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
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                if (sinkName != null) {
                    logger.warn("Ignoring extra <{}> definition, only one is allowed.",
                            sectionElement.getLocalName());
                } else {
                    sinkName = sectionElement.getValue();
                    OutputStream sinkStream = Files.newOutputStream(FileSystems.getDefault()
                            .getPath(".", sinkName));
                    formattedWriter = new Serializer(sinkStream);
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
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {

        try {
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
