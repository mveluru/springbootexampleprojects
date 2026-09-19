package org.bee.retail.repository;

import org.bee.retail.data.LoadProductData;
import org.bee.retail.product.Product;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
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
        return new ArrayList<>(productrepo.values());
    }
}
