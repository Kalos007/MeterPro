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
    public static final Logger logger = LoggerFactory.getLogger(MeterService.class);

    private final Map<String, Map<String, Object>> meterData = new HashMap<>();

    private final MeterRepository meterRepository;
    private final TokenRepository tokenRepository;
    private final UserReporsitory userRepository;
    private final ZenoApiService zenoApiService;
    private final ObjectMapper objectMapper;
    private final PendingOrderRepository pendingOrderRepository;

    public MeterService(MeterRepository meterRepository, TokenRepository tokenRepository,
                        UserReporsitory userRepository, ZenoApiService zenoApiService,
                        ObjectMapper objectMapper, PendingOrderRepository pendingOrderRepository) {
        this.meterRepository = meterRepository;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.zenoApiService = zenoApiService;
        this.objectMapper = objectMapper;
        this.pendingOrderRepository = pendingOrderRepository;
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
MeterResponse addUnitsToMeter(String meterNumber, double unitsToAdd, String tokenCode) {
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
        String meterNumber = "0123456789";
        String buyerEmail = "kalos@gmail.com";
        String buyerName = "kalos";

        request.setMeterNumber(meterNumber);
        request.setBuyerEmail(buyerEmail);
        request.setBuyerName(buyerName);

        Optional<User> userOpt = userRepository.findByMeterNumber(meterNumber);
        if (userOpt.isEmpty()) {
            return new MeterResponse("error", "Meter not found", null);
        }

        double unitsToAdd = request.getAmount() / 100.0;
        String tokenCode = String.format("%020d", new Random().nextLong(1000000000000L));

        try {
            String orderResponse = zenoApiService.createOrder(request);
            ObjectNode responseJson = objectMapper.readValue(orderResponse, ObjectNode.class);
            String orderId = responseJson.has("order_id") ? responseJson.get("order_id").asText() : null;

            if (orderId == null) {
                return new MeterResponse("error", "Failed to create order", null);
            }

            // Save to pending orders
            PendingOrder pending = new PendingOrder();
            pending.setOrderId(orderId);
            pending.setMeterNumber(meterNumber);
            pending.setUnits(unitsToAdd);
            pending.setTokenCode(tokenCode);
            pendingOrderRepository.save(pending);

            return new MeterResponse("pending", "Payment is in progress. You will receive units once completed.", null);

        } catch (Exception e) {
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