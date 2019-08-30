package com.example.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ${hongda} on 2019-08-16.
 */
public class AtEdittext extends AppCompatEditText {

    public static final int CODE_PERSON = 0x05;
    public static final String KEY_CID = "key_id";
    public static final String KEY_NAME = "key_name";
    List<DynamicDrawableSpan> spans = new ArrayList<>();

    private int curAtLength = 0;//at人的字符数量

    private final int MAX_NUM = 10;

    /**
     * 存储@的cid、name对,需要使用有序map
     */
    private Map<String, Person> personMap = new LinkedHashMap<>();

    public AtEdittext(Context context) {
        this(context, null);
    }

    public AtEdittext(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AtEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i("LHD", "AtEdittext 111");
        setFilters(new InputFilter[]{new MyInputFilter()});
        requestFocus();

    }

    public int checkAtLength() {

        curAtLength = 0;

        for (Person m :
                personMap.values()) {
            curAtLength = curAtLength + m.getName().length();
        }

        Log.i("LHD", "LHD at人的字符长度 = " + curAtLength);

        return curAtLength;
    }

    /**
     * 识别输入框的是不是@符号
     */
    private class MyInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (source.toString().equalsIgnoreCase("@")
                    || source.toString().equalsIgnoreCase("＠")) {
                Log.i("LHD", "AtEdittext 111");
                resetSpan(getText());
                if (onJumpListener != null) {
                    Log.i("LHD", "AtEdittext 222");
                    onJumpListener.goToChooseContact(CODE_PERSON);
                }
            }
            return source;
        }
    }

    /**
     * 设置span
     *
     * @param keyId   人员的id
     * @param nameStr 人员的名字
     */
    private void setImageSpan(String keyId, String nameStr) {
        int startIndex = getSelectionStart();//光标的位置
        int endIndex = startIndex + nameStr.length();//字符结束的位置
//        Log.i("LHD", "setImageSpan startIndex = " + startIndex + " endIndex =  " + endIndex + "  " + getText().toString());

        Person lBean = new Person();
        lBean.setId(keyId);
        lBean.setName("@" + nameStr);
        lBean.setStartIndex(startIndex);
        lBean.setEndIndex(endIndex);

        personMap.put(keyId, lBean);

        //插入要添加的字符，此处是为了给span占位
        getText().insert(startIndex, "@" + nameStr);
        //1、使用mEditText构造一个SpannableString
        SpannableString ss = new SpannableString(getText().toString());
        //2、遍历添加span
        for (Person p : personMap.values()) {
            Log.i("LHD", "==========每一个人的位置 start = " + p.getStartIndex() + "  end = " + p.getEndIndex() + "  id = " + p.getId() + "  name = " + p.getName() + "  edittext.tostring = " + getText().toString());
            LDSpan dynamicDrawableSpan = new LDSpan(getContext(), p);
//            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
            spans.add(dynamicDrawableSpan);

            // 把取到的要@的人名，用DynamicDrawableSpan代替,使用这个span是为了防止在@人名中间插入任何字符
            //注意start和end的范围是前闭后开即[start,end)所以end要加1
            ss.setSpan(dynamicDrawableSpan, p.getStartIndex(), p.getEndIndex() + 1,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setTextKeepState(ss);

    }

    /**
     * 重新计算每一个span的位置重置map
     *
     * @param editable 传入需要识别的editable
     */
    private void resetSpan(Editable editable) {
        if (editable == null) return;
//        Log.i("LHD", "===================resetSpan=================");
        LDSpan[] spans = editable.getSpans(0, getText().length(), LDSpan.class);

        personMap.clear();

        for (LDSpan s : spans) {
            Log.i("LHD resetSpan ", "  start = " + getText().getSpanStart(s) + "  end = " + getText().getSpanEnd(s));
            Person p = s.getPerson();
            p.setStartIndex(editable.getSpanStart(s));
            p.setEndIndex(editable.getSpanEnd(s) - 1);
            personMap.put(s.getPerson().getId(), s.getPerson());
        }

        Log.i("LHD resetSpan ", "spans.length = " + spans.length + "  " + personMap.size());
    }

    private OnJumpListener onJumpListener;

    public interface OnJumpListener {
        void goToChooseContact(int requestCode);
    }

    //对外方法
    public void setOnJumpListener(OnJumpListener onJumpListener) {
        this.onJumpListener = onJumpListener;
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_PERSON && resultCode == Activity.RESULT_OK) {

            String keyId = data.getStringExtra(KEY_CID);
            String keyId2 = data.getExtras().getString(KEY_CID);
            Bundle bundle = data.getExtras();

            String nameStr = data.getStringExtra(KEY_NAME);

            data.getBundleExtra("");

            Log.i("DDD", "keyId = " + keyId + " keyId2 = " + keyId2 + "   nameStr = " + nameStr);
            if (TextUtils.isEmpty(keyId) || TextUtils.isEmpty(nameStr)) return;
            //1、判断是否已经添加过，如果添加过则删除@符号并返回
            //找到当前的光标位置,也就是输入@符号的位置
            int curIndex = getSelectionStart();
            if (curIndex >= 1) {
                //删除@符号
                getText().replace(curIndex - 1, curIndex, "");
            }

            if (personMap.containsKey(keyId)) {
                //根据id判断，如果已经添加过则不做任何操作，直接返回
                return;
            } else {
                //2、没有添加过则构造span，添加到Edittext中
                setImageSpan(keyId, nameStr);
            }
        }
    }

    //上传需要的id值
    public String getServiceId() {

        List<String> ids = new ArrayList<>();
        String upIds = "";
        resetSpan(getText());
        for (Person p : personMap.values()) {
            ids.add(p.getId());
            upIds = upIds + p.getId() + "|";
        }
        Log.i("LHD", "要上传的Id集合 = " + upIds);

        return upIds;
    }


    public boolean checkPublishContent() {
        if (!TextUtils.isEmpty(getText().toString())) {
            int contentLength = getText().toString().length();
            int atLength = checkAtLength();
            if (contentLength - atLength > 200) {
                Toast.makeText(getContext(), "最多输入200字", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } else {
            Toast.makeText(getContext(), "输入内容为空", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
