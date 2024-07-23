package hiperium.city.devices.data.function.entities;

import hiperium.city.devices.data.function.common.DeviceStatus;

/**
 * Represents a response object that contains information about a city.
 */
public record Device(

    String id,
    String name,
    String description,
    DeviceStatus status,
    String cityId,
    CityStatus cityStatus) {

    public static final String TABLE_NAME = "Devices";

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String DESCRIPTION_COLUMN_NAME = "description";
    public static final String STATUS_COLUMN_NAME = "status";
    public static final String CITY_ID_COLUMN_NAME = "cityId";
    public static final String CITY_STATUS_COLUMN_NAME = "cityStatus";
}
