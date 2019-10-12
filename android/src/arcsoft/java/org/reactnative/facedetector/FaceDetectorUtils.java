package org.reactnative.facedetector;

import android.graphics.Rect;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;

public class FaceDetectorUtils {

    public static WritableMap serializeFace(RNFaceDetector.Face face) {
        WritableMap encodedFace = Arguments.createMap();

        Rect rect = face.info.getRect();

        WritableMap origin = Arguments.createMap();
        origin.putDouble("x", rect.left);
        origin.putDouble("y", rect.top);

        WritableMap size = Arguments.createMap();
        size.putDouble("width", rect.right - rect.top + 1);
        size.putDouble("height", rect.bottom - rect.top + 1);

        WritableMap bounds = Arguments.createMap();
        bounds.putMap("origin", origin);
        bounds.putMap("size", size);

        encodedFace.putMap("bounds", bounds);

        if (face.feature != null) {
            byte[] featureData = face.feature.getFeatureData();
            encodedFace.putString("feature", Base64.encodeToString(featureData, Base64.DEFAULT));
        }

        return encodedFace;
    }

    public static WritableMap positionTranslatedHorizontally(ReadableMap position, double translateX) {
        WritableMap newPosition = Arguments.createMap();
        newPosition.merge(position);
        newPosition.putDouble("x", position.getDouble("x") + translateX);
        return newPosition;
    }

    public static WritableMap positionMirroredHorizontally(ReadableMap position, int containerWidth, double scaleX) {
        WritableMap newPosition = Arguments.createMap();
        newPosition.merge(position);
        newPosition.putDouble("x", valueMirroredHorizontally(position.getDouble("x"), containerWidth, scaleX));
        return newPosition;
    }

    public static double valueMirroredHorizontally(double elementX, int containerWidth, double scaleX) {
        double originalX = elementX / scaleX;
        double mirroredX = containerWidth - originalX;
        return mirroredX * scaleX;
    }
}
