package com.jx.pay.service.impl;

import com.jx.pay.dao.PayInfoMapper;
import com.jx.pay.enums.PayPlatformEnum;
import com.jx.pay.pojo.PayInfo;
import com.jx.pay.service.IPayService;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayService implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;
    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {

//        bestPayService.setAliPayConfig(aliPayConfig);
        //写入数据库
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayRequest request = new PayRequest();
        request.setOrderName("LjwTest");
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setPayTypeEnum(bestPayTypeEnum);


        PayResponse response = bestPayService.pay(request);
        log.info("发起支付 response={}", response);

        return response;

    }

    @Override
    public String asyncNotify(String notifyData) {
        //1. 签名校验
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 payResponse={}", payResponse);

        //2. 金额校验（从数据库查订单）
        //比较严重（正常情况下是不会发生的） 发出告警： 钉钉、短信
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if(payInfo == null) {
            throw new RuntimeException("通过orderNo查询到的结果是null");
        }
        // 如果订单支付状态不是"已支付"
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0 ) {
                //告警
                throw new RuntimeException("异步通知中的金额和数据库里的不一致, orderNo=" + payResponse.getOrderId());
            }

            //3. 修改订单支付状态
            log.info("修改订单success!!!!!!!!!!!!!!!!!");
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfo.setUpdateTime(null);
            log.info("真的success吗");
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO pay发生MQ消息，mall接收MQ消息

        if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            //4. 告诉微信不要再通知了
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }
       throw new RuntimeException("异步通知中错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
