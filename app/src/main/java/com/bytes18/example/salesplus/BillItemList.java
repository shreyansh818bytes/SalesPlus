package com.bytes18.example.salesplus;

public class BillItemList {

    private String itemName;
    private String itemBarcode;
    private String itemCodename;
    private String itemPrice;
    private String itemAmount;
    private String itemQuantity;

    public BillItemList(String itemName, String itemBarcode, String itemCodename, String itemPrice, String itemAmount, String itemQuantity) {
        this.itemName = itemName;
        this.itemBarcode = itemBarcode;
        this.itemCodename = itemCodename;
        this.itemPrice = itemPrice;
        this.itemAmount = itemAmount;
        this.itemQuantity = itemQuantity;
    }

    public String getItemCodename() {
        return itemCodename;
    }

    public void setItemCodename(String itemCodename) {
        this.itemCodename = itemCodename;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(String itemAmount) {
        this.itemAmount = itemAmount;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }
}
