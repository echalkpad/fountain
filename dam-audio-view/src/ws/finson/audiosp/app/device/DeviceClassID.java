package ws.finson.audiosp.app.device;

/***
 * Identify the top level hardware device classes for which drivers are expected to exist in the
 * Interchangeable Virtual Instrument scheme of things. I'm using the same taxonomy just for
 * general consistency, there is no plan or design that any of this work is IVI compatible.
 * 
 */
public enum DeviceClassID {
    GenericHardwareDevice,
    DigitalMultiMeter,
    Oscilloscope,
    ArbitraryWaveformGenerator,
    PowerSupply,
    Switch,
    PowerMeter,
    SpectrumAnalyzer,
    RFSignalGenerator
}