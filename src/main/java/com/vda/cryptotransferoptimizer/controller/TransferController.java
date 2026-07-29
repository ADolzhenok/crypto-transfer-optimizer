package com.vda.cryptotransferoptimizer.controller;

import com.vda.cryptotransferoptimizer.dto.request.CalculateRequest;
import com.vda.cryptotransferoptimizer.dto.response.TransferResponse;
import com.vda.cryptotransferoptimizer.service.TransferCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransferController {

    private final TransferCalculationService transferCalculationService;

    @PostMapping("/calculate")
    public ResponseEntity<TransferResponse> calculate(
            @RequestBody
            @Valid
            CalculateRequest request
    ){
        TransferResponse response = transferCalculationService.calculate(request);
        return ResponseEntity.ok(response);
    }

}
