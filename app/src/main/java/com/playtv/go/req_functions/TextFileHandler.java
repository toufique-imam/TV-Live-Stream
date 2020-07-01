package com.playtv.go.req_functions;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.playtv.go.MainActivity.FAV_FILE_NAME;
import static com.playtv.go.MainActivity.favourite;

public class TextFileHandler {
    String sessionFileName = "session.txt";

    public TextFileHandler() {
    }

    public void update() {
        String ans = map_to_String();
        WRITE_TEXT(FAV_FILE_NAME, ans);
    }

    public void updateSessionName(String sessionName) {
        WRITE_TEXT(sessionFileName, sessionName);
    }

    public String getSessionName() {
        Pair<Boolean, String> ans = READ_TEXT(sessionFileName);
        return ans.second;
    }

    public String map_to_String() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<String, String> x : favourite.keySet()) {
            if (favourite.get(x)) {
                stringBuilder.append(x.first).append("\n");
                stringBuilder.append(x.second);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public void getUserFavourite() {
        favourite.clear();
        Pair<Boolean, String> fav_string = READ_TEXT(FAV_FILE_NAME);
        if (fav_string.first) {
            //Log.e("DEBUG", fav_string.second);
            String[] lists = fav_string.second.split("\n");
            for (int i = 1; i < lists.length; i += 2) {
                favourite.put(Pair.create(lists[i - 1], lists[i]), true);

            }
        }
    }

    public Pair<Boolean, String> READ_TEXT(String fileName) {
        StringBuilder ans = new StringBuilder();
        String tmp;
        try {
            File root = new File("/sdcard/PlayTVGO/");
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    Log.e("PLAYTVLOG-TEXT :: ", "Problem creating folder");
                } else {
                    WRITE_TEXT(fileName, "");
                    return Pair.create(Boolean.FALSE, "");
                }
            }
            File gpxfile = new File(root, fileName);
            FileReader fileReader = new FileReader(gpxfile);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((tmp = bufferedReader.readLine()) != null) {
                ans.append(tmp);
                ans.append("\n");
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return Pair.create(Boolean.FALSE, null);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return Pair.create(Boolean.FALSE, null);
        }
        Log.e(getClass().getSimpleName(), "DONE READ");
        Log.e("SEE_ME", ans.toString());
        return Pair.create(Boolean.TRUE, ans.toString());
    }

    public boolean WRITE_TEXT(String sFileName, String sBody) {
        try {
            File root = new File("/sdcard/PlayTVGO/");
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    Log.e("PLAYTVLOG-TEXT :: ", "Problem creating folder");
                    return false;
                }
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Log.e(getClass().getSimpleName(), "DONE WRITE");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
