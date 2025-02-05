package com.abdr.employee.dto;

import com.abdr.employee.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDtoMapper {
    public static EmployeeDTO toDto(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .salary(employee.getSalary())
                .joinDate(employee.getJoinDate())
                .build();
    }

    public static Employee toEntity(EmployeeDTO employeeDTO) {
        return Employee.builder()
                .id(employeeDTO.getId())
                .firstName(employeeDTO.getFirstName())
                .lastName(employeeDTO.getLastName())
                .email(employeeDTO.getEmail())
                .department(employeeDTO.getDepartment())
                .salary(employeeDTO.getSalary())
                .joinDate(employeeDTO.getJoinDate())
                .build();
    }
}
