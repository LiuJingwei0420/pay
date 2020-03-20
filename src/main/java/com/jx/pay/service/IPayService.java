package com.jx.pay.service;

import com.jx.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {

    /*
    创建/发起支付
     */

    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /*
    异步
     */
    String asyncNotify(String notifyData);

    /*
    查询支付记录
     */

    PayInfo queryByOrderId(String orderId);
}
