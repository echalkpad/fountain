package ws.finson.wifix.app;

import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * This class implements the common capabilities of the user-specified
 * functions for processing scan data.
 * 
 * @author Doug Johnson
 * @since Nov 19, 2014
 * 
 */
public abstract class AbstractParameterFunction implements ParameterFunction {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String name;
    private String keyFieldName;
    private String expression;

    public AbstractParameterFunction(Element definition) throws PipelineSourceException {
        
        if ((name = definition.getAttributeValue("name")) == null) {
            throw new PipelineSourceException("Parameter definition Elements must have a name attribute.");
        }
        
        Elements detailElements = definition.getChildElements("expression");
        if (detailElements.size() == 0) {
            expression = name;
        } else if (detailElements.size() == 1) {
            expression = detailElements.get(0).getValue();
        } else {
            throw new PipelineSourceException("Parameter definition Elements must have zero or one 'expression' child Elements.");
        }
        
        detailElements = definition.getChildElements("by");
        if (detailElements.size() == 0) {
           keyFieldName = null; 
        } else if (detailElements.size() == 1) {
            keyFieldName = detailElements.get(0).getValue();
        } else if (detailElements.size() > 1 ) {
            throw new PipelineSourceException("Parameter definition Elements must have zero or one 'by' child Elements.");
        }
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public String getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }
}
