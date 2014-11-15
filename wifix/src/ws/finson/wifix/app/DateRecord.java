package ws.finson.wifix.app;

import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.tuxi.lib.pipeline.PipelineSourceException;

/**
 * 
 * Implements field "timetag" extracted from a Unix timetag value
 * 
 * @author Doug Johnson
 * @since Nov 10, 2014
 * 
 */
public class DateRecord extends AbstractRecord {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @throws PipelineSourceException
     * 
     */
    public DateRecord(BufferedReader src) throws PipelineSourceException {

        // Read date and time from raw data file

        try {
            String line = src.readLine(); // first line is the spelled out date
            if (line == null) {
                throw new PipelineSourceException("Unexpected end of file");
            }
            line = src.readLine(); // second line is date expressed as milliseconds since the epoch
            if (line == null) {
                throw new PipelineSourceException("Unexpected end of file");
            }
            long moment = Long.parseLong(line);
            recordValues.add(new String[] { "timetag" });
            recordValues.add(new String[] { Long.toString(moment) });
            skipRemainderOfCurrentRecord(src);
        } catch (IOException e) {
            throw new PipelineSourceException(e);
        }
        logger.trace("{} {}", recordValues.get(0)[0], recordValues.get(1)[0]);
        return;
    }
}
