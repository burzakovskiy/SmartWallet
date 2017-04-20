package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for managing banking card
 *
 * @author Roman Burzakovskiy
 */
public class BankingCard extends Card {
    private final BankingCardDocument mDoc;

    /**
     * Constructor of banking card
     *
     * @param context of application
     * @param doc     banking card document
     */
    public BankingCard(Context context, BankingCardDocument doc) {

        super(context, R.layout.banking_card_main_inner_layout);
        mDoc = doc;
        this.setHeader();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        TextView cardNumberTextView = (TextView) view.findViewById(R.id.banking_card_card_number_text_view);
        TextView accountNumberTextView = (TextView) view.findViewById(R.id.banking_card_account_number_text_view);
        String cardNumber = "**** **** **** " + mDoc.getCardNumber().substring(4);

        cardNumberTextView.setText(cardNumber);
        Drawable cardIcon = new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_credit_card)
                .color(Color.GRAY);
        cardIcon.setBounds(0, 0, Utils.dpToPx(mContext, 24), Utils.dpToPx(mContext, 24));
        cardNumberTextView.setCompoundDrawables(cardIcon, null, null, null);
        cardNumberTextView.setCompoundDrawablePadding(Utils.dpToPx(getContext(), 16));
        if (mDoc.getAccountNumber() != null && !mDoc.getAccountNumber().isEmpty()) {
            String accountNumber = "";
            switch (Integer.valueOf(mDoc.getBank())) {
                case 0:
                    accountNumber = "******" + mDoc.getAccountNumber().substring(6);
                    break;
                case 1:
                    accountNumber = "**********" + mDoc.getAccountNumber().substring(10);
                    break;
                case 2:
                    accountNumber = "*******" + mDoc.getAccountNumber().substring(7);
                    break;
                case 3:
                    accountNumber = "**********" + mDoc.getAccountNumber().substring(10);
                    break;
            }

            accountNumberTextView.setText(accountNumber);
            Drawable accountIcon = new IconicsDrawable(getContext())
                    .icon(GoogleMaterial.Icon.gmd_receipt)
                    .color(Color.GRAY);
            accountIcon.setBounds(0, 0, Utils.dpToPx(mContext, 24), Utils.dpToPx(mContext, 24));
            accountNumberTextView.setCompoundDrawables(accountIcon, null, null, null);
            accountNumberTextView.setCompoundDrawablePadding(Utils.dpToPx(getContext(), 16));
        }
    }

    /**
     * Gets banking card document
     *
     * @return banking card document
     */
    public BankingCardDocument getDocument() {
        return mDoc;
    }

    /**
     * Set custom header to card
     */
    private void setHeader() {
        BankingCardHeader header = new BankingCardHeader(getContext(), mDoc.getBank());
        this.addCardHeader(header);
    }

    private class BankingCardHeader extends CardHeader {

        private final int bank;

        public BankingCardHeader(Context context, String bank) {
            super(context, R.layout.banking_card_header_inner_layout);
            this.bank = Integer.valueOf(bank);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);
            TextView header = (TextView) view.findViewById(R.id.tv_banking_card_header_name);
            String headerText = getContext().getResources().getStringArray(R.array.bank_integration_banks_ukraine)[bank];
            if (header != null)
                header.setText(headerText);

        }
    }
}
