package com.akkafun.order.api.dtos;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liubin on 2016/5/6.
 */
public class PlaceOrderDto {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private List<Long> couponIdList = new ArrayList<>();

    @NotEmpty
    @Valid
    private List<PlaceOrderItemDto> placeOrderItemList = new ArrayList<>(0);

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getCouponIdList() {
        return couponIdList;
    }

    public void setCouponIdList(List<Long> couponIdList) {
        this.couponIdList = couponIdList;
    }

    public List<PlaceOrderItemDto> getPlaceOrderItemList() {
        return placeOrderItemList;
    }

    public void setPlaceOrderItemList(List<PlaceOrderItemDto> placeOrderItemList) {
        this.placeOrderItemList = placeOrderItemList;
    }
}
