package com.spse.gameresolutionchanger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;

import java.util.ArrayList;

public class SettingsManager {
    //PUBLIC OPTIONS
    private boolean ONLY_ADD_GAMES;

    private boolean AGGRESSIVE_LOW_MEMORY_KILLER;
    private boolean KILL_ALL_OTHER_APPS;


    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Activity activity;
    int[] displayStats = new int[3]; //Width, Height, DPI

    final String SETTINGS_FILE = "SETTINGS";

    public SettingsManager(Activity activity){
        this.activity = activity;
        preferences = activity.getSharedPreferences(SETTINGS_FILE,Context.MODE_PRIVATE);
        Display display = activity.getWindowManager().getDefaultDisplay();

        Point point = new Point();
        display.getRealSize(point);
        displayStats[0] = point.x;
        displayStats[1] = point.y;
        displayStats[2] = activity.getResources().getDisplayMetrics().densityDpi;

        ONLY_ADD_GAMES = preferences.getBoolean("onlyAddGames", true);
        AGGRESSIVE_LOW_MEMORY_KILLER = preferences.getBoolean("aggressiveLMK", false);
        KILL_ALL_OTHER_APPS = preferences.getBoolean("isMurderer", false);
    }


    //Get stuff
    public int getCurrentWidth(){
        return displayStats[0];
    }

    public int getCurrentHeight(){
        return displayStats[1];
    }

    public float getCurrentDensity(){
        return displayStats[2];
    }

    public boolean isFirstLaunch(){
        return preferences.getBoolean("firstLaunch",true);
    }

    public float getOriginalDensity(){
        return preferences.getFloat("originalDPI", getCurrentDensity());
    }

    public String getOriginalResolution(){
        return preferences.getString("originalResolution", getCurrentWidth() + "x" + getCurrentHeight());
    }

    public int getOriginalWidth(){
        return preferences.getInt("originalWidth", getCurrentWidth());
    }

    public int getOriginalHeight(){
        return preferences.getInt("originalHeight", getCurrentHeight());
    }


    public GameApp getRecentGameApp(int index){
        return GameAppManager.getGameApp(activity, preferences.getString(String.valueOf(index).concat("thGame"),""));
    }

    public boolean onlyAddGames(){
        return ONLY_ADD_GAMES;
    }

    public boolean isLMKActivated(){
        return AGGRESSIVE_LOW_MEMORY_KILLER;
    }

    public boolean isMurderer(){
        return KILL_ALL_OTHER_APPS;
    }



    //Set stuff
    public void initializeFirstLaunch(){
        //Screen dimension stuff
        editor = preferences.edit();
        int width = getCurrentWidth();
        int height = getCurrentHeight();
        editor.putInt("originalWidth", width);
        editor.putInt("originalHeight", height);
        editor.putString("originalResolution", width + "x" + height);
        editor.putFloat("originalDPI", getCurrentDensity());


        //Now we've collected the info
        editor.putBoolean("firstLaunch", false);



        editor.apply();
    }

    public boolean setScreenDimension(int width, int height, int densityDPI){
        boolean success;
        ArrayList<String> commands = new ArrayList<>(2);
        if (height < getCurrentHeight()){ //Scale Down
            commands.add("su -c wm density " + densityDPI);
            commands.add("su -c wm size " + width +"x"+ height);
        }else{ //Scale up
            commands.add("su -c wm size " + width +"x"+ height);
            commands.add("su -c wm density " + densityDPI);
        }

        success = ExecuteAsRootBase.execute(commands);
        displayStats[0] = width;
        displayStats[1] = height;
        displayStats[2] = densityDPI;

        return success;

    }


    public void addGameApp(String packageName){
        editor = preferences.edit();

        editor.putString("6thGame", preferences.getString("5thGame", ""));
        editor.putString("5thGame", preferences.getString("4thGame", ""));
        editor.putString("4thGame", preferences.getString("3thGame", ""));
        editor.putString("3thGame", preferences.getString("2thGame", ""));
        editor.putString("2thGame", preferences.getString("1thGame", ""));

        //Then the last added game
        editor.putString("1thGame", packageName);

        editor.apply();
    }


    public void setOnlyAddGames(boolean state){
        ONLY_ADD_GAMES = state;

        editor = preferences.edit();
        editor.putBoolean("onlyAddGames", state);
        editor.apply();
    }

    public void setLMK(boolean state){
        AGGRESSIVE_LOW_MEMORY_KILLER = state;

        editor = preferences.edit();
        editor.putBoolean("aggressiveLMK", state);
        editor.apply();
    }

    public void setMurderer(boolean state){
        KILL_ALL_OTHER_APPS = state;

        editor = preferences.edit();
        editor.putBoolean("isMurderer", state);
        editor.apply();
    }



}
