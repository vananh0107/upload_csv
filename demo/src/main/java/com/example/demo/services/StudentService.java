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
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new Exception("Tệp CSV không có dữ liệu");
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");

                if (values.length < headers.length) {
                    throw new Exception("Dòng có số lượng cột ít hơn header: " + line);
                }

                Student student = new Student();
                Class<?> clazz = student.getClass();

                for (String header : headers) {
                    String fieldName = header.trim().toLowerCase();
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    int fieldIndex = headerMap.get(fieldName);
                    String fieldValue = values[fieldIndex];

                    if (field.getType().equals(LocalDate.class)) {
                        field.set(student, LocalDate.parse(fieldValue, dateFormatter));
                    } else {
                        if (fieldName.equals("phone")) {
                            if (fieldValue.startsWith("'")) {
                                fieldValue = fieldValue.substring(1);
                            }
                        }
                        field.set(student, fieldValue);
                    }
                }

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

        // Lưu các đối tượng Student vào cơ sở dữ liệu
        for (Student student : students) {
            studentRepository.save(student);
        }

        return students;
    }

    public String exportStudents(String filePath) throws IOException {
        List<Student> students = studentRepository.findAll();

        // Tạo DateTimeFormatter với định dạng "d/M/yyyy"
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Name,Birthdate,Address,Phone\n");

            for (Student student : students) {
                writer.write(student.getName() + ","
                        + student.getBirthdate().format(dateFormatter) + ","
                        + student.getAddress() + ","
                        + "'" + student.getPhone() + "\n");
            }
        }
        return filePath;
    }


}
