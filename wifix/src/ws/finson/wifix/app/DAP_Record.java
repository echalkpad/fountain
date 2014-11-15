package ws.finson.wifix.app;

import java.util.List;

/**
 * 
 * 
 * @author Doug Johnson
 * @since Nov 10, 2014
 * 
 */
public interface DAP_Record {
    /**
     * Get the field names present in this record.
     * @return an array of the names of the fields in this record.  May be empty, will not be null.
     */
    String[] getFieldNames();

    /**
     * Get a single value for a named field. If the field is actually multi-valued, only the first
     * value is returned. If the named field is not part of this record, or no data was recorded for
     * the named field, the the returned value is null.
     * @return requested value or null
     */
    String getValue(String name);

    /**
     * Get all values for a named field. The values are returned as a list with one or more
     * elements. If the named field is not part of this record, or no data was recorded for
     * the named field, the returned value is null. 
     * @return requested List of values or null
     */
    List<String> getValues(String name);
}
