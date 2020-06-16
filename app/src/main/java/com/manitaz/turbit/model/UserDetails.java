package com.manitaz.turbit.model;

import java.io.Serializable;

public class UserDetails implements Serializable {
    private int userId;
    private String email,name, password, mobile, address, gender, dob;
    private int employee_status;
    private CompanyDetails job_details;
    private int civil_status;
    private int kids;
    private int[] advertisement_cat_id;
    private int[] bank_id_list;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public CompanyDetails getJob_details() {
        return job_details;
    }

    public void setJob_details(CompanyDetails job_details) {
        this.job_details = job_details;
    }

    public int getCivil_status() {
        return civil_status;
    }

    public void setCivil_status(int civil_status) {
        this.civil_status = civil_status;
    }

    public int getKids() {
        return kids;
    }

    public void setKids(int kids) {
        this.kids = kids;
    }

    public int getEmployee_status() {
        return employee_status;
    }

    public void setEmployee_status(int employee_status) {
        this.employee_status = employee_status;
    }

    public int[] getAdvertisement_cat_id() {
        return advertisement_cat_id;
    }

    public void setAdvertisement_cat_id(int[] advertisement_cat_id) {
        this.advertisement_cat_id = advertisement_cat_id;
    }

    public int[] getBank_id_list() {
        return bank_id_list;
    }

    public void setBank_id_list(int[] bank_id_list) {
        this.bank_id_list = bank_id_list;
    }
}
