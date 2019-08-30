package com.example.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

public class LDSpan extends DynamicDrawableSpan {

    private Context context;
    private Person person;

    private Bitmap bitmap;

    public LDSpan(Context context, Person person) {
        this.context = context;
        this.person = person;
        this.bitmap = getNameBitmap(person.getName());
    }

    @Override
    public Drawable getDrawable() {
        BitmapDrawable drawable = new BitmapDrawable(
                context.getResources(), bitmap);
        drawable.setBounds(0, 0,
                bitmap.getWidth(),
                bitmap.getHeight());
        return drawable;
    }


    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * 把返回的人名，转换成bitmap
     * <p>
     * 比如返回@李达达
     *
     * @param name
     * @return
     */
    private Bitmap getNameBitmap(String name) {
        /* 把@相关的字符串转换成bitmap 然后使用DynamicDrawableSpan加入输入框中 */
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        //设置字体画笔的颜色
        paint.setColor(context.getResources().getColor(R.color.color_blue));
        //设置字体的大小
        paint.setTextSize(50);
        Rect rect = new Rect();
        paint.getTextBounds(name, 0, name.length(), rect);
        // 获取字符串在屏幕上的长度
        int width = (int) (paint.measureText(name));
        final Bitmap bmp = Bitmap.createBitmap(width, rect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
//        canvas.drawColor(getResources().getColor(R.color.color_blue));
        canvas.drawText(name, rect.left, rect.height() - rect.bottom, paint);
        return bmp;
    }

}
