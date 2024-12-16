package com.video.spring_video.controllers;

import com.video.spring_video.Constants;
import com.video.spring_video.schemas.Video;
import com.video.spring_video.services.VideoService;
import com.video.spring_video.utils.CustomMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {

    private final VideoService videoService;

    // Inject VideoService into the controller
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Endpoint to upload a video file.
     * @param file Multipart file representing the video to be uploaded.
     * @param title Title of the video.
     * @param description Description of the video.
     * @return ResponseEntity with the saved video details or an error message.
     */
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {
        // Create a new video object
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString()); // Generate a unique ID for the video

        // Save the video using the VideoService
        Video savedVideo = videoService.save(video, file);

        // Return the appropriate response based on save result
        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new CustomMessage.Builder()
                            .setMessage("Video not uploaded")
                            .setSuccess(false)
                            .build()
            );
        }
    }

    /**
     * Endpoint to fetch all videos.
     * @return List of all videos.
     */
    @GetMapping
    public List<Video> getAll() {
        return videoService.getAll();
    }

    /**
     * Endpoint to stream a video file in its entirety.
     * @param videoId Unique ID of the video to stream.
     * @return Resource representing the video file.
     */
    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        // Default to binary content type if not set
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * Endpoint to stream a video file in chunks (for large file support).
     * Supports HTTP Range Requests for partial content.
     * @param videoId Unique ID of the video to stream.
     * @param range HTTP Range header specifying the requested byte range.
     * @return Video content in the specified range or an error message.
     */
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoInRange(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());
        String contentType = video.getContentType();

        // Default to binary content type if not set
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();

        // If no Range header is provided, return the whole video
        if (range == null) {
            Resource resource = new FileSystemResource(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        try {
            // Parse the range header
            String[] ranges = range.replace("bytes=", "").split("-");
            long rangeStart = Long.parseLong(ranges[0]);
            long rangeEnd = rangeStart + Constants.CHUNK_SIZE - 1;

            // Ensure range end doesn't exceed file length
            if (rangeEnd >= fileLength) {
                rangeEnd = fileLength - 1;
            }

            // Read the requested range from the file
            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);

            // Create response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Base directory for HLS video files (injected from application properties)
    @Value("${video-hls}")
    private String hls;

    /**
     * Endpoint to serve the HLS master manifest file.
     * @param videoId Unique ID of the video.
     * @return HLS master manifest file as a resource.
     */
    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(hls, videoId, "master.m3u8");

        // Check if the manifest file exists
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    /**
     * Endpoint to serve HLS segment files.
     * @param videoId Unique ID of the video.
     * @param segment Name of the segment file.
     * @return HLS segment file as a resource.
     */
    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
            @PathVariable String videoId,
            @PathVariable String segment
    ) {
        Path path = Paths.get(hls, videoId, segment + ".ts");

        // Check if the segment file exists
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }
}
