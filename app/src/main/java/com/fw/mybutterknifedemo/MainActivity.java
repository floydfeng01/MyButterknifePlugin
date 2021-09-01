package com.fw.mybutterknifedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.fw.butterknifeapi.InjectBTHelper;
import com.fw.butterknifetool.bt.BindView;
import com.fw.butterknifetool.bt.UIContent;

@UIContent (R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.testAnnotationTV)
    public TextView testAnnotationTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectBTHelper.Inject(this);

        testAnnotationTV.setText("onCreate test Annotation");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InjectBTHelper.UnInject(this);
    }
}