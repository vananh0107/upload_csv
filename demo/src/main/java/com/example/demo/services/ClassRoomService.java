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
import java.util.ArrayList;
import java.util.List;
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
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                ClassRoom classRoom = new ClassRoom();
                classRoom.setName(values[0]);
                classRoom.setCapacity(Integer.parseInt(values[1]));

                Set<ConstraintViolation<ClassRoom>> violations = validator.validate(classRoom);
                if (!violations.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (ConstraintViolation<ClassRoom> violation : violations) {
                        sb.append(violation.getMessage()).append("\n");
                    }
                    throw new Exception("Validation error: \n" + sb.toString());
                }

                classes.add(classRoom);
                classRoomRepository.save(classRoom);
            }
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
