package com.karlov.mp3player.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Tracklist {
    private StringProperty name;
    private String path;
    private ObservableList<Track> trackObservableList;

    public Tracklist() {
        name=new SimpleStringProperty("");
    }

    public Tracklist(StringProperty name, String path, ObservableList<Track> trackObservableList) {
        this.name = name;
        this.path = path;
        this.trackObservableList = trackObservableList;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
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
