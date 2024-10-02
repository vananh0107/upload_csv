package com.example.demo.annotations;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidCapacityValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCapacity {
    String message() default "Capacity phải nằm trong khoảng từ 15 đến 40";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
