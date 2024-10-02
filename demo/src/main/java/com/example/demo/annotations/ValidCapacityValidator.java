package com.example.demo.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ValidCapacityValidator implements ConstraintValidator<ValidCapacity, Integer> {

    @Override
    public boolean isValid(Integer capacity, ConstraintValidatorContext context) {
        if (capacity == null) {
            return false;
        }
        return capacity >= 15 && capacity <= 40;
    }
}