package com.huiwu.temperaturecontrol.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huiwu.temperaturecontrol.bean.JSONModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HuiWu on 2015/11/27.
 */
public class SQLiteManage {
    public static SQLiteManage instance;
    private static TempSQLite sqLite;

    public static String TABLE_BOX = "Box";
    public static String TABLE_GOODS = "Goods";
    public static String TABLE_PICTURE = "Picture";
    public static String TABLE_RFIDGOOD = "RfidGood";
    public static String TABLE_TAG_INFO = "TagInfo";
    public static String TABLE_TMP_RFID = "TmpRfid";

    public static String RECORDS_INDEX = "_id";
    public static String RECORDS = "records";


    public SQLiteManage(Context context) {
        super();
        sqLite = new TempSQLite(context, 3);
    }

    public static SQLiteManage getInstance(Context context) {
        synchronized (SQLiteManage.class) {
            if (null == instance) {
                instance = new SQLiteManage(context);
            }
        }
        return instance;
    }

    /**
     * 初始化数据库数据
     *
     * @param boxes
     * @param goodses
     * @param rfidGoods
     * @param tmpRfids
     * @return
     */
    public boolean initData(JSONModel.Box[] boxes, JSONModel.Goods[] goodses, JSONModel.RfidGood[] rfidGoods, JSONModel.TmpRfid[] tmpRfids) {
        SQLiteDatabase db = sqLite.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            if (boxes != null && boxes.length != 0) {
                db.delete(TABLE_BOX, null, null);
                for (JSONModel.Box box : boxes) {
                    values.clear();
                    values.put("boxid", box.getBoxid());
                    values.put("companyid", box.getCompanyid());
                    values.put("boxno", box.getBoxno());
                    values.put("boxmemo", box.getBoxmemo());
                    values.put("isuse", box.isIsuse());
                    values.put("boxtype", box.getBoxtype());
                    values.put("linkuuid", box.getLinkuuid());
                    db.insert(TABLE_BOX, null, values);
                }
            }
            if (goodses != null && goodses.length != 0) {
                db.delete(TABLE_GOODS, null, null);
                for (JSONModel.Goods goods : goodses) {
                    values.clear();
                    values.put("id", goods.getId());
                    values.put("parentid", goods.getParentid());
                    values.put("companyid", goods.getCompanyid());
                    values.put("parentgoodtype", goods.getParentgoodtype());
                    values.put("goodtype", goods.getGoodtype());
                    values.put("hightmpnumber", goods.getHightmpnumber());
                    values.put("lowtmpnumber", goods.getLowtmpnumber());
                    values.put("highhumiditynumber", goods.getHighhumiditynumber());
                    values.put("lowhumiditynumber", goods.getLowhumiditynumber());
                    values.put("onetime", goods.getOnetime());
                    db.insert(TABLE_GOODS, null, values);
                }
            }
            if (rfidGoods != null && rfidGoods.length != 0) {
                for (JSONModel.RfidGood rfidGood : rfidGoods) {
                    values.clear();
                    values.put("companyid", rfidGood.getCompanyid());
                    values.put("rfidgoodid", rfidGood.getRfidgoodid());
                    values.put("rfidgoodname", rfidGood.getRfidgoodname());
                    Cursor cursor = db.query(TABLE_RFIDGOOD, new String[]{"rfidgoodname"}, "rfidgoodname = ?", new String[]{rfidGood.getRfidgoodname()}, null, null, null);
                    if (cursor.getCount() == 0) {
                        db.insert(TABLE_RFIDGOOD, null, values);
                    } else {
                        db.update(TABLE_RFIDGOOD, values, "rfidgoodname = ?", new String[]{rfidGood.getRfidgoodname()});
                    }
                }
            }
            if (tmpRfids != null && tmpRfids.length != 0) {
                for (JSONModel.TmpRfid tmpRfid : tmpRfids) {
                    values.clear();
                    values.put("companyid", tmpRfid.getCompanyid());
                    values.put("rfid", tmpRfid.getRfid());
                    values.put("isuse", tmpRfid.isIsuse());
                    values.put("iskill", tmpRfid.isIskill());
                    values.put("linkuuid", tmpRfid.getLinkuuid());
                    Cursor cursor = db.query(TABLE_TMP_RFID, new String[]{"rfid"}, "rfid = ?", new String[]{tmpRfid.getRfid()}, null, null, null);
                    if (cursor.getCount() == 0) {
                        db.insert(TABLE_TMP_RFID, null, values);
                    } else {
                        db.update(TABLE_TMP_RFID, values, "rfid = ?", new String[]{tmpRfid.getRfid()});
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 配置完成后插入数据库
     *
     * @param tagInfo
     */
    public void insertFirstTagInfo(JSONModel.TagInfo tagInfo) {
        SQLiteDatabase db = null;
        try {
            db = sqLite.getWritableDatabase();
            Gson gson = new Gson();
            ContentValues contentValues = new ContentValues();
            contentValues.put("uid", tagInfo.getUid());
            contentValues.put("linkuuid", tagInfo.getLinkuuid());
            contentValues.put("box", gson.toJson(tagInfo.getBox()));
            contentValues.put("goods", gson.toJson(tagInfo.getGoods()));
            contentValues.put("object", tagInfo.getObject());
            db.insert(TABLE_TAG_INFO, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 获取最近一次记录信息
     *
     * @param linkuuid
     * @param uid
     * @return
     */
    public JSONModel.TagInfo getLastTagInfo(String linkuuid, String uid) {
        Gson gson = new Gson();
        SQLiteDatabase db = null;
        try {
            db = sqLite.getWritableDatabase();
            JSONModel.TagInfo tagInfo = new JSONModel.TagInfo();
            Cursor cursor = db.query(TABLE_TAG_INFO, null, "linkuuid = ? and uid = ?", new String[]{linkuuid, uid}, null, null, "_id desc");
            if (cursor.getCount() == 0) {
                return tagInfo;
            }
            cursor.moveToFirst();
            JSONModel.Box box = null;
            try {
                box = gson.fromJson(cursor.getString(3), JSONModel.Box.class);
            } catch (Exception e) {

            }
            JSONModel.Goods goods = null;
            try {
                goods = gson.fromJson(cursor.getString(4), JSONModel.Goods.class);
            } catch (Exception e) {

            }
            tagInfo.setBox(box);
            tagInfo.setGoods(goods);
            tagInfo.setObject(cursor.getString(5));
            tagInfo.setStartTime(cursor.getLong(8));
            tagInfo.setEndTime(cursor.getLong(9));
            tagInfo.setTemp_min(cursor.getDouble(12));
            tagInfo.setTemp_max(cursor.getDouble(13));
            tagInfo.setHum_min(cursor.getDouble(15));
            tagInfo.setHum_max(cursor.getDouble(16));
            tagInfo.setOutLimit(cursor.getInt(18) == 1);
            return tagInfo;
        } catch (Exception e) {
            return new JSONModel.TagInfo();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void insertRecords(JSONModel.TagInfo tagInfo) {
        SQLiteDatabase database = sqLite.getWritableDatabase();
        try {
            Gson gson = new Gson();
            Cursor cursor = database.query(TABLE_TAG_INFO, null, "linkuuid = ? and uid = ?", new String[]{String.valueOf(tagInfo.getLinkuuid()), tagInfo.getUid()}, null, null, "_id desc", null);

            ContentValues values = new ContentValues();
            values.put("uid", tagInfo.getUid());
            values.put("linkuuid", tagInfo.getLinkuuid());
            values.put("box", gson.toJson(tagInfo.getBox()));
            values.put("goods", gson.toJson(tagInfo.getGoods()));
            values.put("object", tagInfo.getObject());
            values.put("readTime", tagInfo.getReadTime());
            values.put("justTemp", tagInfo.isJustTemp());
            values.put("readTime", tagInfo.getReadTime());
            values.put("startTime", tagInfo.getStartTime());
            values.put("endTime", tagInfo.getEndTime());
            values.put("power", tagInfo.getPower());
            values.put("recordStatus", tagInfo.getRecordStatus());
            values.put("tempMin", tagInfo.getTemp_min());
            values.put("tempMax", tagInfo.getTemp_max());
            values.put("tempNow", tagInfo.getTem_now());
            values.put("humMin", tagInfo.getHum_min());
            values.put("humMax", tagInfo.getHum_max());
            values.put("humNow", tagInfo.getHum_now());
            values.put("isOutLimit", tagInfo.isOutLimit());
            values.put("dataarray", gson.toJson(tagInfo.getTempList()));
            values.put("humidityArray", gson.toJson(tagInfo.getHumList()));
            values.put("roundCircle", tagInfo.getRoundCircle());
            values.put("number", tagInfo.getIndex());
            if (cursor.getCount() == 0) {
                database.insert(TABLE_TAG_INFO, null, values);
            } else {
                cursor.moveToNext();
                if (tagInfo.getEndTime() != cursor.getLong(9)) {
                    values.put("havepost", 0);
                }
                long startTime = cursor.getLong(8);
                if (Math.abs(startTime - tagInfo.getStartTime()) <= tagInfo.getGoods().getOnetime() * 60 * 1000L){
                    values.put("startTime", startTime);
                }
                database.update(TABLE_TAG_INFO, values, "startTime = ? and uid = ?", new String[]{String.valueOf(tagInfo.getStartTime()), tagInfo.getUid()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public void updateRecords(JSONModel.TagInfo tagInfo) {
        SQLiteDatabase db = null;
        try {
            Gson gson = new Gson();
            db = sqLite.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("box", gson.toJson(tagInfo.getBox()));
            values.put("goods", gson.toJson(tagInfo.getGoods()));
            values.put("object", tagInfo.getObject());
            db.update(TABLE_TAG_INFO, values, "linkuuid = ? and uid = ?", new String[]{tagInfo.getLinkuuid(), tagInfo.getUid()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void changeRecordStatus(JSONModel.TagInfo tagInfo) {
        SQLiteDatabase database = sqLite.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("havepost", 1);
            database.update(TABLE_TAG_INFO, values, "startTime = ? and uid = ?", new String[]{String.valueOf(tagInfo.getStartTime()), tagInfo.getUid()});
        } catch (Exception e) {

        } finally {
            if (database != null) {
                database.close();
            }
        }

    }

    public HashMap<String, Object> getDefaultSearchRecords(int _id) {
        ArrayList<JSONModel.TagInfo> records = new ArrayList<>();
        SQLiteDatabase database = null;
        HashMap<String, Object> map = new HashMap<>();
        try {
            database = sqLite.getReadableDatabase();
            Gson gson = new Gson();
            Cursor cursor = database.query(TABLE_TAG_INFO, null, "_id < ? and startTime != ?", new String[]{String.valueOf(_id), "0"}, null, null, "_id desc");
            if (cursor.getCount() == 0) {
                return null;
            }
            while (cursor.moveToNext()) {
                JSONModel.TagInfo tagInfo = new JSONModel.TagInfo();
                tagInfo.setUid(cursor.getString(1));
                tagInfo.setLinkuuid(cursor.getString(2));
                try {
                    tagInfo.setBox(gson.fromJson(cursor.getString(3), JSONModel.Box.class));
                } catch (Exception e) {

                }
                try {
                    tagInfo.setGoods(gson.fromJson(cursor.getString(4), JSONModel.Goods.class));
                } catch (Exception e) {

                }
                try {
                    tagInfo.setObject(cursor.getString(5));
                } catch (Exception e) {

                }
                tagInfo.setReadTime(cursor.getLong(7));
                tagInfo.setStartTime(cursor.getLong(8));
                tagInfo.setEndTime(cursor.getLong(9));
                tagInfo.setPower(cursor.getInt(10));
                tagInfo.setRecordStatus(cursor.getInt(11));
                tagInfo.setTemp_min(cursor.getInt(12));
                tagInfo.setTemp_max(cursor.getInt(13));
                tagInfo.setHum_min(cursor.getInt(15));
                tagInfo.setHum_max(cursor.getInt(16));
                tagInfo.setOutLimit(cursor.getInt(18) == 1);
                Type type = new TypeToken<ArrayList<Double>>() {
                }.getType();
                ArrayList<Double> tl = gson.fromJson(cursor.getString(19), type);
                tagInfo.setTempList(tl);
                ArrayList<Double> hl = gson.fromJson(cursor.getString(20), type);
                tagInfo.setHumList(hl);
                tagInfo.setJustTemp(cursor.getInt(21) == 1);
                tagInfo.setRoundCircle(cursor.getInt(22));
                tagInfo.setIndex(cursor.getInt(23));
                tagInfo.setHavepost(cursor.getInt(24) == 1);

                records.add(tagInfo);
                if (records.size() >= 10) {
                    map.put(RECORDS_INDEX, cursor.getInt(0));
                    map.put(RECORDS, records);
                    return map;
                }
            }
            map.put(RECORDS_INDEX, 0);
            map.put(RECORDS, records);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public void insertPicture(ContentValues values) {
        SQLiteDatabase db = null;
        try {
            db = sqLite.getWritableDatabase();
            db.insert(TABLE_PICTURE, null, values);
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

}
