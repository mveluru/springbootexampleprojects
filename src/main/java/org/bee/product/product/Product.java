package org.bee.product.product;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Getter
@Setter
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



}
