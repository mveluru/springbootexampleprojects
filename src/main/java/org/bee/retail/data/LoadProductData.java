package org.bee.retail.data;

import org.bee.retail.product.Product;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoadProductData {
    private static Map<String, Product> productrepo = new HashMap<>();

    public static Map<String, Product> loadproddata() {

        Product product1 = new Product("101", "American 24K_Gold", "1kg", 200000);
        Product product2 = new Product("102", "American 22K_Gold", "1kg", 200000);
        Product product3 = new Product("103", "American 20K_Gold", "1kg", 200000);
        Product product4 = new Product("104", "American 18k_Gold", "1kg", 200000);
        Product product5 = new Product("105", "American 1g_Gold", "1kg", 11000);
        Product product6 = new Product("106", "American Silver", "1kg", 100000);
        Product product7 = new Product("107", "American Copper", "1kg", 1000);
        Product product8 = new Product("108", "American Bronze", "1kg", 10000);
        Product product9 = new Product("109", "Canadian 21K_Gold", "1kg", 199000);
        Product product10 = new Product("110", "Canadian 22K_Gold", "1kg", 198000);
        Product product11 = new Product("112", "Canadian 20K_Gold", "1kg", 190000);
        Product product12 = new Product("113", "Canadian 18k_Gold", "1kg", 218000);
        Product product13 = new Product("114", "Canadian 1g_Gold", "1kg", 11000);
        Product product14 = new Product("115", "Canadian Silver", "1kg", 100000);
        Product product15 = new Product("116", "Canadian Copper", "1kg", 1000);
        Product product16 = new Product("117", "Canadian Bronze", "1kg", 10000);

        productrepo.put(product1.getProductId(), product1);
        productrepo.put(product2.getProductId(), product2);
        productrepo.put(product3.getProductId(), product3);
        productrepo.put(product4.getProductId(), product4);
        productrepo.put(product5.getProductId(), product5);
        productrepo.put(product6.getProductId(), product6);
        productrepo.put(product7.getProductId(), product7);
        productrepo.put(product8.getProductId(), product8);
        productrepo.put(product9.getProductId(), product9);
        productrepo.put(product10.getProductId(), product10);
        productrepo.put(product11.getProductId(), product11);
        productrepo.put(product12.getProductId(), product12);
        productrepo.put(product13.getProductId(), product13);
        productrepo.put(product14.getProductId(), product14);
        productrepo.put(product15.getProductId(), product15);
        productrepo.put(product16.getProductId(), product16);

        return productrepo;
    }
}
