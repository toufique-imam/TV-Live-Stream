package com.playtv.go;

import java.util.ArrayList;

class stream_info {
    String name, video_url, user_agent;

    public stream_info() {

    }

    public stream_info(String name, String video_url, String user_token) {
        this.name = name;
        this.video_url = video_url;
        this.user_agent = user_token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getUser_token() {
        return user_agent;
    }

    public void setUser_token(String user_token) {
        this.user_agent = user_token;
    }

    @Override
    public String toString() {
        return "stream_info{" +
                "name='" + name + '\'' +
                ", video_url='" + video_url + '\'' +
                ", user_agent='" + user_agent + '\'' +
                '}';
    }
}

public class channelInfo {
    String category, channel_name, poster_path;
    ArrayList<stream_info> spoken_languages;
    boolean fav;

    public channelInfo() {
        spoken_languages = new ArrayList<>();
        fav = false;
    }

    public channelInfo(String category, String channel_name, String poster_path, ArrayList<stream_info> spoken_languages, boolean fav) {
        this.category = category;
        this.channel_name = channel_name;
        this.poster_path = poster_path;
        this.spoken_languages = spoken_languages;
        this.fav = fav;
    }

    public boolean contains(String query) {
        query = query.toLowerCase();
        return (query.length() <= channel_name.length() && channel_name.toLowerCase().contains(query));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public ArrayList<stream_info> getSpoken_languages() {
        return spoken_languages;
    }

    public void setSpoken_languages(ArrayList<stream_info> spoken_languages) {
        this.spoken_languages = spoken_languages;
    }

    String getChannelName_real() {

        String[] check = {"period", "dollarsign", "leftsquarebracket", "rightsquarebracket", "poundsign", "forwardslash"};
        String[] repcheck = {".", "$", "[", "]", "#", "/"};
        String newCatName = this.channel_name;
        for (int i = 0; i < check.length; i++) {
            newCatName = newCatName.replace(check[i], repcheck[i]);

        }
        return newCatName;
    }

    String getCategoryName_real() {
        String[] check = {"period", "dollarsign", "leftsquarebracket", "rightsquarebracket", "poundsign", "forwardslash"};
        String[] repcheck = {".", "$", "[", "]", "#", "/"};
        String newCatName = this.category;
        for (int i = 0; i < check.length; i++) {
            newCatName = newCatName.replace(check[i], repcheck[i]);

        }
        return newCatName;
    }
}
