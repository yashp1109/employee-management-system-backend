package com.example.ems.request;

import com.example.ems.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("select r from Request r " +
            "where (:employeeId is null or r.employee.id = :employeeId) " +
            "and (:type is null or r.type = :type) " +
            "and (:status is null or r.status = :status)")
    Page<Request> search(@Param("employeeId") Long employeeId,
                         @Param("type") RequestType type,
                         @Param("status") RequestStatus status,
                         Pageable pageable);

    @Query("select r from Request r " +
            "where r.requestedBy = :requestedBy " +
            "and (:employeeId is null or r.employee.id = :employeeId) " +
            "and (:type is null or r.type = :type) " +
            "and (:status is null or r.status = :status)")
    Page<Request> searchByRequester(@Param("requestedBy") Employee requestedBy,
                                    @Param("employeeId") Long employeeId,
                                    @Param("type") RequestType type,
                                    @Param("status") RequestStatus status,
                                    Pageable pageable);
}
