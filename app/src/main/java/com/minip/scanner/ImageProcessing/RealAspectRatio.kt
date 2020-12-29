package com.minip.scanner.ImageProcessing

import android.util.Log
import org.opencv.core.Core
import org.opencv.core.CvType.CV_32FC1
import org.opencv.core.Mat
import org.opencv.core.Scalar
import java.nio.ByteBuffer


class CalculateAspectRatio(x1 : Float, y1 : Float, x2 : Float, y2 : Float, x3 : Float, y3 : Float, x4 : Float, y4 : Float , width : Int, height : Int ) {

    var calculatedAspectRatio : Float
    val realHeight : Float
    val realWidth : Float
    val W : Float
    val H : Float
    init {
//
//        var u0 = width/2.0
//        var v0 = height/2.0
//
        var w1 = Math.sqrt(Math.pow( (x1 - x2).toDouble() , 2.0)  + Math.pow( (y1 - y2).toDouble() , 2.0)    ).toFloat()
        var w2 = Math.sqrt(Math.pow( (x3 - x4).toDouble() , 2.0)  + Math.pow( (y3 - y4).toDouble() , 2.0)    ).toFloat()
        realWidth = Math.max(w1, w2)

        var h1 = Math.sqrt(Math.pow( (x1 - x3).toDouble() , 2.0)  + Math.pow( (y1 - y3).toDouble() , 2.0)    ).toFloat()
        var h2 = Math.sqrt(Math.pow( (x2 - x4).toDouble() , 2.0)  + Math.pow( (y2 - y4).toDouble() , 2.0)    ).toFloat()
        realHeight = Math.max(h1, h2)

        var realAspectRatio = realWidth / realHeight
//
//        var m1 = Mat(1,3, CV_32FC1, ByteBuffer.wrap(byteArrayOf(x1.toByte(), y1.toByte(), 1.toByte())))
//        var m2 = Mat(1,3, CV_32FC1, ByteBuffer.wrap(byteArrayOf(x2.toByte(), y2.toByte(), 1.toByte())))
//        var m3 = Mat(1,3, CV_32FC1, ByteBuffer.wrap(byteArrayOf(x3.toByte(), y3.toByte(), 1.toByte())))
//        var m4 = Mat(1,3, CV_32FC1, ByteBuffer.wrap(byteArrayOf(x4.toByte(), y4.toByte(), 1.toByte())))
//
//        var k2 = m1.cross(m4).dot(m3) / m2.cross(m4).dot(m3)
//        var k3 = m1.cross(m4).dot(m2) / m3.cross(m4).dot(m2)
//
//
//        var n2 = Mat()
//
//        Core.multiply(m2,Scalar(k2) , n2)
//
//        Core.subtract(n2, m1, n2)
//
//        var n3 = Mat()
//
//        Core.multiply(m3,Scalar(k3) , n2)
//
//        Core.subtract(n3, m1, n3)
//
//        var n21 = n2.get(0,0).get(0)
//        var n22 = n2.get(0,1).get(0)
//        var n23 = n2.get(0,2).get(0)
//
//        var n31 = n3.get(0,0).get(0)
//        var n32 = n3.get(0,1).get(0)
//        var n33 = n3.get(0,2).get(0)
//
//        var f = (1f /n23 * n33) * ( (n21 * n31 - (n21*n33+n23*n31)*u0+n23*n33*u0*u0) + (n22*n32-(n22*n33+n23*n32)*v0+n23*n33*v0*v0) )
//        f = Math.sqrt(f)
//
//        var A = Mat(3,3, CV_32FC1, ByteBuffer.wrap(byteArrayOf(f.toByte(),0,u0.toByte(),0,f.toByte(),v0.toByte(),0,0,1)))

        val u0 = width / 2.toDouble()
        val v0 = width / 2.toDouble()
        val m1x: Double = x1 - u0
        val m1y: Double = y1 - v0
        val m2x: Double = x2 - u0
        val m2y: Double = y2 - v0
        val m3x: Double = x3 - u0
        val m3y: Double = y3 - v0
        val m4x: Double = x4 - u0
        val m4y: Double = y4 - v0
        val k2 = ((m1y - m4y) * m3x - (m1x - m4x) * m3y + m1x * m4y - m1y * m4x) /
                ((m2y - m4y) * m3x - (m2x - m4x) * m3y + m2x * m4y - m2y * m4x)
        val k3 = ((m1y - m4y) * m2x - (m1x - m4x) * m2y + m1x * m4y - m1y * m4x) /
                ((m3y - m4y) * m2x - (m3x - m4x) * m2y + m3x * m4y - m3y * m4x)
        val f_squared = -((k3 * m3y - m1y) * (k2 * m2y - m1y) + (k3 * m3x - m1x) * (k2 * m2x - m1x)) /
                ((k3 - 1) * (k2 - 1))
        var whRatio = (
                (Math.pow(k2 - 1, 2.0) + Math.pow(k2 * m2y - m1y, 2.0) / f_squared + Math.pow(k2 * m2x - m1x, 2.0) / f_squared) /
                        (Math.pow(k3 - 1, 2.0) + Math.pow(k3 * m3y - m1y, 2.0) / f_squared + Math.pow(k3 * m3x - m1x, 2.0) / f_squared)
        )

        Log.d("whRatio", whRatio.toString());
        whRatio = Math.sqrt(Math.abs(whRatio))
        if (k2 == 1.0 && k3 == 1.0) {
            whRatio = Math.sqrt(
                    (Math.pow(m2y - m1y, 2.0) + Math.pow(m2x - m1x, 2.0)) /
                            (Math.pow(m3y - m1y, 2.0) + Math.pow(m3x - m1x, 2.0)))
        }
        calculatedAspectRatio = whRatio.toFloat()

        if(calculatedAspectRatio.isNaN() || calculatedAspectRatio.isInfinite()) {
            calculatedAspectRatio = realAspectRatio
        }
        if (calculatedAspectRatio < realAspectRatio) {
            W = realWidth
            H = W / calculatedAspectRatio
        }else {
            H = realHeight
            W = calculatedAspectRatio * H
        }

    }





}