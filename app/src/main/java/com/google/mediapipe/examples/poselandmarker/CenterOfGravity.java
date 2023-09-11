package com.google.mediapipe.examples.poselandmarker;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.HashMap;
import java.util.List;

public class CenterOfGravity {
    private HashMap<String, Float> weightOfBody = new HashMap<>();
    public CenterOfGravity() {
        weightOfBody.put("head", 0.077f);
        weightOfBody.put("trunk", 0.479f);
        weightOfBody.put("upperArm", 0.0265f);
        weightOfBody.put("lowerArm", 0.015f);
        weightOfBody.put("hand", 0.009f);
        weightOfBody.put("thigh", 0.1f);
        weightOfBody.put("shank", 0.0535f);
        weightOfBody.put("foot", 0.019f);
    }

    public NormalizedLandmark getTotalCOG(List<NormalizedLandmark> landmarks) {
        NormalizedLandmark head = landmarks.get(0);
        NormalizedLandmark trunk = calCenterOfGravity(getAVG(List.of(landmarks.get(12), landmarks.get(11))),
                getAVG(List.of(landmarks.get(24), landmarks.get(23))), 0.4486f); // 몸통
        NormalizedLandmark upperArmL = calCenterOfGravity(landmarks.get(11), landmarks.get(13), 0.5772f);
        NormalizedLandmark upperArmR = calCenterOfGravity(landmarks.get(12), landmarks.get(13), 0.5772f);
        NormalizedLandmark lowerArmL = calCenterOfGravity(landmarks.get(13), landmarks.get(15), 0.4574f);
        NormalizedLandmark lowerArmR = calCenterOfGravity(landmarks.get(14), landmarks.get(16), 0.4574f);
        NormalizedLandmark handL = getAVG(List.of(landmarks.get(21), landmarks.get(17), landmarks.get(15)));
        NormalizedLandmark handR = getAVG(List.of(landmarks.get(22), landmarks.get(18), landmarks.get(16)));
        NormalizedLandmark thighL = calCenterOfGravity(landmarks.get(23), landmarks.get(25), 0.4095f);
        NormalizedLandmark thighR = calCenterOfGravity(landmarks.get(24), landmarks.get(26), 0.4095f);
        NormalizedLandmark shankL = calCenterOfGravity(landmarks.get(25), landmarks.get(27), 0.4459f);
        NormalizedLandmark shankR = calCenterOfGravity(landmarks.get(26), landmarks.get(28), 0.4459f);
        NormalizedLandmark footL = getAVG(List.of(landmarks.get(31), landmarks.get(29)));
        NormalizedLandmark footR = getAVG(List.of(landmarks.get(32), landmarks.get(30)));

        Float x = head.x() * weightOfBody.get("head") + trunk.x() * weightOfBody.get("trunk") + upperArmL.x() * weightOfBody.get("upperArm")
                + upperArmR.x() * weightOfBody.get("upperArm") + lowerArmL.x() * weightOfBody.get("lowerArm") + lowerArmR.x() * weightOfBody.get("lowerArm")
                + handL.x() * weightOfBody.get("hand") + handR.x() * weightOfBody.get("hand") + thighL.x() * weightOfBody.get("thigh") + thighR.x() * weightOfBody.get("thigh")
                + shankL.x() * weightOfBody.get("shank") + shankR.x() * weightOfBody.get("shank") + footL.x() * weightOfBody.get("foot") + footR.x() * weightOfBody.get("foot");
        x /= weightOfBody.get("head") + weightOfBody.get("trunk") + weightOfBody.get("upperArm") * 2 + weightOfBody.get("lowerArm") * 2 + weightOfBody.get("hand") * 2
                + weightOfBody.get("thigh") * 2 + weightOfBody.get("shank") * 2 + footL.x() * weightOfBody.get("foot") * 2;


        Float y = head.y() * weightOfBody.get("head") + trunk.y() * weightOfBody.get("trunk") + upperArmL.y() * weightOfBody.get("upperArm")
                + upperArmR.y() * weightOfBody.get("upperArm") + lowerArmL.y() * weightOfBody.get("lowerArm") + lowerArmR.y() * weightOfBody.get("lowerArm")
                + handL.y() * weightOfBody.get("hand") + handR.y() * weightOfBody.get("hand") + thighL.y() * weightOfBody.get("thigh") + thighR.y() * weightOfBody.get("thigh")
                + shankL.y() * weightOfBody.get("shank") + shankR.y() * weightOfBody.get("shank") + footL.y() * weightOfBody.get("foot") + footR.y() * weightOfBody.get("foot");
        y /= weightOfBody.get("head") + weightOfBody.get("trunk") + weightOfBody.get("upperArm") * 2 + weightOfBody.get("lowerArm") * 2 + weightOfBody.get("hand") * 2
                + weightOfBody.get("thigh") * 2 + weightOfBody.get("shank") * 2 + footL.x() * weightOfBody.get("foot") * 2;

        return NormalizedLandmark.create(x, y, 0);
    }

    private NormalizedLandmark calCenterOfGravity(NormalizedLandmark pointD, NormalizedLandmark pointP, Float ratio) {
        Float x = pointP.x() + ratio * (pointD.x() - pointP.x());
        Float y = pointP.y() + ratio * (pointD.y() - pointP.y());
        return NormalizedLandmark.create(x, y, 0);
    }

    private NormalizedLandmark getAVG(List<NormalizedLandmark> list) {
        Float x = 0f;
        Float y = 0f;
        int n = list.size();

        for(NormalizedLandmark point: list) {
            x += point.x();
            y += point.y();
        }
        return NormalizedLandmark.create( x / n, y / n, 0);
    }
}
