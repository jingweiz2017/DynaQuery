package com.jingweizhang.dynaquery.extension;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
* @Description
 * This is an entity listener that prevent a view entity to do anything else but reading.
 * It is recommended to hook this entity listener for your view entity.
 * e.g.
 * @Entity
 * @EntityListeners(ViewEntityListener.class)
 * public class WarehouseOrderMonitor implements WarehouseOrderMonitorView {
 *
* @Author rocky.zhang on 2023/4/3
*/
public class ViewEntityListener {
    @PrePersist
    void onPrePersist(Object o) {
        throw new IllegalStateException("JPA is trying to persist an entity of type " + (o == null ? "null" : o.getClass()));
    }

    @PreUpdate
    void onPreUpdate(Object o) {
        throw new IllegalStateException("JPA is trying to update an entity of type " + (o == null ? "null" : o.getClass()));
    }

    @PreRemove
    void onPreRemove(Object o) {
        throw new IllegalStateException("JPA is trying to remove an entity of type " + (o == null ? "null" : o.getClass()));
    }
}
