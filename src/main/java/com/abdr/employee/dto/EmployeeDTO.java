package com.abdr.employee.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;

    @Size(min = 2, message = "FirstName must be atleast 2 characters")
    @NotBlank(message = "Please provide first name")
    private String firstName;

    @Size(min = 2, message = "LastName must be atleast 2 characters")
    @NotBlank(message = "Please provide last name")
    private String lastName;

    @NotBlank(message = "Please provide email") //for string fields
    @Email(message = "Please provide email format.")
    private String email;

    @NotBlank(message = "Please provide department name")
    private String department;

    @Positive(message = "Salary must be positive.")
    @NotNull(message = "Please provide salary") //other than string fields which not supports notBlank
    private BigDecimal salary;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Please provide a joined date")
    private LocalDate joinDate;
}
