package org.bee.product.service;

import org.bee.product.product.Product;
import org.bee.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    @Autowired
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
