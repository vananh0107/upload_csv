package com.example.demo.services;

import com.example.demo.models.ClassRoom;
import com.example.demo.repositories.ClassRoomRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ClassRoomService {

    @Autowired
    private Validator validator;

    @Autowired
    private ClassRoomRepository classRoomRepository;

    @Transactional
    public List<ClassRoom> importClasses(String filePath) throws Exception {
        List<ClassRoom> classes = new ArrayList<>();
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
                String[] values = line.split(",");
                if (values.length < headers.length) {
                    throw new Exception("Dòng có số lượng cột ít hơn header: " + line);
                }
                ClassRoom classRoom = new ClassRoom();
                Class<?> clazz = classRoom.getClass();
                for (String header : headers) {
                    String fieldName = header.trim().toLowerCase();
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    int fieldIndex = headerMap.get(fieldName);
                    String fieldValue = values[fieldIndex];

                    if (fieldName.equals("capacity")) {
                        field.set(classRoom, Integer.parseInt(fieldValue));
                    } else {
                        field.set(classRoom, fieldValue);
                    }
                }

                Set<ConstraintViolation<ClassRoom>> violations = validator.validate(classRoom);
                if (!violations.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (ConstraintViolation<ClassRoom> violation : violations) {
                        sb.append(violation.getMessage()).append("\n");
                    }
                    validationErrors.add("Validation error for classRoom " + classRoom.getName() + ": \n" + sb.toString());
                } else {
                    classes.add(classRoom);
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (String error : validationErrors) {
                errorMessage.append(error).append("\n");
            }
            throw new Exception("Validation failed for one or more classrooms: \n" + errorMessage.toString());
        }

        for (ClassRoom classRoom : classes) {
            classRoomRepository.save(classRoom);
        }

        return classes;
    }

    @Transactional
    public String exportClasses(String filePath) throws IOException {
        List<ClassRoom> classes = classRoomRepository.findAll();

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Name,Capacity\n");

            for (ClassRoom classRoom : classes) {
                writer.write(classRoom.getName() + ","
                        + classRoom.getCapacity() + "\n");
            }
        }

        return filePath;
    }
}
