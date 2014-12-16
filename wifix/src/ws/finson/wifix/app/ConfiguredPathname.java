package ws.finson.wifix.app;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import nu.xom.Attribute;
import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This ConfiguredPathname class manages the details of building a complete Path using the
 * attributes of a source or sink Element in the config file and the context Element provided at run
 * time.
 * 
 * This class expects one of two cases to be true:
 * <ol>
 * <li>A pathname for the source or sink is provided in the config file. This pathname will be
 * converted to a Path object by assuming that it is relative to the current working directory and
 * needs no additional qualifiers such as directories or extensions added to it.</li>
 * 
 * <li>A pathname is not provided. In this case the class will use various parts and pieces gleaned
 * from the config file and the run time context element to construct a pathname and then turn that
 * into a Path object.</li>
 * </ol>
 * 
 * @author Doug Johnson
 * @since Dec 14, 2014
 * 
 */
public class ConfiguredPathname {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String dstDir = "";
    private String srcDir = "";
    private String namePrefix = "";
    private String commonName = "";
    private String nameSuffix = "";
    private String nameExtension = "";

    private String suppliedPathName = null;
    private String suppliedFileName = null;

    /**
     * 
     */
    public ConfiguredPathname(Element cE) {

        // Process each of the configuration attributes

        int attributeCount = cE.getAttributeCount();
        for (int idx = 0; idx < attributeCount; idx++) {
            Attribute anAttribute = cE.getAttribute(idx);
            String attributeName = anAttribute.getLocalName();
            logger.trace("Attribute '{}'", attributeName);
            if ("type".equals(attributeName)) {
                // ignore
            } else if ("pathname".equals(attributeName)) {
                suppliedPathName = anAttribute.getValue();
            } else if ("filename".equals(attributeName)) {
                suppliedFileName = anAttribute.getValue();
            } else if ("dst-dir".equals(attributeName)) {
                dstDir = anAttribute.getValue();
            } else if ("src-dir".equals(attributeName)) {
                srcDir = anAttribute.getValue();
            } else if ("prefix".equals(attributeName)) {
                namePrefix = anAttribute.getValue();
            } else if ("dataset".equals(attributeName)) {
                commonName = anAttribute.getValue();
            } else if ("suffix".equals(attributeName)) {
                nameSuffix = anAttribute.getValue();
            } else if ("extension".equals(attributeName)) {
                nameExtension = anAttribute.getValue();
            } else {
                logger.warn("Skipping '{}'. Attribute not recognized.", attributeName);
            }
        }
    }

    public Path getSourcePath(Element globalContext) {
        return buildPath(globalContext, null, Direction.SOURCE);
    }

    public Path getSourcePath(Element globalContext, Element localContext) {
        return buildPath(globalContext, localContext, Direction.SOURCE);
    }

    public Path getSinkPath(Element globalContext) {
        return buildPath(globalContext, null, Direction.SINK);
    }

    public Path getSinkPath(Element globalContext, Element localContext) {
        return buildPath(globalContext, localContext, Direction.SINK);
    }

    private enum Direction {
        SOURCE,
        SINK
    }

    private Path buildPath(Element globalContext, Element localContext, Direction d) {

        // Do we have or can we build a pathname?

        Path resultPath;
        if (suppliedPathName != null) {
            resultPath = FileSystems.getDefault().getPath(".", suppliedPathName);
        } else {
            Element anElement;
            Element[] defaults = new Element[] { localContext, globalContext };

            // What to use as the directory portion of the path?

            String contextElementName = "";
            String directory = "";
            switch (d) {
            case SOURCE:
                directory = srcDir;
                contextElementName = "src-dir";
                break;
            case SINK:
                directory = dstDir;
                contextElementName = "dst-dir";
                break;
            }
            for (Element cx : defaults) {
                if (directory.isEmpty() && (cx != null)) {
                    anElement = cx.getFirstChildElement(contextElementName);
                    if (anElement != null) {
                        directory = anElement.getValue();
                    }
                }
            }

            if (suppliedFileName != null) {
                resultPath = FileSystems.getDefault().getPath(".", directory, suppliedFileName);
            } else {

                // What to use as base of the name?

                String base = commonName;
                for (Element cx : defaults) {
                    if (base.isEmpty() && (cx != null)) {
                        anElement = cx.getFirstChildElement("dataset");
                        if (anElement != null) {
                            base = anElement.getValue();
                        }
                    }
                }

                // What to use as the extension?

                String ext = nameExtension;
                for (Element cx : defaults) {
                    if (ext.isEmpty() && (cx != null)) {
                        anElement = cx.getFirstChildElement("extension");
                        if (anElement != null) {
                            ext = anElement.getValue();
                        }
                    }
                }
                if (!ext.startsWith(".")) {
                    ext = "." + ext;
                }

                resultPath = FileSystems.getDefault().getPath(".", directory,
                        namePrefix + base + nameSuffix + ext);
            }
        }
        return resultPath;
    }
}
