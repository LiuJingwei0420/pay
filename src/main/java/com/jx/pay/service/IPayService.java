package com.jx.pay.service;

import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {

    /*
    创建/发起支付
     */

    PayResponse create(String orderId, BigDecimal amount);

    /*
    异步
     */
    String asyncNotify(String notifyData);


}
