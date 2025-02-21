package com.techacademy.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;

@Service
public class UserDetailService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    public UserDetailService(EmployeeRepository repository) {
        this.employeeRepository = repository;
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Optional<Employee> employee = employeeRepository.findById(Integer.parseInt(username));
//
//        if (employee.isEmpty()) {
//            throw new UsernameNotFoundException("Exception:Username Not Found");
//        }
//        return new UserDetail(employee.get());
//    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Employee> employee = employeeRepository.findByEmail(email);
        if (employee.isEmpty()) {
            throw new UsernameNotFoundException("Exception:Email Not Found");
        }
        return new UserDetail(employee.get());
    }
}