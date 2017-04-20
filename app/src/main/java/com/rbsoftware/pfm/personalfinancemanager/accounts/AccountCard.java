package com.rbsoftware.pfm.personalfinancemanager.accounts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.Locale;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for creation account
 *
 * @author Roman Burzakovskiy
 */
public class AccountCard extends Card {
    private final String mName;
    private final float mBalance;
    private final String mAccountId;

    /**
     * Public account card constructor
     *
     * @param context of application
     * @param id      account id
     * @param name    account name
     * @param balance current account balance
     */
    public AccountCard(Context context, String id, String name, float balance) {
        super(context, R.layout.account_card_main_inner_layout);
        mAccountId = id;
        mName = name;
        mBalance = balance;
        this.setHeader();

    }


    /**
     * Gets account id
     *
     * @return account id
     */
    public String getAccountId() {
        return mAccountId;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        String balanceText = getContext().getString(R.string.balance) + " " + String.format(Locale.getDefault(), "%1$,.2f", mBalance) + " " + MainActivity.defaultCurrency;
        TextView balanceTextView = (TextView) view.findViewById(R.id.tv_account_card_main_inner_balance);
        if (balanceTextView != null) {
            balanceTextView.setText(balanceText);
        }


    }

    /**
     * Sets custom header
     */
    private void setHeader() {
        //Create a CardHeader
        AccountHeaderInnerCard header = new AccountHeaderInnerCard(mContext);
        this.addCardHeader(header);
    }

    private class AccountHeaderInnerCard extends CardHeader {


        public AccountHeaderInnerCard(Context context) {
            super(context, R.layout.account_card_header_inner_layout);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);
            TextView accountNameTextView = (TextView) view.findViewById(R.id.tv_account_card_header_name);
            if (accountNameTextView != null) {
                accountNameTextView.setText(mName);
            }
        }
    }
}
