package org.bee.retail.service;

import org.bee.retail.product.Product;
import org.bee.retail.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private ProductRepository productRepository;

    @Autowired
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addproduct(Product product) {
        return productRepository.addproduct(product);

    }

    public Product findById(String productid) {
        return productRepository.findById(productid);
    }

    public Iterable<Product> findAll(){
        return productRepository.findAll();
    }
}
