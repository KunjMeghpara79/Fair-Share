package com.expense_splitter.Fair_Share.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class healthcheck {

    @GetMapping("/Health")
    public String healthcheck(){
        return "Project working Status : Okay";

    }
}
