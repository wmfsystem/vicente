/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.framework.vicente.api;

import br.com.munif.framework.vicente.application.VicServiceable;
import br.com.munif.framework.vicente.core.ReflectionUtil;
import br.com.munif.framework.vicente.core.VicQuery;
import br.com.munif.framework.vicente.core.VicReturn;
import br.com.munif.framework.vicente.domain.BaseEntity;
import br.com.munif.framework.vicente.domain.BaseEntityHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author munif
 */
@RestController
@Scope("prototype")
public class BaseAPI<T extends BaseEntity> {

    public VicServiceable<T> service;

    public BaseAPI(VicServiceable service) {
        this.service = service;
    }

    @Transactional
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        T entity = service.loadNoTenancy(id);
        if (entity == null) {
            throw new VicenteNotFoundException("Not found");
        }
        if (!entity.canDelete()) {
            throw new VicenteRightsException("DELETE," + id + "," + entity.r());
        }
        beforeDelete(entity);
        service.delete(entity);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<T> save(@RequestBody @Valid T model) {
        beforeSave(model);
        if (service.load(model.getId()) != null) {
            throw new VicenteCreateWithExistingIdException("create With Existing Id=" + model.getId());
        }

        T entity = service.save(model);
        return new ResponseEntity<>(entity, HttpStatus.CREATED);
    }

    @Transactional
    @PutMapping(value = "", consumes = "application/json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<T> updateWithoutId(@RequestBody @Valid T model) {
        return doUpdate(model);
    }

    @Transactional
    @PutMapping(value = "/{id}", consumes = "application/json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<T> updateWithId(@PathVariable("id") String id, @RequestBody @Valid T model) {
        model.setId(id);
        return doUpdate(model);
    }

    @Transactional
    @PatchMapping(value = "/{id}", consumes = "application/json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Void> patch(@PathVariable("id") String id, @RequestBody @Valid Map model) {
        model.put("id", id);
        service.patch(model);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PatchMapping(value = "/returning/{id}", consumes = "application/json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity patchReturning(@PathVariable("id") String id, @RequestBody @Valid Map model) {
        model.put("id", id);
        return ResponseEntity.ok(service.patchReturning(model));
    }

    private ResponseEntity<T> doUpdate(T model) {
        T entity = null;
        HttpStatus ht = HttpStatus.OK;
        T oldEntity = service.loadNoTenancy(model.getId());
        if (oldEntity != null) {
            if (!oldEntity.canUpdate()) {
                throw new VicenteRightsException("PUT," + oldEntity.getId() + "," + oldEntity.r());
            }
            beforeUpdate(model.getId(), model);
            BaseEntityHelper.overwriteJsonIgnoreFields(model, oldEntity);
            entity = service.save(model);
        } else {
            beforeSave(model);

            entity = service.save(model);
            ht = HttpStatus.CREATED;
        }
        return new ResponseEntity(entity, ht);
    }

    @Transactional
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<VicReturn<T>> findHQL(HttpServletRequest request, VicQuery query) {
        return getVicReturnByQuery(query);
    }

    @Transactional
    @PostMapping(value = "/vquery", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<VicReturn<T>> findVQuery(@RequestBody VicQuery query) {
        return getVicReturnByQuery(query);
    }

    @Transactional
    public ResponseEntity<VicReturn<T>> getVicReturnByQuery(@RequestBody VicQuery query) {
        if (query.getHql() == null || query.getHql().trim().isEmpty()) {
            query.setHql(VicQuery.DEFAULT_QUERY);
        }
        if (query.getMaxResults() == -1) {
            query.setMaxResults(this.getDefaultSize());
        }
        int maxResults = query.getMaxResults();
        query.setMaxResults(maxResults + 1);
        query.setHql(query.getHql().replace("\"", ""));
        Set<T> result = new LinkedHashSet<>(service.findByHql(query));
        boolean hasMore = result.size() > maxResults;
        if (hasMore) {
            result.remove(maxResults);
        }
        if (query.getQuery() != null && query.getQuery().getFields() != null) {
            String[] fields = query.getQuery().getFields();
            Set<Map<String, Object>> collect = result.stream().map(s -> getFields(fields, s)).collect(Collectors.toSet());
            return ResponseEntity.ok(new VicReturn(collect, collect.size(), query.getFirstResult(), hasMore));
        }
        return ResponseEntity.ok(new VicReturn<T>(result, result.size(), query.getFirstResult(), hasMore));
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity load(@PathVariable String id, @RequestParam(required = false) String fields) {
        T view = service.loadNoTenancy(id);
        if (view == null) {
            throw new VicenteNotFoundException("Not found");
        }
        if (!view.canRead()) {
            throw new VicenteRightsException("READ," + id + "," + view.r());
        }

        beforeReturnOne(view);
        if (fields != null) {
            Map<String, Object> stringObjectMap = getFields(fields, view);
            return new ResponseEntity(stringObjectMap, HttpStatus.OK);
        }
        return ResponseEntity.ok(view);
    }

    @Transactional
    @GetMapping(value = "/is-new/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Boolean> isNew(@PathVariable String id) {
        return ResponseEntity.ok(service.isNew(id));
    }

    private Map<String, Object> getFields(String fields, T view) {
        String[] split = fields.split(",");
        return getFields(split, view);
    }

    private Map<String, Object> getFields(String[] fields, T view) {
        return ReflectionUtil.objectFieldsToMap(fields, view);
    }

    @ResponseBody
    @Transactional
    @GetMapping(value = "/draw/{id}", produces = "image/svg+xml")
    public ResponseEntity<String> draw(@PathVariable String id) {
        String svg = service.draw(id);
        return ResponseEntity.ok(svg);
    }

    @GetMapping(value = "/new", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<T> initialState() {
        return ResponseEntity.ok(service.newEntity());
    }

    public int getDefaultSize() {
        return 20;
    }

    protected void beforeSave(T model) {
    }

    protected void beforeUpdate(String id, T model) {
    }

    protected void beforeReturnOne(T view) {
    }

    protected void beforeDelete(T entity) {
    }

}
