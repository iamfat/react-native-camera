package org.reactnative.facedetector;

import android.content.Context;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;

import java.util.ArrayList;
import java.util.List;

public class RNFaceDetector {

    public class Face {
        public FaceInfo info;
        public FaceFeature feature;
    }

    public static int ALL_CLASSIFICATIONS = 1;
    public static int NO_CLASSIFICATIONS = 0;
    public static int ALL_LANDMARKS = 1;
    public static int NO_LANDMARKS = 0;
    public static int ACCURATE_MODE = 1;
    public static int FAST_MODE = 0;

    public static String mAppId = "";
    public static String mSdkKey = "";

    private FaceEngine mEngine = null;
    private Context mContext = null;
    private int mClassificationType = NO_CLASSIFICATIONS;
    private int mLandmarkType = NO_LANDMARKS;
    private int mMode = FAST_MODE;
    private int mDetectFaceScale = 30;
    private int mDetectFaceMaxNum = 5;
    private boolean mDetectVideo;

    public RNFaceDetector(Context context, boolean detectVideo) {
        mContext = context;
        mDetectVideo = detectVideo;
    }

    public RNFaceDetector(Context context) {
        this(context, true);
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    // Public API
    public boolean isOperational() {
        return true;
    }

    public List<Face> detect(byte[] imageData, int width, int height, int format) {
        if (mEngine == null) {
            createEngine();
        }

        List<Face> faces = new ArrayList<>();
        List<FaceInfo> faceInfoList = new ArrayList<>();
        int retCode = mEngine.detectFaces(imageData, width, height, format, faceInfoList);

        if (retCode == ErrorInfo.MOK) {
            for (int i = 0; i < faceInfoList.size(); i++) {
                Face face = new Face();
                face.info = faceInfoList.get(i);
                face.feature = new FaceFeature();
                retCode = mEngine.extractFaceFeature(imageData, width, height, format, face.info, face.feature);

                if (retCode != ErrorInfo.MOK) {
                    face.feature = null;
                }
                faces.add(face);
            }

        }

        return faces;
    }

    public float compare(byte[] feature1, byte[] feature2) {
        if (mEngine == null) {
            createEngine();
        }

        FaceSimilar similar = new FaceSimilar();
        FaceFeature ff1 = new FaceFeature(feature1);
        FaceFeature ff2 = new FaceFeature(feature2);

        int code = mEngine.compareFaceFeature(ff1, ff2, similar);
        if (code == ErrorInfo.MOK) {
            return similar.getScore();
        }

        return 0;
    }

    public void setTracking(boolean trackingEnabled) {
        releaseEngine();
    }

    public void setClassificationType(int classificationType) {
        if (classificationType != mClassificationType) {
            releaseEngine();
            mClassificationType = classificationType;
        }
    }

    public void setLandmarkType(int landmarkType) {
        if (landmarkType != mLandmarkType) {
            releaseEngine();
            mLandmarkType = landmarkType;
        }
    }

    public void setMode(int mode) {

        if (mode != mMode) {
            releaseEngine();
            mMode = mode;
        }
    }

    public void setEngineOptions(String appId, String sdkKey) {
        if (appId != mAppId || sdkKey != mSdkKey) {
            releaseEngine();
            mAppId = appId;
            mSdkKey = sdkKey;
        }
    }

    public void release() {
        releaseEngine();
    }

    // Lifecycle methods
    private void releaseEngine() {
        if (mEngine != null) {
            mEngine.unInit();
            mEngine = null;
        }
    }

    private void createEngine() {
        mEngine = new FaceEngine();
        int retCode;

        retCode = mEngine.activeOnline(mContext, mAppId, mSdkKey);
        if (retCode != ErrorInfo.MOK && retCode != 90114) {
            // throw new ExceptionInInitializerError("createEngine activate failed `" + retCode + "`.");
            Log.d("arcsoft", "createEngine:activate error=" + retCode);
            return;
        }

        retCode = mEngine.init(mContext,
                mDetectVideo ? DetectMode.ASF_DETECT_MODE_VIDEO : DetectMode.ASF_DETECT_MODE_IMAGE,
                DetectFaceOrientPriority.ASF_OP_0_ONLY,
                mDetectFaceScale,
                mDetectFaceMaxNum,
                mMode == FAST_MODE
                        ? FaceEngine.ASF_FACE_DETECT
                        : (FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_FACE_RECOGNITION)
        );
        if (retCode != ErrorInfo.MOK) {
            // throw new ExceptionInInitializerError("createEngine init failed `" + retCode + "`.");
            Log.d("arcsoft", "createEngine:init error=" + retCode);
            return;
        }
    }

}
