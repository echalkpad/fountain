package ws.finson.audiosp.app.device;

public class DeviceParameter {
    private String name;
    private String label;
    private Boolean writable;
    private Class<?> type;
    private Boolean cacheable;

    /**
     * Store information about a particular device parameter.
     * 
     * @param name
     *            String name of the parameter as recognized by the device firmware
     * @param label
     *            User friendlier String display label
     * @param writable
     *            true if the parameter can be set, false if read only
     * @param type
     *            the Java class of this parameter
     * @param cacheable
     *            true if the value of this parameter can be stored internally
     *            by the device driver, false if it must be read fresh upon each
     *            access
     */
    public DeviceParameter(String name, String label, Boolean writable, Class<?> type, Boolean cacheable) {
        this.name = name;
        this.label = label;
        this.writable = writable;
        this.type = type;
        this.cacheable = cacheable;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getWritable() {
        return writable;
    }

    public Class<?> getType() {
        return type;
    }

    public Boolean getCacheable() {
        return cacheable;
    }

}