package io.github.yusuzhan.eventbikelib;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);
        Log.i("ysz", "hf");
    }

}
