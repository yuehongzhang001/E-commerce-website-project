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
 * @author mqx
 */
@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    //  http://api.gmall.com/api/payment/alipay/submit/194
    //  @ResponseBody 第一个：返回数据是Json ，第二个，直接将数据输入到页面！
    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String aliPay(@PathVariable Long orderId){
        String pageContent = null;
        try {
            pageContent = alipayService.createaliPay(orderId);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //  返回数据
        return pageContent;
    }

    //  编写同步回调：http://api.gmall.com/api/payment/alipay/callback/return
    @GetMapping("callback/return")
    public String callbackReturn(){
        //  重定向到： return_order_url=http://payment.gmall.com/pay/success.html
        return "redirect:"+ AlipayConfig.return_order_url;
    }

    //  notify_payment_url=http://qgnq4b.natappfree.cc/api/payment/alipay/callback/notify
    //  启动内网穿透工具：
    //  https: //商家网站通知地址?voucher_detail_list=[{"amount":"0.20","merchantContribute":"0.00","name":"5折券","otherContribute":"0.20","type":"ALIPAY_DISCOUNT_VOUCHER","voucherId":"2016101200073002586200003BQ4"}]&fund_bill_list=[{"amount":"0.80","fundChannel":"ALIPAYACCOUNT"},{"amount":"0.20","fundChannel":"MDISCOUNT"}]&subject=PC网站支付交易&trade_no=2016101221001004580200203978&gmt_create=2016-10-12 21:36:12&notify_type=trade_status_sync&total_amount=1.00&out_trade_no=mobile_rdm862016-10-12213600&invoice_amount=0.80&seller_id=2088201909970555&notify_time=2016-10-12 21:41:23&trade_status=TRADE_SUCCESS&gmt_payment=2016-10-12 21:37:19&receipt_amount=0.80&passback_params=passback_params123&buyer_id=2088102114562585&app_id=2016092101248425&notify_id=7676a2e1e4e737cff30015c4b7b55e3kh6& sign_type=RSA2&buyer_pay_amount=0.80&sign=***&point_amount=0.00
    @PostMapping("callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String, String> paramMap) throws AlipayApiException {

        //  通过key 获取到对应的数据
        String outTradeNo = paramMap.get("out_trade_no");
        //  outTradeNo 与 商家的outTradeNo！ 如果通过这个outTradeNo 在交易记录中能够获取到数据，则说明验证成功！
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        //  说明验证失败！outTradeNo
        //        if (paymentInfo==null){
        //            return "failure";
        //        }
        //        String totalAmount = paramMap.get("total_amount");
        //        if (paymentInfo.getTotalAmount().compareTo(new BigDecimal(totalAmount))!=0){
        //            return "failure";
        //        }
        String status = paramMap.get("trade_status");
        //  验证总金额
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        if(flag){
            //  TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //  校验各种参数是否正确！
            if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)){
                //  细节： 防止万一 {当交易状态是支付完成，或者交易结束时 } 支付状态是CLOSED 或者是PAID 则返回failure！
                if ("PAID".equals(paymentInfo.getPaymentStatus()) || "ClOSED".equals(paymentInfo.getPaymentStatus())){
                    return "failure";
                }
                //  更新交易记录状态！
                paymentService.paySuccess(outTradeNo,PaymentType.ALIPAY.name(),paramMap);

                return "success";
            }

        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
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
