package com.jeequan.jeepay.mch.ctrl.anon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class ApiDocController {

    @GetMapping(value = "/api/anon/apiDoc")
    public String apiDoc() {
        return "apiDoc";
    }
}
