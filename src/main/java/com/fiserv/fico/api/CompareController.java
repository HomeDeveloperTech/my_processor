package com.fiserv.fico.api;

import com.fiserv.fico.service.CompareReport;
import com.fiserv.fico.service.CompareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compare")
public class CompareController {

    private final CompareService service;

    public CompareController(CompareService service) {
        this.service = service;
    }

    @GetMapping
    public CompareReport compare(
            @RequestParam String institutionNumber,
            @RequestParam String serviceContract,
            @RequestParam String anomes) {
        return service.compare(institutionNumber, serviceContract, anomes);
    }
}
