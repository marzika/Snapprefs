package jp.co.cyberagent.android.gpuimage.sample.other;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by sam on 14-8-9.
 */
public class GPUImageRiseFilter extends GPUImageThreeTextureFilter {
    private static final String SHADER = "\n precision lowp float;\n\n " +
            "varying highp vec2 textureCoordinate;\n\n " +
            "uniform sampler2D inputImageTexture;\n " +
            "uniform sampler2D inputImageTexture2;\n " +
            "uniform sampler2D inputImageTexture3;\n " +
            "uniform sampler2D inputImageTexture4;\n\n " +
            "void main()\n  " +
            "{\n\n  " +
            "vec4 texel = texture2D(inputImageTexture, textureCoordinate);\n   " +
            "vec3 bbTexel = texture2D(inputImageTexture2, textureCoordinate).rgb;\n\n   " +
            "texel.r = texture2D(inputImageTexture3, vec2(bbTexel.r, texel.r)).r;\n   " +
            "texel.g = texture2D(inputImageTexture3, vec2(bbTexel.g, texel.g)).g;\n   " +
            "texel.b = texture2D(inputImageTexture3, vec2(bbTexel.b, texel.b)).b;\n\n   " +
            "vec4 mapped;\n   mapped.r = texture2D(inputImageTexture4, vec2(texel.r, .16666)).r;\n   " +
            "mapped.g = texture2D(inputImageTexture4, vec2(texel.g, .5)).g;\n   " +
            "mapped.b = texture2D(inputImageTexture4, vec2(texel.b, .83333)).b;\n   " +
            "mapped.a = 1.0;\n\n   " +
            "gl_FragColor = mapped;\n }";

    public GPUImageRiseFilter() {
        super(SHADER);
        setRes();
    }

    private void setRes() {
        Bitmap[] arrayOfBitmap = new Bitmap[3];
        arrayOfBitmap[0] = BitmapFactory.decodeFile(PathUtil.getPath("blackboard"));
        arrayOfBitmap[1] = BitmapFactory.decodeFile(PathUtil.getPath("overlay_map"));
        arrayOfBitmap[2] = BitmapFactory.decodeFile(PathUtil.getPath("rise_map"));
        setBitmap(arrayOfBitmap);
    }
}
