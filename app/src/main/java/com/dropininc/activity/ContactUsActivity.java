package com.dropininc.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dropininc.R;
import com.dropininc.model.VerifyModel;
import com.dropininc.utils.Logs;
import com.zendesk.sdk.feedback.ui.ContactZendeskFragment;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.model.request.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.model.request.ZendeskFeedbackConfiguration;
import com.zendesk.sdk.network.SubmissionListener;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.storage.SdkStorage;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

public class ContactUsActivity extends BaseActivity {

    private final String TAG = "ContactUsActivity";

    private ContactZendeskFragment contactFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_contact_us);
        setupView();

        contactFragment = ContactZendeskFragment.newInstance(ZendeskConfig.INSTANCE.getContactConfiguration());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_container, contactFragment);
        fragmentTransaction.commit();
        contactFragment.setFeedbackListener(new SubmissionListener() {
            @Override
            public void onSubmissionStarted() {
                Logs.log(TAG, "onSubmissionStarted");
            }

            @Override
            public void onSubmissionCompleted() {
                Logs.log(TAG, "onSubmissionCompleted");
                finish();
            }

            @Override
            public void onSubmissionCancel() {
                Logs.log(TAG, "onSubmissionCancel");
            }

            @Override
            public void onSubmissionError(ErrorResponse errorResponse) {
                Logs.log(TAG, "onSubmissionError : " + errorResponse.getReason());
                finish();
            }
        });
        invalidateOptionsMenu();
    }

    private void setupZendesk(final VerifyModel model) {
        try {
            SdkStorage.INSTANCE.init(context);
            SdkStorage.INSTANCE.clearUserData();
            SdkStorage.INSTANCE.settings().deleteStoredSettings();
            ZendeskConfig.INSTANCE.init(this, "https://dropinhelp.zendesk.com", "666bd3939a99d869a1c0d7b4e5f51520ab17808c85d73824", "mobile_sdk_client_1ec034b808a4e29ebce2", new ZendeskCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Logs.log(TAG, "onSuccess : " + result);
                }

                @Override
                public void onError(ErrorResponse error) {
                    Logs.log(TAG, "error: " + error.getReason());
                }
            });
            ZendeskFeedbackConfiguration configuration = new BaseZendeskFeedbackConfiguration() {
                @Override
                public String getRequestSubject() {
                    return "Contact request";
                }
            };
            ZendeskConfig.INSTANCE.setContactConfiguration(configuration);
            Identity identity = new AnonymousIdentity.Builder()
                    .withNameIdentifier(model.account.firstName + " " + model.account.lastName)
                    .withEmailIdentifier(model.account.identities.get(0).value)
                    .build();
            Logs.log(TAG, "EmailIdentifier " + (model.account.identities.get(0).value));
            ZendeskConfig.INSTANCE.setIdentity(identity);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void setupView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.text_gray));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.contact_us);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
