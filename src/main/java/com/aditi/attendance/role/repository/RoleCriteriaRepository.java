package com.aditi.attendance.role.repository;

import com.aditi.attendance.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RoleCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Role> searchRole(String keyword) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);

        Root<Role> root = criteriaQuery.from(Role.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("roleName")),
                        "%" + keyword.toLowerCase() + "%"
                )
        );

        predicates.add(
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + keyword.toLowerCase() + "%"
                )
        );

        criteriaQuery.where(
                criteriaBuilder.or(predicates.toArray(new Predicate[0]))
        );

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}