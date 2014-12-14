package ws.finson.wifix.app;

import java.io.IOException;

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
 * This CreateXMLDocument class uses the XML provided in its configuration element as the root
 * element of a new XML document.
 * 
 * @author Doug Johnson, Dec 2014
 * 
 */
public class CreateXMLDocument implements PipelineSource<Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Element rootElement;

    public CreateXMLDocument(ApplicationComponent ac, Element cE) throws ConfigurationException,
            IOException {

        // The content of the configuration element is the root of the new tree.

        Elements sectionElements = cE.getChildElements();
        if (sectionElements.size() != 1) {
            throw new ConfigurationException(
                    "There must be one and only one root element for the new document.");
        }
        rootElement = new Element(sectionElements.get(0));
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineSource#readPipelineSource()
     */
    @Override
    public Document readPipelineSource() throws PipelineSourceException {
        return new Document(rootElement);
    }
}
