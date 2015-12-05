package jp.co.cyberagent.android.gpuimage.sample.other;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by sam on 14-8-9.
 */
public class GPUImageLomofiFilter extends GPUImageTwoTextureFilter {
    private static final String SHADER = " precision lowp float;\n\n " +
            "varying highp vec2 textureCoordinate;\n\n " +
            "uniform sampler2D inputImageTexture;\n " +
            "uniform sampler2D inputImageTexture2;\n " +
            "uniform sampler2D inputImageTexture3;\n\n " +
            "void main()\n  " +
            "{\n\n  " +
            "vec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;\n\n   " +
            "vec2 tc = (2.0 * textureCoordinate) - 1.0;\n   " +
            "float d = dot(tc, tc);\n   " +
            "vec2 lookup = vec2(d, texel.r);\n   " +
            "texel.r = texture2D(inputImageTexture3, lookup).r;\n   " +
            "lookup.y = texel.g;\n   " +
            "texel.g = texture2D(inputImageTexture3, lookup).g;\n   " +
            "lookup.y = texel.b;\n   " +
            "texel.b = texture2D(inputImageTexture3, lookup).b;\n\n   " +
            "vec2 red = vec2(texel.r, 0.16666);\n   " +
            "vec2 green = vec2(texel.g, 0.5);\n   " +
            "vec2 blue = vec2(texel.b, .83333);\n   " +
            "texel.r = texture2D(inputImageTexture2, red).r;\n   " +
            "texel.g = texture2D(inputImageTexture2, green).g;\n   " +
            "texel.b = texture2D(inputImageTexture2, blue).b;\n\n   " +
            "gl_FragColor = vec4(texel, 1.0);\n\n " +
            "}";

    public GPUImageLomofiFilter() {
        super(SHADER);
        setRes();
    }

    private void setRes() {
        Bitmap[] arrayOfBitmap = new Bitmap[2];
        arrayOfBitmap[0] = BitmapFactory.decodeFile(PathUtil.getPath("nmap"));
        arrayOfBitmap[1] = BitmapFactory.decodeFile(PathUtil.getPath("nblowout"));
        setBitmap(arrayOfBitmap);
    }
}