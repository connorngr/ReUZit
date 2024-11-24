package com.connorng.ReUzit.controller.listing;


import com.connorng.ReUzit.model.Condition;
import com.connorng.ReUzit.model.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/conditions")
    public Condition[] getConditions() {
        return Condition.values();
    }

    @GetMapping("/statuses")
    public Status[] getStatuses() {
        return Status.values();
    }

}
