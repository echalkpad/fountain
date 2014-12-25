package ws.finson.wifix.app;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This SaveXMLDocumentToSink class writes an XML Document to the specified file using the XOM
 * Serializer class.
 * 
 * @author Doug Johnson, Jun 13, 2013
 * 
 */
public class SaveXMLDocumentToSink implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConfiguredPathname sinkPathname = null;
    private Path sinkPath = null;
    private OutputStream sinkStream = null;
    private Serializer formattedWriter = null;
    private Document defaultContextDocument = null;
    /**
     * @param ac
     * @param cE
     * @throws ConfigurationException
     * @throws IOException
     */
    public SaveXMLDocumentToSink(ApplicationComponent ac, Element cE)
            throws ConfigurationException, IOException {
        try {
            defaultContextDocument = new Builder().build("<context><extension>xml</extension></context>",null);
        } catch (ParsingException e) {
            throw new ConfigurationException(e);
        }

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.trace("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                sinkPathname = new ConfiguredPathname(sectionElement);
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
        
        Element twig = tree.getRootElement().getFirstChildElement("context");
        Element globalContextElement = (twig == null) ? null : new Element(twig);
        Element localContextElement = defaultContextDocument.getRootElement();
        sinkPath = sinkPathname.getSinkPath(globalContextElement,localContextElement);
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
