package com.neal.adpannel.entity;

import org.litepal.crud.DataSupport;

/**
 * 每条广告元素的实体类
 * Created by lichao on 17/5/9.
 */

public class AdEntity extends DataSupport {


    /**
     * font_bold : false
     * font_size : 0
     * data : https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1494521130900&di=64c531e9e45405e07fb819346f9f8a5d&imgtype=0&src=http%3A%2F%2Fstock.591hx.com%2Fimages%2Fhnimg%2F20151208%2F18%2F6463988493360928614.jpg
     * top : 0
     * time_stamp : 1
     * height : 525
     * width : 350
     * file_name : picture.jpg
     * font_color :
     * set_type : 0
     * type : 2
     * font_family : 0
     * left : 0
     * play_time : 3
     * banner_group : 20203
     * scene_group : 202020202
     * scene_time : 33
     */

    private boolean font_bold;
    private int font_size;
    private String data;
    private int top;
    private String time_stamp;
    private int height;
    private int width;
    private String file_name;
    private String font_color;
    private int set_type;
    private int type;
    private int font_family;
    private int left;
    private int play_time;
    private String banner_group;
    private String scene_group;
    private int scene_time;
    private int file_length;


    public int getFile_length() {
        return file_length;
    }

    public void setFile_length(int file_length) {
        this.file_length = file_length;
    }


    public String getScene_group() {
        return scene_group;
    }

    public void setScene_group(String scene_group) {
        this.scene_group = scene_group;
    }

    public int getScene_time() {
        return scene_time;
    }

    public void setScene_time(int scene_time) {
        this.scene_time = scene_time;
    }


    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    private String file_path;

    public boolean isFont_bold() {
        return font_bold;
    }

    public void setFont_bold(boolean font_bold) {
        this.font_bold = font_bold;
    }

    public int getFont_size() {
        return font_size;
    }

    public void setFont_size(int font_size) {
        this.font_size = font_size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFont_color() {
        return font_color;
    }

    public void setFont_color(String font_color) {
        this.font_color = font_color;
    }

    public int getSet_type() {
        return set_type;
    }

    public void setSet_type(int set_type) {
        this.set_type = set_type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFont_family() {
        return font_family;
    }

    public void setFont_family(int font_family) {
        this.font_family = font_family;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getPlay_time() {
        return play_time;
    }

    public void setPlay_time(int play_time) {
        this.play_time = play_time;
    }

    public String getBanner_group() {
        return banner_group;
    }

    public void setBanner_group(String banner_group) {
        this.banner_group = banner_group;
    }
}
