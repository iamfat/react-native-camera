package org.reactnative.facedetector;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.reactnative.facedetector.tasks.FaceIdentificationAsyncTask;
import org.reactnative.facedetector.tasks.FileFaceDetectionAsyncTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class FaceDetectorModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNFaceDetector";
    //  private ScopedContext mScopedContext;
    private static ReactApplicationContext mScopedContext;

    public FaceDetectorModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mScopedContext = reactContext;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("Mode", getFaceDetectionModeConstants());
                put("Landmarks", getFaceDetectionLandmarksConstants());
                put("Classifications", getFaceDetectionClassificationsConstants());
            }

            private Map<String, Object> getFaceDetectionModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("fast", 0);
                        put("accurate", 1);
                    }
                });
            }

            private Map<String, Object> getFaceDetectionClassificationsConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("all", 1);
                        put("none", 0);
                    }
                });
            }

            private Map<String, Object> getFaceDetectionLandmarksConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("all", 1);
                        put("none", 0);
                    }
                });
            }
        });
    }

    @ReactMethod
    public void detectFaces(ReadableMap options, final Promise promise) {
        new FileFaceDetectionAsyncTask(mScopedContext, options, promise).execute();
    }

    @ReactMethod
    public void identifyFace(String feature, ReadableMap candidates, float threshold, final Promise promise) {
        new FaceIdentificationAsyncTask(mScopedContext, feature, candidates, threshold, promise).execute();
    }

}
