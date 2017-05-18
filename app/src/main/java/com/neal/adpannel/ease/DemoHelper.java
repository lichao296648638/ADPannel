package com.neal.adpannel.ease;


public class DemoHelper {

    public boolean isVoiceCalling;
    public boolean isVideoCalling;
    private static DemoHelper instance = null;

    private DemoHelper() {
    }

    public synchronized static DemoHelper getInstance() {
        if (instance == null) {
            instance = new DemoHelper();
        }
        return instance;
    }

}
