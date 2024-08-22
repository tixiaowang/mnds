package com.uu.txw.auto.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import com.uu.txw.auto.common.utils.Logger;

public class ImageUtils {

    public static final int TF_OD_API_INPUT_SIZE = 200;


    private static Bitmap getScaleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) TF_OD_API_INPUT_SIZE) / width;
        float scaleHeight = ((float) TF_OD_API_INPUT_SIZE) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }


    public synchronized static Rect imageCompareByPix(int[] bound, Bitmap largeBitmap, Bitmap smallBitmap) {
        if (largeBitmap == null || smallBitmap == null) {
            if (largeBitmap != null) {
                largeBitmap.recycle();
            }
            if (smallBitmap != null) {
                smallBitmap.recycle();
            }
            return null;
        }

        long start = System.currentTimeMillis();
        Rect rect = imageCompareByPix(bound, largeBitmap, smallBitmap, 10, 1, 1);
        if (rect == null) {
            rect = imageCompareByPix(bound, largeBitmap, smallBitmap, 20, 1, 1);
        }
        if (rect == null) {
            rect = imageCompareByPix(bound, largeBitmap, smallBitmap, 50, 1, 1);
        }
        if (rect == null) {
            rect = imageCompareByPix(bound, largeBitmap, smallBitmap, 100, 1, 1);
        }
        largeBitmap.recycle();
        smallBitmap.recycle();
        if (rect != null) {
            Logger.v("getPixLocation: " + rect.centerX() + "--" + rect.centerY() + " time:" + (System.currentTimeMillis() - start));
        } else {
            Logger.v("getPixLocation fail");
        }
        return rect;
    }

    /**
     * @param largeBitmap 截屏图
     * @param smallBitmap 截取小图
     *                    //     * @param times       比较像素点个数(范围10-30左右,值越大越精确速度越慢,正常可取值10,)
     * @param threshold   大图与小图比较像素的差值(范围5-50左右,值越大越容易比到,越容易出错,在半透明背景图片不太一样的时候可以取值50,正常比较取值5即可)
     * @param scale       图像截屏倍数(此参数必须与取小图时的缩放倍数一致)
     * @param type        图像算法类型
     * @return 返回图像左上角坐标 (smallBitmap 已自动recycle ，largeBitmap需要手动recycle)
     * 注意,此算法比较的是小图中的竖轴及横向第一行像素点
     */
    public synchronized static Rect imageCompareByPix(int[] bound, Bitmap largeBitmap, Bitmap smallBitmap, int threshold, int scale, int type) {
        long start = System.currentTimeMillis();
        int bw = largeBitmap.getWidth();
        int b1w = smallBitmap.getWidth();
        int bh = largeBitmap.getHeight();
        int b1h = smallBitmap.getHeight();
        int[] pixs1 = new int[b1h * b1w];
        int[] pixs = new int[bh * bw];
        //防止越界,如果times超过图片宽高,则赋值为宽高-2
        int times = Math.min(b1w, b1h);//匹配最大面积
        if (times >= b1h) {
            times = b1h - 2;
        }
        if (times >= b1w) {
            times = b1w - 2;
        }
        Logger.v("比对像素行 " + times + " threshold " + threshold);
        smallBitmap.getPixels(pixs1, 0, b1w, 0, 0, b1w, b1h);
        largeBitmap.getPixels(pixs, 0, bw, 0, 0, bw, bh);
        for (int i = 0; i < pixs.length - b1h * bw; i++) {//所有像素循环
            if (bound != null && bound.length == 4) {
                int rowOffset = i % bw;
                int colOffset = i / bw;
                if (rowOffset < bound[0] || colOffset < bound[1] || (bound[2] !=0 && rowOffset > bound[2]) || (bound[3] != 0 && colOffset > bound[3])) {
                    continue;
                }
            }
            int score = 0;
            int errorScore = 0;
            for (int j = 0; j < times; j++) {//行
                int h = i + bw * j + b1w / 2;
                int h1 = b1w * j + b1w / 2;
                boolean isSame = false;
                switch (type) {
                    case 0://竖轴+横向第一行,适用于复杂图形,不适用中轴空白或第一行空白的比图,但速度较快,定位矩形稍有偏移
                        if (isSame(pixs[h], pixs1[h1], threshold) && isSame(pixs[i + j], pixs1[0 + j], threshold)) {
                            isSame = true;
                        }
                        break;
                    case 1://竖轴+竖轴左右两边的斜线:适用于中心复杂图形,也适用于文字,耗时较多,定位矩形无偏移,耗时为0算法的1.3倍
                        /*
                        .................
                        .      ...      .
                        .     . . .     .
                        .    .  .  .    .
                        .   .   .    .  .
                        .................

                        * */
                        if (isSame(pixs[h], pixs1[h1], threshold) && isSame(pixs[h + j / 2], pixs1[h1 + j / 2], threshold) && isSame(pixs[h - j / 2], pixs1[h1 - j / 2], threshold)) {//竖轴相似
                            isSame = true;
                        }
                        break;
                    case 2://在1算法的基础上加上了0算法中最第一行像素的比对,有点多余,不建议适用,耗时是算法0的1.5倍
                        if (isSame(pixs[h], pixs1[h1], threshold) && isSame(pixs[h + j / 2], pixs1[h1 + j / 2], threshold) && isSame(pixs[h - j / 2], pixs1[h1 - j / 2], threshold) && isSame(pixs[i + j], pixs1[0 + j], threshold)) {//竖轴相似
                            isSame = true;
                        }
                        break;
                    case 3:

                        break;
                }
                if (isSame) {//竖轴相似
                    score++;
                } else if (errorScore < times / 10) {
                    errorScore++;
                } else {
                    break;
                }
            }
            if (score > 2 * times / 3) {
                Logger.v("相似 score " + score + " times " + times + " threshold " + threshold);
            }
            if (score >= 9 * times / 10) {
                Logger.v("success threshold " + threshold);
                Rect rect = new Rect();
                rect.left = (i % bw) * scale;
                rect.top = (i / bw) * scale;
                rect.bottom = (i / bw) * scale + b1h;
                rect.right = (i % bw) * scale + b1w;
                return rect;
            }
        }
        return null;
    }


    public static boolean isSame(int a, int b, int threshold) {
        int g = Color.green(a) - Color.green(b);
        if (Math.abs(g) > threshold) {
            return false;
        }
        int bl = Color.blue(a) - Color.blue(b);
        if (Math.abs(bl) > threshold) {
            return false;
        }
        int r = Color.red(a) - Color.red(b);
        return Math.abs(r) <= threshold;
    }


    public static int[][] getHDpixUsetemp(Bitmap bitmap, Bitmap bitmap1, int offset, int top, int bottom) {
        long start = System.currentTimeMillis();
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        int[] pixs1 = new int[(bottom - top)];
        int[] pixs = new int[(bottom - top)];
        int[][] results = new int[2][2];
        int score = 0;
        int badscore = 0;
        for (int i = offset; i < bw; i++) {
            bitmap.getPixels(pixs, 0, 1, i, top, 1, bottom - top);
            bitmap1.getPixels(pixs1, 0, 1, i, top, 1, bottom - top);
            for (int j = 0; j < bottom - top; j++) {
                //第一行前15个像素大多数都不同,则不是一张图,返回null
                if (i == offset && j < 15 && !canResult(pixs[j], pixs1[j])) {
                    badscore++;
                }
                if (badscore > 7) {
                    return null;
                }
                if (!canResult(pixs[j], pixs1[j])) {
                    if (results[0][0] == 0) {
                        results[0][0] = i;
                        results[0][1] = j;
                        int[] a = new int[2];
                        a[0] = results[0][0];
                        a[1] = results[0][1];
                    } else if (results[0][1] + 150 < j || results[0][0] + 150 < i) {
                        if (score > 20) {
                            results[1][0] = i;
                            results[1][1] = j;
                            Log.e("--找到图片-->", "[" + results[0][0] + "]" + results[0][1] + "-" + results[1][0] + "-" + results[1][1] + "耗时:" + (System.currentTimeMillis() - start));
                            break;
                        } else {
                            score++;
                        }

                    }
                }
            }
            if (results[1][1] != 0) {
                return results;
            }
        }
        return null;
    }

    /**
     * @param bitmap   截屏
     * @param offset   x轴偏移,最少要偏移出滑块的位置,最多不能超过滑块阴影的位置
     * @param top      滑块出现区域的最小顶部
     * @param bottom   滑块出现区域的最小底部
     * @param pixCount 匹配到边界的像素点数量,一般偏小于滑块的高度
     * @param sbuscore 边界像素相减的值,通过这个值来确定是不是边界
     * @param span     边界像素跨度 4像素左右,请查看边界颜色衰减
     * @return ！！！不太准啊！！！！
     */
    public static int[] getHDpixUsepix(Bitmap bitmap, int offset, int top, int bottom, int pixCount, int sbuscore, int span) {
        long start = System.currentTimeMillis();
        int bw = bitmap.getWidth();
        int[] pixs = new int[(bottom - top) * span];
        int score = 0;
        int[] results = new int[2];
        for (int i = offset; i < bw - span; i++) {
            try {

                bitmap.getPixels(pixs, 0, span, i, top, span, bottom - top);
            } catch (Exception e) {
                Log.e("异常", e.getMessage());
            }
            for (int j = 0; j < (bottom - top) * span; j += span) {
                int pixF = (Color.green(pixs[j]) + Color.blue(pixs[j]) + Color.red(pixs[j])) / 3;
                int pixL = (Color.green(pixs[j + span - 1]) + Color.blue(pixs[j + span - 1]) + Color.red(pixs[j + span - 1])) / 3;
                if (Math.abs(pixL - pixF) >= sbuscore) {
                    Log.v("--^_^-->", pixL - pixF + "差值");
                    score++;
                }
            }
            if (score > pixCount) {
                results[0] = i;
                results[1] = 0;
                Log.e("--找到图片-->", "[" + results[0] + "]" + results[1] + "耗时:" + (System.currentTimeMillis() - start));
                return results;
            } else {
                score = 0;
            }
        }

        return null;
    }

    /**
     * @param bitmap      截屏
     * @param offset      x轴偏移,最少要偏移出滑块的位置,最多不能超过滑块阴影的位置
     * @param top         滑块出现区域的最小顶部
     * @param bottom      滑块出现区域的最小底部
     * @param pixCount    匹配到边界的像素点数量,一般偏小于滑块的高度
     * @param sbuscore    边界像素相减的值,通过这个值来确定是不是边界
     * @param span        边界像素跨度 4像素左右,请查看边界颜色衰减如果subscore小于30则代表对比隔行像素列
     * @param sbuscoresbu 衰减阈值 若sbuscore=5,sbuscoresbu=3则阈值范围2-8
     * @return
     */
    public static int[] getHDpixUsepix(Bitmap bitmap, int offset, int top, int bottom, int pixCount, int sbuscore, int sbuscoresbu, int span) {
        if (sbuscore > 30) {
            return getHDpixUsepix(bitmap, offset, top, bottom, pixCount, sbuscore, span);
        } else {
            long start = System.currentTimeMillis();
            int bw = bitmap.getWidth();
            int[] pixs = new int[(bottom - top) * span];
            int score = 0;
            int AAscore = 0;
            int[] results = new int[2];
            for (int i = offset; i < bw - span; i++) {
                try {
                    bitmap.getPixels(pixs, 0, span, i, top, span, bottom - top);
                } catch (Exception e) {
                    Logger.e(e.getMessage());
                }
                for (int j = 0; j < (bottom - top) * span; j += span) {
                    int pixF = (Color.green(pixs[j]) + Color.blue(pixs[j]) + Color.red(pixs[j])) / 3;
                    int pixL = (Color.green(pixs[j + span - 1]) + Color.blue(pixs[j + span - 1]) + Color.red(pixs[j + span - 1])) / 3;
                    if (Math.abs(pixL - pixF) > sbuscore - sbuscoresbu && Math.abs(pixL - pixF) < sbuscore + sbuscoresbu) {
                        for (int k = 0; k < span - 1; k++) {
                            int pix1 = (Color.green(pixs[j + k]) + Color.blue(pixs[j + k]) + Color.red(pixs[j + k])) / 3;
                            int pix2 = (Color.green(pixs[j + k + 1]) + Color.blue(pixs[j + k + 1]) + Color.red(pixs[j + k + 1])) / 3;
                            if (Math.abs(pix1 - pix2) > sbuscore - sbuscoresbu && Math.abs(pix1 - pix2) < sbuscore + sbuscoresbu) {
                                AAscore++;
                            } else {
                                AAscore = 0;
                                break;
                            }
                        }
                    }
                    if (AAscore >= span - 1) {
                        score++;
                    }
                    AAscore = 0;

                }
                if (score > pixCount) {
                    results[0] = i;
                    results[1] = 0;
                    Log.e("--找到图片-->", "[" + results[0] + "]" + results[1] + "耗时:" + (System.currentTimeMillis() - start));
                    return results;
                } else {
                    score = 0;
                }
            }
        }

        return null;
    }

    public static boolean canResult(int a, int b) {
        int g = Color.green(a) - Color.green(b);
        if (Math.abs(g) > 10) {
            return false;
        }
        int bl = Color.blue(a) - Color.blue(b);
        if (Math.abs(bl) > 10) {
            return false;
        }
        int r = Color.red(a) - Color.red(b);
        return Math.abs(r) <= 10;
    }
}
