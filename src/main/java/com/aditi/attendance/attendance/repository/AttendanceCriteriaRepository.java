package com.aditi.attendance.attendance.repository;

import com.aditi.attendance.entity.Attendance;
import com.aditi.attendance.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AttendanceCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Attendance> searchAttendance(String keyword) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Attendance> query = cb.createQuery(Attendance.class);

        Root<Attendance> attendance = query.from(Attendance.class);

        Join<Attendance, Employee> employee = attendance.join("employee");

        List<Predicate> predicates = new ArrayList<>();

        String search = "%" + keyword.toLowerCase() + "%";

        predicates.add(cb.like(cb.lower(employee.get("firstName")), search));

        predicates.add(cb.like(cb.lower(employee.get("lastName")), search));

        predicates.add(cb.like(cb.lower(employee.get("employeeCode")), search));

        predicates.add(cb.like(cb.lower(employee.get("department")), search));

        predicates.add(cb.like(cb.lower(employee.get("designation")), search));

        predicates.add(cb.like(cb.lower(attendance.get("status")), search));

        predicates.add(cb.like(cb.lower(attendance.get("remarks")), search));

        query.select(attendance)
                .where(cb.or(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

}