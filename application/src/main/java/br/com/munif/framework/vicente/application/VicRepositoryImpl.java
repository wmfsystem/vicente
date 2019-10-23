package br.com.munif.framework.vicente.application;

import br.com.munif.framework.vicente.core.VicQuery;
import br.com.munif.framework.vicente.core.VicTenancyPolicy;
import br.com.munif.framework.vicente.core.VicTenancyType;
import br.com.munif.framework.vicente.core.VicThreadScope;
import br.com.munif.framework.vicente.domain.BaseEntity;
import br.com.munif.framework.vicente.domain.VicTemporalEntity.VicTemporalBaseEntity;
import br.com.munif.framework.vicente.domain.VicTemporalEntity.VicTemporalBaseEntityHelper;
import org.hibernate.transform.ResultTransformer;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @param <T> Entity that extends BaseEntity
 * @author munif
 */
@NoRepositoryBean
public class VicRepositoryImpl<T extends BaseEntity> extends SimpleJpaRepository<T, Serializable> implements VicRepository<T> {

    private final EntityManager entityManager;

    public VicRepositoryImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;

    }

    /**
     * Search elements that
     * (data have the user id and the user rights) or
     * (data contains the group id and the group rights) or
     * (data contains the organization id) or
     * (data is public) or
     * (data is ative in the current time if the domain is a VicTemporalEntity)
     *
     * @param publics includes public elements
     * @return hql with tenancy
     */
    public String geTenancyHQL(boolean publics) {
        Class<T> domainClass = this.getDomainClass();
        boolean isVicTemporalEntity = VicTemporalBaseEntity.class.isAssignableFrom(domainClass) && (!Boolean.TRUE.equals(VicThreadScope.ignoreTime.get()));
        boolean assignableFrom2 = BaseEntity.class.isAssignableFrom(domainClass);
        if (!assignableFrom2) {
            return "(1=1)";
        }
        VicTenancyType vtt = getPolicy(domainClass);
        StringBuilder sb = new StringBuilder("(\n");
        if (isVicTemporalEntity) {
            sb.append("(\n");
        }
        sb.append("   (obj.ui=:ui and mod(obj.rights/64,8)/4>=1) \n");

        if (vtt.equals(VicTenancyType.GROUPS) || vtt.equals(VicTenancyType.GROUPS_AND_HIERARCHICAL_TOP_DOWN) || vtt.equals(VicTenancyType.GROUPS_AND_ORGANIZATIONAL)) {
            sb.append("or (:gi like concat('%',obj.gi,',%') and mod(obj.rights/8,8)/4>=1) \n");
        }

        if (vtt.equals(VicTenancyType.HIERARCHICAL_TOP_DOWN) || vtt.equals(VicTenancyType.ORGANIZATIONAL) || vtt.equals(VicTenancyType.GROUPS_AND_HIERARCHICAL_TOP_DOWN) || vtt.equals(VicTenancyType.GROUPS_AND_ORGANIZATIONAL)) {
            sb.append("or (obj.oi like :oi)\n");
        }
        if (publics) {
            sb.append("or (1=1    and    mod(obj.rights  ,8)/4>=1)\n");
        }
        if (isVicTemporalEntity) {
            sb.append(") and (obj.startTime<=:et and :et<=obj.endTime) \n");
        }
        sb.append(
                ")");

        return sb.toString();
    }

    /**
     * Get the type of tenancy of the domain
     *
     * @param domainClass class that contains the VicTenancyType Annotation
     * @return tenancy policy
     */
    public VicTenancyType getPolicy(Class<T> domainClass) {
        VicTenancyPolicy vtp = domainClass.getAnnotation(VicTenancyPolicy.class);
        return vtp == null ? VicTenancyType.GROUPS : vtp.value();
    }

    public void setTenancyParameters(Query query) {
        Set<Parameter<?>> parameters = query.getParameters();
        for (Parameter p : parameters) {
            if ("et".equals(p.getName())) {
                query.setParameter("et", VicTemporalBaseEntityHelper.getEffectiveTime());
            }
            if ("ui".equals(p.getName())) {
                query.setParameter("ui", VicThreadScope.ui.get());
            }
            if ("gi".equals(p.getName())) {
                query.setParameter("gi", VicThreadScope.gi.get() + ",");
            }
            if ("oi".equals(p.getName())) {
                VicTenancyType vtt = getPolicy(getDomainClass());
                String oi = VicThreadScope.oi.get();
                if (oi == null) {
                    oi = ".";
                }
                if (vtt.equals(VicTenancyType.ORGANIZATIONAL) || vtt.equals(VicTenancyType.GROUPS_AND_ORGANIZATIONAL)) {
                    query.setParameter("oi", oi.substring(0, oi.indexOf('.') + 1) + "%");
                } else {
                    query.setParameter("oi", oi + "%");
                }
            }

        }
    }

    /**
     * Find all elements without tenancy filter
     *
     * @return all domain elements
     */
    public List<T> findAllNoTenancy() {
        return super.findAll();
    }

    /**
     * Find all elements according tenancy including public elements
     *
     * @return all domain elements by tenancy
     */
    @Override
    public List<T> findAll() {
        String hql = "FROM " + getDomainClass().getSimpleName() + " obj where \n" + geTenancyHQL(true);
        Query query = entityManager.createQuery(hql);
        setTenancyParameters(query);
        return query.getResultList();
    }

    /**
     * Find all elements according tenancy without public elements
     *
     * @return all domain elements by tenancy
     */
    public List<T> findAllNoPublic() {
        String hql = "FROM " + getDomainClass().getSimpleName() + " obj where \n" + geTenancyHQL(false);
        Query query = entityManager.createQuery(hql);
        setTenancyParameters(query);
        return query.getResultList();
    }

    /**
     * Find elements according using the VicQuery
     *
     * @param vicQuery VicQuery
     * @return domain elements according query
     */
    @Override
    public List<T> findByHql(VicQuery vicQuery) {
        if (vicQuery.getMaxResults() == -1) {
            vicQuery.setMaxResults(VicQuery.DEFAULT_QUERY_SIZE);
        }

        String clause = (vicQuery.getHql() != null ? vicQuery.getHql() : "1=1")
                .concat(" and ")
                .concat((vicQuery.getQuery() != null ? vicQuery.getQuery().toString() : "1=1"));

        String joins = "";
        String obj = "obj";
        if (vicQuery.getQuery() != null) {
            vicQuery.getQuery().setAlias("obj");
            obj = vicQuery.getQuery().getFieldsWithAlias();
            joins = vicQuery.getQuery().getJoins();
        }



        String hql = mountHQL(vicQuery, clause, joins, obj);
        Query query = entityManager.createQuery(hql);
        query.setFirstResult(vicQuery.getFirstResult());
        query.setMaxResults(vicQuery.getMaxResults());
        setTenancyParameters(query);
        if (!"obj".equals(obj)) {
            query = selectAttributes(query);

        }
        return query.getResultList();
    }

    private org.hibernate.query.Query selectAttributes(Query query) {
        return query.unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new ResultTransformer() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        T t = null;
                        try {
                            t = getDomainClass().newInstance();
                            Class<? extends BaseEntity> aClass = t.getClass();
                            for (int i = 0; i < aliases.length; i++) {
                                Field declaredField = aClass.getDeclaredField(aliases[i]);
                                declaredField.setAccessible(true);
                                declaredField.set(t, tuple[i]);
                            }
                        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                        return t;
                    }

                    @Override
                    public List transformList(List collection) {
                        return collection;
                    }
                });
    }

    private String mountHQL(VicQuery vicQuery, String clause, String joins, String obj) {
        return "select " + obj + " FROM " + getDomainClass().getSimpleName() + " obj " + joins + " where \n"
                + "(" + clause + ") and "
                + "("
                + geTenancyHQL(true)
                + ") "
                + " ORDER BY obj." + vicQuery.getOrderBy() + " , obj.id asc";
    }
}
