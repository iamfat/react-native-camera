package org.reactnative.facedetector;

import android.content.Context;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;

import java.util.ArrayList;
import java.util.List;

public class RNFaceDetector {

    public class Face {
        public FaceInfo info;
        public FaceFeature feature;
        public LivenessInfo liveness;
    }

    public static int ALL_CLASSIFICATIONS = 1;
    public static int NO_CLASSIFICATIONS = 0;
    public static int ALL_LANDMARKS = 1;
    public static int NO_LANDMARKS = 0;
    public static int ACCURATE_MODE = 1;
    public static int FAST_MODE = 0;

    public static String mAppId = "";
    public static String mSdkKey = "";
    public static String mActiveKey = "";

    private static FaceEngine mEngine = null;
    private static boolean mEngineInited = false;
    private static Context mContext = null;
    private static int mDetectFaceScale = 30;
    private static int mDetectFaceMaxNum = 5;

    private int mClassificationType = NO_CLASSIFICATIONS;
    private int mLandmarkType = NO_LANDMARKS;
    private int mMode = FAST_MODE;
    private boolean mDetectVideo;

    public RNFaceDetector(Context context, boolean detectVideo) {
        // mContext = context;
        // mDetectVideo = detectVideo;
    }

    public RNFaceDetector(Context context) {
        this(context, true);
    }

    // Public API
    public boolean isOperational() {
        return true;
    }

    public List<Face> detect(byte[] imageData, int width, int height, int format) {
        List<Face> faces = new ArrayList<>();

        if (mEngine == null || !mEngineInited)
            return faces;
        synchronized (mEngine) {
            List<FaceInfo> faceInfoList = new ArrayList<>();
            int retCode = mEngine.detectFaces(imageData, width, height, format, faceInfoList);
            if (retCode == ErrorInfo.MOK && faceInfoList.size() > 0) {
                retCode = mEngine.process(imageData, width, height, format, faceInfoList, FaceEngine.ASF_LIVENESS);
                if (retCode == ErrorInfo.MOK) {
                    List<LivenessInfo> livenessInfoList = new ArrayList<>();
                    int livenessCode = mEngine.getLiveness(livenessInfoList);
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        Face face = new Face();
                        face.info = faceInfoList.get(i);
                        face.feature = new FaceFeature();
                        retCode = mEngine.extractFaceFeature(imageData, width, height, format, face.info, face.feature);
                        if (retCode != ErrorInfo.MOK)
                            continue;
                        if (livenessCode == ErrorInfo.MOK && livenessInfoList.size() > i) {
                            face.liveness = livenessInfoList.get(i);
                        } else {
                            face.liveness = null;
                        }
                        faces.add(face);
                    }
                }
            }
            Log.i("arcsoft", "detect: found=" + faceInfoList.size() + " extracted=" + faces.size());
        }

        return faces;
    }

    public float compare(byte[] feature1, byte[] feature2) {
        float score = 0.0f;

        if (mEngine == null || !mEngineInited)
            return score;
        synchronized (mEngine) {
            FaceSimilar similar = new FaceSimilar();
            FaceFeature ff1 = new FaceFeature(feature1);
            FaceFeature ff2 = new FaceFeature(feature2);
            int code = mEngine.compareFaceFeature(ff1, ff2, similar);
            if (code == ErrorInfo.MOK) {
                score = similar.getScore();
            }
        }

        return score;
    }

    public void setTracking(boolean trackingEnabled) {
        // releaseEngine();
    }

    public void setClassificationType(int classificationType) {
        // if (classificationType != mClassificationType) {
        // releaseEngine();
        // mClassificationType = classificationType;
        // }
    }

    public void setLandmarkType(int landmarkType) {
        // if (landmarkType != mLandmarkType) {
        // releaseEngine();
        // mLandmarkType = landmarkType;
        // }
    }

    public void setMode(int mode) {
        // if (mode != mMode) {
        // mMode = mode;
        // reloadEngine();
        // }
    }

    public static boolean init(Context context, String appId, String sdkKey, String activeKey) {
        mContext = context;
        if (mEngine == null) {
            mEngine = new FaceEngine();
        }
        boolean ret = false;
        if (appId != mAppId || sdkKey != mSdkKey || activeKey != mActiveKey) {
            mAppId = appId;
            mSdkKey = sdkKey;
            mActiveKey = activeKey;
            ret = reloadEngine();
        }
        return ret;
    }

    // Lifecycle methods
    public void release() {
        // DO NOTHING
    }

    public static void releaseEngine() {
        synchronized (mEngine) {
            if (mEngineInited)
                mEngine.unInit();
            mEngineInited = false;
        }
    }

    private static boolean reloadEngine() {
        boolean ret = false;
        synchronized (mEngine) {
            if (mEngineInited)
                mEngine.unInit();
            mEngineInited = false;
            ret = initEngine();
        }
        return ret;
    }

    private static boolean initEngine() {
        if (mActiveKey == null || mActiveKey == ""
                || (!mActiveKey.startsWith("/") && (mAppId == "" || mSdkKey == ""))) {
            return false;
        }

        int retCode;
        if (mActiveKey.startsWith("/")) {
            retCode = mEngine.activeOffline(mContext, mActiveKey);
        } else {
            retCode = mEngine.activeOnline(mContext, mActiveKey, mAppId, mSdkKey);
        }
        if (retCode != ErrorInfo.MOK && retCode != 90114) {
            // throw new ExceptionInInitializerError("createEngine activate failed `" +
            // retCode + "`.");
            Log.e("arcsoft", "createEngine:activate error=" + retCode);
            return false;
        }
        Log.i("arcsoft", "createEngine:activated");

        final int userRotation = Settings.System.getIntForUser(resolver,
                Settings.System.USER_ROTATION, Surface.ROTATION_0,
                UserHandle.USER_CURRENT);

        retCode = mEngine.init(mContext,
                DetectMode.ASF_DETECT_MODE_VIDEO,
                DetectFaceOrientPriority.ASF_OP_0_ONLY,
                mDetectFaceScale,
                mDetectFaceMaxNum,
                FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_LIVENESS);
        if (retCode != ErrorInfo.MOK) {
            // throw new ExceptionInInitializerError("createEngine init failed `" + retCode
            // + "`.");
            Log.e("arcsoft", "createEngine:init error=" + retCode);
            return false;
        }
        Log.i("arcsoft", "createEngine:initialized");
        mEngineInited = true;
        return true;
    }

}
