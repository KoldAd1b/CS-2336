package com.video.spring_video.repo;


import com.video.spring_video.schemas.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface VideoStore extends JpaRepository<Video,String> {

    Optional<Video> findByTitle(String title);

    //query methods

    //native

    //criteria api

}
