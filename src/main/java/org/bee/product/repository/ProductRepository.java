package org.bee.product.repository;

import org.bee.product.data.LoadProductData;
import org.bee.product.product.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductRepository {
    private static Map<String, Product> productrepo = LoadProductData.loadproddata();

    public Product addproduct(Product product) {

        productrepo.put(product.getProductId(), product);
        return productrepo.get(product.getProductId());
    }

    public Product findById(String productid) {
        return productrepo.get(productid);
    }

    public List<Product> findAll(){
        return productrepo.entrySet().stream().map(e->e.getValue()).collect(Collectors.toList());
    }
}
