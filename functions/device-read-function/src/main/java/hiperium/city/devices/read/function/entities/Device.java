package hiperium.city.devices.read.function.entities;

import hiperium.city.devices.read.function.common.DeviceStatus;

/**
 * Represents a response object that contains information about a device.
 */
public record Device(

    String id,
    String name,
    String cityId,
    String description,
    DeviceStatus status) {

    public static final String TABLE_NAME = "Devices";

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String STATUS_COLUMN_NAME = "status";
    public static final String CITY_ID_COLUMN_NAME = "cityId";
    public static final String DESCRIPTION_COLUMN_NAME = "description";
}
