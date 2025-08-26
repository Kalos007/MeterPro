package com.example.meter;

import com.example.meter.CreateOrderRequest;
import com.example.meter.MeterResponse;
import com.example.meter.MeterRequest;
import com.example.meter.Token;
import com.example.meter.MeterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.meter.MeterService.logger;

@RestController
@RequestMapping("/api")
public class MeterController {

    @Autowired
    private final MeterService meterService;

    @Autowired
    private final MeterRepository meterRepository;

    public MeterController(MeterService meterService, MeterRepository meterRepository) {
        this.meterService = meterService;
        this.meterRepository = meterRepository;
    }

    @PostMapping("/meter_number/{number}/action")
    public ResponseEntity<MeterResponse> handleMeterAction(
            @PathVariable String number,
            @RequestBody MeterRequest request) {
        switch (request.getType()) {
            case "fetch_units":
                return ResponseEntity.ok(meterService.fetchUnits(number));
            case "post_units":
                if (request.getUnits() == null) {
                    return ResponseEntity.badRequest().body(
                            new MeterResponse("error", "Units must be provided", null));
                }
                return ResponseEntity.ok(meterService.postUnits(number, request.getUnits(), request.getWatts()));
            case "bulb":
                if (request.getAction() == null) {
                    return ResponseEntity.badRequest().body(
                            new MeterResponse("error", "Action must be provided for bulb control", null));
                }
                return ResponseEntity.ok(meterService.controlBulb(number, request.getAction()));
            default:
                return ResponseEntity.badRequest().body(
                        new MeterResponse("error", "Invalid type value", null));
        }
    }

    @GetMapping("/meter_number/{meterNumber}/status")
    public ResponseEntity<MeterResponse> fetchMeterStatus(@PathVariable String meterNumber) {
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new RuntimeException("Meter not found"));

        // Prepare response data with current values
        Map<String, Object> data = new HashMap<>();
        data.put("watts", meter.getWatts());
        data.put("bulb", meter.getBulb());
        data.put("units", meter.getUnits());

        MeterResponse response = new MeterResponse("success", "Units, watts, and bulb state fetched", data);

        // Reset units after returning response
        meter.setUnits(0.0);
        meterRepository.save(meter);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/meter_number/{meterNumber}/watts")
    public ResponseEntity<MeterResponse> receiveWatts(
            @PathVariable String meterNumber,
            @RequestBody Map<String, Double> body) {
        if (!body.containsKey("watts")) {
            return ResponseEntity.badRequest()
                    .body(new MeterResponse("error", "Missing 'watts' in body", null));
        }

        double watts = body.get("watts");
        meterService.saveWatts(meterNumber, watts);
        return ResponseEntity.ok(new MeterResponse("success", "Watts received", watts));
    }

    @PostMapping("/payment")
    public ResponseEntity<MeterResponse> processPayment(@Valid @RequestBody CreateOrderRequest request) {
        MeterResponse response = meterService.processPayment(request);
        if ("error".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tokens/{meterNumber}")
    public ResponseEntity<MeterResponse> getTokenHistory(@PathVariable String meterNumber) {
        List<Token> tokens = meterService.getTokensForMeter(meterNumber);
        return ResponseEntity.ok(new MeterResponse("success", "Token history", tokens));
    }

//    @PostMapping("/postUnit")
//    public ResponseEntity<?> postUnit(@RequestBody PostUnitRequest request) {
//        meterService.save(request.getMeterNumber(),request.getUnits(), request.getWatts());
//        return ResponseEntity.ok(Map.of(
//                "status", "success",
//                "message", "Meter data saved",
//                "data", request
//        ));
//    }
//
//    @GetMapping("/meter/{meterNumber}/latest")
//    public ResponseEntity<?> getLatest(@PathVariable String meterNumber) {
//        return ResponseEntity.ok(meterService.getLatest(meterNumber));
//    }
@GetMapping("/meter/{meterNumber}/units")
public ResponseEntity<MeterResponse> status(@PathVariable String meterNumber) {
    Optional<Meter> meterOpt = meterRepository.findByMeterNumber(meterNumber);

    if (meterOpt.isPresent()) {
        Meter meter = meterOpt.get();
        double currentUnits = meter.getUnits();

        // ✅ Give units to the meter, then reset them
        if (currentUnits > 0) {
            meter.setUnits(0.0);
            meterRepository.save(meter);
        }

        return ResponseEntity.ok(
                new MeterResponse("success", "Fetched units for meter", currentUnits)
        );
    }

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new MeterResponse("error", "Meter not found", 0));
}

    @PostMapping("/zeno/callback")
    public ResponseEntity<String> handleCallback(@RequestBody String payload) {
        logger.info("Received callback from Zeno: {}", payload);
        // Parse JSON, check payment_status == COMPLETED
        // If completed -> call addUnitsToMeter()
        return ResponseEntity.ok("payment completed");
    }


}
