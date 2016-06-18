package com.huiwu.temperaturecontrol.application;

import android.app.Application;
import android.app.Service;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Vibrator;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.huiwu.model.utils.CrashHandler;
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


    public AMapLocationClient locationClient = null;
    public AMapLocationClientOption locationOption = null;
    public MyLocationListener mMyLocationListener;
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
    }

    private void setupDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "temperature-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        daoMaster = new DaoMaster(db);
    }

    public DaoSession getDaoSession() {
        return daoMaster.newSession();
    }

    private void initOkHttp() {
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
                if (locationText != null) {
                    locationText.setText(aMapLocation.getAddress());
                }
                LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                latLng = LocationUtils.convertGToGPS(getApplicationContext(), latLng);
                aMapLocation.setLatitude(latLng.latitude);
                aMapLocation.setLongitude(latLng.longitude);
                bdLocation = aMapLocation;
            }
        }
    }


}
