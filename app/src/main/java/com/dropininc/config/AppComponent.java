package com.dropininc.config;


import android.app.Service;

import com.dropininc.BaseSlidingFragmentActivity;
import com.dropininc.activity.BaseActivity;
import com.dropininc.dialog.BaseDialog;
import com.dropininc.fragment.BaseFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(BaseActivity baseActivity);

    void inject(BaseFragment baseFragment);

    void inject(BaseSlidingFragmentActivity baseActivity);

    void inject(BaseDialog baseDialog);

    void inject(Service service);

}
