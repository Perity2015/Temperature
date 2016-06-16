package com.huiwu.temperaturecontrol.sqlite.bean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

/**
 * Entity mapped to table "RFID_GOOD".
 */
public class RfidGood {

    private Long id;
    private Integer rfidgoodid;
    private String rfidgoodname;
    private Integer companyid;
    private String company;

    public RfidGood() {
    }

    public RfidGood(Long id) {
        this.id = id;
    }

    public RfidGood(Long id, Integer rfidgoodid, String rfidgoodname, Integer companyid, String company) {
        this.id = id;
        this.rfidgoodid = rfidgoodid;
        this.rfidgoodname = rfidgoodname;
        this.companyid = companyid;
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRfidgoodid() {
        return rfidgoodid;
    }

    public void setRfidgoodid(Integer rfidgoodid) {
        this.rfidgoodid = rfidgoodid;
    }

    public String getRfidgoodname() {
        return rfidgoodname;
    }

    public void setRfidgoodname(String rfidgoodname) {
        this.rfidgoodname = rfidgoodname;
    }

    public Integer getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Integer companyid) {
        this.companyid = companyid;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

}