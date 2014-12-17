package ws.finson.wifix.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This ImportXMLDocumentFromSource class reads one or more XML Documents from
 * the specified file(s) and appends them to the pipeline Document.
 * 
 * @author Doug Johnson, Dec 2014
 * 
 */
public class ImportXMLDocumentFromSource implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<ConfiguredPathname> sourcePathNames = new ArrayList<>();;
    private BufferedReader sourceReader = null;

    /**
     * @param ac
     * @param cE
     * @throws ConfigurationException
     * @throws IOException
     */
    public ImportXMLDocumentFromSource(ApplicationComponent ac, Element cE)
            throws ConfigurationException, IOException {

        // Process each of the configuration sections

        Elements sectionElements = cE.getChildElements();
        for (int idx = 0; idx < sectionElements.size(); idx++) {
            Element sectionElement = sectionElements.get(idx);
            logger.debug("Begin section element <{}>", sectionElement.getLocalName());
            if ("file".equals(sectionElement.getLocalName())) {
                sourcePathNames.add(new ConfiguredPathname(sectionElement));
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (sourcePathNames.size() == 0) {
            throw new ConfigurationException("At least one import file must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {
        for (ConfiguredPathname cpn : sourcePathNames) {
            Document registerTree;
            try {
                Element globalContextElement = new Element(tree.getRootElement()
                        .getFirstChildElement("context"));
                Path srcPath = cpn.getSourcePath(globalContextElement);
                logger.info("Opening file '{}' for XML parsing.", srcPath.toString());
                sourceReader = Files.newBufferedReader(srcPath, Charset.defaultCharset());
                Builder parser = new Builder();
                registerTree = parser.build(sourceReader);
            } catch (ParsingException | IOException e) {
                throw new PipelineOperationException(e);
            }
            Element branch = registerTree.getRootElement();
            Element stub = new Element("stub");
            registerTree.setRootElement(stub);
            branch.detach();
            tree.getRootElement().appendChild(branch);
        }
        return tree;
    }
}
