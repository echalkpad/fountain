package ws.finson.wifix.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * 
 * 
 * @author Doug Johnson
 * @since Nov 11, 2014
 * 
 */
public abstract class AbstractRecord implements DAP_Record {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Pattern startRecordPattern = Pattern
            .compile("^\\+\\+\\+\\s+(\\w+)(?:\\s+(\\w+))*\\s*$");
    private final Pattern endPattern = Pattern.compile("^\\-\\-\\-.*$");

    protected final List<String[]> recordValues = new ArrayList<>();

    /**
     * @see ws.finson.wifix.app.DAP_Record#getFieldNames()
     */
    @Override
    public String[] getFieldNames() {
        if (recordValues.isEmpty()) {
            return new String[0];
        } else {
            return recordValues.get(0);
        }
    }

    /**
     * @see ws.finson.wifix.app.DAP_Record#getValue(java.lang.String)
     */
    @Override
    public String getValue(String name) {
        if (recordValues.size() >= 2) {
            Integer offset = getFieldIndex(name);
            if (offset != null) {
                return recordValues.get(1)[offset];
            }
        }
        return null;
    }

    /**
     * @see ws.finson.wifix.app.DAP_Record#getValues(java.lang.String)
     */
    @Override
    public List<String> getValues(String name) {
        List<String> result = null;
        if (recordValues.size() >= 2) {
            Integer offset = getFieldIndex(name);
            if (offset != null) {
                result = new ArrayList<>(recordValues.size()-1);
                for (int idx=1; idx<recordValues.size();idx++) {
                    result.add(recordValues.get(idx)[offset]);
                }
            }
        }
        return result;
    }


    protected void skipRemainderOfCurrentRecord(BufferedReader src) throws PipelineSourceException {
        String line = null;
        int depth = 1;
        try {
            while ((line = src.readLine()) != null) {
                Matcher m = startRecordPattern.matcher(line);
                if (m.matches()) {
                    depth++;
                    logger.debug("Skipping enclosed record {}", m.group(1));
                } else if (endPattern.matcher(line).matches()) {
                    if (--depth == 0) {
                        return;
                    }
                }
            }
            throw new PipelineSourceException(
                    "Unexpected end of file before end of record indicator.");
        } catch (IOException e) {
            throw new PipelineSourceException(e);
        }
    }

    protected String getNextLineOfCurrentRecord(BufferedReader src) throws PipelineSourceException {
        String line = null;
        try {
            line = src.readLine();
            if (line == null) {
                throw new PipelineSourceException(
                        "Unexpected end of file before end of record indicator.");
            }
        } catch (IOException e) {
            throw new PipelineSourceException(e);
        }
        return line;
    }

    protected boolean isEndOfRecordIndicator(String line) {
        return endPattern.matcher(line).matches();
    }

    // Do we have the field the caller is looking for? If so, what is the
    // array index where it is stored?

    protected Integer getFieldIndex(String name) {
        for (int idx = 0; idx < recordValues.get(0).length; idx++) {
            if (recordValues.get(0)[idx].equals(name.trim())) {
                logger.trace("Offset to {} data is {}",name,idx);
                return new Integer(idx);
            }
        }
        return null;
    }
}