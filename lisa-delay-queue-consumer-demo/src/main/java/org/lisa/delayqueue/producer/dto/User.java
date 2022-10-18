package org.lisa.delayqueue.producer.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/26
 */
@Data
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = -5085323625744225501L;

    private Long id;

    private String name;
}
