package com.abdr.employee.service;

import com.abdr.employee.model.Employee;
import com.abdr.employee.repositories.EmpRepo;
import com.abdr.employee.utils.EmployeeNotFoundException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EmpService {

    private static final Logger log = LoggerFactory.getLogger(EmpService.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final String EMPLOYEE_CACHE = "employeeCache";
    private static final String EMPLOYEES_LIST_CACHE = "employeesListCache";
    private static final String DEPARTMENT_CACHE = "departmentCache";
    private static final String SEARCH_CACHE = "searchCache";
    private final EmpRepo empRepo;
    public EmpService(EmpRepo empRepo) {
        this.empRepo = empRepo;
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Integer page, Integer size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("Retrieved all employees.");
        return empRepo.findAll(pageable);
    }

    @Cacheable(value = EMPLOYEES_LIST_CACHE, key = "#page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public Page<Employee> getActiveEmployees(Integer page, Integer size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("Retrieved active employees.");
        return empRepo.findAllActiveEmployees(pageable);
    }

    @CacheEvict(value = {EMPLOYEE_CACHE, EMPLOYEES_LIST_CACHE, DEPARTMENT_CACHE}, allEntries = true)
    @Transactional(rollbackFor = {Exception.class},
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public String addEmployee(Employee employee) {
        log.info("Adding new employee with email: {}", employee.getEmail());
        try {
            empRepo.save(employee);
            log.info("Successfully added employee with email: {}", employee.getEmail());
            return "Employee added successfully";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity issue while adding employee with email: {}", employee.getEmail(), e);
            throw new DataIntegrityViolationException("Duplicate email or other constraint violation: " + employee.getEmail());
        } catch (Exception e) {
            log.error("Unexpected error while adding employee: {}", employee.getEmail(), e);
            throw new RuntimeException("Unexpected error occurred while adding employee", e);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = EMPLOYEE_CACHE, key = "#employee.id", condition = "#employee != null && #employee.id != null"),
            @CacheEvict(value = {EMPLOYEES_LIST_CACHE, DEPARTMENT_CACHE}, allEntries = true)
    })
    @Transactional(rollbackFor = {Exception.class},
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public String updateEmployee(Employee employee) {
        log.info("Updating employee: {}", employee.getEmail());
        if (employee.getId() == null) {
            log.error("Employee ID is missing for update.");
            throw new IllegalArgumentException("Employee ID must be provided for update.");
        }
        try {
            if (!empRepo.existsById(employee.getId())) {
                log.error("Employee not found with ID in update: {}", employee.getId());
                throw new EmployeeNotFoundException("Employee not found with ID: " + employee.getId());
            }
            empRepo.save(employee);
            log.info("Employee updated successfully: {}", employee.getEmail());
            return "Employee updated successfully";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity issue while updating employee with email: {}", employee.getEmail(), e);
            throw new DataIntegrityViolationException("Duplicate email or other constraint violation during update: " + employee.getEmail());
        } catch (Exception e) {
            log.error("Unexpected error while updating employee: {}", employee.getEmail(), e);
            throw new RuntimeException("Unexpected error occurred while updating employee", e);
        }
    }

    @Cacheable(value = DEPARTMENT_CACHE,
            key = "#dept + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public Page<Employee> searchByDept(String dept, Integer page, Integer size, String sortBy, String sortDirection) throws Exception {
        log.info("Searching employees in department: {}", dept);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employees = empRepo.findByDepartment(dept, pageable);
        if (employees.isEmpty()) {
            log.warn("No employees found in department: {}", dept);
            return Page.empty(pageable);  // Return an empty list if no employees found
        }
        return employees;
    }

    @Cacheable(value = EMPLOYEE_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public Employee findEmpById(Long id) {
        log.info("Finding employee by ID: {}", id);
        return empRepo.findById(id).orElseThrow(() -> {
            log.error("Employee not found with ID: {}", id);
            return new EmployeeNotFoundException("Employee not found with ID: " + id);
        });
    }

    @Caching(evict = {
            @CacheEvict(value = EMPLOYEE_CACHE, key = "#employee.id", condition = "#employee != null && #employee.id != null"),
            @CacheEvict(value = {EMPLOYEES_LIST_CACHE, DEPARTMENT_CACHE}, allEntries = true)
    })
    @Transactional(rollbackFor = {Exception.class},
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public void softDeleteEmployeeById(Long id) {
        log.info("Soft-deleting employee with ID: {}", id);
        int rowsUpdated = empRepo.softDeleteById(id);
        if (rowsUpdated == 0) {
            log.error("No employee found with ID: {}", id);
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        }
        log.info("Successfully soft-deleted employee with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(@NotBlank(message = "Please provide email") @Email(message = "Please provide email format.") String email) {
        return empRepo.findByEmail(email).isPresent();
    }

    @Cacheable(value = SEARCH_CACHE,
            key = "{#department, #firstName, #lastName, #minSalary, #maxSalary, #page, #size, #sortBy, #sortDirection}")
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(
            String department,
            String firstName,
            String lastName,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, sort);
        log.debug("Sort by: {}, Direction: {}", sortBy, sortDirection);
        return empRepo.searchEmployees(
                department,
                firstName,
                lastName,
                minSalary,
                maxSalary,
                pageable
        );
    }
}
