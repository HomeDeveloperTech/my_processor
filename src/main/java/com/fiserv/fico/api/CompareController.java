package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareReport;
import com.fiserv.fico.service.CompareService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compare")
@Validated
public class CompareController {

    private final CompareService service;

    public CompareController(CompareService service) {
        this.service = service;
    }

    @GetMapping
    public CompareReport compare(
            @RequestParam @NotBlank String institutionNumber,
            @RequestParam @NotBlank String serviceContract,
            @RequestParam @NotBlank String anomes) {
        return service.compare(institutionNumber, serviceContract, anomes);
    }
}
