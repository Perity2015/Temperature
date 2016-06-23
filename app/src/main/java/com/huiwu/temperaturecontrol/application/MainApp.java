package com.huiwu.temperaturecontrol.application;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.utils.CrashHandler;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.nfc.Helper;
import com.huiwu.temperaturecontrol.service.SyncService;
import com.huiwu.temperaturecontrol.sqlite.dao.DaoMaster;
import com.huiwu.temperaturecontrol.sqlite.dao.DaoSession;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cookie.store.PersistentCookieStore;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;

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

    /**
     * 解析Tag属性信息
     *
     * @param GetSystemInfoResponse
     * @return
     */
    public boolean DecodeGetSystemInfoResponse(byte[] GetSystemInfoResponse) {
        //if the tag has returned a good response
        if (GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12) {
            String uidToString = "";
            byte[] uid = new byte[8];
            // change uid format from byteArray to a String
            for (int i = 1; i <= 8; i++) {
                uid[i - 1] = GetSystemInfoResponse[10 - i];
                uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
            }

            //***** TECHNO ******
//            mainApp.setUid(uidToString);
            if (uid[0] == (byte) 0xE0)
                setTechno("ISO 15693");
            else if (uid[0] == (byte) 0xD0)
                setTechno("ISO 14443");
            else
                setTechno("Unknown techno");

            //***** MANUFACTURER ****
            if (uid[1] == (byte) 0x02)
                setManufacturer("STMicroelectronics");
            else if (uid[1] == (byte) 0x04)
                setManufacturer("NXP");
            else if (uid[1] == (byte) 0x07)
                setManufacturer("Texas Instrument");
            else
                setManufacturer("Unknown manufacturer");

            //**** PRODUCT NAME *****
            if (uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07) {
                setProductName("LRI512");
                setMultipleReadSupported(false);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17) {
                setProductName("LRI64");
                setMultipleReadSupported(false);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23) {
                setProductName("LRI2K");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B) {
                setProductName("LRIS2K");
                setMultipleReadSupported(false);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F) {
                setProductName("M24LR64");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43) {
                setProductName("LRI1K");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47) {
                setProductName("LRIS64K");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B) {
                setProductName("M24LR01E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F) {
                setProductName("M24LR16E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
                if (isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53) {
                setProductName("M24LR02E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57) {
                setProductName("M24LR32E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
                if (isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B) {
                setProductName("M24LR04E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F) {
                setProductName("M24LR64E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
                if (isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63) {
                setProductName("M24LR08E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67) {
                setProductName("M24LR128E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
                if (isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F) {
                setProductName("M24LR256E");
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
                if (isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB) {
                setProductName("detected product");
                setBasedOnTwoBytesAddress(true);
                setMultipleReadSupported(true);
                setMemoryExceed2048bytesSize(true);
            } else {
                setProductName("Unknown product");
                setBasedOnTwoBytesAddress(false);
                setMultipleReadSupported(false);
                setMemoryExceed2048bytesSize(false);
            }

            //*** DSFID ***
            setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));

            //*** AFI ***
            setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));

            //*** MEMORY SIZE ***
            if (isBasedOnTwoBytesAddress()) {
                String temp = new String();
                temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
                temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
                setMemorySize(temp);
            } else
                setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));

            //*** BLOCK SIZE ***
            if (isBasedOnTwoBytesAddress())
                setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
            else
                setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

            //*** IC REFERENCE ***
            if (isBasedOnTwoBytesAddress())
                setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
            else
                setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));

            return true;
        }

        //if the tag has returned an error code
        else
            return false;
    }


    public AMapLocationClient locationClient = null;
    public AMapLocationClientOption locationOption = null;
    public Vibrator mVibrator;
    public TextView locationText;
    public AMapLocation bdLocation;

    public DaoMaster daoMaster;

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance(getApplicationContext());
        crashHandler.init(getApplicationContext());

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

        setupDatabase();

        initOkHttp();

        initImageLoader();

        initLocation();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        SyncBroadcastReceiver receiver = new SyncBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    private void setupDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constants.DB_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        daoMaster = new DaoMaster(db);
    }

    public DaoSession getDaoSession() {
        if (daoMaster == null) {
            setupDatabase();
        }
        return daoMaster.newSession();
    }

    private void initOkHttp() {
        ConnectionUtil.getInstance().debug(false);
        //必须调用初始化
        OkHttpUtils.init(this);
//        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //所有的 header 都 不支持 中文
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        HttpParams params = new HttpParams();
//        params.put("lat", String.valueOf(desLatLng.latitude));     //所有的 params 都 支持 中文
//        params.put("lng", String.valueOf(desLatLng.longitude));
//        params.put("address", address);
        //以下都不是必须的，根据需要自行选择
        OkHttpUtils.getInstance()//
                .debug("OkHttpUtils")                                              //是否打开调试
                .setConnectTimeout(OkHttpUtils.DEFAULT_MILLISECONDS)               //全局的连接超时时间
                .setReadTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS)                  //全局的读取超时时间
                .setWriteTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS)                 //全局的写入超时时间
//                .setCookieStore(new MemoryCookieStore())                           //cookie使用内存缓存（app退出后，cookie消失）
//                .addCommonParams(params)
                .setCookieStore(new PersistentCookieStore());                    //cookie持久化存储，如果cookie不过期，则一直有效
    }

    private void initImageLoader() {
        File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());  //缓存文件夹路径
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(360, 640) // default = device screen dimensions 内存缓存文件的最大长宽
                .threadPoolSize(3) // default  线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2) // default 设置当前线程的优先级
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024)) //可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)  // 内存缓存的最大值
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiscCache(cacheDir)) // default 可以自定义缓存路径
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb sd卡(本地)缓存的最大值
                .diskCacheFileCount(100)  // 可以缓存的文件数量
                // default为使用HASHCODE对UIL进行加密命名， 还可以用MD5(new Md5FileNameGenerator())加密
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(getApplicationContext())) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs() // 打印debug log
                .build(); //开始构建
        ImageLoader.getInstance().init(config);
    }

    private void initLocation() {
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();

        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(false);
        locationOption.setNeedAddress(true);
        locationOption.setGpsFirst(true);
        locationOption.setInterval(10000);
        locationClient.setLocationOption(locationOption);

        // 设置定位监听
        locationClient.setLocationListener(new MyLocationListener());
    }

    public void startLocation() {
        if (!locationClient.isStarted()) {
            locationClient.startLocation();
        }
    }

    public void stopLocation() {
        if (locationClient.isStarted()) {
            locationClient.stopLocation();
        }
    }

    public class MyLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (TextUtils.isEmpty(aMapLocation.getAddress())) {
                    aMapLocation.setAddress("未获取定位信息");
                }
                if (locationText != null) {
                    locationText.setText(aMapLocation.getAddress());
                }
                LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                latLng = convertGToGPS(getApplicationContext(), latLng);
                aMapLocation.setLatitude(latLng.latitude);
                aMapLocation.setLongitude(latLng.longitude);
                bdLocation = aMapLocation;
            }
        }

    }

    public static LatLng convertGToGPS(Context context, LatLng sourceLatLng) {
        com.amap.api.maps.CoordinateConverter converter = new com.amap.api.maps.CoordinateConverter(context);
        converter.from(com.amap.api.maps.CoordinateConverter.CoordType.GPS);
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
        com.amap.api.maps.CoordinateConverter converter = new com.amap.api.maps.CoordinateConverter(context);
        converter.from(com.amap.api.maps.CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        return converter.convert();
    }

    private class SyncBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                Calendar calendar = Calendar.getInstance();
                int minute = calendar.get(Calendar.MINUTE);
                if (minute % 5 == 0) {
                    SyncService.startActionSync(context);
                }
            }
        }
    }
}
