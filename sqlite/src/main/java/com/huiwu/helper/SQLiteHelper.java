package com.huiwu.helper;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class SQLiteHelper {
    public static final String TABLE_BOX = "Box";
    public static final String TABLE_RFID_GOOD = "RfidGood";
    public static final String TABLE_GOODS_TYPE = "GoodsType";
    public static final String TABLE_PICTURE = "Picture";
    public static final String TABLE_TAG_INFO = "TagInfo";

    public static void main(String[] args) throws Exception {
        // 正如你所见的，你创建了一个用于添加实体（Entity）的模式（Schema）对象。
        // 两个参数分别代表：数据库版本号与自动生成代码的包路径。
        Schema schema = new Schema(1, "com.huiwu.temperaturecontrol.sqlite.bean");

//      当然，如果你愿意，你也可以分别指定生成的 Bean 与 DAO 类所在的目录，只要如下所示：
//      Schema schema = new Schema(1, "me.itangqi.bean");
//      schema.setDefaultJavaPackageDao("me.itangqi.dao");

        schema.setDefaultJavaPackageDao("com.huiwu.temperaturecontrol.sqlite.dao");

        // 模式（Schema）同时也拥有两个默认的 flags，分别用来标示 entity 是否是 activie 以及是否使用 keep sections。
        // schema2.enableActiveEntitiesByDefault();
        // schema2.enableKeepSectionsByDefault();

        // 一旦你拥有了一个 Schema 对象后，你便可以使用它添加实体（Entities）了。
        addRfidGood(schema);

//        addBox(schema);

        addGoodsType(schema);

        addPicture(schema);

        addRecordPicture(schema);

        // 最后我们将使用 DAOGenerator 类的 generateAll() 方法自动生成代码，此处你需要根据自己的情况更改输出目录（既之前创建的 java-gen)。
        // 其实，输出目录的路径可以在 build.gradle 中设置，有兴趣的朋友可以自行搜索，这里就不再详解。
        new DaoGenerator().generateAll(schema, "D:\\Android\\New\\Temperature\\data");
    }

    /**
     * @param schema
     */
    private static void addRfidGood(Schema schema) {
        // 一个实体（类）就关联到数据库中的一张表
        Entity rfidGood = schema.addEntity(TABLE_RFID_GOOD);
        // 你也可以重新给表命名
        // note.setTableName("NODE");

        // greenDAO 会自动根据实体类的属性值来创建表字段，并赋予默认值
        // 接下来你便可以设置表中的字段：
        rfidGood.addIdProperty();
        rfidGood.addStringProperty("rfidgoodid");
        rfidGood.addStringProperty("rfidgoodname");
        rfidGood.addStringProperty("companyid");
        rfidGood.addStringProperty("company");
        // 与在 Java 中使用驼峰命名法不同，默认数据库中的命名是使用大写和下划线来分割单词的。
        // For example, a property called “creationDate” will become a database column “CREATION_DATE”.
    }

    private static void addBox(Schema schema) {
        Entity box = schema.addEntity(TABLE_BOX);
        box.addLongProperty("boxid").primaryKey();
        box.addIntProperty("companyid");
        box.addStringProperty("boxno");
        box.addStringProperty("boxmemo");
        box.addStringProperty("createtime");
        box.addStringProperty("actuser");
        box.addBooleanProperty("isuse");
        box.addStringProperty("linkuuid");
        box.addIntProperty("orgna_id");
        box.addStringProperty("boxtype");
    }

    private static void addGoodsType(Schema schema) {
        Entity goodsType = schema.addEntity(TABLE_GOODS_TYPE);

        goodsType.addLongProperty("id").primaryKey();
        goodsType.addStringProperty("company");
        goodsType.addStringProperty("companyid");
        goodsType.addStringProperty("parentgoodtype");
        goodsType.addStringProperty("goodtype");
        goodsType.addStringProperty("onetime");
        goodsType.addStringProperty("hightmpnumber");
        goodsType.addStringProperty("lowtmpnumber");
        goodsType.addStringProperty("parentid");
        goodsType.addStringProperty("createtime");
        goodsType.addStringProperty("highhumiditynumber");
        goodsType.addStringProperty("lowhumiditynumber");
    }

    private static void addPicture(Schema schema) {
        Entity picture = schema.addEntity(TABLE_PICTURE);
        picture.addIdProperty();
        picture.addStringProperty("boxno");
        picture.addStringProperty("linkuuid");
        picture.addStringProperty("file");
        picture.addStringProperty("sealOropen");
        picture.addBooleanProperty("havepost");
    }

    private static void addRecordPicture(Schema schema) {
        Entity recordPicture = schema.addEntity(TABLE_TAG_INFO);
        recordPicture.addIdProperty();
        recordPicture.addStringProperty("uid");
        recordPicture.addStringProperty("linkuuid");
        recordPicture.addStringProperty("box");
        recordPicture.addStringProperty("goods");
        recordPicture.addStringProperty("object");
        recordPicture.addBooleanProperty("bover");
        recordPicture.addLongProperty("readTime");
        recordPicture.addLongProperty("startTime");
        recordPicture.addLongProperty("endTime");
        recordPicture.addIntProperty("delayTime");
        recordPicture.addIntProperty("power");
        recordPicture.addIntProperty("recordStatus");
        recordPicture.addDoubleProperty("tempMin");
        recordPicture.addDoubleProperty("tempMax");
        recordPicture.addDoubleProperty("humMin");
        recordPicture.addDoubleProperty("humMax");
        recordPicture.addBooleanProperty("isOutLimit");
        recordPicture.addStringProperty("dataarray");
        recordPicture.addStringProperty("humidityArray");
        recordPicture.addBooleanProperty("justTemp");
        recordPicture.addIntProperty("roundCircle");
        recordPicture.addIntProperty("number");
        recordPicture.addBooleanProperty("havepost");
    }
}
