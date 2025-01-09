package com.abdr.employee.repositories;

import com.abdr.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmpRepo extends JpaRepository<Employee,Long> {
    @Query("SELECT e FROM Employee e WHERE LOWER(e.department) LIKE LOWER(CONCAT('%', :dept ,'%')) and e.deleted = false")
    List<Employee> findByDepartment(String dept);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    List<Employee> findAllActiveEmployees();

    Optional<Employee> findByEmail(String email);

}
