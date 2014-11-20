package ws.finson.wifix.app;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Doug Johnson
 * @since Nov 19, 2014
 * 
 */
public class BasicParameter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    String key;
    String expression;
    String name;

    public BasicParameter(Element definition) {
        key = definition.getFirstChildElement("key").getValue();
        expression = definition.getFirstChildElement("expression").getValue();
    }
    
    // return a list of parameter-values elements
    
    -----> implement!
    
    public List<Element> getParameterValues(Element scan) {
        return new ArrayList<>();
    }
}
