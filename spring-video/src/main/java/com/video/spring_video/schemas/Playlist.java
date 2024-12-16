package com.video.spring_video.schemas;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
public class Playlist {

    @Id
    private  String id;

    private  String title;

//    @OneToMany(mappedBy = "course")
//    private List<Video> list=new ArrayList<>();
}