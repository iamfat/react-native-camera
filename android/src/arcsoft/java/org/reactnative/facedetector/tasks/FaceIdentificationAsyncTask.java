package org.reactnative.facedetector.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.reactnative.facedetector.RNFaceDetector;

import java.util.Map;

public class FaceIdentificationAsyncTask extends AsyncTask<Void, Void, WritableMap> {
    private byte[] mTarget;
    private Map<String, Object> mCandidates;

    private Promise mPromise;
    private float mThreshold;
    private Context mContext;

    public FaceIdentificationAsyncTask(Context context, String target, ReadableMap candidates, float threshold, Promise promise) {
        mPromise = promise;
        mTarget = Base64.decode(target, Base64.NO_WRAP);
        mCandidates = candidates.toHashMap();
        mContext = context;
        mThreshold = threshold;
    }

    @Override
    protected void onPreExecute() {
        // DO NOTHING
    }

    @Override
    protected WritableMap doInBackground(Void... voids) {
        WritableMap hits = Arguments.createMap();
        RNFaceDetector detector = detectorForIdentify(mContext);
        for (Map.Entry<String, Object> entry : mCandidates.entrySet()) {
            String faceId = entry.getKey();
            byte[] candidate = Base64.decode(entry.getValue().toString(), Base64.NO_WRAP);
            float similarity = detector.compare(mTarget, candidate);
            if (similarity >= mThreshold) {
                hits.putDouble(faceId, (double) similarity);
            }
        }
        return hits;
    }

    @Override
    protected void onPostExecute(WritableMap hits) {
        mPromise.resolve(hits);
    }

    private static RNFaceDetector detectorForIdentify(Context context) {
        RNFaceDetector detector = new RNFaceDetector(context, false);
        detector.setTracking(false);
        detector.setMode(1);
        return detector;
    }

}
