package com.techacademy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmail(String email);
}