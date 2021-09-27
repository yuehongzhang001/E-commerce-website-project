package com.atguigu.gmall.common.execption;

import com.atguigu.gmall.common.result.ResultCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Customize the global exception class
 *
 */
@Data
@ApiModel(value = "Custom global exception class")
public class GmallException extends RuntimeException {

    @ApiModelProperty(value = "Exception status code")
    private Integer code;

    /**
     * Create exception objects through status codes and error messages
     * @param message
     * @param code
     */
    public GmallException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    /**
     * Receive enumerated type objects
     * @param resultCodeEnum
     */
    public GmallException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "GuliException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}