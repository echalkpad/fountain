package ws.finson.audiosp.app.gui;

import ws.tuxi.lib.cfg.ConfigurationException;

/**
 * Implemented by classes which provide a Swing-based view of some data source.
 * These methods must be called from the Swing Event Dispatch thread, not the
 * main thread or any other non-Swing-dispatch thread.
 *
 * @author Doug Johnson
 * @since Sep 1, 2014
 *
 */
public interface DeviceView {
    
    /**
     * Connect to data source(s), other GUI components, etc.  Called during the 
     * preRun() phase of ApplicationComponent initialization which is after the
     * entire configuration file has been processed and all the active objects
     * defined therein are present.  The name of the data source used is the one
     * specified during configuration.
     */
    void setDataSource() throws ConfigurationException;
    
    /**
     * @param sourceName
     * @throws ConfigurationException
     */
    void setDataSource(String sourceName) throws ConfigurationException;
}
