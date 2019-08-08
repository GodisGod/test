package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEditText;
    private Button sa;
    private static final int CODE_PERSON = 1;
    String upId = "";

    List<DynamicDrawableSpan> spans = new ArrayList<>();
    private EditText etStart;
    private EditText etContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (EditText) findViewById(R.id.list_item);
        sa = (Button) findViewById(R.id.sa);
        sa.setOnClickListener(this);
        mEditText.setFilters(new InputFilter[]{new MyInputFilter()});

        findViewById(R.id.btn_clear_spans).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText.getText().clearSpans();
            }
        });

        etStart = findViewById(R.id.et_start);
        etContent = findViewById(R.id.et_end);

        findViewById(R.id.btn_add_span).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int start = Integer.valueOf(etStart.getText().toString());
                int end = Integer.valueOf(etContent.getText().toString());
                SpannableString ss = new SpannableString(mEditText.getText().toString());
                Log.i("LHD", "手动设置span = start = " + start + "  end = " + end);
//                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);

                Person person = new Person("1", "@" + "王五01", start, end);
                LDSpan dynamicDrawableSpan = new LDSpan(MainActivity.this, person);
                ss.setSpan(dynamicDrawableSpan, start, end,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEditText.setTextKeepState(ss);

                int spanStart = mEditText.getText().getSpanStart(dynamicDrawableSpan);
                int spanEnd = mEditText.getText().getSpanEnd(dynamicDrawableSpan);
                Log.i("LHD", "获取的span = spanStart = " + spanStart + "  spanEnd = " + spanEnd);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sa:
                upServiceId();
                break;
        }
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
                resetSpan(mEditText.getText());
                goAt();
            }
            return source;
        }
    }

    private void goAt() {
        Intent intent = new Intent(this, PersonActivity.class);
        startActivityForResult(intent, CODE_PERSON);
    }

    //上传需要的id值
    public void upServiceId() {

        List<String> ids = new ArrayList<>();
        String upIds = "";
        resetSpan(mEditText.getText());
        for (Person p : personMap.values()) {
            ids.add(p.getId());
            upIds = upIds + p.getId() + " ";
        }
        Log.i("LHD", "要上传的Id集合 = " + upIds);
    }

    /**
     * 存储@的cid、name对,需要使用有序map
     */
    private Map<String, Person> personMap = new LinkedHashMap<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CODE_PERSON:
                String keyId = data.getStringExtra(PersonActivity.KEY_CID);
                String nameStr = data.getStringExtra(PersonActivity.KEY_NAME);

                Log.i("DDD", "keyId = " + keyId + "   nameStr = " + nameStr);
                if (TextUtils.isEmpty(keyId) || TextUtils.isEmpty(nameStr)) return;
                //1、判断是否已经添加过，如果添加过则删除@符号并返回
                //找到当前的光标位置,也就是输入@符号的位置
                int curIndex = mEditText.getSelectionStart();
                if (curIndex >= 1) {
                    //删除@符号
                    mEditText.getText().replace(curIndex - 1, curIndex, "");
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

    /**
     * 设置span
     *
     * @param keyId   人员的id
     * @param nameStr 人员的名字
     */
    private void setImageSpan(String keyId, String nameStr) {
        int startIndex = mEditText.getSelectionStart();//光标的位置
        int endIndex = startIndex + nameStr.length();//字符结束的位置
        Log.i("LHD", "setImageSpan startIndex = " + startIndex + " endIndex =  " + endIndex + "  " + mEditText.getText().toString());

        Person person = new Person(keyId, "@" + nameStr, startIndex, endIndex);
        personMap.put(keyId, person);

        //插入要添加的字符，此处是为了给span占位
        mEditText.getText().insert(startIndex, "@" + nameStr);
        //1、使用mEditText构造一个SpannableString
        SpannableString ss = new SpannableString(mEditText.getText().toString());
        //2、遍历添加span
        for (Person p : personMap.values()) {
            Log.i("LHD", "==========每一个人的位置 start = " + p.getStartIndex() + "  end = " + p.getEndIndex() + "  id = " + p.getId() + "  name = " + p.getName() + "  edittext.tostring = " + mEditText.getText().toString());
            LDSpan dynamicDrawableSpan = new LDSpan(MainActivity.this, p);
//            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
            spans.add(dynamicDrawableSpan);

            // 把取到的要@的人名，用DynamicDrawableSpan代替,使用这个span是为了防止在@人名中间插入任何字符
            //注意start和end的范围是前闭后开即[start,end)所以end要加1
            ss.setSpan(dynamicDrawableSpan, p.getStartIndex(), p.getEndIndex() + 1,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mEditText.setTextKeepState(ss);

    }

    /**
     * 重新计算每一个span的位置重置map
     *
     * @param editable 传入需要识别的editable
     */
    private void resetSpan(Editable editable) {
        Log.i("LHD", "===================resetSpan=================");
        LDSpan[] spans = editable.getSpans(0, mEditText.getText().length(), LDSpan.class);

        personMap.clear();

        for (LDSpan s : spans) {
            Log.i("LHD resetSpan ", "  start = " + mEditText.getText().getSpanStart(s) + "  end = " + mEditText.getText().getSpanEnd(s));
            Person p = s.getPerson();
            p.setStartIndex(editable.getSpanStart(s));
            p.setEndIndex(editable.getSpanEnd(s) - 1);
            personMap.put(s.getPerson().getId(), s.getPerson());
        }

        Log.i("LHD resetSpan ", "spans.length = " + spans.length + "  " + personMap.size());
    }

}