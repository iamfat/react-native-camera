package org.reactnative.facedetector.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceSimilar;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.reactnative.facedetector.RNFaceDetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Base64;

public class FaceIdentificationAsyncTask extends AsyncTask<Void, Void, Map<String, Double>> {
  private byte[] mFeature;
  private Map<String,Object> mfaceCandidates;

  private Promise mPromise;
  private float mthreshold;
  private Context mContext;
  private RNFaceDetector mRNFaceDetector;

  public FaceIdentificationAsyncTask(Context context, String feature, ReadableMap candidates,float threshold, Promise promise) {
    mPromise = promise;
    mFeature = Base64.decode(feature,Base64.NO_WRAP );
    mfaceCandidates = candidates.toHashMap();
    mContext = context;
    mthreshold = threshold;
  }

  @Override
  protected void onPreExecute() {
    if (mFeature == null) {
      return;
    }

    if (mfaceCandidates == null){
      return;
    }

  }

  @Override
  protected Map<String, Double> doInBackground(Void... voids) {
    HashMap<String,Double> hits = new HashMap<String, Double>();
    mRNFaceDetector = detectorForIdentify(mContext);
    for (Map.Entry<String, Object> entry : mfaceCandidates.entrySet()){
      float code = mRNFaceDetector.compare(mFeature,Base64.decode(String.valueOf(entry.getValue()),Base64.NO_WRAP));
      if(code != 0 && code >= mthreshold){
        String faceID = entry.getKey();
        hits.put(faceID,Double.valueOf(code));
      }
    }
    return hits;
  }

  @Override
  protected void onPostExecute(Map<String, Double> similarities) {
    WritableMap result = Arguments.createMap();
    for (Map.Entry<String, Double> entry : similarities.entrySet()){
      result.putDouble(entry.getKey(),entry.getValue());
    }
    mPromise.resolve(result);
  }

  private static RNFaceDetector detectorForIdentify( Context context) {
    RNFaceDetector detector = new RNFaceDetector(context, false);
    detector.setTracking(false);
    detector.setMode(1);
    return detector;
  }

}
