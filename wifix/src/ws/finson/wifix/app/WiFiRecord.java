package ws.finson.wifix.app;

import java.io.BufferedReader;
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
public class WiFiRecord extends AbstractRecord {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pattern firstLinePattern = Pattern
            .compile("^\\s*(\\w+)\\s+(\\w+)\\s+(\\w+)\\s+(\\w+)\\s+(\\w+)\\s+(\\w+)\\s+(\\w+).*$");

    private final Pattern valueLinePattern = Pattern
            .compile("^(.{32})\\s*(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+).*$");

    /**
     * @throws PipelineSourceException
     * 
     */
    public WiFiRecord(BufferedReader src) throws PipelineSourceException {

        // Read WiFi info from raw data file

        String line = getNextLineOfCurrentRecord(src);
        if (isEndOfRecordIndicator(line)) {
            return;
        }

        // First line is (probably) the column labels.

        Matcher m = firstLinePattern.matcher(line);
        if (!m.matches()) {
            if (line.startsWith("No networks found")) {
                skipRemainderOfCurrentRecord(src);
                return;
            } else {
                throw new PipelineSourceException("Unexpected line format: " + line);
            }
        }

        String[] row = new String[6];
        for (int idx = 0; idx < row.length; idx++) {
            row[idx] = m.group(idx + 1).trim();
        }
        recordValues.add(row);

        // Following lines are the actual data values

        line = getNextLineOfCurrentRecord(src);
        while (!isEndOfRecordIndicator(line)) {
            logger.trace(line);
            m = valueLinePattern.matcher(line);
            if (!m.matches()) {
                throw new PipelineSourceException("Unexpected line format: " + line);
            }
            row = new String[6];
            for (int idx = 0; idx < row.length; idx++) {
                row[idx] = m.group(idx + 1).trim();
            }
            recordValues.add(row);
            line = getNextLineOfCurrentRecord(src);
        }
        logger.debug("{} values for each field in this record.", recordValues.size() - 1);
        return;
    }
}
