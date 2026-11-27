package org.example.buffet.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String,String> health(){
        return Map.of(
                "status","OK",
                "service_name","buffet_management"
        );
    }

    @GetMapping("/api/info")
    public Map<String, Serializable> info(
            @RequestParam(name = "label",defaultValue = "local")
            String label
    ){
        return Map.of(
                "service_name","buffet_man",
                "ready",true,
                "source",label
        );
    }

}
