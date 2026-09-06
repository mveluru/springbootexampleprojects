package org.bee.product.data;

import org.bee.product.product.Product;
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

        productrepo.put(String.valueOf(101), product1);
        productrepo.put(String.valueOf(102), product2);
        productrepo.put(String.valueOf(103), product3);
        productrepo.put(String.valueOf(104), product4);
        productrepo.put(String.valueOf(105), product5);
        productrepo.put(String.valueOf(106), product6);
        productrepo.put(String.valueOf(107), product7);
        productrepo.put(String.valueOf(108), product8);
        productrepo.put(String.valueOf(109), product9);
        productrepo.put(String.valueOf(110), product10);
        productrepo.put(String.valueOf(111), product11);
        productrepo.put(String.valueOf(112), product12);
        productrepo.put(String.valueOf(113), product13);
        productrepo.put(String.valueOf(114), product14);
        productrepo.put(String.valueOf(115), product15);
        productrepo.put(String.valueOf(116), product16);

        return productrepo;
    }
}
