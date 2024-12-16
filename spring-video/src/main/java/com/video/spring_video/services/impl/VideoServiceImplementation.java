package com.video.spring_video.services.impl;

import com.video.spring_video.repo.VideoStore;
import com.video.spring_video.schemas.Video;
import com.video.spring_video.services.VideoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImplementation implements VideoService {

    @Value("${video-folder}")
    private String videoDir;

    @Value("${video-hls}")
    private String hlsDir;

    private final VideoStore videoStore;

    // Constructor injection of VideoStore
    public VideoServiceImplementation(VideoStore videoStore) {
        this.videoStore = videoStore;
    }

    /**
     * Initializes necessary directories for video storage and processing.
     */
    @PostConstruct
    public void init() {
        File videoFolder = new File(videoDir);
        try {
            Files.createDirectories(Paths.get(hlsDir)); // Ensure HLS directory exists
        } catch (IOException e) {
            throw new RuntimeException("Failed to create HLS directory", e);
        }

        if (!videoFolder.exists()) {
            videoFolder.mkdir(); // Create video storage folder if it doesn't exist
            System.out.println("Video folder created: " + videoDir);
        } else {
            System.out.println("Video folder already exists: " + videoDir);
        }
    }

    /**
     * Saves a video file and its metadata to the storage.
     *
     * @param video Video metadata to be saved.
     * @param file  Multipart file representing the video file.
     * @return The saved video metadata object.
     */
    @Override
    public Video save(Video video, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            // Clean and format filenames and directory paths
            String sanitizedFilename = StringUtils.cleanPath(originalFilename);
            String sanitizedFolderPath = StringUtils.cleanPath(videoDir);

            // Construct file path and save the file
            Path filePath = Paths.get(sanitizedFolderPath, sanitizedFilename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Set video metadata
            video.setContentType(contentType);
            video.setFilePath(filePath.toString());

            // Save metadata in the database
            Video savedVideo = videoStore.save(video);

            // Process the video (e.g., create HLS segments)
            processVideo(savedVideo.getVideoId());

            return savedVideo; // Return saved video metadata
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves a video by its ID.
     *
     * @param videoId Unique ID of the video.
     * @return Video metadata object.
     */
    @Override
    public Video get(String videoId) {
        return videoStore.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + videoId));
    }

    /**
     * Retrieves a video by its title.
     * (Currently unimplemented)
     *
     * @param title Title of the video.
     * @return Video metadata object.
     */
    @Override
    public Video getByTitle(String title) {
        return null; // Placeholder for future implementation
    }

    /**
     * Retrieves all videos from the database.
     *
     * @return List of all video metadata objects.
     */
    @Override
    public List<Video> getAll() {
        return List.of(); // Placeholder for actual implementation
    }

    /**
     * Processes a video to generate HLS segments using FFmpeg.
     *
     * @param videoId Unique ID of the video to process.
     * @return The ID of the processed video.
     */
    @Override
    public String processVideo(String videoId) {
        Video video = this.get(videoId); // Retrieve video metadata
        String filePath = video.getFilePath();
        Path videoPath = Paths.get(filePath);

        try {
            // Create output directory for HLS segments
            Path outputPath = Paths.get(hlsDir, videoId);
            Files.createDirectories(outputPath);

            // Construct FFmpeg command for HLS conversion
            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 "
                            + "-hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/master.m3u8\"",
                    videoPath, outputPath, outputPath
            );

            System.out.println("Executing FFmpeg command: " + ffmpegCmd);

            // Execute FFmpeg command
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
            processBuilder.inheritIO(); // Redirect process output to console for debugging
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Video processing failed with exit code: " + exitCode);
            }

            return videoId; // Return the processed video ID
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during video processing", e);
        }
    }
}
