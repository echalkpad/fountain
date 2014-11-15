package ws.finson.wifix.app;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Doug Johnson
 * @since Nov 10, 2014
 *
 */
public interface DAP_Scan {
    /**
     * Get a single value for a named field. If the field is actually multi-valued, only the first
     * value is returned. If the named field is not part of this record, the returned value is null.
     * @return requested value or null
     */
    String getValue(String name);

    /**
     * Get all values for a named field. The values are returned as a list with one or more
     * elements. If the named field is not part of this record, the returned value is null. 
     * @return requested List of values or null
     */
    List<String> getValues(String name);
    /**
     * Get all values for one or more named fields. The values are returned as a Map of lists with one or more
     * elements. If any ofs the named field is not part of this record, the returned value is null. 
     * @return requested List of values or null
     */
    Map<String,List<String>> getValues(String[] names);
 }
