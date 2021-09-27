//
//
package com.atguigu.gmall.model.payment;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * PaymentInfo
 * </p>
 *
 */
@Data
@ApiModel(description = "Payment Information")
@TableName("payment_info")
public class PaymentInfo extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "External Business Number")
	@TableField("out_trade_no")
	private String outTradeNo;

	@ApiModelProperty(value = "Order Number")
	@TableField("order_id")
	private Long orderId;

	@ApiModelProperty(value = "Payment type (WeChat Alipay)")
	@TableField("payment_type")
	private String paymentType;

	@ApiModelProperty(value = "Transaction Number")
	@TableField("trade_no")
	private String tradeNo;

	@ApiModelProperty(value = "payment amount")
	@TableField("total_amount")
	private BigDecimal totalAmount;

	@ApiModelProperty(value = "transaction content")
	@TableField("subject")
	private String subject;

	@ApiModelProperty(value = "Payment Status")
	@TableField("payment_status")
	private String paymentStatus;

	@ApiModelProperty(value = "Creation Time")
	@TableField("create_time")
	private Date createTime;

	@ApiModelProperty(value = "callback time")
	@TableField("callback_time")
	private Date callbackTime;

	@ApiModelProperty(value = "callback information")
	@TableField("callback_content")
	private String callbackContent;

}