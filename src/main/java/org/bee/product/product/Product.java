package org.bee.product.product;
import org.springframework.lang.NonNull;

public class Product {
    private String productId;


    private String productName;
    private String quantity;
    private float  price;

    public Product(String productId, String productName, String productquantity, float price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = productquantity;
        this.price = price;
    }

    public Product() {

    }

    public String
    getProductId() {
        return productId;
    }


    public void setProductId(@NonNull String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
