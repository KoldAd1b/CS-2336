package com.video.spring_video.schemas;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    private String videoId;

    private String title;

    private String description;

    private String contentType;

    private String filePath;

    // Uncomment and implement the relationship with Course if necessary
    // @ManyToOne
    // private Course course;

    // Default constructor
    public Video() {
    }

    // Constructor with all fields
    public Video(String videoId, String title, String description, String contentType, String filePath) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.contentType = contentType;
        this.filePath = filePath;
    }

    // Getters and Setters
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Builder pattern
    public static class Builder {
        private String videoId;
        private String title;
        private String description;
        private String contentType;
        private String filePath;

        public Builder setVideoId(String videoId) {
            this.videoId = videoId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Video build() {
            return new Video(videoId, title, description, contentType, filePath);
        }
    }

}
