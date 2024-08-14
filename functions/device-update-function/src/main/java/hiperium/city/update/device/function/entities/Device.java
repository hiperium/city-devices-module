package hiperium.city.update.device.function.entities;

import hiperium.city.update.device.function.common.DeviceStatus;

/**
 * Represents a response object that contains information about a device.
 */
public record Device(

    String id,
    String cityId,
    CityStatus cityStatus,
    DeviceStatus deviceStatus) {

    public static final String TABLE_NAME = "Devices";

    public static final String ID_COLUMN_NAME = "id";
    public static final String STATUS_COLUMN_NAME = "deviceStatus";
    public static final String CITY_ID_COLUMN_NAME = "cityId";
    public static final String CITY_STATUS_COLUMN_NAME = "cityStatus";
}
