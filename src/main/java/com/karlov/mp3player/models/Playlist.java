package com.karlov.mp3player.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "name")
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "playlist")
    private List<Track> tracksArrayList;

    public Playlist() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Track> getTracksArrayList() {
        return tracksArrayList;
    }

    public void setTracksArrayList(List<Track> tracksArrayList) {
        this.tracksArrayList = tracksArrayList;
    }
}
