package org.bee.retail.controller;

import org.bee.retail.product.Product;
import org.bee.retail.service.ProductService;
import org.bee.retail.webclient.ProductUserWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/product")
public class ProductController {
    private ProductService productService;

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(path = "/productmessage")
    public @ResponseBody String productmessage() {
        ProductUserWebClient producuserwebclient = new ProductUserWebClient();
        producuserwebclient.getproductUserData();
        return "Product Messages";
    }

    @PostMapping(path = "/addproduct")
    public @ResponseBody Product addproduct(@RequestBody Product product) {
        return productService.addproduct(product);
    }

    @GetMapping(path = "/productId/{productId}")
    public ResponseEntity<Product> findById(@PathVariable String productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }


    @GetMapping(path = "/allproducts")
    public @ResponseBody Iterable<Product> getAllProducts() {
        return productService.findAll();
    }
}
