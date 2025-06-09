package com.carapp.constant;

public final class ValidationConstants {
    private ValidationConstants() {
    }

    // regex
    public static final String LICENSE_PLATE_PATTERN = "^[A-Z0-9]{2,10}$";

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MIN_YEAR = 1900;
    public static final int MAX_YEAR = 2025;

    public static final String MIN_DAILY_RATE = "0.01";
    public static final String MAX_DAILY_RATE = "10000.00";
    
    // validation
    public static final String MAKE_REQUIRED = "Make is required";
    public static final String MAKE_SIZE = "Make must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters";
    public static final String MODEL_REQUIRED = "Model is required";
    public static final String MODEL_SIZE = "Model must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters";
    public static final String YEAR_REQUIRED = "Year is required";
    public static final String YEAR_MIN = "Year must be after " + MIN_YEAR;
    public static final String YEAR_MAX = "Year cannot be in the future";
    public static final String LICENSE_PLATE_REQUIRED = "License plate is required";
    public static final String LICENSE_PLATE_FORMAT = "License plate must be 2-10 characters long and contain only uppercase letters and numbers";
    public static final String DAILY_RATE_REQUIRED = "Daily rate is required";
    public static final String DAILY_RATE_MIN = "Daily rate must be greater than 0";
    public static final String DAILY_RATE_MAX = "Daily rate cannot exceed 10000";
    public static final String DESCRIPTION_SIZE = "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters";
    public static final String STATUS_REQUIRED = "Status is required";
    public static final String RENTAL_PRICE_REQUIRED = "Rental price is required";
    public static final String RENTAL_PRICE_MIN = "Rental price must be greater than 0";
    public static final String RENTAL_PRICE_MAX = "Rental price cannot exceed 10000";
    
    // errors
    public static final String CAR_NOT_FOUND = "Car not found";
    public static final String CAR_NOT_AVAILABLE = "Car is not available";
    public static final String INVALID_STATUS_UPDATE = "Car status can only be changed to AVAILABLE or MAINTENANCE";
    public static final String CAR_STATUS_CHANGE_NOT_ALLOWED = "Car status can only be changed when car is AVAILABLE or in MAINTENANCE";
    public static final String LICENSE_PLATE_EXISTS = "Car with this license plate already exists";

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
} 