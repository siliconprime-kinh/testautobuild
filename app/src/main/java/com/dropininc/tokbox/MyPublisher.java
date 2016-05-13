package com.dropininc.tokbox;

import android.content.Context;

import com.dropininc.utils.Logs;
import com.opentok.android.Publisher;

public class MyPublisher extends Publisher {

    private String userId;
    private String name;
    private CustomVideoRenderer renderer;

    public MyPublisher(Context context, String stream) {
        super(context, stream);
        // With the userId we can query our own database
        // to extract player information
        setName("User" + ((int) (Math.random() * 1000)));

        renderer = new CustomVideoRenderer(context);
        setRenderer(renderer);
    }

    public MyPublisher(Context context, String name, Publisher.CameraCaptureResolution resolution, Publisher.CameraCaptureFrameRate frameRate) {
        super(context, name, resolution, frameRate);
        setName("User" + ((int) (Math.random() * 1000)));

        renderer = new CustomVideoRenderer(context);
        setRenderer(renderer);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String name) {
        this.userId = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void saveScreenshot(CustomVideoRenderer.SaveScreenshotListener listener) {
        if (renderer != null) {
            renderer.saveScreenshot(listener);
        } else {
            Logs.log("MySubscriber", "renderer null");
        }
    }
}
