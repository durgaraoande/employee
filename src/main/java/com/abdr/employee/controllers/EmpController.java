package com.abdr.employee.controllers;

import com.abdr.employee.dto.EmployeeDTO;
import com.abdr.employee.dto.EmployeeDtoMapper;
import com.abdr.employee.service.EmpService;
import com.abdr.employee.utils.EmployeeNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/employees")
public class EmpController {

    private static final Logger log = LoggerFactory.getLogger(EmpController.class);
    private final EmpService empService;
    private final EmployeeDtoMapper employeeDtoMapper;

    public EmpController(EmpService empService, EmployeeDtoMapper employeeDtoMapper) {
        this.empService = empService;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    @GetMapping("/getAllEmployees")
    public String getAllEmployees(@RequestParam(defaultValue = "firstName") String sortBy,
                                  @RequestParam(defaultValue = "ASC") String sortDirection,
                                  @RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  Model model) {
        Page<EmployeeDTO> employeeDTOs = empService.getAllEmployees(page, size, sortBy, sortDirection).map(EmployeeDtoMapper::toDto);
        if (employeeDTOs.isEmpty()) {
            model.addAttribute("message", "No employees found");
        } else {
            model.addAttribute("employees", employeeDTOs.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", employeeDTOs.getTotalPages());
            model.addAttribute("totalItems", employeeDTOs.getTotalElements());
        }
        return "AllEmployees";
    }

    @GetMapping("/getActive")
    public String getActiveEmployees(@RequestParam(defaultValue = "firstName") String sortBy,
                                     @RequestParam(defaultValue = "ASC") String sortDirection,
                                     @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     Model model) {
        Page<EmployeeDTO> employeeDTOs = empService.getActiveEmployees(page, size, sortBy, sortDirection).map(EmployeeDtoMapper::toDto);
        if (employeeDTOs.isEmpty()) {
            model.addAttribute("message", "No employees found");
        } else {
            model.addAttribute("employees", employeeDTOs.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", employeeDTOs.getTotalPages());
            model.addAttribute("totalItems", employeeDTOs.getTotalElements());
        }
        return "AllEmployees";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/add")
    public String addEmployee(@ModelAttribute @Valid EmployeeDTO employeeDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        // Handle validation errors
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> log.error(error.toString()));
            model.addAttribute("employeeDTO", employeeDTO);
            return "addEmployee"; // Reload the form with error messages
        }

        try {
            // Check for email uniqueness
            if (empService.existsByEmail(employeeDTO.getEmail())) {
                model.addAttribute("employeeDTO", employeeDTO);
                model.addAttribute("error", "Email already exists: " + employeeDTO.getEmail());
                return "addEmployee";
            }

            // Add employee
            String message = empService.addEmployee(EmployeeDtoMapper.toEntity(employeeDTO));
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/employees/getActive";
        } catch (Exception e) {
            // Handle other errors
            model.addAttribute("employeeDTO", employeeDTO);
            model.addAttribute("error", "Error adding employee: " + e.getMessage());
            return "addEmployee"; // Reload the form with error messages
        }
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/add")
    public String showAddEmpForm(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employeeDTO", new EmployeeDTO());
        }
        return "addEmployee";
    }


    @PutMapping("/edit")
    public String editEmployee(@ModelAttribute @Valid EmployeeDTO employeeDTO, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        log.debug("Received employeeDTO for update: {}", employeeDTO);
        if (result.hasErrors()) {
            model.addAttribute("employeeDTO", employeeDTO);
//            result.getAllErrors().forEach(error -> log.error(error.toString()));
            log.error("Validation errors occurred: {}", result.getAllErrors());
//            return "addEmployee"; // Reload the form with error messages
            return "edit-employee";
        }
        try {
            String message = empService.updateEmployee(EmployeeDtoMapper.toEntity(employeeDTO));
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/employees/getActive";
        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error updating employee: " + e.getMessage());
//            return "redirect:/employees/edit/" + employeeDTO.getId();  // Go back to edit page on error
            log.error("Error updating employee: ", e);
            redirectAttributes.addFlashAttribute("error", "Error updating employee: " + e.getMessage());
            model.addAttribute("employeeDTO", employeeDTO);
            return "edit-employee";
        }
    }

    @GetMapping("/edit/{id}")
    public String editEmp(Model model, @PathVariable("id") Long id) {
        try {
            EmployeeDTO emp = EmployeeDtoMapper.toDto(empService.findEmpById(id));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = emp.getJoinDate().format(formatter);
            model.addAttribute("formattedJoinDate", formattedDate);
            model.addAttribute("employeeDTO", emp);
            return "edit-employee";
        } catch (EmployeeNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "error";  // Show error page if employee not found
        }
    }


//    @DeleteMapping("/delete/{id}")
//    @ResponseBody
//    public String deleteEmployee(@PathVariable Long id,RedirectAttributes redirectAttributes) {
//        try {
//            empService.softDeleteEmployeeById(id);  // Mark employee as deleted
//            redirectAttributes.addFlashAttribute("message", "Employee marked as deleted");
//            return "redirect:/employees/getActive";  // Redirect to active employees
//        } catch (EmployeeNotFoundException e) {
//            redirectAttributes.addFlashAttribute("error", "Employee not found: " + e.getMessage());
//            return "redirect:/employees/getActive";  // Redirect to the employee list with error message
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error occurred during deletion: " + e.getMessage());
//            return "redirect:/employees/getActive";  // Redirect to the employee list with error message
//        }
//    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    @ResponseBody //for json serialization
    public Map<String, String> deleteEmployee(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current User: {}", authentication.getName());
        log.info("User Roles: {}", authentication.getAuthorities());

        Map<String, String> response = new HashMap<>();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            response.put("status", "error");
            response.put("message", "Access Denied: Only administrators can delete employees");
            return response;
        }

        try {
            empService.softDeleteEmployeeById(id);
            response.put("status", "success");
            response.put("message", "Employee successfully deleted");
            return response;
        } catch (EmployeeNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Employee not found: " + e.getMessage());
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error occurred during deletion: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/search/{searchItem}")
    public String searchEmployees(@PathVariable("searchItem") String searchItem,
                                  @RequestParam(defaultValue = "firstName") String sortBy,
                                  @RequestParam(defaultValue = "ASC") String sortDirection,
                                  @RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  Model model) throws Exception {
        try {
            Page<EmployeeDTO> employees = empService.searchByDept(searchItem, page, size, sortBy, sortDirection)
                    .map(EmployeeDtoMapper::toDto);
            if (employees.isEmpty()) {
                model.addAttribute("message", "No employees found for department: " + searchItem);
            } else {
                model.addAttribute("employees", employees);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", employees.getTotalPages());
                model.addAttribute("totalItems", employees.getTotalElements());
            }
            return "AllEmployees";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching employees: " + e.getMessage());
            return "error";  // Display an error page if there was an issue during the search
        }
    }

    @GetMapping("/advSearch")
    public String searchEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Model model) {

        try {
            Page<EmployeeDTO> results = empService.searchEmployees(
                    department, firstName, lastName, minSalary, maxSalary, page, size, sortBy, sortDirection
            ).map(EmployeeDtoMapper::toDto);

            if (results.isEmpty()) {
                model.addAttribute("message", "No employees found matching the criteria");
            } else {
                model.addAttribute("employees", results.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", results.getTotalPages());
                model.addAttribute("totalItems", results.getTotalElements());
            }

            return "AllEmployees";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching employees: " + e.getMessage());
            return "error";
        }
    }

}
