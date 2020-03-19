package com.jx.pay.controller;

import com.jx.pay.service.impl.PayService;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.ws.rs.POST;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Controller
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private PayService payService;

    @GetMapping("/create")
    public ModelAndView create(@RequestParam("OrderId") String orderId,
                               @RequestParam("amount") BigDecimal amount) {

        PayResponse response = payService.create(orderId, amount);

        Map map = new HashMap<>();
        map.put("codeUrl",response.getCodeUrl());

//       map.put("codeUrl","weixin://wxpay/bizpayurl?pr=t5G8Pl11111");
        return new ModelAndView("create", map);

    }

    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData) {
        return payService.asyncNotify(notifyData);
    }


}
