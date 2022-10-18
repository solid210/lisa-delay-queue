package org.lisa.delayqueue.producer.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/26
 */
@Data
@ToString
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = -3246066729154836541L;

    private Long orderNo;

    private Long userId;

    private LocalDateTime createTime;
}
