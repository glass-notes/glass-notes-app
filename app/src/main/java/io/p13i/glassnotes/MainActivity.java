package io.p13i.glassnotes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, new String[]{
                "A",
                "B",
                "C"
        });

        ListView listView = (ListView) findViewById(R.id.existing_notes_listview);
        listView.setAdapter(adapter);

    }

    public void onClick_CreateNewNoteButton(View view) {
        // Transition to new view
    }
}
