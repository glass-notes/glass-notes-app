package io.p13i.glassnotes;


import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class EditActivity extends Activity {

    private EditText noteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        noteEditText = findViewById(R.id.note_edit_text);
    }
}
