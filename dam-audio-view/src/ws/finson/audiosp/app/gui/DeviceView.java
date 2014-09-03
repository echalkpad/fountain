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
     * defined therein are present.  The class that implements this interface
     * must remember the name of the data source specified during configuration, 
     * because it is not supplied in this call.
     */
    void setDataSource() throws ConfigurationException;
    /**
     * Show or hide this view.
     * @param b true to show, false to hide
     */
    void setVisible(Boolean b);

}
