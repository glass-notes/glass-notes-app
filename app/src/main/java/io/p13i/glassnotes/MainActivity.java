package io.p13i.glassnotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.p13i.glassnotes.API.GlassNotesClient;

public class MainActivity extends Activity implements SelectableTextViewsManager.OnTextViewSelectedListener {

    private final static String TAG = MainActivity.class.getName();

    @BindView(R.id.activity_main_layout)
    LinearLayout mLinearLayout;

    SelectableTextViewsManager mSelectableTextViewsManager;

    List<Note> mNotes = new ArrayList<Note>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mLinearLayout.setFocusable(true);

        populateLayout();

        GlassNotesClient.getNotes(new GlassNotesClient.Promise<List<Note>>() {
            @Override
            public void resolved(List<Note> data) {
                for (Note note : data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSelectableTextViewsManager.addManagedTextViewChild(new TextView(MainActivity.this) {{
                                setId(View.generateViewId());
                                setText(note.getTitle());
                                setTextViewCommonStyles(MainActivity.this, this);
                            }});
                        }
                    });
                }
                mNotes = data;
            }

            @Override
            public void failed(Throwable t) {
                Log.e(TAG, "Failed getGist fetch", t);
            }
        });

    }

    static void setTextViewCommonStyles(Context context, TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        textView.setTextSize(4f);
    }

    void populateLayout() {
        // Add the controls
        mSelectableTextViewsManager = new SelectableTextViewsManager(mLinearLayout, this) {{
            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.create_new_note);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.add_new_todo);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("");
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addManagedTextViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText(R.string.add_existing);
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            addViewChild(new TextView(MainActivity.this) {{
                setId(View.generateViewId());
                setText("");
                setTextViewCommonStyles(MainActivity.this, this);
            }});

            init();
        }};

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.e("HERE", Integer.toString(keyCode));
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSelectableTextViewsManager.handleKeypadDpadDown();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                mSelectableTextViewsManager.handleKeypadDpadUp();
                return true;
            case KeyEvent.KEYCODE_ENTER:
                mSelectableTextViewsManager.handleEnter();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    Note getNoteWithTitle(String title) {
        for (Note note : mNotes) {
            if (note.getTitle().equals(title)) {
                return note;
            }
        }
        return null;
    }

    @Override
    public void onTextViewSelected(TextView textView) {
        String text = textView.getText().toString();
        if (text.equals(getResources().getString(R.string.create_new_note))) {
            textView.setText(text + " *");
        } else if (text.equals(getResources().getString(R.string.add_new_todo))) {
            textView.setText(text + " **");
        } else if (getNoteWithTitle(text) != null) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(Note.EXTRA_TAG, getNoteWithTitle(text));
            startActivity(intent);
        }
    }
}
