package com.example.meter;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ZenoApiService {
    private static final Logger logger = LoggerFactory.getLogger(ZenoApiService.class);

    @Value("${zeno.api.url:https://api.zeno.africa}")
    private String apiUrl;

    @Value("${zeno.api.order-status-url:https://api.zeno.africa/order-status}")
    private String orderStatusUrl;

    @Value("${zeno.api.key}")
    private String apiKey;

    @Value("${zeno.api.secret}")
    private String secretKey;

    @Value("${zeno.api.account-id:zp23627108}")
    private String accountId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ZenoApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String createOrder(CreateOrderRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> params = new HashMap<>();
            params.put("create_order", "1");
            params.put("buyer_email", request.getBuyerEmail());
            params.put("buyer_name", request.getBuyerName());
            params.put("buyer_phone", request.getBuyerPhone());
            params.put("amount", String.valueOf(request.getAmount()));
            params.put("account_id", accountId);
            params.put("api_key", apiKey);
            params.put("secret_key", secretKey);

            String formData = buildFormData(params);
            HttpEntity<String> entity = new HttpEntity<>(formData, headers);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            logger.info("Order creation response: {}", response);
            return response;
        } catch (RestClientException e) {
            logger.error("Error creating order: {}", e.getMessage());
            throw new ZenoApiException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public OrderStatusResponse checkOrderStatus(String orderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> params = new HashMap<>();
            params.put("check_status", "1");
            params.put("order_id", orderId);
            params.put("api_key", apiKey);
            params.put("secret_key", secretKey);

            String formData = buildFormData(params);
            HttpEntity<String> entity = new HttpEntity<>(formData, headers);

            String response = restTemplate.postForObject(orderStatusUrl, entity, String.class);
            logger.info("Order status response: {}", response);

            return objectMapper.readValue(response, OrderStatusResponse.class);
        } catch (Exception e) {
            logger.error("Error checking order status: {}", e.getMessage());
            throw new ZenoApiException("Failed to check order status: " + e.getMessage(), e);
        }
    }

    private String buildFormData(Map<String, String> params) {
        StringBuilder formData = new StringBuilder();
        params.forEach((key, value) -> {
            if (formData.length() > 0) {
                formData.append('&');
            }
            formData.append(key).append('=').append(value);
        });
        return formData.toString();
    }
}