package com.minip.scanner.ImageProcessing

import android.R.attr
import android.R.attr.src
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters


class GenerateWarpedImage(filterType : String, img: Mat, W: Float, H : Float, x1 : Float, y1 : Float, x2 : Float, y2 : Float, x3 : Float, y3 : Float, x4 : Float, y4 : Float) {
    var image : Bitmap

    init {

        var startCoords: ArrayList<Point> = ArrayList()
        var resultCoords: ArrayList<Point> = ArrayList()

        Log.d("x1 ", x1.toString())
        Log.d("y1 ", y1.toString())

        Log.d("x4 ", x4.toString())
        Log.d("y4 ", y4.toString())

        Log.d("W", W.toString())
        Log.d("H", H.toString())

        startCoords.add(Point(x1.toDouble(), y1.toDouble()))
        startCoords.add(Point(x2.toDouble(), y2.toDouble()))
        startCoords.add(Point(x3.toDouble(), y3.toDouble()))
        startCoords.add(Point(x4.toDouble(), y4.toDouble()))

        resultCoords.add(Point(0.0, 0.0))
        resultCoords.add(Point((W - 1).toDouble(), 0.0))
        resultCoords.add(Point(0.0, (H - 1).toDouble()))
        resultCoords.add(Point((W - 1).toDouble(), (H - 1).toDouble()))


        val start = Converters.vector_Point2f_to_Mat(startCoords)
        val result = Converters.vector_Point2d_to_Mat(resultCoords)
        start.convertTo(start, CvType.CV_32FC2)
        result.convertTo(result, CvType.CV_32FC2)
        val mat = Mat()
        val perspective = Imgproc.getPerspectiveTransform(start, result)
        Imgproc.warpPerspective(img, mat, perspective, Size(W.toDouble(), H.toDouble()))

        image =  Bitmap.createBitmap(W.toInt(), H.toInt(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, image)
    }

}