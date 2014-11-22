package ws.finson.wifix.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String xArgument;
    private String xFunction;
    
    private final Pattern singleValuePattern = Pattern.compile("^(\\w+)$");
    private final Pattern functionCallPattern = Pattern.compile("^(\\w+)\\(\\s*(\\w+)\\s*\\)$");

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
        
        Matcher svm = singleValuePattern.matcher(expression);
        Matcher fcm = functionCallPattern.matcher(expression);
        
        if (svm.matches()) {
            xFunction = "select";
            xArgument = svm.group(1);
        } else if (fcm.matches()) {
            xFunction = fcm.group(1);
            xArgument = fcm.group(2);
        } else {
            throw new PipelineSourceException("Invalid expression: '"+expression+"''");
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

    public String getXArgument() {
        return xArgument;
    }

    public String getXFunction() {
        return xFunction;
    }
}
