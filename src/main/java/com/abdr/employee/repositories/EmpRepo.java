package com.abdr.employee.repositories;

import com.abdr.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface EmpRepo extends JpaRepository<Employee, Long> {
//    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) LIKE LOWER(CONCAT('%', :dept ,'%')) and e.deleted = false")
//    List<Employee> findByDepartment(String dept, Sort sort);

    @Query("SELECT e FROM Employee e WHERE e.department ILIKE CONCAT('%', CAST(:dept AS string), '%') AND e.deleted = false")
    Page<Employee> findByDepartment(@Param("dept") String dept, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActiveEmployees(Pageable pageable);

    Optional<Employee> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.deleted = true WHERE e.id = :id")
    int softDeleteById(@Param("id") Long id);

    //    @Query("SELECT e FROM Employee e WHERE " +
//            "(:department IS NULL OR LOWER(e.department) LIKE LOWER(CONCAT('%', :department, '%'))) AND " +
//            "(:firstName IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
//            "(:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
//            "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
//            "(:maxSalary IS NULL OR e.salary <= :maxSalary) AND " +
//            "e.deleted = false")
//    List<Employee> searchEmployees(
//            @Param("department") String department,
//            @Param("firstName") String firstName,
//            @Param("lastName") String lastName,
//            @Param("minSalary") BigDecimal minSalary,
//            @Param("maxSalary") BigDecimal maxSalary,
//            Sort sort
//            );
    @Query("SELECT e FROM Employee e WHERE " +
            "(:department IS NULL OR e.department ILIKE CONCAT('%', CAST(:department AS string), '%')) AND " +
            "(:firstName IS NULL OR e.firstName ILIKE CONCAT('%', CAST(:firstName AS string), '%')) AND " +
            "(:lastName IS NULL OR e.lastName ILIKE CONCAT('%', CAST(:lastName AS string), '%')) AND " +
            "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
            "(:maxSalary IS NULL OR e.salary <= :maxSalary) AND " +
            "e.deleted = false")
    Page<Employee> searchEmployees(
            @Param("department") String department,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary,
            Pageable pageable);
}
