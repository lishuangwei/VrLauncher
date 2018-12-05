package com.xunixianshi.vrlanucher.vrui;

/**
 * Created by Administrator on 2017/10/9.
 */

public class BuyInfoObj {
    int buyType;//购买类型  1 视频 2 会员
    String buyResourceId; // 资源id  如果是视频 传id  如果是会员传""空字符
    String buyMessage;  //购买信息
    String buyPrice;    //购买价格
    String buyDeviceName; //购买设备名字
    private int productId; // 产品id
    private int packageId; // 包id

    public BuyInfoObj() {
    }

    public BuyInfoObj(int buyType, String buyResourceId, String buyMessage, String buyPrice, String buyDeviceName) {
        this.buyType = buyType;
        this.buyResourceId = buyResourceId;
        this.buyMessage = buyMessage;
        this.buyPrice = buyPrice;
        this.buyDeviceName = buyDeviceName;
    }

    public BuyInfoObj(int buyType, String buyMessage, String buyPrice, String buyDeviceName, int productId, int packageId) {
        this.buyType = buyType;
        this.buyMessage = buyMessage;
        this.buyPrice = buyPrice;
        this.buyDeviceName = buyDeviceName;
        this.productId = productId;
        this.packageId = packageId;
    }

    public int getBuyType() {
        return buyType;
    }

    public void setBuyType(int buyType) {
        this.buyType = buyType;
    }

    public String getBuyResourceId() {
        return buyResourceId;
    }

    public void setBuyResourceId(String buyResourceId) {
        this.buyResourceId = buyResourceId;
    }

    public String getBuyMessage() {
        return buyMessage;
    }

    public void setBuyMessage(String buyMessage) {
        this.buyMessage = buyMessage;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(String buyPrice) {
        this.buyPrice = buyPrice;
    }

    public String getBuyDeviceName() {
        return buyDeviceName;
    }

    public void setBuyDeviceName(String buyDeviceName) {
        this.buyDeviceName = buyDeviceName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }
}
