package ws.finson.wifix.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineSource;
import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * This LoadXMLDocumentFromSource class reads an XML Document from the specified file.
 * 
 * @author Doug Johnson, Jun 13, 2013
 * 
 */
public class LoadXMLDocumentFromSource implements PipelineSource<Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConfiguredPathname sourcePathName = null;
    private BufferedReader sourceReader = null;

    /**
     * @param ac
     * @param cE
     * @throws ConfigurationException
     * @throws IOException
     */
    public LoadXMLDocumentFromSource(ApplicationComponent ac, Element cE)
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
     * @see ws.tuxi.lib.pipeline.PipelineSource#readPipelineSource()
     */
    @Override
    public Document readPipelineSource() throws PipelineSourceException {
        Document registerTree;

        try {
            Path srcPath = sourcePathName.getSourcePath(null);
            logger.info("Opening file '{}' for XML parsing.", srcPath.toString());
            sourceReader = Files.newBufferedReader(sourcePathName.getSourcePath(null),
                    Charset.defaultCharset());
            Builder parser = new Builder();
            registerTree = parser.build(sourceReader);
        } catch (ParsingException | IOException e) {
            throw new PipelineSourceException(e);
        }
        return registerTree;
    }
}
