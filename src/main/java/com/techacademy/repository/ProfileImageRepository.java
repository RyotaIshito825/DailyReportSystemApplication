package com.techacademy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.ProfileImage;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Integer> {

    Optional<ProfileImage> findByEmployee(Employee employee);

}
