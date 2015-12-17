package com.rnfstudio.babytracker.utility;

/**
 * Created by Roger on 2015/7/10.
 */
public class DirectionUtils {
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_DOWN = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;

    public static int getDirection(int x1, int y1, int x2, int y2) {
        int diffX = x2 - x1;
        int diffY = y1 - y2; // flip y-axis, because we have the origin at top-left corner
        float angle = getAngle(diffX, diffY);

        if (angle >= 45 && angle < 135) {
            return DIRECTION_UP;
        } else if (angle >= 135 && angle < 225) {
            return DIRECTION_LEFT;
        } else if (angle >= 225 && angle < 315) {
            return DIRECTION_DOWN;
        } else {
            return DIRECTION_RIGHT;
        }
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static float getAngle(float x1, float y1, float x2, float y2) {
        return getAngle(x1 - x2, y1 - y2);
    }

    // refer to: http://stackoverflow.com/questions/9970281/java-calculating-the-angle-between-two-points-in-degrees
    public static float getAngle(float diffX, float diffY) {
        float angle = (float) Math.toDegrees(Math.atan2(diffY, diffX));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }
}
