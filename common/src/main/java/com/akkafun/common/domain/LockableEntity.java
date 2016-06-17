package com.akkafun.common.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Created by liubin on 2016/3/28.
 */
@MappedSuperclass
public abstract class LockableEntity {

    @Version
    @Column(name = "optlock", columnDefinition = "integer DEFAULT 0", nullable = false)
    private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
