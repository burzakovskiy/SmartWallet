package com.rbsoftware.pfm.personalfinancemanager.banking;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.OnCancelAction;
import me.drozdzynski.library.steppers.OnFinishAction;
import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Roman Burzakovskiy
 */
public class NewBankIntegrationStepper extends Fragment implements BasicNewBankIntegration.OnInitListener {
    private static final String TAG = "BankIntegrationStepper";
    /**
     * Third step item
     */
    private SteppersItem stepThird;
    /**
     * Bank country
     */
    private String mCountry;
    /**
     * Bank name
     */
    private String mBank;
    /**
     * User's card number
     */
    private String mCardNumber;
    /**
     * User's account number
     */
    private String mAccountNumber;

    public NewBankIntegrationStepper() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_bank_integraton_stepper, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[4]);

        SteppersView.Config steppersViewConfig = new SteppersView.Config();
        steppersViewConfig.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {
                createBankingCardDocument();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BankIntegration(), "BankIntegration").commit();

            }
        });

        steppersViewConfig.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BankIntegration(), "BankIntegration").commit();
            }
        });

// Setup Support Fragment Manager for fragments in steps
        steppersViewConfig.setFragmentManager(getChildFragmentManager());

        ArrayList<SteppersItem> steps = new ArrayList<>();

        final SteppersItem stepFirst = new SteppersItem();
        final SteppersItem stepSecond = new SteppersItem();
        stepThird = new SteppersItem();
        stepFirst.setLabel(getString(R.string.select_country));

        SelectCountry selectCountry = new SelectCountry();
        selectCountry.setOnClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCountry = String.valueOf(position);
                stepFirst.setPositiveButtonEnable(true);
            }


        });
        stepFirst.setFragment(selectCountry);
        stepFirst.setPositiveButtonEnable(false);


        stepSecond.setLabel(getString(R.string.select_bank));

        //create fragment according to selected bank
        SelectBank selectBank = new SelectBank();
        selectBank.setOnClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBank = String.valueOf(position);
                stepSecond.setPositiveButtonEnable(true);
                switch (Integer.valueOf(mBank)) {
                    case 0:
                        stepThird.setFragment(new NewRaiffeisenBankAvalIntegration());
                        break;
                    case 1:
                        stepThird.setFragment(new NewUkrsotsBankIntegration());
                        break;
                    case 2:
                        stepThird.setFragment(new NewOschadbankIntegration());
                        break;
                    case 3:
                        stepThird.setFragment(new NewUkrsibbankIntegration());
                        break;

                }
            }
        });
        stepSecond.setFragment(selectBank);
        stepSecond.setPositiveButtonEnable(false);


        stepThird.setLabel(getString(R.string.step_finish));


        stepThird.setPositiveButtonEnable(false);


        steps.add(stepFirst);
        steps.add(stepSecond);
        steps.add(stepThird);

        SteppersView steppersView = (SteppersView) getActivity().findViewById(R.id.bank_integration_steppers_view);
        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
    }

    /**
     * Get card number and account number if initialization was successful
     */
    @Override
    public void onInitSuccess() {
        stepThird.setPositiveButtonEnable(true);
        switch (Integer.valueOf(mBank)) {
            case 0:
                mCardNumber = ((NewRaiffeisenBankAvalIntegration) stepThird.getFragment()).getCardNumber();
                mAccountNumber = ((NewRaiffeisenBankAvalIntegration) stepThird.getFragment()).getAccountNumber();
                break;
            case 1:
                mCardNumber = ((NewUkrsotsBankIntegration) stepThird.getFragment()).getCardNumber();
                mAccountNumber = ((NewUkrsotsBankIntegration) stepThird.getFragment()).getAccountNumber();
                break;
            case 2:
                mCardNumber = ((NewOschadbankIntegration) stepThird.getFragment()).getCardNumber();
                mAccountNumber = ((NewOschadbankIntegration) stepThird.getFragment()).getAccountNumber();
                break;
            case 3:
                mCardNumber = ((NewUkrsibbankIntegration) stepThird.getFragment()).getCardNumber();
                mAccountNumber = ((NewUkrsibbankIntegration) stepThird.getFragment()).getAccountNumber();
                break;

        }
    }

    @Override
    public void onInitFailed() {
        stepThird.setPositiveButtonEnable(false);

    }

    /**
     * Creates banking card document
     */
    private void createBankingCardDocument() {
        MainActivity.financeDocumentModel.createDocument(
                new BankingCardDocument(MainActivity.getUserId(),
                        mCountry,
                        mBank,
                        mCardNumber,
                        mAccountNumber));
    }
}
