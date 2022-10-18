package org.lisa.delayqueue.base.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/26
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> implements Serializable {

    private static final long serialVersionUID = 5384673765770002045L;

    private T body;

    private Class<?> clazz;

    public Message(T body){
        this.body = body;
        this.clazz = body.getClass();
    }
}
