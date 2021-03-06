package br.com.munif.framework.vicente.application;

import br.com.munif.framework.vicente.core.VicQuery;
import br.com.munif.framework.vicente.domain.BaseEntity;

import java.util.List;
import java.util.Map;

public interface VicServiceable<T extends BaseEntity> {
    T loadNoTenancy(String id);

    void delete(T entity);

    T load(String id);

    T save(T model);

    void patch(Map<String, Object> map);

    T patchReturning(Map<String, Object> map);

    List<T> findByHql(VicQuery query);

    String draw(String id);

    T newEntity();

    Boolean isNew(String id);
}
