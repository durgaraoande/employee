package com.abdr.employee.service;

import com.abdr.employee.model.Employee;
import com.abdr.employee.repositories.EmpRepo;
import com.abdr.employee.utils.EmployeeNotFoundException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmpService {

    private final EmpRepo empRepo;
    private static final Logger log = LoggerFactory.getLogger(EmpService.class);

    public EmpService(EmpRepo empRepo) {
        this.empRepo = empRepo;
    }

    public List<Employee> getAllEmployees(){
        log.info("Retrieved all employees.");
        return empRepo.findAll();
    }

    public List<Employee> getActiveEmployees(){
        log.info("Retrieved active employees.");
        return empRepo.findAllActiveEmployees();
    }

    @Transactional
    public String addEmployee(Employee employee){
        log.info("Adding new employee with email: {}", employee.getEmail());
        try {
            empRepo.save(employee);
            log.info("Successfully added employee with email: {}", employee.getEmail());
            return "Employee added successfully";
        } catch (Exception e) {
            log.error("Error adding employee with email: {}. Exception: {}", employee.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error occurred while adding employee.");
        }
    }

    @Transactional
    public String updateEmployee(Employee employee){
        log.info("Updating employee: {}", employee.getEmail());
        try {
            empRepo.save(employee);
            log.info("Employee updated success: {}",employee.getEmail());
            return "Employee updated successfully";
        } catch (Exception e) {
            log.error("Error updating employee: {}",e.getMessage(),e);
            throw new RuntimeException("Error occurred while updating employee.");
        }
    }

    @Transactional
    public ResponseEntity<String> deleteEmployee(Long id) {
        try {
            empRepo.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Employee deleted successfully: " + id);
        } catch (Exception e) {
            log.error("Error deleting employee: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during deletion");
        }
    }


    public List<Employee> searchByDept(String dept) throws Exception {
        log.info("Searching employees in department: {}", dept);
        List<Employee> employees = empRepo.findByDepartment(dept);
        if (employees.isEmpty()) {
            log.warn("No employees found in department: {}", dept);
            return List.of();  // Return an empty list if no employees found
        }
        return employees;
    }

    public Employee findEmpById(Long id) {
        log.info("Finding employee by ID: {}", id);
        return empRepo.findById(id).orElseThrow(() -> {
            log.error("Employee not found with ID: {}", id);
            return new EmployeeNotFoundException("Employee not found with ID: " + id);
        });
    }

    @Transactional
    public void softDeleteEmployeeById(Long id) {
        log.info("Request for delete with id: {}",id);
        Optional<Employee> employee = empRepo.findById(id);
        if (employee.isPresent()) {
            Employee emp = employee.get();
            emp.setDeleted(true);  // Set 'deleted' flag to true
            empRepo.save(emp); // Save the updated employee
            log.info("Delete success : {}",id);
        } else {
            log.error("No employee found with id: {}", id);
            throw new EmployeeNotFoundException("Employee not found: "+id);
        }
    }

    public boolean existsByEmail(@NotBlank(message = "Please provide email") @Email(message = "Please provide email format.") String email) {
        return empRepo.findByEmail(email).isPresent();
    }
}
