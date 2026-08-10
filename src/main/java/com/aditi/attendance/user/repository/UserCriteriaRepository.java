package com.aditi.attendance.user.repository;

import com.aditi.attendance.entity.User;
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
public class UserCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> searchUser(String keyword) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

        Root<User> root = criteriaQuery.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + keyword.toLowerCase() + "%"
                )
        );

        criteriaQuery.where(
                criteriaBuilder.or(predicates.toArray(new Predicate[0]))
        );

        return entityManager
                .createQuery(criteriaQuery)
                .getResultList();
    }

}