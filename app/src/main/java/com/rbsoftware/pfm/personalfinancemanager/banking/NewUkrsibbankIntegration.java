package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rbsoftware.pfm.personalfinancemanager.R;

/**
 * Holds methods for Ukrsibabnk inegration
 *
 * Created by Roman Burzakovskiy on 7/26/2016.
 */
public class NewUkrsibbankIntegration extends BasicNewBankIntegration {
    private final static String TAG = "NewUkrsibbankIntegration";
    /**
     * First 4 digits of card number
     */
    private EditText mCardNumberEditText1;
    /**
     * Last 4 digits of card number
     */
    private EditText mCardNumberEditText4;
    /**
     * Account number
     */
    private EditText mAccountNumber;

    public NewUkrsibbankIntegration() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_uksibbank_integration, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCardNumberEditText1 = (EditText) getActivity().findViewById(R.id.bank_integration_card_number_1);
        mCardNumberEditText4 = (EditText) getActivity().findViewById(R.id.bank_integration_card_number_4);
        mAccountNumber = (EditText) getActivity().findViewById(R.id.bank_integration_account_number);
        Button btnCheck = (Button) getActivity().findViewById(R.id.bank_integration_check);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_SMS)
                        == PackageManager.PERMISSION_GRANTED) {

                    validateCard();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_SMS}, 2);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    validateCard();

                } else {

                    Toast.makeText(getContext(), getString(R.string.permission_required),
                            Toast.LENGTH_SHORT).show();
                }
            }


        }
    }

    /**
     * Performs card validation
     */
    private void validateCard() {
        if (validateFields()) {


            if (isSMS() && mOnInitListener != null) {
                mCardNumberEditText1.setEnabled(false);
                mCardNumberEditText4.setEnabled(false);
                mAccountNumber.setEnabled(false);
                mOnInitListener.onInitSuccess();
            } else if (!isSMS() && mOnInitListener != null) {
                mOnInitListener.onInitFailed();
            }
        }
    }

    /**
     * Validates input fields
     *
     * @return true if data is correct
     */
    private boolean validateFields() {
        if (mCardNumberEditText1.getText().toString().isEmpty()) {
            mCardNumberEditText1.requestFocus();
            Toast.makeText(getContext(), getString(R.string.set_value), Toast.LENGTH_LONG).show();
            return false;
        }
        if (mCardNumberEditText1.getText().toString().length() < 4) {
            mCardNumberEditText1.requestFocus();
            Toast.makeText(getContext(), getString(R.string.wrong_card_number), Toast.LENGTH_LONG).show();
            return false;
        }
        if (mCardNumberEditText4.getText().toString().isEmpty()) {
            mCardNumberEditText4.requestFocus();
            Toast.makeText(getContext(), getString(R.string.set_value), Toast.LENGTH_LONG).show();
            return false;
        }

        if (mCardNumberEditText4.getText().toString().length() < 4) {
            mCardNumberEditText4.requestFocus();
            Toast.makeText(getContext(), getString(R.string.wrong_card_number), Toast.LENGTH_LONG).show();
            return false;
        }

        if (!mAccountNumber.getText().toString().isEmpty() && mAccountNumber.getText().toString().length() < 14) {
            mAccountNumber.requestFocus();
            Toast.makeText(getContext(), getString(R.string.wrong_account_number), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Checks if SMS banking is enabled and sms is parsable
     *
     * @return true if card number is in sms
     */
    private boolean isSMS() {

        UkrsibbankSMSParser parser = new UkrsibbankSMSParser(getContext(), 20);
        parser.setCardNumber(UkrsibbankSMSParser.convertCardNumber(mCardNumberEditText1.getText().toString(), mCardNumberEditText4.getText().toString()));
        if (parser.initValidation()) {

            Toast.makeText(getContext(), getString(R.string.check_successful),
                    Toast.LENGTH_LONG).show();
            parser.closeCursor();
            return true;
        } else {
            Toast.makeText(getContext(), getString(R.string.check_unsuccessful),
                    Toast.LENGTH_LONG).show();

            parser.closeCursor();
            return false;
        }

    }


    /**
     * Gets card number from input field
     *
     * @return card number
     */
    public String getCardNumber() {
        return mCardNumberEditText1.getText().toString() + mCardNumberEditText4.getText().toString();
    }

    /**
     * Get account number from input fields
     *
     * @return account number
     */
    public String getAccountNumber() {
        return mAccountNumber.getText().toString();

    }
}
