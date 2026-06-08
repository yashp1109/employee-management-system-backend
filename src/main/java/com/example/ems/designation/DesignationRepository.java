package com.example.ems.designation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DesignationRepository extends JpaRepository<Designation, Long> {
    boolean existsByNameIgnoreCase(String name);

    @Query("""
        SELECT d
        FROM Designation d
        LEFT JOIN FETCH d.department
    """)
    List<Designation> findAllWithDepartment();
}
