package com.manitaz.turbit.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class OfferModel implements Serializable {
    private int id;
    private String company_name;
    private String content;
    private int notification_type;
    private Date start_time;
    private Date end_date;
    private String logo_company;
    private String image_publisher;
    private List<String> categories;
    private boolean btnLike = false;
    private boolean btnDislike = false;
    private boolean btnUseful = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(int notification_type) {
        this.notification_type = notification_type;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public String getLogo_company() {
        return logo_company;
    }

    public void setLogo_company(String logo_company) {
        this.logo_company = logo_company;
    }

    public String getImage_publisher() {
        return image_publisher;
    }

    public void setImage_publisher(String image_publisher) {
        this.image_publisher = image_publisher;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public boolean isBtnLike() {
        return btnLike;
    }

    public void setBtnLike(boolean btnLike) {
        this.btnLike = btnLike;
    }

    public boolean isBtnDislike() {
        return btnDislike;
    }

    public void setBtnDislike(boolean btnDislike) {
        this.btnDislike = btnDislike;
    }

    public boolean isBtnUseful() {
        return btnUseful;
    }

    public void setBtnUseful(boolean btnUseful) {
        this.btnUseful = btnUseful;
    }
}
