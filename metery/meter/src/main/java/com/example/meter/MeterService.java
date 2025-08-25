package com.example.meter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MeterService {
    private static final Logger logger = LoggerFactory.getLogger(MeterService.class);

    private final Map<String, Map<String, Object>> meterData = new HashMap<>();

    private final MeterRepository meterRepository;
    private final TokenRepository tokenRepository;
    private final UserReporsitory userRepository;
    private final ZenoApiService zenoApiService;
    private final ObjectMapper objectMapper;

    public MeterService(MeterRepository meterRepository, TokenRepository tokenRepository,
                        UserReporsitory userRepository, ZenoApiService zenoApiService,
                        ObjectMapper objectMapper) {
        this.meterRepository = meterRepository;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.zenoApiService = zenoApiService;
        this.objectMapper = objectMapper;
    }

    public MeterResponse fetchUnits(String meterNumber) {
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseGet(() -> {
                    Meter m = new Meter();
                    m.setMeterNumber(meterNumber);
                    m.setUnits(0.0);
                    m.setWatts(0.0);
                    m.setBulb("off");
                    return meterRepository.save(m);
                });

        Map<String, Object> data = new HashMap<>();
        data.put("units", meter.getUnits());
        data.put("bulb", meter.getBulb());
        data.put("watts", meter.getWatts());

        return new MeterResponse("success", "Units, watts, and bulb state fetched", data);
    }

    public void saveWatts(String meterNumber, double watts) {
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ZenoApiException("Meter not found: " + meterNumber));
        meter.setWatts(watts);
        meterRepository.save(meter);
    }

    public MeterResponse postUnits(String meterNumber, double units, double watts) {
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ZenoApiException("Meter not found: " + meterNumber));
        meter.setUnits(units);
        meter.setWatts(watts);
        meterRepository.save(meter);

        List<Token> tokens = tokenRepository.findByMeterNumber(meterNumber);
        if (!tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
        }

        return new MeterResponse("success", "Units updated and tokens deleted", units);
    }

    public MeterResponse controlBulb(String meterNumber, String action) {
        if (!action.equalsIgnoreCase("on") && !action.equalsIgnoreCase("off")) {
            return new MeterResponse("error", "Invalid bulb action: " + action, null);
        }
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ZenoApiException("Meter not found: " + meterNumber));
        meter.setBulb(action);
        meterRepository.save(meter);
        return new MeterResponse("success", "Bulb turned " + action, action);
    }

//    private MeterResponse addUnitsToMeter(String meterNumber, double unitsToAdd, String tokenCode) {
//        Meter meter = meterRepository.findByMeterNumber(meterNumber)
//                .orElseGet(() -> {
//                    Meter m = new Meter();
//                    m.setMeterNumber(meterNumber);
//                    m.setUnits(0.0);
//                    m.setWatts(0.0);
//                    m.setBulb("off");
//                    return meterRepository.save(m);
//                });
//
//        meter.setUnits(meter.getUnits() + unitsToAdd);
//        meterRepository.save(meter);
//
//        Optional<Meter> meterOpt = meterRepository.findByMeterNumber(meterNumber);
//        meterOpt.ifPresent(meteropt -> {
//            meteropt.setUnits(0); // or 0 or whatever "empty" value you want
//            meterRepository.save(meter);
//        });
//
//        return new MeterResponse("success", "Units added to meter. Token: " + tokenCode, unitsToAdd);
//    }
private MeterResponse addUnitsToMeter(String meterNumber, double unitsToAdd, String tokenCode) {
    Meter meter = meterRepository.findByMeterNumber(meterNumber)
            .orElseGet(() -> {
                Meter m = new Meter();
                m.setMeterNumber(meterNumber);
                m.setUnits(0.0);
                m.setWatts(0.0);
                m.setBulb("off");
                return meterRepository.save(m);
            });

    // ✅ Just add units here
    meter.setUnits(meter.getUnits() + unitsToAdd);
    meterRepository.save(meter);

    return new MeterResponse("success", "Units added to meter. Token: " + tokenCode, unitsToAdd);
}


    public Token convertDtoToEntity(PaymentToken dto) {
        Token entity = new Token();
        entity.setCode(dto.getCode());
        entity.setMeterNumber(dto.getMeterNumber());
        entity.setAmount(dto.getAmount());
        entity.setUnits(dto.getUnits());
        entity.setTimestamp(LocalDateTime.parse(dto.getTimestamp()));
        return entity;
    }

    public PaymentToken convertEntityToDto(Token entity) {
        PaymentToken dto = new PaymentToken();
        dto.setCode(entity.getCode());
        dto.setMeterNumber(entity.getMeterNumber());
        dto.setAmount(entity.getAmount());
        dto.setUnits(entity.getUnits());
        dto.setTimestamp(String.valueOf(entity.getTimestamp()));
        return dto;
    }

    public MeterResponse processPayment(CreateOrderRequest request) {

        String meterNumber = "0123456789"; // Your constant meter number
        String buyerEmail = "kalos@gmail.com"; // Your constant email
        String buyerName = "kalos";

        request.setMeterNumber(meterNumber);
        request.setBuyerEmail(buyerEmail);
        request.setBuyerName(buyerName);

        String buyerPhone = request.getBuyerPhone();
        double amount = request.getAmount();

        Optional<User> userOpt = userRepository.findByMeterNumber(meterNumber);
        if (userOpt.isEmpty()) {
            logger.error("Meter {} not registered to any user", meterNumber);
            return new MeterResponse("error", "Meter not found for meter " + meterNumber, null);
        }

        double unitsToAdd = request.getAmount() / 100.0;
        String tokenCode = String.format("%020d", new Random().nextLong(1000000000000L));

        try {
            // Step 1: Create order
            String orderResponse = zenoApiService.createOrder(request);
            logger.debug("Raw Zeno API createOrder response: {}", orderResponse);

            ObjectNode responseJson = objectMapper.readValue(orderResponse, ObjectNode.class);
            String orderId = null;

            if (responseJson.has("order_id") && !responseJson.get("order_id").isNull()) {
                orderId = responseJson.get("order_id").asText();
            } else if (responseJson.has("orderId") && !responseJson.get("orderId").isNull()) {
                orderId = responseJson.get("orderId").asText();
            } else {
                logger.error("Zeno API response missing order_id or orderId: {}", orderResponse);
                return new MeterResponse("error", "Failed to create order: Missing order_id or orderId in response", null);
            }

            // Step 2: Check order status
            OrderStatusResponse statusResponse = zenoApiService.checkOrderStatus(orderId);
            logger.debug("Zeno API checkOrderStatus response: {}", statusResponse);

            String status = statusResponse.getStatus();
            String paymentStatus = statusResponse.getPaymentStatus();

            // Normalize for safety
            status = status != null ? status.toUpperCase() : "";
            paymentStatus = paymentStatus != null ? paymentStatus.toUpperCase() : "";

            if ("SUCCESS".equals(status) && "COMPLETED".equals(paymentStatus)) {
                // ✅ Payment successful – Add units
                MeterResponse meterResponse = addUnitsToMeter(meterNumber, unitsToAdd, tokenCode);

                PaymentToken dto = new PaymentToken();
                dto.setCode(tokenCode);
                dto.setMeterNumber(meterNumber);
                dto.setAmount((double) request.getAmount());
                dto.setUnits(unitsToAdd);
                dto.setTimestamp(LocalDateTime.now().toString());

                Token entity = convertDtoToEntity(dto);
                tokenRepository.save(entity);

                logger.info("Payment processed successfully for meter {}. Token: {}", meterNumber, tokenCode);
                return meterResponse;

            } else if ("SUCCESS".equals(status) && "PENDING".equals(paymentStatus)) {
                // ⏳ Payment not yet completed
                logger.warn("Payment for meter {} is still pending. Order ID: {}", meterNumber, orderId);

                // OPTIONAL: save to a pending_orders table here for scheduled polling

                return new MeterResponse("pending", "Payment is still pending. Please wait for confirmation.", null);

            } else {
                // ❌ Payment failed or unknown status
                logger.error("Payment failed for meter {}. Status: {}, PaymentStatus: {}", meterNumber, status, paymentStatus);
                return new MeterResponse("error", "Payment failed: " + statusResponse.getMessage(), null);
            }

        } catch (Exception e) {
            logger.error("Error processing payment for meter {}: {}", meterNumber, e.getMessage(), e);
            return new MeterResponse("error", "Payment processing failed: " + e.getMessage(), null);
        }
    }


    public List<Token> getTokensForMeter(String meterNumber) {
        return tokenRepository.findByMeterNumber(meterNumber);
    }

    public void save(String meterNumber, Double units, Integer watts) {
        Map<String, Object> data = new HashMap<>();
        data.put("meter",meterNumber);
        data.put("units", units);
        data.put("watts", watts);


        meterData.put(meterNumber,data);
    }

    // this methods fetches data frombased with the meter number but its not implimrnted
    // the main part is in the controller for the value to be induiced is directry fetched from the controller
    // so to impliment the methode inject it directry so the number of units are completry fect
    public Map<String, Object> getLatest(String meterNumber) {
        return meterData.getOrDefault(meterNumber, new HashMap<>());
    }
}