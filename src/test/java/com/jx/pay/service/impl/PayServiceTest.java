package com.jx.pay.service.impl;

import com.jx.pay.PayApplicationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class PayServiceTest extends PayApplicationTest {

    @Autowired
    private PayService payService;

    @Test
    public void create() {
        payService.create("616980152222222", BigDecimal.valueOf(0.01));
    }

}