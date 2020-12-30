package com.bytes18.example.salesplus;

public class StocklistItem {

    private String itemName;
    private String itemCodename;
    private String itemPrice;
    private String itemStock;

    public StocklistItem(String itemName, String itemCodename, String itemPrice, String itemStock) {
        this.itemName = itemName;
        this.itemCodename = itemCodename;
        this.itemPrice = itemPrice;
        this.itemStock = itemStock;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public String getItemStock() {
        return itemStock;
    }

    public void setItemStock(String itemStock) {
        this.itemStock = itemStock;
    }
}
