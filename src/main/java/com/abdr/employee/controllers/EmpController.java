package com.abdr.employee.controllers;

import com.abdr.employee.dto.EmployeeDTO;
import com.abdr.employee.dto.EmployeeDtoMapper;
import com.abdr.employee.service.EmpService;
import com.abdr.employee.utils.EmployeeNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.List;



@Controller
@RequestMapping("/employees")
public class EmpController {

    private final EmpService empService;
    private final EmployeeDtoMapper employeeDtoMapper;
    private static final Logger log = LoggerFactory.getLogger(EmpController.class);

    public EmpController(EmpService empService, EmployeeDtoMapper employeeDtoMapper) {
        this.empService = empService;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    @GetMapping("/getAllEmployees")
    public String getAllEmployees(Model model){
        List<EmployeeDTO> employeeDTOs=empService.getAllEmployees().stream().map(EmployeeDtoMapper::toDto).toList();
        model.addAttribute("employees",employeeDTOs);
        return "AllEmployees";
    }

    @GetMapping("/getActive")
    public String getActiveEmployees(Model model){
        List<EmployeeDTO> employeeDTOs=empService.getActiveEmployees().stream().map(EmployeeDtoMapper::toDto).toList();
        model.addAttribute("employees",employeeDTOs);
        return "AllEmployees";
    }

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


    @GetMapping("/add")
    public String showAddEmpForm(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employeeDTO", new EmployeeDTO());
        }
        return "addEmployee";
    }


    @PutMapping("/edit")
    public String editEmployee(@ModelAttribute @Valid EmployeeDTO employeeDTO,BindingResult result, RedirectAttributes redirectAttributes,Model model){
        if (result.hasErrors()) {
            model.addAttribute("employeeDTO", employeeDTO);
            result.getAllErrors().forEach(error -> log.error(error.toString()));
            return "addEmployee"; // Reload the form with error messages
        }
        try {
            String message = empService.updateEmployee(EmployeeDtoMapper.toEntity(employeeDTO));
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/employees/getActive";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating employee: " + e.getMessage());
            return "redirect:/employees/edit/" + employeeDTO.getId();  // Go back to edit page on error
        }
    }

    @GetMapping("/edit/{id}")
    public String editEmp(Model model,@PathVariable("id") Long id){
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


    @DeleteMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id,RedirectAttributes redirectAttributes) {
        try {
            empService.softDeleteEmployeeById(id);  // Mark employee as deleted
            redirectAttributes.addFlashAttribute("message", "Employee marked as deleted");
            return "redirect:/employees/getActive";  // Redirect to active employees
        } catch (EmployeeNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Employee not found: " + e.getMessage());
            return "redirect:/employees/getActive";  // Redirect to the employee list with error message
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error occurred during deletion: " + e.getMessage());
            return "redirect:/employees/getActive";  // Redirect to the employee list with error message
        }
    }

    @GetMapping("/search/{searchItem}")
    public String searchEmployees(@PathVariable("searchItem") String searchItem,Model model) throws Exception {
        try {
            List<EmployeeDTO> employees = empService.searchByDept(searchItem).stream()
                    .map(EmployeeDtoMapper::toDto)
                    .toList();
            if (employees.isEmpty()) {
                model.addAttribute("message", "No employees found for department: " + searchItem);
            } else {
                model.addAttribute("employees", employees);
            }
            return "AllEmployees";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching employees: " + e.getMessage());
            return "error";  // Display an error page if there was an issue during the search
        }
    }

}
