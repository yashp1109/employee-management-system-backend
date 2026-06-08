package com.example.ems.employee;

import com.example.ems.common.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);
    long countByStatus(Status status);
    long countByDepartmentId(Long departmentId);
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Employee> findById(Long id, Pageable pageable);
    @Query("""
            select e from Employee e
            left join e.department d
            left join e.designation des
            where lower(e.firstName) like lower(concat('%', :q, '%'))
               or lower(e.lastName) like lower(concat('%', :q, '%'))
               or lower(e.email) like lower(concat('%', :q, '%'))
               or lower(e.employeeCode) like lower(concat('%', :q, '%'))
               or lower(coalesce(d.name, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(des.name, '')) like lower(concat('%', :q, '%'))
            """)
    Page<Employee> search(String q, Pageable pageable);
}
