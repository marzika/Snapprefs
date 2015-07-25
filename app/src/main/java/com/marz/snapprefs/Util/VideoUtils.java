package com.marz.snapprefs.Util;

/**
 * VideoUtils.java created on 2014-09-01.
 * <p/>
 * Copyright (C) 2014 P1nGu1n
 * <p/>
 * This file is part of Snapshare.
 * <p/>
 * Snapshare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Snapshare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * a gazillion times. If not, see <http://www.gnu.org/licenses/>.
 */

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Matrix;
import com.marz.snapprefs.Common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.List;

/**
 * A set of commonly used utilities for altering videos.
 */
public class VideoUtils {

    /**
     * Restrict instantiation of this class, it only contains static methods.
     */
    private VideoUtils() {
    }

    /**
     * Reads the rotation flag from the video track, rotates it and writes it to a temporary file.
     *
     * @param videoFile The video to be rotated
     * @param tempFile  The file to be written to
     * @throws IOException File could not be found or read
     */
    public static void rotateVideo(File videoFile, File tempFile) throws IOException {
        DataSource dataSource = new FileDataSourceImpl(videoFile);
        IsoFile isoFile = new IsoFile(dataSource);

        boolean videoTrackFound = false;
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        // Iterate through all tracks until the track is a video track (type equals 'vide')
        for (TrackBox trackBox : trackBoxes) {
            if (trackBox.getMediaBox().getHandlerBox().getHandlerType().equals("vide")) {
                videoTrackFound = true;
                TrackHeaderBox trackHeaderBox = trackBox.getTrackHeaderBox();

                // Get the dimensions of the video
                double width = trackHeaderBox.getWidth();
                double height = trackHeaderBox.getHeight();
                DecimalFormat format = new DecimalFormat("#");
                XposedUtils.log("Video resolution: " + format.format(width) + " x " + format.format(height) + " (w x h)");

                Matrix currentMatrix = trackHeaderBox.getMatrix();
                int rotation = getDegreesFromMatrix(currentMatrix);

                // Determine the way to rotate
                if (((rotation % 180 == 90) && (width < height)) || ((rotation % 180 == 0) && (width > height))) {
                    // Add rotation according to the settings
                    rotation += Common.ROTATION_MODE;
                    Matrix matrix = getMatrixFromDegrees(rotation);
                    XposedUtils.log("Rotation changed from " + getStringFromMatrix(currentMatrix) + " to " + getStringFromMatrix(matrix));

                    // Set the rotation matrix
                    trackHeaderBox.setMatrix(matrix);
                    // Write the video to the temp file
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    FileChannel fc = fos.getChannel();
                    isoFile.writeContainer(fc);
                    fc.close();
                    fos.close();
                } else { // No need to rotate if the rotation hasn't changed
                    XposedUtils.log("Keeping rotation at " + getStringFromMatrix(currentMatrix) + ", just creating a copy");
                    CommonUtils.copyFile(videoFile, tempFile);
                }
                break;
            }
        }

        // No video track with the type 'vide' found
        if (!videoTrackFound) {
            XposedUtils.log("No video track found, just creating a copy");
            CommonUtils.copyFile(videoFile, tempFile);
        }
    }

    /**
     * Get the number of degrees of a matrix.
     *
     * @param matrix The matrix used as source
     * @return The number of degrees
     */
    private static int getDegreesFromMatrix(Matrix matrix) {
        if (matrix.equals(Matrix.ROTATE_90)) {
            return 90;
        } else if (matrix.equals(Matrix.ROTATE_180)) {
            return 180;
        } else if (matrix.equals(Matrix.ROTATE_270)) {
            return 270;
        } else {
            return 0;
        }
    }

    /**
     * Get the matrix from a number of degrees
     *
     * @param degrees The degrees used as source
     * @return The matrix from the number of degrees
     */
    private static Matrix getMatrixFromDegrees(int degrees) {
        degrees %= 360;
        if (degrees == 90) {
            return Matrix.ROTATE_90;
        } else if (degrees == 180) {
            return Matrix.ROTATE_180;
        } else if (degrees == 270) {
            return Matrix.ROTATE_270;
        } else {
            return Matrix.ROTATE_0;
        }
    }

    /**
     * Get the string representation of a matrix.
     *
     * @param matrix The matrix used as source
     * @return The string formatted as [degrees]°
     */
    private static String getStringFromMatrix(Matrix matrix) {
        if (matrix.equals(Matrix.ROTATE_0)) {
            return "0°";
        } else if (matrix.equals(Matrix.ROTATE_90)) {
            return "90°";
        } else if (matrix.equals(Matrix.ROTATE_180)) {
            return "180°";
        } else if (matrix.equals(Matrix.ROTATE_270)) {
            return "270°";
        } else {
            return "(unknown)°";
        }
    }
}
