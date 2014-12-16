package ws.finson.wifix.app;

import java.io.LineNumberReader;
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
            .compile("^(.*?)([a-fA-F\\d]{2}(?::[a-fA-F\\d]{2}){5})\\s+(\\S+)\\s+([^\\s,]+)(?:,\\S+)?\\s+(\\S+)\\s+(\\S+)\\s+(\\S+).*$");

    private final Pattern blankLinePattern = Pattern.compile("^\\s*$");
    private final Pattern ibssLinePattern = Pattern.compile("^.*?IBSS networks? found.*$");
    /**
     * @throws PipelineSourceException
     * 
     */
    public WiFiRecord(LineNumberReader src) throws PipelineSourceException {

        // Read WiFi info from raw data file

        String line = getNextLineOfCurrentRecord(src);
        if (isEndOfRecordIndicator(line)) {
            return;
        }

        // First line is (probably) the column labels.
        // but it might be data if they left the label line out for some reason!

        Matcher m = firstLinePattern.matcher(line);
        if (!m.matches()) {
            if (line.startsWith("No networks found")) {
                skipRemainderOfCurrentRecord(src);
                return;
            } else {
                logger.warn("("+Integer.toString(src.getLineNumber()) +") Unexpected first line format: " + line);
                logger.warn("This whole record will be skipped.");
                skipRemainderOfCurrentRecord(src);
                return;
            }
        }

        String[] row = new String[6];
        for (int idx = 0; idx < row.length; idx++) {
            row[idx] = m.group(idx + 1).trim();
        }
        recordValues.add(row);

        // the following lines are (mostly) actual data values

        do {
            line = getNextLineOfCurrentRecord(src);
            logger.trace(line);
            if (blankLinePattern.matcher(line).matches()) {
                continue;
            }
            if (isEndOfRecordIndicator(line)) {
                break;
            }
            if (ibssLinePattern.matcher(line).matches()) {
                skipRemainderOfCurrentRecord(src);
                break;
            }
            m = valueLinePattern.matcher(line);
            if (!m.matches()) {
                logger.warn("("+Integer.toString(src.getLineNumber()) +") Unexpected WiFi value line format: " + line);
                logger.warn("The rest of this record will be skipped.");
                skipRemainderOfCurrentRecord(src);
                return;
            }
            row = new String[6];
            for (int idx = 0; idx < row.length; idx++) {
                row[idx] = m.group(idx + 1).trim();
            }
            recordValues.add(row);
        } while (true);
        
        logger.trace("{} values for each field in this record.", recordValues.size() - 1);
        
        return;
    }
}
