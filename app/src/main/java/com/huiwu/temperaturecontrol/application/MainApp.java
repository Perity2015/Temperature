package com.huiwu.temperaturecontrol.application;

import android.app.Application;
import android.app.Service;
import android.nfc.Tag;
import android.os.Vibrator;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.huiwu.model.utils.CrashHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.math.BigDecimal;

/**
 * Created by HuiWu on 2016/4/11.
 */
public class MainApp extends Application {
    private Tag currentTag;
    private String uid;
    private String techno;
    private String manufacturer;
    private String productName;
    private String dsfid;
    private String afi;
    private String memorySize;
    private String blockSize;
    private String icReference;
    private boolean basedOnTwoBytesAddress;
    private boolean MultipleReadSupported;
    private boolean MemoryExceed2048bytesSize;

    public void setCurrentTag(Tag currentTag) {
        this.currentTag = currentTag;
    }

    public Tag getCurrentTag() {
        return currentTag;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid.toUpperCase();
    }

    public void setTechno(String techno) {
        this.techno = techno;
    }

    public String getTechno() {
        return techno;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setDsfid(String dsfid) {
        this.dsfid = dsfid;
    }

    public String getDsfid() {
        return dsfid;
    }

    public void setAfi(String afi) {
        this.afi = afi;
    }

    public String getAfi() {
        return afi;
    }

    public void setMemorySize(String memorySize) {
        this.memorySize = memorySize;
    }

    public String getMemorySize() {
        return memorySize;
    }

    public void setBlockSize(String blockSize) {
        this.blockSize = blockSize;
    }

    public String getBlockSize() {
        return blockSize;
    }

    public void setIcReference(String icReference) {
        this.icReference = icReference;
    }

    public String getIcReference() {
        return icReference;
    }

    public void setBasedOnTwoBytesAddress(boolean basedOnTwoBytesAddress) {
        this.basedOnTwoBytesAddress = basedOnTwoBytesAddress;
    }

    public boolean isBasedOnTwoBytesAddress() {
        return basedOnTwoBytesAddress;
    }

    public void setMultipleReadSupported(boolean MultipleReadSupported) {
        this.MultipleReadSupported = MultipleReadSupported;
    }

    public boolean isMultipleReadSupported() {
        return MultipleReadSupported;
    }

    public void setMemoryExceed2048bytesSize(boolean MemoryExceed2048bytesSize) {
        this.MemoryExceed2048bytesSize = MemoryExceed2048bytesSize;
    }

    public boolean isMemoryExceed2048bytesSize() {
        return MemoryExceed2048bytesSize;
    }


    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;
    public Vibrator mVibrator;
    public TextView locationText;
    public BDLocation bdLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance(getApplicationContext());
        crashHandler.init(getApplicationContext());

        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(10000);
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationPoiList(true);
        mLocationClient.setLocOption(option);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);
        ImageLoader.getInstance().init(configuration);
    }

    public void startLocation() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    public void stopLocation() {
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {
                if (locationText != null) {
                    locationText.setText(location.getAddrStr());
                }
//                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
//                latLng = convertBaiduToGPS(latLng);
//                location.setLatitude(latLng.latitude);
//                location.setLongitude(latLng.longitude);
            }
            bdLocation = location;
        }
    }

    public LatLng convertBaiduToGPS(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
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

    public LatLng convertGPSToBaidu(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        return converter.convert();
    }
}
