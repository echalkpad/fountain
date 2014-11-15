package ws.finson.wifix.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * 
 * 
 * @author Doug Johnson
 * @since Nov 10, 2014
 * 
 */
public class BasicScan implements DAP_Scan {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pattern startRecordPattern = Pattern
            .compile("^\\+\\+\\+\\s+(\\w+)(?:\\s+(\\w+))*\\s*$");
    private final Pattern endScanPattern = Pattern.compile("^\\-\\-\\-.*$");

    private final List<DAP_Record> records = new ArrayList<>();

    /**
     * @throws PipelineSourceException
     * 
     */
    public BasicScan(BufferedReader src) throws PipelineSourceException {

        // Read and interpret records from the raw file until we find the end-of-scan marker

        String line = null;
        String suffix = null;

        try {
            while ((line = src.readLine()) != null) {
                logger.trace(line);
                Matcher m = startRecordPattern.matcher(line);
                if (m.matches()) {
                    suffix = (m.group(2) == null) ? "" : "_" + m.group(2);
                    String className = "ws.finson.wifix.app." + m.group(1) + "Record" + suffix;
                    logger.debug("Record start: {}", className);
                    Class<DAP_Record> c = (Class<DAP_Record>) Class.forName(className);
                    Constructor<DAP_Record> maker = c.getConstructor(BufferedReader.class);
                    records.add(maker.newInstance(src));
                } else if (endScanPattern.matcher(line).matches()) {
                    break;
                } else {
                    throw new PipelineSourceException("Unexpected line format: " + line);
                }
            }
        } catch (IOException | ReflectiveOperationException | SecurityException e) {
            throw new PipelineSourceException(e);
        }
        logger.trace("Record count: {}", records.size());
    }

    /**
     * @see ws.finson.wifix.app.DAP_Scan#getValue(java.lang.String)
     */
    @Override
    public String getValue(String name) {
        String result = null;
        for (DAP_Record r : records) {
            if ((result = r.getValue(name)) != null) {
                break;
            }
        }
        return result;
    }

    /**
     * @see ws.finson.wifix.app.DAP_Scan#getValues(java.lang.String)
     */
    @Override
    public List<String> getValues(String name) {
        List<String> result = null;
        for (DAP_Record r : records) {
            if ((result = r.getValues(name)) != null) {
                break;
            }
        }
        return result;
    }

    /**
     * @see ws.finson.wifix.app.DAP_Scan#getValues(java.lang.String[])
     */
    @Override
    public Map<String, List<String>> getValues(String[] names) {
        List<String> col = null;
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        
        for (String fieldName : names) {
            for (DAP_Record r : records) {
                if ((col = r.getValues(fieldName)) != null) {
                    result.put(fieldName, col);
                }
            }
        }
        return (result.size() == names.length)  ? result : null;
    }
}
