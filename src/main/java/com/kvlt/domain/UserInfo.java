package com.kvlt.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author daishengkai
 * 2018-04-21 10:20
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 7355386696365031386L;

    private Integer id;

    private String userName;

    private Integer userAge;

    private String address;

    private Date addTime;

    private String remarks;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserAge() {
        return userAge;
    }

    public void setUserAge(Integer userAge) {
        this.userAge = userAge;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
