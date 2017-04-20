package com.rbsoftware.pfm.personalfinancemanager.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rbsoftware.pfm.personalfinancemanager.R;

/**
 * Holds methods for setting user password in settings
 *
 * @author Roman Burzakovskiy
 */
public class PasswordPreference extends DialogPreference {
    private final static String TAG = "PasswordPreference";
    private EditText mCurrentPasswordEditText;
    private EditText mNewPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private String mCurrentPassword;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.password_preferences_layout);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mCurrentPassword = getPersistedString(null);
                if (mCurrentPassword != null && !mCurrentPassword.isEmpty()) {
                    setSummary(R.string.password_set);

                } else {
                    setSummary(R.string.password_not_set);
                }
            }
        }, 100);
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mCurrentPasswordEditText = (EditText) view.findViewById(R.id.password_preferences_current_password);
        mNewPasswordEditText = (EditText) view.findViewById(R.id.password_preferences_new_password);
        mConfirmPasswordEditText = (EditText) view.findViewById(R.id.password_preferences_confirm_password);
        mCurrentPassword = getPersistedString(null);
        if (mCurrentPassword == null || mCurrentPassword.isEmpty()) {
            mCurrentPasswordEditText.setVisibility(View.GONE);
        }

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validateCurrentPassword() && validateNewPassword()) {
                            if ((mCurrentPassword == null || mCurrentPassword.isEmpty()) && !mConfirmPasswordEditText.getText().toString().isEmpty()) {
                                Toast.makeText(getContext(), getContext().getString(R.string.password_created),
                                        Toast.LENGTH_LONG).show();
                                persistString(mConfirmPasswordEditText.getText().toString());
                                getDialog().dismiss();
                                setSummary(R.string.password_set);
                            }
                            if ((mCurrentPassword != null && !mCurrentPassword.isEmpty()) && mConfirmPasswordEditText.getText().toString().isEmpty()) {
                                Toast.makeText(getContext(), getContext().getString(R.string.password_removed),
                                        Toast.LENGTH_LONG).show();
                                persistString(mConfirmPasswordEditText.getText().toString());
                                getDialog().dismiss();
                                setSummary(R.string.password_not_set);
                            }
                            if ((mCurrentPassword != null && !mCurrentPassword.isEmpty()) && !mConfirmPasswordEditText.getText().toString().isEmpty()) {
                                Toast.makeText(getContext(), getContext().getString(R.string.password_changed),
                                        Toast.LENGTH_LONG).show();
                                persistString(mConfirmPasswordEditText.getText().toString());
                                getDialog().dismiss();
                                setSummary(R.string.password_set);
                            }
                            getDialog().dismiss();

                        }
                    }
                });
    }


    private boolean validateCurrentPassword() {
        if (mCurrentPassword != null && !mCurrentPassword.isEmpty()) {
            if (mCurrentPasswordEditText.getText().toString().isEmpty()) {
                mCurrentPasswordEditText.requestFocus();
                mCurrentPasswordEditText.setError(getContext().getString(R.string.enter_password));
                return false;
            }
            if (!mCurrentPasswordEditText.getText().toString().equals(mCurrentPassword)) {
                mCurrentPasswordEditText.requestFocus();
                mCurrentPasswordEditText.setError(getContext().getString(R.string.wrong_password));
                return false;

            }
        }
        return true;
    }

    private boolean validateNewPassword() {

        if (mNewPasswordEditText.getText().toString().isEmpty() && mConfirmPasswordEditText.getText().toString().isEmpty()) {

            return true;
        }
        if (mNewPasswordEditText.getText().toString().length() < 6) {
            mNewPasswordEditText.requestFocus();
            mNewPasswordEditText.setError(getContext().getString(R.string.short_password));
            return false;
        }

        if (!mNewPasswordEditText.getText().toString().equals(mConfirmPasswordEditText.getText().toString())) {
            mConfirmPasswordEditText.setError(getContext().getString(R.string.password_match));
            return false;
        }
        return true;
    }


}
