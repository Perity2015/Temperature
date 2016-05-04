package com.huiwu.temperaturecontrol.bean;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.huiwu.temperaturecontrol.bluetooth.BluetoothUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by HuiWu on 2016/4/11.
 */
public class JSONModel {

    public static class ReturnObject {
        private boolean bOK = false;
        private String sMsg = "";
        private Object m_ReturnOBJ = null;

        public boolean isbOK() {
            return bOK;
        }

        public void setbOK(boolean bOK) {
            this.bOK = bOK;
        }

        public String getsMsg() {
            return sMsg;
        }

        public void setsMsg(String sMsg) {
            this.sMsg = sMsg;
        }

        public Object getM_ReturnOBJ() {
            return m_ReturnOBJ;
        }

        public void setM_ReturnOBJ(Object m_ReturnOBJ) {
            this.m_ReturnOBJ = m_ReturnOBJ;
        }

        public JsonObject getM_ReturnOBJJsonObject() {
            Gson gson = new Gson();
            JsonObject jso = gson.fromJson(gson.toJson(this.getM_ReturnOBJ()), JsonObject.class);
            return jso;
        }

        public JsonArray getM_ReturnOBJJsonArray() {
            Gson gson = new Gson();
            JsonArray jsa = gson.fromJson(gson.toJson(this.getM_ReturnOBJ()), JsonArray.class);
            return jsa;
        }
    }

    public static class ReturnData {
        private int total;
        private JsonArray rows;
        private JsonArray data;
        private boolean bOK;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public JsonArray getRows() {
            return rows;
        }

        public void setRows(JsonArray rows) {
            this.rows = rows;
        }

        public JsonArray getData() {
            return data;
        }

        public void setData(JsonArray data) {
            this.data = data;
        }

        public boolean isbOK() {
            return bOK;
        }

        public void setbOK(boolean bOK) {
            this.bOK = bOK;
        }
    }

    public static class UserInfo {

        /**
         * LGKey : 7ed3d0f4bddf4744ba6ce42ec452b3b3
         * realname : 张冷冻
         * canuse : Y
         * powername : 冷链企业管理员
         * username : llcs
         * company : 上海冷链总公司
         * overtime : 2099/1/1 0:00:00
         * addusername : llhyadmin
         * orgna_name : 行政总部
         */

        private String LGKey;
        private String realname;
        private String canuse;
        private String powername;
        private String username;
        private String password;
        private String company;
        private String overtime;
        private String addusername;
        private String orgna_name;
        private boolean HaveAddBox;
        private UserPower userPower;

        public boolean isHaveAddBox() {
            return HaveAddBox;
        }

        public void setHaveAddBox(boolean haveAddBox) {
            HaveAddBox = haveAddBox;
        }

        public String getLGKey() {
            return LGKey;
        }

        public void setLGKey(String LGKey) {
            this.LGKey = LGKey;
        }

        public String getRealname() {
            return realname;
        }

        public void setRealname(String realname) {
            this.realname = realname;
        }

        public String getCanuse() {
            return canuse;
        }

        public void setCanuse(String canuse) {
            this.canuse = canuse;
        }

        public String getPowername() {
            return powername;
        }

        public void setPowername(String powername) {
            this.powername = powername;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getOvertime() {
            return overtime;
        }

        public void setOvertime(String overtime) {
            this.overtime = overtime;
        }

        public String getAddusername() {
            return addusername;
        }

        public void setAddusername(String addusername) {
            this.addusername = addusername;
        }

        public String getOrgna_name() {
            return orgna_name;
        }

        public void setOrgna_name(String orgna_name) {
            this.orgna_name = orgna_name;
        }

        public UserPower getUserPower() {
            return userPower;
        }

        public void setUserPower(UserPower userPower) {
            this.userPower = userPower;
        }

        public static class UserPower {

            /**
             * userid : 651
             * orgna_id : 240
             * jsid : 008101010
             * companyid : 95
             */

            private int userid;
            private int orgna_id;
            private String jsid;
            private int companyid;

            public int getUserid() {
                return userid;
            }

            public void setUserid(int userid) {
                this.userid = userid;
            }

            public int getOrgna_id() {
                return orgna_id;
            }

            public void setOrgna_id(int orgna_id) {
                this.orgna_id = orgna_id;
            }

            public String getJsid() {
                return jsid;
            }

            public void setJsid(String jsid) {
                this.jsid = jsid;
            }

            public int getCompanyid() {
                return companyid;
            }

            public void setCompanyid(int companyid) {
                this.companyid = companyid;
            }
        }
    }

    public static class Box implements Parcelable {

        /**
         * boxid : 25
         * boxno : 测试002
         * boxmemo : 测试002
         * linkuuid : 8a454920e01a4e229ed243db98186198
         * company : 上海冷链总公司
         * companyid : 95
         * createtime : /Date(1450347564993)/
         * actuser : 梁高峰
         * isuse : false
         * boxtype : 0
         * orgna_name : 安全配送部
         * orgna_id : 239
         */

        private int boxid;
        private String boxno;
        private String boxmemo;
        private String linkuuid;
        private String company;
        private int companyid;
        private String createtime;
        private String actuser;
        private boolean isuse;
        private String boxtype;
        private String orgna_name;
        private int orgna_id;

        public int getBoxid() {
            return boxid;
        }

        public void setBoxid(int boxid) {
            this.boxid = boxid;
        }

        public String getBoxno() {
            return boxno;
        }

        public void setBoxno(String boxno) {
            this.boxno = boxno;
        }

        public String getBoxmemo() {
            return boxmemo;
        }

        public void setBoxmemo(String boxmemo) {
            this.boxmemo = boxmemo;
        }

        public String getLinkuuid() {
            return linkuuid;
        }

        public void setLinkuuid(String linkuuid) {
            this.linkuuid = linkuuid;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public String getActuser() {
            return actuser;
        }

        public void setActuser(String actuser) {
            this.actuser = actuser;
        }

        public boolean isIsuse() {
            return isuse;
        }

        public void setIsuse(boolean isuse) {
            this.isuse = isuse;
        }

        public String getBoxtype() {
            return boxtype;
        }

        public void setBoxtype(String boxtype) {
            this.boxtype = boxtype;
        }

        public String getOrgna_name() {
            return orgna_name;
        }

        public void setOrgna_name(String orgna_name) {
            this.orgna_name = orgna_name;
        }

        public int getOrgna_id() {
            return orgna_id;
        }

        public void setOrgna_id(int orgna_id) {
            this.orgna_id = orgna_id;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.boxid);
            dest.writeString(this.boxno);
            dest.writeString(this.boxmemo);
            dest.writeString(this.linkuuid);
            dest.writeString(this.company);
            dest.writeInt(this.companyid);
            dest.writeString(this.createtime);
            dest.writeString(this.actuser);
            dest.writeByte(isuse ? (byte) 1 : (byte) 0);
            dest.writeString(this.boxtype);
            dest.writeString(this.orgna_name);
            dest.writeInt(this.orgna_id);
        }

        public Box() {
        }

        protected Box(Parcel in) {
            this.boxid = in.readInt();
            this.boxno = in.readString();
            this.boxmemo = in.readString();
            this.linkuuid = in.readString();
            this.company = in.readString();
            this.companyid = in.readInt();
            this.createtime = in.readString();
            this.actuser = in.readString();
            this.isuse = in.readByte() != 0;
            this.boxtype = in.readString();
            this.orgna_name = in.readString();
            this.orgna_id = in.readInt();
        }

        public static final Parcelable.Creator<Box> CREATOR = new Parcelable.Creator<Box>() {
            @Override
            public Box createFromParcel(Parcel source) {
                return new Box(source);
            }

            @Override
            public Box[] newArray(int size) {
                return new Box[size];
            }
        };
    }

    public static class Goods implements Parcelable {

        /**
         * id : 81
         * company : 上海冷链总公司
         * companyid : 95
         * parentgoodtype : test0
         * goodtype : test04
         * onetime : 1
         * hightmpnumber : 10.0
         * lowtmpnumber : -10.0
         * parentid : 77
         * createtime : /Date(1450495648033)/
         * highhumiditynumber : 50.0
         * lowhumiditynumber : 20.0
         */

        private int id;
        private String company;
        private int companyid;
        private String parentgoodtype = "未知";
        private String goodtype = "未知";
        private int onetime;
        private double hightmpnumber;
        private double lowtmpnumber;
        private int parentid;
        private String createtime;
        private double highhumiditynumber;
        private double lowhumiditynumber;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public String getParentgoodtype() {
            return parentgoodtype;
        }

        public void setParentgoodtype(String parentgoodtype) {
            this.parentgoodtype = parentgoodtype;
        }

        public String getGoodtype() {
            return goodtype;
        }

        public void setGoodtype(String goodtype) {
            this.goodtype = goodtype;
        }

        public int getOnetime() {
            return onetime;
        }

        public void setOnetime(int onetime) {
            this.onetime = onetime;
        }

        public double getHightmpnumber() {
            return hightmpnumber;
        }

        public void setHightmpnumber(double hightmpnumber) {
            this.hightmpnumber = hightmpnumber;
        }

        public double getLowtmpnumber() {
            return lowtmpnumber;
        }

        public void setLowtmpnumber(double lowtmpnumber) {
            this.lowtmpnumber = lowtmpnumber;
        }

        public int getParentid() {
            return parentid;
        }

        public void setParentid(int parentid) {
            this.parentid = parentid;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public double getHighhumiditynumber() {
            return highhumiditynumber;
        }

        public void setHighhumiditynumber(double highhumiditynumber) {
            this.highhumiditynumber = highhumiditynumber;
        }

        public double getLowhumiditynumber() {
            return lowhumiditynumber;
        }

        public void setLowhumiditynumber(double lowhumiditynumber) {
            this.lowhumiditynumber = lowhumiditynumber;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeString(this.company);
            dest.writeInt(this.companyid);
            dest.writeString(this.parentgoodtype);
            dest.writeString(this.goodtype);
            dest.writeInt(this.onetime);
            dest.writeDouble(this.hightmpnumber);
            dest.writeDouble(this.lowtmpnumber);
            dest.writeInt(this.parentid);
            dest.writeString(this.createtime);
            dest.writeDouble(this.highhumiditynumber);
            dest.writeDouble(this.lowhumiditynumber);
        }

        public Goods() {
        }

        protected Goods(Parcel in) {
            this.id = in.readInt();
            this.company = in.readString();
            this.companyid = in.readInt();
            this.parentgoodtype = in.readString();
            this.goodtype = in.readString();
            this.onetime = in.readInt();
            this.hightmpnumber = in.readDouble();
            this.lowtmpnumber = in.readDouble();
            this.parentid = in.readInt();
            this.createtime = in.readString();
            this.highhumiditynumber = in.readDouble();
            this.lowhumiditynumber = in.readDouble();
        }

        public static final Parcelable.Creator<Goods> CREATOR = new Parcelable.Creator<Goods>() {
            @Override
            public Goods createFromParcel(Parcel source) {
                return new Goods(source);
            }

            @Override
            public Goods[] newArray(int size) {
                return new Goods[size];
            }
        };
    }

    public static class Lock implements Parcelable {

        @Override
        public String toString() {
            return "Lock{" +
                    "newPwd=" + newPwd +
                    ", lockpwd='" + lockpwd + '\'' +
                    ", firstpwd='" + firstpwd + '\'' +
                    '}';
        }

        /**
         * newPwd : false
         * lockpwd : 28881574a1e5423d
         * firstpwd : FEIJU LOCK
         */



        private boolean newPwd;
        private String lockpwd;
        private String firstpwd;

        public boolean isNewPwd() {
            return newPwd;
        }

        public void setNewPwd(boolean newPwd) {
            this.newPwd = newPwd;
        }

        public String getLockpwd() {
            return lockpwd;
        }

        public void setLockpwd(String lockpwd) {
            this.lockpwd = lockpwd;
        }

        public String getFirstpwd() {
            return firstpwd;
        }

        public void setFirstpwd(String firstpwd) {
            this.firstpwd = firstpwd;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(newPwd ? (byte) 1 : (byte) 0);
            dest.writeString(this.lockpwd);
            dest.writeString(this.firstpwd);
        }

        public Lock() {
        }

        protected Lock(Parcel in) {
            this.newPwd = in.readByte() != 0;
            this.lockpwd = in.readString();
            this.firstpwd = in.readString();
        }

        public static final Parcelable.Creator<Lock> CREATOR = new Parcelable.Creator<Lock>() {
            @Override
            public Lock createFromParcel(Parcel source) {
                return new Lock(source);
            }

            @Override
            public Lock[] newArray(int size) {
                return new Lock[size];
            }
        };
    }

    public static class RfidGood implements Parcelable, Comparable {

        /**
         * rfidgoodid : 10891
         * rfidgoodname : 物行天下冷库01
         * company : 湖北物行天下供应链服务有限公司
         * companyid : 182
         * tradecode : 008
         */

        private int rfidgoodid;
        private String rfidgoodname;
        private String company;
        private int companyid;
        private String tradecode;

        public int getRfidgoodid() {
            return rfidgoodid;
        }

        public void setRfidgoodid(int rfidgoodid) {
            this.rfidgoodid = rfidgoodid;
        }

        public String getRfidgoodname() {
            return rfidgoodname;
        }

        public void setRfidgoodname(String rfidgoodname) {
            this.rfidgoodname = rfidgoodname;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public String getTradecode() {
            return tradecode;
        }

        public void setTradecode(String tradecode) {
            this.tradecode = tradecode;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.rfidgoodid);
            dest.writeString(this.rfidgoodname);
            dest.writeString(this.company);
            dest.writeInt(this.companyid);
            dest.writeString(this.tradecode);
        }

        public RfidGood() {
        }

        protected RfidGood(Parcel in) {
            this.rfidgoodid = in.readInt();
            this.rfidgoodname = in.readString();
            this.company = in.readString();
            this.companyid = in.readInt();
            this.tradecode = in.readString();
        }

        public static final Parcelable.Creator<RfidGood> CREATOR = new Parcelable.Creator<RfidGood>() {
            @Override
            public RfidGood createFromParcel(Parcel source) {
                return new RfidGood(source);
            }

            @Override
            public RfidGood[] newArray(int size) {
                return new RfidGood[size];
            }
        };

        @Override
        public int compareTo(Object another) {
            if (another instanceof RfidGood) {
                RfidGood rfidGood = (RfidGood) another;
                return rfidgoodname.compareTo(rfidGood.getRfidgoodname());
            }
            return 0;
        }
    }

    public static class TmpRfid {

        /**
         * company : 湖北物行天下供应链服务有限公司
         * rfid : E0025CEB9CC47C36
         * isuse : false
         * iskill : false
         * id : 44
         * companyid : 182
         * linkuuid : e5b647c10f1c429b941bc1db2edeb2b3
         */

        private String company;
        private String rfid;
        private boolean isuse;
        private boolean iskill;
        private int id;
        private int companyid;
        private String linkuuid;

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getRfid() {
            return rfid;
        }

        public void setRfid(String rfid) {
            this.rfid = rfid;
        }

        public boolean isIsuse() {
            return isuse;
        }

        public void setIsuse(boolean isuse) {
            this.isuse = isuse;
        }

        public boolean isIskill() {
            return iskill;
        }

        public void setIskill(boolean iskill) {
            this.iskill = iskill;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public String getLinkuuid() {
            return linkuuid;
        }

        public void setLinkuuid(String linkuuid) {
            this.linkuuid = linkuuid;
        }
    }

    public static class TagInfo implements Parcelable {
        private int delayTime;
        private String object = "未知";
        private Box box;
        private Goods goods;
        private String uid;
        private String linkuuid;
        private long readTime;
        private long startTime;
        private long endTime;
        private int power;
        private int recordStatus;
        private double temp_min;
        private double temp_max;
        private double hum_min;
        private double hum_max;
        private double tem_now;
        private double hum_now;
        private boolean isOutLimit;
        private ArrayList<Double> tempList;
        private ArrayList<Double> humList;
        private Double[] dataArray;
        private Double[] humidityArray;
        private boolean havepost;
        private boolean justTemp;
        private int roundCircle;
        private int index;

        @Override
        public String toString() {
            return "TagInfo{" +
                    "delayTime=" + delayTime +
                    ", object='" + object + '\'' +
                    ", box=" + box +
                    ", goods=" + goods +
                    ", uid='" + uid + '\'' +
                    ", linkuuid='" + linkuuid + '\'' +
                    ", readTime=" + readTime +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", power=" + power +
                    ", recordStatus=" + recordStatus +
                    ", temp_min=" + temp_min +
                    ", temp_max=" + temp_max +
                    ", hum_min=" + hum_min +
                    ", hum_max=" + hum_max +
                    ", tem_now=" + tem_now +
                    ", hum_now=" + hum_now +
                    ", isOutLimit=" + isOutLimit +
                    ", tempList=" + tempList +
                    ", humList=" + humList +
                    ", dataArray=" + Arrays.toString(dataArray) +
                    ", humidityArray=" + Arrays.toString(humidityArray) +
                    ", havepost=" + havepost +
                    ", justTemp=" + justTemp +
                    ", roundCircle=" + roundCircle +
                    ", index=" + index +
                    '}';
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public int getDelayTime() {
            return delayTime;
        }

        public void setDelayTime(int delayTime) {
            this.delayTime = delayTime;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public Box getBox() {
            return box;
        }

        public void setBox(Box box) {
            this.box = box;
        }

        public Goods getGoods() {
            return goods;
        }

        public void setGoods(Goods goods) {
            this.goods = goods;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getLinkuuid() {
            return linkuuid;
        }

        public void setLinkuuid(String linkuuid) {
            this.linkuuid = linkuuid;
        }

        public long getReadTime() {
            return readTime;
        }

        public void setReadTime(long readTime) {
            this.readTime = readTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public int getPower() {
            return power;
        }

        public void setPower(int power) {
            this.power = power;
        }

        public int getRecordStatus() {
            return recordStatus;
        }

        public void setRecordStatus(int recordStatus) {
            this.recordStatus = recordStatus;
        }

        public double getTemp_min() {
            return temp_min;
        }

        public void setTemp_min(double temp_min) {
            this.temp_min = temp_min;
        }

        public double getTemp_max() {
            return temp_max;
        }

        public void setTemp_max(double temp_max) {
            this.temp_max = temp_max;
        }

        public double getHum_min() {
            return hum_min;
        }

        public void setHum_min(double hum_min) {
            this.hum_min = hum_min;
        }

        public double getHum_max() {
            return hum_max;
        }

        public void setHum_max(double hum_max) {
            this.hum_max = hum_max;
        }

        public double getTem_now() {
            return tem_now;
        }

        public void setTem_now(double tem_now) {
            this.tem_now = tem_now;
        }

        public double getHum_now() {
            return hum_now;
        }

        public void setHum_now(double hum_now) {
            this.hum_now = hum_now;
        }

        public boolean isOutLimit() {
            return isOutLimit;
        }

        public void setOutLimit(boolean outLimit) {
            isOutLimit = outLimit;
        }

        public ArrayList<Double> getTempList() {
            return tempList;
        }

        public void setTempList(ArrayList<Double> tempList) {
            this.tempList = tempList;
        }

        public ArrayList<Double> getHumList() {
            return humList;
        }

        public void setHumList(ArrayList<Double> humList) {
            this.humList = humList;
        }

        public Double[] getDataArray() {
            return dataArray;
        }

        public void setDataArray(Double[] dataArray) {
            this.dataArray = dataArray;
        }

        public Double[] getHumidityArray() {
            return humidityArray;
        }

        public void setHumidityArray(Double[] humidityArray) {
            this.humidityArray = humidityArray;
        }

        public boolean isHavepost() {
            return havepost;
        }

        public void setHavepost(boolean havepost) {
            this.havepost = havepost;
        }

        public boolean isJustTemp() {
            return justTemp;
        }

        public void setJustTemp(boolean justTemp) {
            this.justTemp = justTemp;
        }

        public int getRoundCircle() {
            return roundCircle;
        }

        public void setRoundCircle(int roundCircle) {
            this.roundCircle = roundCircle;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public TagInfo() {
            super();
            tempList = new ArrayList<>();
            humList = new ArrayList<>();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.delayTime);
            dest.writeString(this.object);
            dest.writeParcelable(this.box, flags);
            dest.writeParcelable(this.goods, flags);
            dest.writeString(this.uid);
            dest.writeString(this.linkuuid);
            dest.writeLong(this.readTime);
            dest.writeLong(this.startTime);
            dest.writeLong(this.endTime);
            dest.writeInt(this.power);
            dest.writeInt(this.recordStatus);
            dest.writeDouble(this.temp_min);
            dest.writeDouble(this.temp_max);
            dest.writeDouble(this.hum_min);
            dest.writeDouble(this.hum_max);
            dest.writeDouble(this.tem_now);
            dest.writeDouble(this.hum_now);
            dest.writeByte(isOutLimit ? (byte) 1 : (byte) 0);
            dest.writeList(this.tempList);
            dest.writeList(this.humList);
            dest.writeArray(this.dataArray);
            dest.writeArray(this.humidityArray);
            dest.writeByte(havepost ? (byte) 1 : (byte) 0);
            dest.writeByte(justTemp ? (byte) 1 : (byte) 0);
            dest.writeInt(this.roundCircle);
            dest.writeInt(this.index);
        }

        protected TagInfo(Parcel in) {
            this.delayTime = in.readInt();
            this.object = in.readString();
            this.box = in.readParcelable(Box.class.getClassLoader());
            this.goods = in.readParcelable(Goods.class.getClassLoader());
            this.uid = in.readString();
            this.linkuuid = in.readString();
            this.readTime = in.readLong();
            this.startTime = in.readLong();
            this.endTime = in.readLong();
            this.power = in.readInt();
            this.recordStatus = in.readInt();
            this.temp_min = in.readDouble();
            this.temp_max = in.readDouble();
            this.hum_min = in.readDouble();
            this.hum_max = in.readDouble();
            this.tem_now = in.readDouble();
            this.hum_now = in.readDouble();
            this.isOutLimit = in.readByte() != 0;
            this.tempList = new ArrayList<Double>();
            in.readList(this.tempList, Double.class.getClassLoader());
            this.humList = new ArrayList<Double>();
            in.readList(this.humList, Double.class.getClassLoader());
            this.dataArray = (Double[]) in.readArray(Double[].class.getClassLoader());
            this.humidityArray = (Double[]) in.readArray(Double[].class.getClassLoader());
            this.havepost = in.readByte() != 0;
            this.justTemp = in.readByte() != 0;
            this.roundCircle = in.readInt();
            this.index = in.readInt();
        }

        public static final Creator<TagInfo> CREATOR = new Creator<TagInfo>() {
            @Override
            public TagInfo createFromParcel(Parcel source) {
                return new TagInfo(source);
            }

            @Override
            public TagInfo[] newArray(int size) {
                return new TagInfo[size];
            }
        };
    }

    public static class TempLink implements Parcelable {

        /**
         * boxtype : 1
         * sealrfid :
         * opened : true
         * sealpic :
         * openedpic :
         * iserror : true
         * errormsg :
         * sealaddr :
         * openedaddr :
         * sealtime : /Date(1459928959263)/
         * openedtime : /Date(1459928959263)/
         * sealactuser :
         * sealrealname :
         * openedactuser :
         * openedrealname :
         * company : 上海冷链总公司
         * linkuuid : 8a454920e01a4e229ed243db98186198
         * rfid : E0025C97E3054153
         * carno : 沪B88999
         * boxid : 25
         * boxno : 测试002
         * companyid : 95
         * lowtmpnumber : -18
         * hightmpnumber : -1
         * goodtype : 冷冻运输
         * goodchildtype : 冰淇淋
         * beginaddr : 中国上海市徐汇区宜山北路68号
         * endaddr : 中国上海市徐汇区宜山北路68号
         * begintime : /Date(1451269005000)/
         * endtime : /Date(1456126462353)/
         * onetime : 1
         * bover : true
         * haveunusual : true
         * actrealname : 张冷冻
         * actuser : llcs
         * number : 3
         */

        private String boxtype;
        private String sealrfid;
        private boolean opened;
        private String sealpic;
        private String openedpic;
        private boolean iserror;
        private String errormsg;
        private String sealaddr;
        private String openedaddr;
        private String sealtime;
        private String openedtime;
        private String sealactuser;
        private String sealrealname;
        private String openedactuser;
        private String openedrealname;
        private String company;
        private String linkuuid;
        private String rfid;
        private String carno;
        private int boxid;
        private String boxno;
        private int companyid;
        private int lowtmpnumber;
        private int hightmpnumber;
        private String goodtype;
        private String goodchildtype;
        private String beginaddr;
        private String endaddr;
        private String begintime;
        private String endtime;
        private int onetime;
        private boolean bover;
        private boolean haveunusual;
        private String actrealname;
        private String actuser;
        private int number;

        public String getBoxtype() {
            return boxtype;
        }

        public void setBoxtype(String boxtype) {
            this.boxtype = boxtype;
        }

        public String getSealrfid() {
            return sealrfid;
        }

        public void setSealrfid(String sealrfid) {
            this.sealrfid = sealrfid;
        }

        public boolean isOpened() {
            return opened;
        }

        public void setOpened(boolean opened) {
            this.opened = opened;
        }

        public String getSealpic() {
            return sealpic;
        }

        public void setSealpic(String sealpic) {
            this.sealpic = sealpic;
        }

        public String getOpenedpic() {
            return openedpic;
        }

        public void setOpenedpic(String openedpic) {
            this.openedpic = openedpic;
        }

        public boolean isIserror() {
            return iserror;
        }

        public void setIserror(boolean iserror) {
            this.iserror = iserror;
        }

        public String getErrormsg() {
            return errormsg;
        }

        public void setErrormsg(String errormsg) {
            this.errormsg = errormsg;
        }

        public String getSealaddr() {
            return sealaddr;
        }

        public void setSealaddr(String sealaddr) {
            this.sealaddr = sealaddr;
        }

        public String getOpenedaddr() {
            return openedaddr;
        }

        public void setOpenedaddr(String openedaddr) {
            this.openedaddr = openedaddr;
        }

        public String getSealtime() {
            return sealtime;
        }

        public void setSealtime(String sealtime) {
            this.sealtime = sealtime;
        }

        public String getOpenedtime() {
            return openedtime;
        }

        public void setOpenedtime(String openedtime) {
            this.openedtime = openedtime;
        }

        public String getSealactuser() {
            return sealactuser;
        }

        public void setSealactuser(String sealactuser) {
            this.sealactuser = sealactuser;
        }

        public String getSealrealname() {
            return sealrealname;
        }

        public void setSealrealname(String sealrealname) {
            this.sealrealname = sealrealname;
        }

        public String getOpenedactuser() {
            return openedactuser;
        }

        public void setOpenedactuser(String openedactuser) {
            this.openedactuser = openedactuser;
        }

        public String getOpenedrealname() {
            return openedrealname;
        }

        public void setOpenedrealname(String openedrealname) {
            this.openedrealname = openedrealname;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getLinkuuid() {
            return linkuuid;
        }

        public void setLinkuuid(String linkuuid) {
            this.linkuuid = linkuuid;
        }

        public String getRfid() {
            return rfid;
        }

        public void setRfid(String rfid) {
            this.rfid = rfid;
        }

        public String getCarno() {
            return carno;
        }

        public void setCarno(String carno) {
            this.carno = carno;
        }

        public int getBoxid() {
            return boxid;
        }

        public void setBoxid(int boxid) {
            this.boxid = boxid;
        }

        public String getBoxno() {
            return boxno;
        }

        public void setBoxno(String boxno) {
            this.boxno = boxno;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public int getLowtmpnumber() {
            return lowtmpnumber;
        }

        public void setLowtmpnumber(int lowtmpnumber) {
            this.lowtmpnumber = lowtmpnumber;
        }

        public int getHightmpnumber() {
            return hightmpnumber;
        }

        public void setHightmpnumber(int hightmpnumber) {
            this.hightmpnumber = hightmpnumber;
        }

        public String getGoodtype() {
            return goodtype;
        }

        public void setGoodtype(String goodtype) {
            this.goodtype = goodtype;
        }

        public String getGoodchildtype() {
            return goodchildtype;
        }

        public void setGoodchildtype(String goodchildtype) {
            this.goodchildtype = goodchildtype;
        }

        public String getBeginaddr() {
            return beginaddr;
        }

        public void setBeginaddr(String beginaddr) {
            this.beginaddr = beginaddr;
        }

        public String getEndaddr() {
            return endaddr;
        }

        public void setEndaddr(String endaddr) {
            this.endaddr = endaddr;
        }

        public String getBegintime() {
            return begintime;
        }

        public void setBegintime(String begintime) {
            this.begintime = begintime;
        }

        public String getEndtime() {
            return endtime;
        }

        public void setEndtime(String endtime) {
            this.endtime = endtime;
        }

        public int getOnetime() {
            return onetime;
        }

        public void setOnetime(int onetime) {
            this.onetime = onetime;
        }

        public boolean isBover() {
            return bover;
        }

        public void setBover(boolean bover) {
            this.bover = bover;
        }

        public boolean isHaveunusual() {
            return haveunusual;
        }

        public void setHaveunusual(boolean haveunusual) {
            this.haveunusual = haveunusual;
        }

        public String getActrealname() {
            return actrealname;
        }

        public void setActrealname(String actrealname) {
            this.actrealname = actrealname;
        }

        public String getActuser() {
            return actuser;
        }

        public void setActuser(String actuser) {
            this.actuser = actuser;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.boxtype);
            dest.writeString(this.sealrfid);
            dest.writeByte(opened ? (byte) 1 : (byte) 0);
            dest.writeString(this.sealpic);
            dest.writeString(this.openedpic);
            dest.writeByte(iserror ? (byte) 1 : (byte) 0);
            dest.writeString(this.errormsg);
            dest.writeString(this.sealaddr);
            dest.writeString(this.openedaddr);
            dest.writeString(this.sealtime);
            dest.writeString(this.openedtime);
            dest.writeString(this.sealactuser);
            dest.writeString(this.sealrealname);
            dest.writeString(this.openedactuser);
            dest.writeString(this.openedrealname);
            dest.writeString(this.company);
            dest.writeString(this.linkuuid);
            dest.writeString(this.rfid);
            dest.writeString(this.carno);
            dest.writeInt(this.boxid);
            dest.writeString(this.boxno);
            dest.writeInt(this.companyid);
            dest.writeInt(this.lowtmpnumber);
            dest.writeInt(this.hightmpnumber);
            dest.writeString(this.goodtype);
            dest.writeString(this.goodchildtype);
            dest.writeString(this.beginaddr);
            dest.writeString(this.endaddr);
            dest.writeString(this.begintime);
            dest.writeString(this.endtime);
            dest.writeInt(this.onetime);
            dest.writeByte(bover ? (byte) 1 : (byte) 0);
            dest.writeByte(haveunusual ? (byte) 1 : (byte) 0);
            dest.writeString(this.actrealname);
            dest.writeString(this.actuser);
            dest.writeInt(this.number);
        }

        public TempLink() {
        }

        protected TempLink(Parcel in) {
            this.boxtype = in.readString();
            this.sealrfid = in.readString();
            this.opened = in.readByte() != 0;
            this.sealpic = in.readString();
            this.openedpic = in.readString();
            this.iserror = in.readByte() != 0;
            this.errormsg = in.readString();
            this.sealaddr = in.readString();
            this.openedaddr = in.readString();
            this.sealtime = in.readString();
            this.openedtime = in.readString();
            this.sealactuser = in.readString();
            this.sealrealname = in.readString();
            this.openedactuser = in.readString();
            this.openedrealname = in.readString();
            this.company = in.readString();
            this.linkuuid = in.readString();
            this.rfid = in.readString();
            this.carno = in.readString();
            this.boxid = in.readInt();
            this.boxno = in.readString();
            this.companyid = in.readInt();
            this.lowtmpnumber = in.readInt();
            this.hightmpnumber = in.readInt();
            this.goodtype = in.readString();
            this.goodchildtype = in.readString();
            this.beginaddr = in.readString();
            this.endaddr = in.readString();
            this.begintime = in.readString();
            this.endtime = in.readString();
            this.onetime = in.readInt();
            this.bover = in.readByte() != 0;
            this.haveunusual = in.readByte() != 0;
            this.actrealname = in.readString();
            this.actuser = in.readString();
            this.number = in.readInt();
        }

        public static final Parcelable.Creator<TempLink> CREATOR = new Parcelable.Creator<TempLink>() {
            @Override
            public TempLink createFromParcel(Parcel source) {
                return new TempLink(source);
            }

            @Override
            public TempLink[] newArray(int size) {
                return new TempLink[size];
            }
        };
    }

    public static class GatherRecord {

        /**
         * createtime : 2016/4/18 13:55:37
         * haveunusual : False
         * cntuuid : fe03bcd866a7457a8cbcdae8246f40b4
         */

        private String createtime;
        private String haveunusual;
        private String cntuuid;

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public String getHaveunusual() {
            return haveunusual;
        }

        public void setHaveunusual(String haveunusual) {
            this.haveunusual = haveunusual;
        }

        public String getCntuuid() {
            return cntuuid;
        }

        public void setCntuuid(String cntuuid) {
            this.cntuuid = cntuuid;
        }
    }

    public static class TempData {

        /**
         * hightmpnumber : 28.00
         * highhumiditynumber : 30.00
         * lowtmpnumber : 20.00
         * haveunusual : False
         * lowhumiditynumber : 20.00
         * tmpnumber : 26.66
         * humiditynumber : -99.00
         * recordtime : 2016/4/18 4:02:33
         */

        private double hightmpnumber;
        private double highhumiditynumber;
        private double lowtmpnumber;
        private String haveunusual;
        private double lowhumiditynumber;
        private double tmpnumber;
        private double humiditynumber;
        private String recordtime;

        public double getHightmpnumber() {
            return hightmpnumber;
        }

        public void setHightmpnumber(double hightmpnumber) {
            this.hightmpnumber = hightmpnumber;
        }

        public double getHighhumiditynumber() {
            return highhumiditynumber;
        }

        public void setHighhumiditynumber(double highhumiditynumber) {
            this.highhumiditynumber = highhumiditynumber;
        }

        public double getLowtmpnumber() {
            return lowtmpnumber;
        }

        public void setLowtmpnumber(double lowtmpnumber) {
            this.lowtmpnumber = lowtmpnumber;
        }

        public String getHaveunusual() {
            return haveunusual;
        }

        public void setHaveunusual(String haveunusual) {
            this.haveunusual = haveunusual;
        }

        public double getLowhumiditynumber() {
            return lowhumiditynumber;
        }

        public void setLowhumiditynumber(double lowhumiditynumber) {
            this.lowhumiditynumber = lowhumiditynumber;
        }

        public double getTmpnumber() {
            return tmpnumber;
        }

        public void setTmpnumber(double tmpnumber) {
            this.tmpnumber = tmpnumber;
        }

        public double getHumiditynumber() {
            return humiditynumber;
        }

        public void setHumiditynumber(double humiditynumber) {
            this.humiditynumber = humiditynumber;
        }

        public String getRecordtime() {
            return recordtime;
        }

        public void setRecordtime(String recordtime) {
            this.recordtime = recordtime;
        }
    }

    public static class BLETag {
        private String address;
        private String remark;
        private int rssi;
        private boolean config_status;
        private boolean record_status;

        @Override
        public String toString() {
            return remark + "\n" + (config_status ? "配置成功" : "未配置") + "\n" + (record_status ? "开始记录" : "未记录") + "\nRSSI:" + rssi;
        }

        public String getStatus() {
            return config_status ? record_status ? "配置成功 正在记录" : "配置成功 未记录" : "未配置";
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BLETag) {
                return this.address.equals(((BLETag) o).address);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }

        public BLETag() {
        }

        public BLETag(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.rssi = rssi;
            this.address = device.getAddress();
            //[2, 1, 6, 14, -1, -1, -1, 7, -98, -15, -14, -13, -12, 4, 116, 101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
            byte[] statusBytes = BluetoothUtil.byteToBitBytes(scanRecord[8]);

            this.config_status = statusBytes[3] == 0x01;
            this.record_status = statusBytes[1] == 0x01;
            if (statusBytes[4] == 0) {
                this.remark = this.address;
            } else if (scanRecord[13] == 0) {
                this.remark = BluetoothUtil.bytesToHexString(new byte[]{scanRecord[9], scanRecord[10], scanRecord[11], scanRecord[12]});
            } else {
                byte[] remarkBytes = new byte[scanRecord[13]];
                System.arraycopy(scanRecord, 14, remarkBytes, 0, remarkBytes.length);
                this.remark = new String(remarkBytes, Charset.forName("GB2312"));
            }
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }

        public boolean isConfig_status() {
            return config_status;
        }

        public void setConfig_status(boolean config_status) {
            this.config_status = config_status;
        }

        public boolean isRecord_status() {
            return record_status;
        }

        public void setRecord_status(boolean record_status) {
            this.record_status = record_status;
        }
    }
}
