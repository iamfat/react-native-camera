package org.reactnative.camera.tasks;

import com.arcsoft.face.FaceEngine;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.cameraview.CameraView;

import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import org.reactnative.facedetector.RNFaceDetector;

import java.util.List;

public class FaceDetectorAsyncTask extends android.os.AsyncTask<Void, Void, List<RNFaceDetector.Face>> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNFaceDetector mFaceDetector;
  private FaceDetectorAsyncTaskDelegate mDelegate;
  private ImageDimensions mImageDimensions;
  private double mScaleX;
  private double mScaleY;
  private int mPaddingLeft;
  private int mPaddingTop;

  public FaceDetectorAsyncTask(
      FaceDetectorAsyncTaskDelegate delegate,
      RNFaceDetector faceDetector,
      byte[] imageData,
      int width,
      int height,
      int rotation,
      float density,
      int facing,
      int viewWidth,
      int viewHeight,
      int viewPaddingLeft,
      int viewPaddingTop
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mFaceDetector = faceDetector;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = (double) (viewHeight) / (mImageDimensions.getHeight() * density);
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  @Override
  protected List<RNFaceDetector.Face> doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mFaceDetector == null || !mFaceDetector.isOperational()) {
      return null;
    }
    return mFaceDetector.detect(mImageData, mWidth, mHeight, FaceEngine.CP_PAF_NV21);
  }

  @Override
  protected void onPostExecute(List<RNFaceDetector.Face> faces) {
    super.onPostExecute(faces);

    if (faces == null) {
      mDelegate.onFaceDetectionError(mFaceDetector);
    } else {
      if (faces.size() > 0) {
        mDelegate.onFacesDetected(serializeEventData(faces));
      }
      mDelegate.onFaceDetectingTaskCompleted();
    }
  }

  private WritableArray serializeEventData(List<RNFaceDetector.Face> faces) {
    WritableArray facesList = Arguments.createArray();

    for(int i = 0; i < faces.size(); i++) {
      RNFaceDetector.Face face = faces.get(i);
      WritableMap serializedFace = FaceDetectorUtils.serializeFace(face);
      facesList.pushMap(serializedFace);
    }

    return facesList;
  }
}
