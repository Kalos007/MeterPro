package com.example.meter;

import com.example.meter.PendingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Service
public class PendingOrderScheduler {

    @Autowired
    private PendingOrderRepository pendingOrderRepository;

    @Autowired
    private MeterService meterService;

    @Autowired
    private ZenoApiService zenoApiService;

    @Scheduled(fixedDelay = 60000) // every 60 seconds
    public void pollPendingOrders() {
        List<PendingOrder> pendingOrders = pendingOrderRepository.findAll();
        for (PendingOrder order : pendingOrders) {
            OrderStatusResponse status = zenoApiService.checkOrderStatus(order.getOrderId());
            if ("SUCCESS".equalsIgnoreCase(status.getStatus())
                    && "COMPLETED".equalsIgnoreCase(status.getPaymentStatus())) {

                meterService.addUnitsToMeter(order.getMeterNumber(), order.getUnits(), order.getTokenCode());
                pendingOrderRepository.delete(order);
            }
        }
    }
}
