package com.pattonsoft.zhaofilledittext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SuppressLint("AppCompatCustomView")
public class FillEditView extends EditText implements View.OnKeyListener {
    /**
     * 需要填充 文字分段
     */
    ArrayList<String> stringList;
    /**
     * 记录可以编辑的光标
     */
    List<Integer> canList;
    /**
     * 间隔文字数
     */
    int spaceCount;

    Context context;

    private OnDelKeyEventListener delKeyEventListener;


    public FillEditView self = this;

    public FillEditView(Context context) {
        super(context);
        this.context = context;
        self = this;
    }

    public FillEditView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        self = this;
    }

    public FillEditView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        new FillEditView(context, attrs);
    }

    /**
     * 初始化EidtText
     */
    public void init(ArrayList<String> stringList, int spaceCount) {
        self.stringList = stringList;
        self.spaceCount = spaceCount;
        int canStart = 0;//可编辑开始
        int canEnd = 0;//可编辑结束
        int start = 0;//不可编辑样式开始
        int end = 0;//不可编辑样式结束
        canList = new ArrayList<>();
        List<DefaultWordStyle> defaultWordStyles = new ArrayList<>();
        String a = "";
        SpannableString spanString = null;
        try {
            for (int i = 0; i < stringList.size(); i++) {
                a += stringList.get(i);
                if ((i + 1) < stringList.size()) {
                    canStart = canEnd + stringList.get(i).length();
                    canEnd = canStart + spaceCount;
                    Log.e("TAG", canStart + "---" + canEnd);
                    for (int j = 0; j <= spaceCount; j++) {
                        if (!canList.contains(canStart + j)) {
                            canList.add(canStart + j);
                        }
                    }

                    for (int j = 0; j < spaceCount; j++) {
                        //增加空格
                        a += " ";
                    }
                }
                //不可编辑文本的样式 以及位置
                start = end + (i > 0 ? 1 : 0) * spaceCount;
                TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(context, android.R.style.TextAppearance_Large);
                end = end + stringList.get(i).length() + (i > 0 ? 1 : 0) * spaceCount;
                defaultWordStyles.add(new DefaultWordStyle(textAppearanceSpan, start, end));

            }
            //设置不可变
            spanString = new SpannableString(a);
            for (int i = 0; i < defaultWordStyles.size(); i++) {
                DefaultWordStyle df = defaultWordStyles.get(i);

                spanString.setSpan(df.textAppearanceSpan, df.start, df.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        self.setOnKeyListener(self);
        self.setFilters(filter2);
        self.setText(spanString);//不知为何 这里设置输入框内容 会清空输入框
        self.setSelection(4);
        self.setFilters(filter);

    }

    /**
     * 默认文字样式基类
     */
    class DefaultWordStyle {
        public TextAppearanceSpan textAppearanceSpan;
        public int start;
        public int end;

        public DefaultWordStyle(TextAppearanceSpan textAppearanceSpan, int start, int end) {
            this.textAppearanceSpan = textAppearanceSpan;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "DefaultWordStyle{" +
                    "textAppearanceSpan=" + textAppearanceSpan +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    /**
     * 输入限制器 限制不可编辑光标区域无法输入
     */
    private InputFilter[] filter = new InputFilter[]{

            new InputFilter() {
                /**
                 * @param source 输入的文字
                 * @param start 输入-0，删除-0
                 * @param end 输入-文字的长度，删除-0
                 * @param dest 原先显示的内容
                 * @param dstart 输入-原光标位置，删除-光标删除结束位置
                 * @param dend  输入-原光标位置，删除-光标删除开始位置
                 * @return
                 */
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    System.out.println("source  ==" + source + "  start==" + start + "   end==" + end + "   dest==" + dest + "  dstart==" + dstart + "  dend==" + dend);
                    if (dstart < dend) {
                        Log.e("FillEditView", "表示删除:" + dstart + "---" + dend);
                        //为防止 输入法把英文字符识别成单词从而无法获取 删除事件
                        if (!canList.contains(dend - 1) ) {
                            String before = dest.toString().substring(dstart, dend);
                            return before;
                        }
                        //表示删除
                    } else {

                        Log.e("FillEditView", "添加前光标位置:" + dstart);
                        Log.e("FillEditView", "可添加位置:" + canList.toString());
                        //判断是否可编辑 则跳过
                        if (!canList.contains(dstart)) {
                            //输入 无法输入
                            return "";
                        } else {
                            //设置可输入位置
                            canList.remove((Integer) dstart);

                            for (int i = 0; i < canList.size(); i++) {
                                if (canList.get(i) > dstart) {
                                    canList.set(i, canList.get(i) + end);
                                }
                            }
                            for (int i = 0; i <= end; i++) {
                                canList.add(dstart + i);
                            }

                        }

                    }
                    return null;
                }
            }
    };
    /**
     * 输入限制器 无限制
     */
    private InputFilter[] filter2 = new InputFilter[]{
            //啥也不做
            new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                    return null;
                }
            }
    };


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        //以下代码 中文输入键盘下可以实现  但是 英文键盘 不行
        //改用 重写 InputConnection方法实现

        Log.e("TAG", "keyCode:" + keyCode + "---" + "event" + event.getAction());
        //键盘 按下先判断
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                //删除
                int index = this.getSelectionStart();
                Log.e("FillEditView", "删除前光标位置:" + index);
                Log.e("FillEditView", "可删除位置:" + canList.toString());
                //如果删除到 不可编辑位置
                //1.当没有更小的编辑位置时  跳出提示并重置文本
                int minCan = getPreviousCursorByIndex(index);

                if (!canList.contains(index - 1) && (minCan == 0 || minCan >= index)) {
                    init(stringList, spaceCount);
                    return true;
                } else if (!canList.contains(index - 1) && minCan < index) {
                    //光标移动到上一个 可编辑位置
                    this.setSelection(minCan);
                    return true;
                }
                //设置可输入位置
                canList.remove((Integer) index);
                for (int i = 0; i < canList.size(); i++) {
                    if (canList.get(i) > index) {
                        canList.set(i, canList.get(i) - 1);
                    }
                }
            } else {
                Log.e("FillEditView", "非删除操作");
            }

        }
        return false;
    }

    /**
     * 获取指定index 光标位置 位置前最大的 可编辑的光标位置
     *
     * @param index 最近的可编辑光标位置
     */
    int getPreviousCursorByIndex(int index) {

        int cursor = 0;
        for (int i = 0; i < canList.size(); i++) {
            int can = canList.get(i);
            if (can < index && can > cursor) {
                cursor = can;
            }
        }
        return cursor;

    }



    //以下方法实现了  无论中文键盘 英文键盘  都能响应删除事件
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        Log.e("outAttrs", outAttrs + "");
        return new ZhaoInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }

    private class ZhaoInputConnection extends InputConnectionWrapper {

        public ZhaoInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            //相应删除 按钮按下  以及为删除按钮
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if (delKeyEventListener != null) {
                    boolean a = delKeyEventListener.onDeleteClick();
                    if (!a) {
                        return super.sendKeyEvent(event);
                    } else {
                        return !a;
                    }

                }
            }
            boolean b = super.sendKeyEvent(event);
            Log.e("b", b + "");
            return b;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            Log.e("AAA", beforeLength + "---" + afterLength);
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    /**
     * 功能描述: <br>
     * 〈功能详细描述〉
     *
     * @param delKeyEventListener EditText删除回调
     */
    public void setDelKeyEventListener(OnDelKeyEventListener delKeyEventListener) {
        this.delKeyEventListener = delKeyEventListener;
    }

    public interface OnDelKeyEventListener {
        boolean onDeleteClick();
    }

    //*********************************************************************

}
