package org.bee.orders.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BriteOrdersViewConfigValuesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void appConfigValues_returnsConfiguredValues() throws Exception {
        mockMvc.perform(get("/v1/orders/app-config/values"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectionPoolSize").value("10"))
                .andExpect(jsonPath("$.timeoutInSeconds").value("1"));
    }

    @Test
    void emailConfigValues_returnsConfiguredValues() throws Exception {
        mockMvc.perform(get("/v1/orders/email-config/values"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value("true"))
                .andExpect(jsonPath("$.fromAddress").value("no-reply@britesolutions.org"))
                .andExpect(jsonPath("$.supportAddress").value("britesupport@britesolutions.org"))
                .andExpect(jsonPath("$.dailyLimit").value("200"));
    }

    @Test
    void smsConfigValues_returnsConfiguredValues() throws Exception {
        mockMvc.perform(get("/v1/orders/sms-confi/values"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value("false"))
                .andExpect(jsonPath("$.sender-id").value("customerGM"))
                .andExpect(jsonPath("$.dailyLimit").value("100"));
    }
}
