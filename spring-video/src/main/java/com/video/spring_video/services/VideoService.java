package com.video.spring_video.services;


import com.video.spring_video.schemas.Video;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface VideoService {

    //save  video
    Video save(Video video, MultipartFile file);


    // get video by  id
    Video get(String videoId);


    // get video by title

    Video getByTitle(String title);

    List<Video> getAll();


    //video processing
    String processVideo(String videoId);


}