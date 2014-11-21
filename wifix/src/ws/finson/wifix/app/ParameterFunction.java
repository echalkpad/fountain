package ws.finson.wifix.app;

import nu.xom.Element;

/**
 * This interface describes the API of a function working on an XML data capture tree.
 * 
 * @author Doug Johnson
 * @since Nov 18, 2014
 * 
 */
public interface ParameterFunction {

    /**
     * Create and return a small XML tree describing the results of the function implemented by this
     * object.
     * 
     * @param scanSequenceElement
     *            The scan-sequence Element that all the scan Elements attach to
     * @return A parameter element describing the results of the function, ready
     * to be attached to the sensor-sequence element
     */
    public Element twig(Element scanSequenceElement);

    public String getKeyFieldName();
    public String getExpression();
    public String getName();

}
