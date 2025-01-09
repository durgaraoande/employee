package com.abdr.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Min(2)  for numeric fields
    @Size(min = 2, message = "FirstName must be atleast 2 characters")
    @Column(nullable = false)
    @NotBlank(message = "Please provide first name")
    private String firstName;

    @Size(min = 2, message = "LastName must be atleast 2 characters")
    @Column(nullable = false)
    @NotBlank(message = "Please provide last name")
    private String lastName;

    @Column(nullable = false,unique = true)
    @NotBlank(message = "Please provide email") //for string fields
    @Email(message = "Please provide email format.")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Please provide department name")
    private String department;

    @Positive(message = "Salary must be positive.")
    @Column(nullable = false)
    @NotNull(message = "Please provide salary") //other than string fields which not supports notBlank
    private BigDecimal salary;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    @NotNull(message = "Please provide a joined date")
    private LocalDate joinDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(min = 2, message = "FirstName must be atleast 2 characters") @NotBlank(message = "Please provide first name") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Size(min = 2, message = "FirstName must be atleast 2 characters") @NotBlank(message = "Please provide first name") String firstName) {
        this.firstName = firstName;
    }

    public @Size(min = 2, message = "LastName must be atleast 2 characters") @NotBlank(message = "Please provide last name") String getLastName() {
        return lastName;
    }

    public void setLastName(@Size(min = 2, message = "LastName must be atleast 2 characters") @NotBlank(message = "Please provide last name") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "Please provide email") @Email(message = "Please provide email format.") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Please provide email") @Email(message = "Please provide email format.") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Please provide department name") String getDepartment() {
        return department;
    }

    public void setDepartment(@NotBlank(message = "Please provide department name") String department) {
        this.department = department;
    }

    public @Positive(message = "Salary must be positive.") @NotNull(message = "Please provide salary") BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(@Positive(message = "Salary must be positive.") @NotNull(message = "Please provide salary") BigDecimal salary) {
        this.salary = salary;
    }

    public @NotNull(message = "Please provide a joined date") LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(@NotNull(message = "Please provide a joined date") LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
