package com.huiwu.temperaturecontrol.sqlite;

import android.content.Context;

import com.huiwu.temperaturecontrol.sqlite.bean.GoodsType;
import com.huiwu.temperaturecontrol.sqlite.bean.Picture;
import com.huiwu.temperaturecontrol.sqlite.bean.RfidGood;
import com.huiwu.temperaturecontrol.sqlite.bean.TagInfo;
import com.huiwu.temperaturecontrol.sqlite.dao.DaoSession;
import com.huiwu.temperaturecontrol.sqlite.dao.GoodsTypeDao;
import com.huiwu.temperaturecontrol.sqlite.dao.PictureDao;
import com.huiwu.temperaturecontrol.sqlite.dao.RfidGoodDao;
import com.huiwu.temperaturecontrol.sqlite.dao.TagInfoDao;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by HuiWu on 2015/11/27.
 */
public class SQLiteManage {
    public static SQLiteManage instance;

    public SQLiteManage(Context context) {
        super();
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
     * @param daoSession
     * @param rfidGoods
     */
    public void insertRfidGoods(DaoSession daoSession, RfidGood[] rfidGoods) {
        for (RfidGood rfidGood : rfidGoods) {
            Query query = daoSession.getRfidGoodDao().queryBuilder()
                    .where(RfidGoodDao.Properties.Rfidgoodname.eq(rfidGood.getRfidgoodname()))
                    .build();
            List<RfidGood> list = query.list();
            if (list.size() == 0) {
                daoSession.getRfidGoodDao().insert(rfidGood);
            } else {
                rfidGood.setId(list.get(0).getId());
                daoSession.getRfidGoodDao().update(rfidGood);
            }
        }
    }

    /**
     * @param daoSession
     * @param goodsTypes
     */
    public void insertGoodsTypes(DaoSession daoSession, GoodsType[] goodsTypes) {
        for (GoodsType goodsType : goodsTypes) {
            Query query = daoSession.getGoodsTypeDao().queryBuilder()
                    .where(GoodsTypeDao.Properties.Id.eq(goodsType.getId()))
                    .build();
            List<GoodsType> list = query.list();
            if (list.size() == 0) {
                daoSession.getGoodsTypeDao().insert(goodsType);
            } else {
                daoSession.getGoodsTypeDao().update(goodsType);
            }
        }
    }

    /**
     * @param daoSession
     * @param tagInfo
     */
    public void insertConfigTagInfo(DaoSession daoSession, TagInfo tagInfo) {
        QueryBuilder queryBuilder = daoSession.getTagInfoDao().queryBuilder();
        queryBuilder.where(queryBuilder.and(TagInfoDao.Properties.Uid.eq(tagInfo.getUid()), TagInfoDao.Properties.Linkuuid.eq(tagInfo.getLinkuuid())));
        queryBuilder.orderDesc(TagInfoDao.Properties.Id);
        Query query = queryBuilder.build();
        List<TagInfo> tagInfos = query.list();
        if (tagInfos.size() > 0) {
            tagInfo.setId(tagInfos.get(0).getId());
            daoSession.getTagInfoDao().update(tagInfo);
        } else {
            daoSession.getTagInfoDao().insert(tagInfo);
        }
    }

    /**
     * @param daoSession
     * @param linkuuid
     * @param uid
     * @return
     */
    public TagInfo getConfigTagInfo(DaoSession daoSession, String linkuuid, String uid) {
        QueryBuilder queryBuilder = daoSession.getTagInfoDao().queryBuilder();
        queryBuilder.where(queryBuilder.and(TagInfoDao.Properties.Uid.eq(uid), TagInfoDao.Properties.Linkuuid.eq(linkuuid)));
        queryBuilder.orderDesc(TagInfoDao.Properties.Id);
        Query query = queryBuilder.build();
        List<TagInfo> tagInfos = query.list();
        if (tagInfos.size() > 0) {
            TagInfo tagInfo = tagInfos.get(0);
            tagInfo.setHavepost(false);
            return tagInfo;
        } else {
            TagInfo tagInfo = new TagInfo();
            tagInfo.setLinkuuid(linkuuid);
            tagInfo.setUid(uid);
            return tagInfo;
        }
    }

    /**
     * @param daoSession
     * @param tagInfo
     */
    public void updateConfigTagInfoStatus(DaoSession daoSession, TagInfo tagInfo) {
        QueryBuilder queryBuilder = daoSession.getTagInfoDao().queryBuilder();
        queryBuilder.where(queryBuilder.and(TagInfoDao.Properties.Uid.eq(tagInfo.getUid()), TagInfoDao.Properties.Linkuuid.eq(tagInfo.getLinkuuid())));
        queryBuilder.orderDesc(TagInfoDao.Properties.Id);
        Query query = queryBuilder.build();
        List<TagInfo> tagInfos = query.list();
        if (tagInfos.size() > 0) {
            tagInfo.setId(tagInfos.get(0).getId());
            daoSession.getTagInfoDao().update(tagInfo);
        } else {
            daoSession.getTagInfoDao().insert(tagInfo);
        }
    }

    /**
     * @param daoSession
     * @param id
     * @return
     */
    public ArrayList<TagInfo> getConfigTagInfos(DaoSession daoSession, long id) {
        QueryBuilder queryBuilder = daoSession.getTagInfoDao().queryBuilder();
        queryBuilder.where(queryBuilder.and(TagInfoDao.Properties.StartTime.gt(0), TagInfoDao.Properties.Id.lt(id)));
        queryBuilder.orderDesc(TagInfoDao.Properties.Id);
        queryBuilder.limit(10);
        Query query = queryBuilder.build();
        return (ArrayList<TagInfo>) query.list();
    }

    /**
     * @param daoSession
     * @param picture
     */
    public void insertPicture(DaoSession daoSession, Picture picture) {
        daoSession.getPictureDao().insert(picture);
    }


    /**
     * @param daoSession
     * @return
     */
    public ArrayList<Picture> getNotPostPictures(DaoSession daoSession) {
        Query query = daoSession.getPictureDao().queryBuilder()
                .where(PictureDao.Properties.Havepost.eq(false))
                .build();
        return (ArrayList<Picture>) query.list();
    }

}
