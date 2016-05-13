package com.dropininc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.fragment.BaseFragment;
import com.dropininc.fragment.ReferFriendFragment;

public class ReferFriendActivity extends BaseActivity {

    Activity mActivity;
    ReferFriendFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = ReferFriendActivity.this;

        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_refer_friend);
        setupView();

         fragment = new ReferFriendFragment();
        pushFragments(fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("MyApp", "Activity: onActivityResult ");
        if(fragment != null){
            Log.i("MyApp", "Activity: ActivityResult != null");
            fragment.onActivityResult(requestCode, resultCode,data);
        }
    }

    public void pushFragments(BaseFragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

    private void setupView() {
        ((TextView) findViewById(R.id.txtTitle)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.txtTitle)).setText(R.string.refer_a_friend);
        findViewById(R.id.relativeLogo).setVisibility(View.GONE);
        findViewById(R.id.relativeRight).setVisibility(View.GONE);
        findViewById(R.id.relativeLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
