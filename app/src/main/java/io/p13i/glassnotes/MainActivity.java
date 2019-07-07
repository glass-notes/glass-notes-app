package io.p13i.glassnotes;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @BindView(R.id.activity_main_layout)
    LinearLayout mLinearLayout;

    SelectableTextViewsManager mSelectableTextViewsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        populateLayout();
    }

    void setTextViewCommonStyles(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        textView.setTextSize(4f);
    }

    void populateLayout() {
        mSelectableTextViewsManager = new SelectableTextViewsManager(mLinearLayout) {{
            addChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("Create new note    →");
                setTextViewCommonStyles(this);
            }});

            addChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("Add new TODO       →");
                setTextViewCommonStyles(this);
            }});

            addChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("Open existing:");
                setTextViewCommonStyles(this);
            }});

            init();
        }};
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return mSelectableTextViewsManager.onKeyUp(keyCode, event);
            case KeyEvent.KEYCODE_DPAD_UP:
                return mSelectableTextViewsManager.onKeyUp(keyCode, event);
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
