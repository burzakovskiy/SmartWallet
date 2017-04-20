package com.rbsoftware.pfm.personalfinancemanager.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.accounts.AccountDocument;
import com.rbsoftware.pfm.personalfinancemanager.accounts.RemoveAccountDataTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds methods for Wipe data preference
 * Created by Roman Burzakovskiy on 7/10/2016.
 */
public class WipeDataPreference extends DialogPreference {
    private static final String TAG = "WipeDataPreference";
    private HashMap<String, List<String>> mAccountsMap;
    private String accountId = "";

    public WipeDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        AccountDocument accountDocument = MainActivity.financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());
        mAccountsMap = accountDocument.getAccountsMap();
        setDialogTitle(R.string.select_account);
        setDialogLayoutResource(R.layout.wipe_data_preference_layout);

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ListView listViewWrapper = (ListView) view.findViewById(R.id.wipe_data_preference_dialog_listview);
        final List<String> accountNamesList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : mAccountsMap.entrySet()) {
            accountNamesList.add(entry.getValue().get(0));

        }
        ArrayAdapter<String> accountNamesAdapter = new ArrayAdapter<>(getContext(), R.layout.select_default_currency_list_item, accountNamesList);
        listViewWrapper.setAdapter(accountNamesAdapter);
        listViewWrapper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (Map.Entry<String, List<String>> entry : mAccountsMap.entrySet()) {
                    if (accountNamesList.get(i).equals(entry.getValue().get(0))) {
                        accountId = entry.getKey();
                        break;
                    }

                }
            }
        });

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        accountId = null;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accountId != null && !accountId.isEmpty() && mAccountsMap.containsKey(accountId)) {
                    RemoveAccountDataTask task = new RemoveAccountDataTask(new RemoveAccountDataTask.AsyncResponse() {
                        @Override
                        public void processFinish() {
                            Toast.makeText(getContext(), getContext().getString(R.string.data_removed), Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                        }
                    }, accountId);
                    task.execute();

                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.select_account),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
