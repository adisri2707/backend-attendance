package com.aditi.attendance.employee.repository;

import com.aditi.attendance.entity.Employee;
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
public class EmployeeCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Employee> searchEmployees(String keyword) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);

        Root<Employee> employee = cq.from(Employee.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                cb.isTrue(employee.get("active"))
        );

        if (keyword != null && !keyword.trim().isEmpty()) {

            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            Predicate firstNamePredicate =
                    cb.like(cb.lower(employee.get("firstName")), searchKeyword);

            Predicate lastNamePredicate =
                    cb.like(cb.lower(employee.get("lastName")), searchKeyword);

            Predicate departmentPredicate =
                    cb.like(cb.lower(employee.get("department")), searchKeyword);

            Predicate designationPredicate =
                    cb.like(cb.lower(employee.get("designation")), searchKeyword);

            predicates.add(
                    cb.or(
                            firstNamePredicate,
                            lastNamePredicate,
                            departmentPredicate,
                            designationPredicate
                    )
            );
        }

        cq.where(predicates.toArray(new Predicate[0]));

        cq.orderBy(cb.asc(employee.get("firstName")));

        return entityManager.createQuery(cq).getResultList();
    }

}