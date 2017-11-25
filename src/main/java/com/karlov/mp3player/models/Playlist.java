package com.karlov.mp3player.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Playlist {
    private StringProperty name;
    private String path;
    private ObservableList<Track> trackObservableList;

    public Playlist() {
        name=new SimpleStringProperty("");
    }

    public Playlist(StringProperty name, String path, ObservableList<Track> trackObservableList) {
        this.name = name;
        this.path = path;
        this.trackObservableList = trackObservableList;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ObservableList<Track> getTrackObservableList() {
        return trackObservableList;
    }

    public void setTrackObservableList(ObservableList<Track> trackObservableList) {
        this.trackObservableList = trackObservableList;
    }
}
