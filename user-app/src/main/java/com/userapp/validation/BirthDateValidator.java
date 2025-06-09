package com.userapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class BirthDateValidator implements ConstraintValidator<BirthDate, LocalDate> {
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 100;

    @Override
    public void initialize(BirthDate constraintAnnotation) {}

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        int age = Period.between(birthDate, now).getYears();

        return !birthDate.isAfter(now) && age >= MIN_AGE && age <= MAX_AGE;
    }
} 