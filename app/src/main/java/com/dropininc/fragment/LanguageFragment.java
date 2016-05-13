package com.dropininc.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.interfaces.Language;


public class LanguageFragment extends BaseFragment implements View.OnClickListener {

    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;

    private Button bt_cancel;
    private Button bt_save;
    private RelativeLayout lay_english;
    private RelativeLayout lay_spanish;
    private RelativeLayout lay_french;
    private RelativeLayout lay_russian;
    private RelativeLayout lay_deutsch;
    private RelativeLayout lay_arabic;
    private RelativeLayout lay_mandarin;

    private ImageView imgEnglish;
    private ImageView imgSpanish;
    private ImageView imgFrench;
    private ImageView imgRussian;
    private ImageView imgDeutsch;
    private ImageView imgArabic;
    private ImageView imgMadarin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        ((MainActivity) mActivity).addToolBarNormal(getString(R.string.language));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_language, container, false);

        initView();
        controlUISelected(Language.ENGLISH);
        return mRootView;
    }

    private void initView() {
        bt_cancel = (Button) mRootView.findViewById(R.id.bt_cancel);
        bt_save = (Button) mRootView.findViewById(R.id.bt_save);

        lay_english = (RelativeLayout) mRootView.findViewById(R.id.lay_english);
        lay_spanish = (RelativeLayout) mRootView.findViewById(R.id.lay_spanish);
        lay_french = (RelativeLayout) mRootView.findViewById(R.id.lay_french);
        lay_russian = (RelativeLayout) mRootView.findViewById(R.id.lay_russian);
        lay_deutsch = (RelativeLayout) mRootView.findViewById(R.id.lay_deutsch);
        lay_arabic = (RelativeLayout) mRootView.findViewById(R.id.lay_arabic);
        lay_mandarin = (RelativeLayout) mRootView.findViewById(R.id.lay_mandarin);

        imgEnglish = (ImageView) mRootView.findViewById(R.id.imgEnglish);
        imgSpanish = (ImageView) mRootView.findViewById(R.id.imgSpanish);
        imgFrench = (ImageView) mRootView.findViewById(R.id.imgFrench);
        imgRussian = (ImageView) mRootView.findViewById(R.id.imgRussian);
        imgDeutsch = (ImageView) mRootView.findViewById(R.id.imgDeutsch);
        imgArabic = (ImageView) mRootView.findViewById(R.id.imgArabic);
        imgMadarin = (ImageView) mRootView.findViewById(R.id.imgMadarin);

        bt_cancel.setOnClickListener(this);
        bt_save.setOnClickListener(this);
        lay_english.setOnClickListener(this);
        lay_spanish.setOnClickListener(this);
        lay_french.setOnClickListener(this);
        lay_russian.setOnClickListener(this);
        lay_deutsch.setOnClickListener(this);
        lay_arabic.setOnClickListener(this);
        lay_mandarin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cancel:
                ((MainActivity) mActivity).popFragments();
                break;
            case R.id.bt_save:
                ((MainActivity) mActivity).popFragments();
                break;
            case R.id.lay_english:
                controlUISelected(Language.ENGLISH);
                break;
            case R.id.lay_spanish:
                controlUISelected(Language.SPANISH);
                break;
            case R.id.lay_french:
                controlUISelected(Language.FRENCH);
                break;
            case R.id.lay_russian:
                controlUISelected(Language.RUSSIAN);
                break;
            case R.id.lay_deutsch:
                controlUISelected(Language.DEUTSCH);
                break;
            case R.id.lay_arabic:
                controlUISelected(Language.ARABIC);
                break;
            case R.id.lay_mandarin:
                controlUISelected(Language.MANDARIN);
                break;
        }
    }

    private void controlUISelected(int langID) {
        imgEnglish.setImageResource(R.drawable.ic_language_uncheck);
        imgSpanish.setImageResource(R.drawable.ic_language_uncheck);
        imgFrench.setImageResource(R.drawable.ic_language_uncheck);
        imgRussian.setImageResource(R.drawable.ic_language_uncheck);
        imgDeutsch.setImageResource(R.drawable.ic_language_uncheck);
        imgArabic.setImageResource(R.drawable.ic_language_uncheck);
        imgMadarin.setImageResource(R.drawable.ic_language_uncheck);
        switch (langID) {
            case Language.ENGLISH:
                imgEnglish.setImageResource(R.drawable.ic_language_check);
                break;
            case Language.SPANISH:
                imgSpanish.setImageResource(R.drawable.ic_language_check);
                break;
            case Language.FRENCH:
                imgFrench.setImageResource(R.drawable.ic_language_check);
                break;
            case Language.RUSSIAN:
                imgRussian.setImageResource(R.drawable.ic_language_check);
                break;
            case Language.DEUTSCH:
                imgDeutsch.setImageResource(R.drawable.ic_language_check);
                break;
            case Language.ARABIC:
                imgArabic.setImageResource(R.drawable.ic_language_check);
                break;
            case Language.MANDARIN:
                imgMadarin.setImageResource(R.drawable.ic_language_check);
                break;
        }
    }

}
