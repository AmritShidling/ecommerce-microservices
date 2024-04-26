package com.microservice.inventoryservice.controller;

import com.microservice.inventoryservice.dto.InventoryResponse;
import com.microservice.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {


    @Autowired
    private final InventoryService inventoryService;
    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode){
        return inventoryService.isInStock(skuCode);
    }
}
