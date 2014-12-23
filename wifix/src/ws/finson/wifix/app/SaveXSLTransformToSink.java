package ws.finson.wifix.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.cfg.ApplicationComponent;
import ws.tuxi.lib.cfg.ConfigurationException;
import ws.tuxi.lib.pipeline.PipelineOperation;
import ws.tuxi.lib.pipeline.PipelineOperationException;

/**
 * This SaveXSLTransformToSink class applies an XSLT stylesheet to the pipelined XML document and
 * writes the result to the given path. The initial document is passed on to the next stage of the
 * pipeline unchanged.
 * 
 * @author Doug Johnson, Dec 21, 2014
 * 
 */
public class SaveXSLTransformToSink implements PipelineOperation<Document, Document> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String stylesheetResource = null;
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
    public SaveXSLTransformToSink(ApplicationComponent ac, Element cE)
            throws ConfigurationException, IOException {
        try {
            defaultContextDocument = new Builder().build(
                    "<context><extension>txt</extension></context>", null);
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
            } else if ("stylesheet".equals(sectionElement.getLocalName())) {
                stylesheetResource = sectionElement.getAttributeValue("resource-path");
            } else {
                logger.warn("Skipping <{}> element. Element not recognized.",
                        sectionElement.getLocalName());
            }
        }
        if (stylesheetResource == null || sinkPathname == null) {
            throw new ConfigurationException(
                    "Names of the XSLT stylesheet and the sink must be specified.");
        }
    }

    /**
     * @see ws.tuxi.lib.pipeline.PipelineOperation#doStep(java.lang.Object)
     */
    @Override
    public Document doStep(Document tree) throws PipelineOperationException {

        Document stylesheet;
        Nodes transformResult;
        PrintWriter pw;

        try {
            Builder builder = new Builder();
            InputStream resource = this.getClass().getResourceAsStream(stylesheetResource);
            stylesheet = builder.build(resource);
            XSLTransform transform = new XSLTransform(stylesheet);
            String today = DateFormat.getDateInstance().format(new Date());
            transform.setParameter("today", today);
            transformResult = transform.transform(tree);
        } catch (ParsingException | IOException | XSLException e) {
            throw new PipelineOperationException(e);
        }

        Element twig = tree.getRootElement().getFirstChildElement("context");
        Element globalContextElement = (twig == null) ? null : new Element(twig);
        Element localContextElement = defaultContextDocument.getRootElement();
        sinkPath = sinkPathname.getSinkPath(globalContextElement, localContextElement);
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(sinkPath.toFile())));
            for (int idx = 0; idx < transformResult.size(); idx++) {
                pw.println(transformResult.get(idx).getValue());
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            throw new PipelineOperationException(e);
        }
        return tree;
    }

}
