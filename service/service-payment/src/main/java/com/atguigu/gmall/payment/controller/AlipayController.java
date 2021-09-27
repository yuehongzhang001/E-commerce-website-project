package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Yuehong Zhang
 */
@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    //  http://api.gmall.com/api/payment/alipay/submit/194
    // @ResponseBody The first one: the return data is Json, the second one, directly input the data into the page!
    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String aliPay(@PathVariable Long orderId){
        String pageContent = null;
        try {
            pageContent = alipayService.createaliPay(orderId);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // return data
        return pageContent;
    }

    // Write a synchronous callback: http://api.gmall.com/api/payment/alipay/callback/return
    @GetMapping("callback/return")
    public String callbackReturn(){
        // Redirect to: return_order_url=http://payment.gmall.com/pay/success.html
        return "redirect:"+ AlipayConfig.return_order_url;
    }

    // notify_payment_url=http://qgnq4b.natappfree.cc/api/payment/alipay/callback/notify
    // Start the intranet penetration tool:
    // https: //Merchant website notification address?voucher_detail_list=[{"amount":"0.20","merchantContribute":"0.00","name":"50% discount coupon","otherContribute":"0.20"," type":"ALIPAY_DISCOUNT_VOUCHER","voucherId":"2016101200073002586200003BQ4"}]&fund_bill_list=[{"amount":"0.80","fundChannel":"ALIPAYACCOUNT"},{"amount":"0.20","fundChannel": "MDISCOUNT"}]&subject=PC website payment transaction&trade_no=2016101221001004580200203978&gmt_create=2016-10-12 21:36:12&notify_type=trade_status_sync&total_amount=1.00&out_trade_no=mobile_rdm862016-10-12213600&invoice_amount=0.202019-10-1288_timeeller_id=2016-10-1288_09970555&sify :23&trade_status=TRADE_SUCCESS&gmt_payment=2016-10-12 21:37:19&receipt_amount=0.80&passback_params=passback_params123&buyer_id=2088102114562585&app_id=2016092101248425&notify_id=2016092101248425&notify_id=2016092101248425&notify_id=2016092101248425&notify_id=7676a2e1e4e6cff30015csignamounta=0.
    @PostMapping("callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String, String> paramMap) throws AlipayApiException {

        // Obtain the corresponding data through the key
        String outTradeNo = paramMap.get("out_trade_no");
        // outTradeNo and the merchant's outTradeNo! If the data can be obtained in the transaction record through this outTradeNo, the verification is successful!
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        // Indicates that the verification failed! outTradeNo
        // if (paymentInfo==null){
        // return "failure";
        //}
        // String totalAmount = paramMap.get("total_amount");
        // if (paymentInfo.getTotalAmount().compareTo(new BigDecimal(totalAmount))!=0){
        // return "failure";
        //}
        String status = paramMap.get("trade_status");
        // verify the total amount
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //Call SDK to verify signature
        if(flag){
            // After the TODO verification is successful, follow the description in the asynchronous notification of the payment result to perform a second verification of the business content in the payment result. After the verification is successful, it will return success in the response and continue the merchant's own business processing, and the verification failure will be returned failure
            // Verify that the various parameters are correct!
            if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)){
                // Details: Prevent in case {when the transaction status is payment completed or the transaction is over} If the payment status is CLOSED or PAID, then return to failure!
                if ("PAID".equals(paymentInfo.getPaymentStatus()) || "ClOSED".equals(paymentInfo.getPaymentStatus())){
                    return "failure";
                }
                // Update the status of the transaction record!
                paymentService.paySuccess(outTradeNo,PaymentType.ALIPAY.name(),paramMap);

                return "success";
            }

        }else{
            // Record the exception log when TODO verification fails, and return failure in response.
            return "failure";
        }

        return "failure";
    }

    //  申请退款：
    @GetMapping("refund/{orderId}")
    @ResponseBody
    public Result refund(@PathVariable Long orderId){

        //  调用服务层方法
        boolean flag = alipayService.refund(orderId);
        return Result.ok(flag);
    }

    //  关闭支付宝交易
    @GetMapping("closePay/{orderId}")
    @ResponseBody
    public Boolean closePay(@PathVariable Long orderId){
        //  关闭交易
        boolean flag = alipayService.closePay(orderId);
        return flag;
    }

    // 查看是否有交易记录
    @RequestMapping("checkPayment/{orderId}")
    @ResponseBody
    public Boolean checkPayment(@PathVariable Long orderId){
        // 调用退款接口
        boolean flag = alipayService.checkPayment(orderId);
        return flag;
    }
    //  查询本地是否有交易记录！
    @GetMapping("getPaymentInfo/{outTradeNo}")
    @ResponseBody
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo){
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        if (null!=paymentInfo){
            return paymentInfo;
        }
        return null;
    }


}
