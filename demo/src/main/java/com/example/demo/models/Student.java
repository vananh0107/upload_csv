package com.example.demo.models;

import com.example.demo.annotations.ValidDate;
import com.example.demo.annotations.ValidPhone;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ValidDate(message = "Ngày sinh không hợp lệ và phải nhỏ hơn ngày hiện tại")
    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false)
    private String address;

    @ValidPhone(message = "Số điện thoại phải bắt đầu bằng '09', chỉ chứa ký tự số và dài đúng 10 ký tự")
    @Column(nullable = false, length = 10)
    private String phone;
}