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
  private String mFeature;
  private ReadableMap faceCandidates;

  private Promise mPromise;
  private Context mContext;
  private RNFaceDetector mRNFaceDetector;

  public FaceIdentificationAsyncTask(Context context, String feature, ReadableMap candidates, Promise promise) {
    mPromise = promise;
    mFeature = feature;
    faceCandidates = candidates;
    mContext = context;
  }

  @Override
  protected void onPreExecute() {
    if (mFeature == null) {
      return;
    }

    if (faceCandidates == null){
      return;
    }

  }

  @Override
  protected Map<String, Double> doInBackground(Void... voids) {
    FaceSimilar faceSimilar = new FaceSimilar();
    float similar = 0.8f;
    HashMap<String,Double> similarities = new HashMap<String, Double>();
    mRNFaceDetector = detectorForIdentify(mContext);
    Map<String,Object> map = faceCandidates.toHashMap();
    for (Map.Entry<String, Object> entry : map.entrySet()){
      byte[] faceFeature1 = Base64.decode(mFeature,Base64.NO_WRAP );
      byte[] faceFeature2 = Base64.decode(String.valueOf(entry.getValue()),Base64.NO_WRAP);
      FaceFeature faceFeatureObject1 = new FaceFeature(faceFeature1);
      FaceFeature faceFeatureObject2 = new FaceFeature(faceFeature2);

      float code = mRNFaceDetector.compare(faceFeatureObject1,faceFeatureObject2,faceSimilar);
      if(code != 0 && code >= similar){
        String faceID = entry.getKey();
        Double doubleSimilarity=Double.valueOf(String.valueOf(code));
        similarities.put(faceID,doubleSimilarity);
      }
    }
    return similarities;
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
