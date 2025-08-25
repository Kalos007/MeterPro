//package com.example.meter;
//
//import jakarta.annotation.PostConstruct;
//import org.eclipse.paho.client.mqttv3.*;
//import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//@Service
//public class MqttService {
//
//    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
//
//    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
//    private static final String CLIENT_ID = "smart-meter-backend";
//    private static final String TOPIC_POST_UNITS = "meter/post/units";
//    private static final String TOPIC_POST_WATTS = "meter/post/watts";
//    private static final String TOPIC_GET_COMMANDS = "meter/get/commands";
//
//    private MqttClient client;
//    private final MeterRepository meterRepository;
//
//    public MqttService(MeterRepository meterRepository) {
//        this.meterRepository = meterRepository;
//    }
//
//    @PostConstruct
//    public void connect() {
//        try {
//            client = new MqttClient(BROKER_URL, CLIENT_ID, null);
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setAutomaticReconnect(true);
//            options.setCleanSession(true);
//            client.connect(options);
//            logger.info("Connected to MQTT broker at {}", BROKER_URL);
//            subscribeToTopics();
//        } catch (MqttException e) {
//            logger.error("Error connecting to MQTT broker", e);
//        }
//    }
//
//    private void subscribeToTopics() {
//        try {
//            client.subscribe(TOPIC_POST_UNITS, (topic, message) -> {
//                String payload = new String(message.getPayload());
//                logger.info("Received units from meter: {}", payload);
//                JSONObject json = new JSONObject(payload);
//
//                if (json.has("meter_number") && json.has("units")) {
//                    String meterNumber = json.getString("meter_number");
//                    double units = json.getDouble("units");
//                    saveUnitsFromDevice(meterNumber, units);
//                }
//            });
//
//            client.subscribe(TOPIC_POST_WATTS, (topic, message) -> {
//                String payload = new String(message.getPayload());
//                logger.info("Received watts from meter: {}", payload);
//                JSONObject json = new JSONObject(payload);
//
//                if (json.has("meter_number") && json.has("watts")) {
//                    String meterNumber = json.getString("meter_number");
//                    double watts = json.getDouble("watts");
//                    saveWattsFromDevice(meterNumber, watts);
//                }
//            });
//
//        } catch (MqttException e) {
//            logger.error("Error subscribing to MQTT topics", e);
//        }
//    }
//
//    // Save units received from MQTT
//    private void saveUnitsFromDevice(String meterNumber, double units) {
//        meterRepository.findByMeterNumber(meterNumber).ifPresentOrElse(meter -> {
//            meter.setUnits(units);
//            meterRepository.save(meter);
//            logger.info("Updated DB with new units for {}: {}", meterNumber, units);
//        }, () -> {
//            logger.warn("Meter not found while saving units: {}", meterNumber);
//        });
//    }
//
//    // Save watts received from MQTT
//    private void saveWattsFromDevice(String meterNumber, double watts) {
//        meterRepository.findByMeterNumber(meterNumber).ifPresentOrElse(meter -> {
//            meter.setWatts(watts);
//            meterRepository.save(meter);
//            logger.info("Updated DB with new watts for {}: {}", meterNumber, watts);
//        }, () -> {
//            logger.warn("Meter not found while saving watts: {}", meterNumber);
//        });
//    }
//
//    // Publish command (used by MeterService)
//    public void publishCommands(String meterNumber, double newUnits, String bulbStatus) {
//        try {
//            JSONObject payload = new JSONObject();
//            payload.put("meter_number", meterNumber);
//            payload.put("new_units", newUnits);
//            payload.put("bulb", bulbStatus);
//
//            MqttMessage message = new MqttMessage(payload.toString().getBytes());
//            message.setQos(1);
//            client.publish(TOPIC_GET_COMMANDS, message);
//            logger.info("Published command to meter {}: {}", meterNumber, payload);
//        } catch (MqttException e) {
//            logger.error("Failed to publish MQTT command", e);
//        }
//    }
//
//    // Used by MeterService for cleaner method names
//    public void publishUnitsUpdate(String meterNumber, double units) {
//        publishCommands(meterNumber, units, null);
//    }
//
//    public void publishBulbCommand(String meterNumber, String bulbStatus) {
//        publishCommands(meterNumber, -1, bulbStatus);
//    }
//}
