/*
Navicat SQLite Data Transfer

Source Server         : temp
Source Server Version : 30808
Source Host           : :0

Target Server Type    : SQLite
Target Server Version : 30808
File Encoding         : 65001

Date: 2016-04-17 22:03:23
*/

PRAGMA foreign_keys = OFF;

-- ----------------------------
-- Table structure for android_metadata
-- ----------------------------
DROP TABLE IF EXISTS "main"."android_metadata";
CREATE TABLE android_metadata (locale TEXT);

-- ----------------------------
-- Table structure for Box
-- ----------------------------
DROP TABLE IF EXISTS "main"."Box";
CREATE TABLE "Box" (
"boxid"  INTEGER NOT NULL,
"companyid"  INTEGER NOT NULL,
"boxno"  TEXT NOT NULL,
"boxmemo"  TEXT NOT NULL,
"isuse"  INTEGER NOT NULL DEFAULT 0,
"boxtype"  TEXT NOT NULL,
"linkuuid"  TEXT
);

-- ----------------------------
-- Table structure for Goods
-- ----------------------------
DROP TABLE IF EXISTS "main"."Goods";
CREATE TABLE "Goods" (
"id"  INTEGER NOT NULL,
"parentid"  INTEGER NOT NULL,
"companyid"  INTEGER NOT NULL,
"parentgoodtype"  TEXT,
"goodtype"  TEXT NOT NULL,
"onetime"  INTEGER NOT NULL,
"hightmpnumber"  INTEGER NOT NULL,
"lowtmpnumber"  INTEGER NOT NULL,
"highhumiditynumber"  INTEGER NOT NULL,
"lowhumiditynumber"  INTEGER NOT NULL
);

-- ----------------------------
-- Table structure for Picture
-- ----------------------------
DROP TABLE IF EXISTS "main"."Picture";
CREATE TABLE "Picture" (
"_id"  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"boxno"  TEXT NOT NULL,
"linkuuid"  TEXT NOT NULL,
"file"  TEXT NOT NULL,
"sealOropen"  TEXT NOT NULL DEFAULT seal,
"havepost"  INTEGER NOT NULL DEFAULT 0
);

-- ----------------------------
-- Table structure for RfidGood
-- ----------------------------
DROP TABLE IF EXISTS "main"."RfidGood";
CREATE TABLE "RfidGood" (
"_id"  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"companyid"  INTEGER NOT NULL,
"rfidgoodid"  INTEGER,
"rfidgoodname"  TEXT NOT NULL
);

-- ----------------------------
-- Table structure for sqlite_sequence
-- ----------------------------
DROP TABLE IF EXISTS "main"."sqlite_sequence";
CREATE TABLE sqlite_sequence(name,seq);

-- ----------------------------
-- Table structure for TagInfo
-- ----------------------------
DROP TABLE IF EXISTS "main"."TagInfo";
CREATE TABLE "TagInfo" (
"_id"  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"uid"  TEXT NOT NULL,
"linkuuid"  TEXT DEFAULT NULL,
"box"  TEXT,
"goods"  TEXT,
"object"  TEXT,
"bover"  INTEGER NOT NULL DEFAULT 0,
"readTime"  TEXT DEFAULT 0,
"startTime"  TEXT DEFAULT 0,
"endTime"  TEXT DEFAULT 0,
"power"  INTEGER DEFAULT 0,
"recordStatus"  INTEGER DEFAULT 0,
"tempMin"  TEXT DEFAULT 0,
"tempMax"  TEXT DEFAULT 0,
"tempNow"  TEXT DEFAULT 0,
"humMin"  TEXT DEFAULT 0,
"humMax"  TEXT DEFAULT 0,
"humNow"  TEXT DEFAULT 0,
"isOutLimit"  INTEGER NOT NULL DEFAULT 0,
"dataarray"  TEXT,
"humidityArray"  TEXT,
"justTemp"  INTEGER NOT NULL DEFAULT 0,
"roundCircle"  INTEGER DEFAULT 0,
"number"  INTEGER DEFAULT 0,
"havepost"  INTEGER NOT NULL DEFAULT 0
);

-- ----------------------------
-- Table structure for TmpRfid
-- ----------------------------
DROP TABLE IF EXISTS "main"."TmpRfid";
CREATE TABLE "TmpRfid" (
"_id"  INTEGER PRIMARY KEY AUTOINCREMENT,
"companyid"  INTEGER NOT NULL,
"rfid"  TEXT NOT NULL,
"isuse"  INTEGER NOT NULL DEFAULT 0,
"iskill"  INTEGER NOT NULL DEFAULT 0,
"linkuuid"  TEXT
);
