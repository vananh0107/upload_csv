package com.example.demo.services;

import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class StudentService {

    @Autowired
    private Validator validator;

    @Autowired
    private StudentRepository studentRepository;

    @Transactional
    public List<Student> importStudents(String filePath) throws Exception {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        List<Student> students = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                Student student = new Student();
                student.setName(values[0]);
                student.setBirthdate(LocalDate.parse(values[1], dateFormatter));
                student.setAddress(values[2]);
                student.setPhone(values[3]);

                Set<ConstraintViolation<Student>> violations = validator.validate(student);
                if (!violations.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (ConstraintViolation<Student> violation : violations) {
                        sb.append(violation.getMessage()).append("\n");
                    }
                    validationErrors.add("Validation error for student " + student.getName() + ": \n" + sb.toString());
                } else {
                    students.add(student);
                }
            }
        }
        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (String error : validationErrors) {
                errorMessage.append(error).append("\n");
            }
            throw new Exception("Validation failed for one or more students: \n" + errorMessage.toString());
        }

        for (Student student : students) {
            studentRepository.save(student);
        }

        return students;
    }

    @Transactional
    public String exportStudents(String filePath) throws IOException {
        List<Student> students = studentRepository.findAll();

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Name,Birthdate,Address,Phone\n");

            for (Student student : students) {
                writer.write(student.getName() + ","
                        + student.getBirthdate() + ","
                        + student.getAddress() + ","
                        + "'" + student.getPhone() + "\n");
            }
        }
        return filePath;
    }


}
