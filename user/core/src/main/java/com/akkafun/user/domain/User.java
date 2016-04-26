package com.akkafun.user.domain;

import com.akkafun.common.domain.VersionEntity;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "user")
public class User extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
