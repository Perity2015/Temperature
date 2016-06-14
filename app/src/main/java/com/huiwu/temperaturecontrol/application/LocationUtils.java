package com.huiwu.temperaturecontrol.application;

import android.content.Context;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;

import java.math.BigDecimal;

public class LocationUtils {

    public static LatLng convertGToGPS(Context context, LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        double latitude = 2 * sourceLatLng.latitude - desLatLng.latitude;
        double longitude = 2 * sourceLatLng.longitude - desLatLng.longitude;
        BigDecimal bdLatitude = new BigDecimal(latitude);
        bdLatitude = bdLatitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        BigDecimal bdLongitude = new BigDecimal(longitude);
        bdLongitude = bdLongitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        return new LatLng(bdLatitude.doubleValue(), bdLongitude.doubleValue());
    }

    public static LatLng convertGPSToG(Context context, LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        return converter.convert();
    }

}
