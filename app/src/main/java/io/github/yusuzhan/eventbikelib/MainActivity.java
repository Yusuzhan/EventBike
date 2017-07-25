package io.github.yusuzhan.eventbikelib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.EventBike;
import com.example.Subscribe;
import com.example.util.L;

public class MainActivity extends Activity {

    EventBike eventBike = EventBike.getBike();

    Button send;

    @Override
    protected void onStart() {
        super.onStart();
        eventBike.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventBike.unregister(this);
    }

    @Subscribe
    public void onMessageReceived(FooMessage message) {
        L.i("onMessageReceived " + message.content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBike.getBike().post(new FooMessage("Hello EventBike!"));
        L.i("onCreate " + System.nanoTime());
        //L.i("onCreate " + System.nanoTime());

        Long onCreate = 676939374270L;
        Long subscribe = 676940176540L;
        L.i("time : " + (onCreate-subscribe));

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BActivity.class));
            }
        });
    }
}
