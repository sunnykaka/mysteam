package com.akkafun.common.event.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@DiscriminatorValue("NOTIFY")
public class NotifyEventPublish extends EventPublish {

}
