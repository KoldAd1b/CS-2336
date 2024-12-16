package com.video.spring_video.utils;

public class CustomMessage {

    private String message;
    private boolean success = false;

    // No-argument constructor
    public CustomMessage() {
    }

    // Constructor with all fields
    public CustomMessage(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Setter for message
    public void setMessage(String message) {
        this.message = message;
    }

    // Getter for success
    public boolean isSuccess() {
        return success;
    }

    // Setter for success
    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Builder pattern
    public static class Builder {
        private String message;
        private boolean success = false;  // Default value

        // Set the message
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        // Set the success flag
        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        // Build the CustomMessage instance
        public CustomMessage build() {
            return new CustomMessage(message, success);
        }
    }
}
