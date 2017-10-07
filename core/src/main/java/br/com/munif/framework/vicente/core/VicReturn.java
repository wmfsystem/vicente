/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.framework.vicente.core;

import java.util.List;

/**
 *
 * @author munif
 */
public class VicReturn<T> {

    private final List<T> values;

    private final Integer quantity;

    private final Integer firstResult;

    private final Boolean hasMore;

    public VicReturn(List<T> values, Integer quantity, Integer firstResult, Boolean hasMore) {
        this.values = values;
        this.quantity = quantity;
        this.firstResult = firstResult;
        this.hasMore = hasMore;
    }

    public List<T> getValues() {
        return values;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

}
