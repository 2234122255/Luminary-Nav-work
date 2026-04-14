package com.example.coauthoranalysis.controller;

import com.example.coauthoranalysis.service.NetworkGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/network")
@CrossOrigin(origins = "*")
public class NetworkGraphController {

    @Autowired
    private NetworkGraphService networkGraphService;

    @GetMapping("/graph")
    public Map<String, Object> graph(
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) Integer limitNodes
    ) {
        return networkGraphService.buildGraph(startYear, endYear, field, limitNodes);
    }
}
