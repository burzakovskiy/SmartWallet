package com.rbsoftware.pfm.personalfinancemanager.banking;


import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Roman Burzakovskiy
 */
public class BasicNewBankIntegration extends Fragment {
    OnInitListener mOnInitListener;

    public interface OnInitListener {
        void onInitSuccess();
        void onInitFailed();
    }

    public BasicNewBankIntegration() {
        // Required empty public constructor
    }
    public void onAttachFragment(Fragment fragment) {
        try {
            mOnInitListener = (OnInitListener) fragment;

        } catch (ClassCastException e) {
            throw new ClassCastException(
                    fragment.toString() + " must implement OnInitListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachFragment(getParentFragment());

    }



}
