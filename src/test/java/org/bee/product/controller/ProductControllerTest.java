package org.bee.product.controller;

import org.bee.orders.SpringBootProjectsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SpringBootProjectsApplication.class)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllProducts_returnsSeededProducts() throws Exception {
        mockMvc.perform(get("/v1/product/allproducts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getProductById_existingId_returnsProduct() throws Exception {
        mockMvc.perform(get("/v1/product/productId/101"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("101"))
                .andExpect(jsonPath("$.productName").value("American 24K_Gold"));
    }

    @Test
    void getProductById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/v1/product/productId/does-not-exist"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void addProduct_returnsCreatedProduct() throws Exception {
        String newProductJson = """
                {
                    "productId": "118",
                    "productName": "American Diamond",
                    "quantity": "10ct",
                    "price": 500000.0
                }
                """;

        mockMvc.perform(post("/v1/product/addproduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("118"))
                .andExpect(jsonPath("$.productName").value("American Diamond"));
    }

    @Test
    void productMessage_returnsConfirmationText() throws Exception {
        mockMvc.perform(get("/v1/product/productmessage"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Product Messages"));
    }
}
