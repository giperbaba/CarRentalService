package com.bookingapp.constant;

public final class MessageConstants {
    private MessageConstants() {
    }

    public static final String STATUS_CANNOT_BE_NULL = "Status cannot be null";

    public static final String CAR_INFO_ERROR = "Failed to get car information";
    public static final String AVAILABLE_CARS_ERROR = "Failed to get list of available cars";
    public static final String UPDATE_BOOKING_STATUS_ERROR = "Failed to update car booking status";
    
    public static final String CAR_INFO_ERROR_LOG = "Error while getting car information with ID: {}";
    public static final String AVAILABLE_CARS_ERROR_LOG = "Error while getting list of available cars";
    public static final String UPDATE_BOOKING_STATUS_ERROR_LOG = "Error while updating car booking status with ID: {}";

    public static final String AUTH_HEADER_MISSING = "Authorization header is missing";
    public static final String AUTH_HEADER_INVALID = "Invalid authorization header format";
    public static final String JWT_AUTH_ERROR = "JWT Authentication failed: {}";
    public static final String ACCESS_DENIED = "Access denied: insufficient permissions";
    public static final String TOKEN_BLACKLISTED = "Token is blacklisted";
    
    public static final String PROCESSING_REQUEST = "Processing request: {} {}";
    public static final String ROLES_HEADER = "X-Roles header: {}";
    public static final String USERNAME_HEADER = "X-Username header: {}";
    public static final String TOKEN_BLACKLISTED_DEBUG = "Token is blacklisted: {}";
    public static final String CURRENT_AUTH = "Current authentication: {}";
    public static final String FINAL_AUTH_STATE = "Final authentication state: {}";
} 