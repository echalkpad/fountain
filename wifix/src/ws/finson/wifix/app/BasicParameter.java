package ws.finson.wifix.app;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * This BasicParameter class implements the capability of post-processing
 * scan data and producing new parameters that are equivalent to the measured
 * sensor data.
 * 
 * @author Doug Johnson
 * @since Nov 19, 2014
 * 
 */
public class BasicParameter extends AbstractParameterFunction {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BasicParameter(Element definition) throws PipelineSourceException {
        super(definition);
    }
    
    /**
     * @see ws.finson.wifix.app.ParameterFunction#twig(nu.xom.Element)
     */
    @Override
    public Element twig(Element scanSequenceElement) {
        assert (false) : "Not supposed to get here ...";
//        Map<String,String> expressionResult;
//        List<Map<String,String>> expressionResultList = new ArrayList<>();
//        
//        Elements scanElements = scanSequenceElement.getChildElements("scan");
//       for (int scanIndex = 0; scanIndex < scanElements.size(); scanIndex++) {
//            Element scanElement = scanElements.get(scanIndex);
//            
//            //------------------
//            
//             // Generate the new value or values
//            
//            Map<String,String> anExpressionResult = new HashMap<>();
//            anExpressionResult.put("", Integer.toString(scanElement.getChildCount()));
//             
//            //------------------
//            
//            expressionResultList.add(anExpressionResult);
//        }
//       
//       // Redistribute the results based on parameter rather than scan
//       
//       for (Map<String,String> resultMap : expressionResultList) {
////           copy from build sensor branch using Mapped Lists to hold the values
//       }
//       
//       // build a new twig based on the results
//       
//       Element parameterElement = new Element("parameter");
//       parameterElement.addAttribute(new Attribute("name",getName()));
//       String sortBy = getKeyFieldName();
//       if (sortBy != null) {
//           parameterElement.addAttribute(new Attribute("by",sortBy));
//       }
//
//       // write the elements scan by scan
//       
//
//       Element parameterValuesElement = new Element("parameter-values");
//       if (getKeyFieldName() != null) {
//           parameterValuesElement.addAttribute(new Attribute("by",getKeyFieldName()));
//       }
//
//       Element ve = new Element("value");
//       ve.appendChild(resultValue);
//       
//       if (getKeyName() == null) {
//           resultValues.
//       }
//       
//       parameterValuesElement.appendChild(ve);
//        resultParameterElement.appendChild(parameterValuesElement);
//    
//
//        // attach the new sensor twig to the sensor-sequence branch
//
//
//        
//        return resultParameterElement;
        return null;
        }
}
