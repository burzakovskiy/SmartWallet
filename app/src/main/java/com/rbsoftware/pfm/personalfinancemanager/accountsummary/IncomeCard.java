package com.rbsoftware.pfm.personalfinancemanager.accountsummary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for income card
 *
 * @author Roman Burzakovskiy
 */
public class IncomeCard extends Card {
    private LinearLayout mLinearLayoutWrapper;
    private HashMap<String, Float> incomeMap;
    private float totalincome = 0;

    /**
     * Public constructor of income map
     *
     * @param context     application context
     * @param totalIncome total income
     * @param incomeMap   hashmap of income ids and values
     */
    public IncomeCard(Context context, float totalIncome, HashMap<String, Float> incomeMap) {
        super(context, R.layout.account_summary_income_card_main_inner_layout);
        this.totalincome = totalIncome;
        this.incomeMap = incomeMap;
        IncomeHeaderInnerCard header = new IncomeHeaderInnerCard(context);
        this.addCardHeader(header);
    }

    public String getTotalIncomeValue() {
        return ((IncomeHeaderInnerCard) this.getCardHeader()).incomeTextView.getText().toString();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        mLinearLayoutWrapper = (LinearLayout) view.findViewById(R.id.account_summary_income_card_main_inner_wrapper);
        mLinearLayoutWrapper.removeAllViews();
        if (incomeMap != null) {
            for (Map.Entry<String, Float> entry : incomeMap.entrySet()) {
                mLinearLayoutWrapper.addView(createNewIncomeRow(entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * Generates relative layout with income row
     * @param key of category
     * @param value of category
     * @return income row
     */
    private RelativeLayout createNewIncomeRow(String key, float value) {
        //setting row wrapping relative layout
        final RelativeLayout relativeLayout = new RelativeLayout(getContext());
        final RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayout.setLayoutParams(relativeLayoutParams);
        relativeLayout.setPadding(0, 0, 0, Utils.dpToPx(getContext(), 4));

        //setting category text view
        final RelativeLayout.LayoutParams categoryLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        categoryLayoutParams.setMargins(0, 0, Utils.dpToPx(getContext(), 5), 0);
        categoryLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        final TextView categoryTextView = new TextView(getContext());
        categoryTextView.setLayoutParams(categoryLayoutParams);
        categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        categoryTextView.setText(Utils.keyToString(getContext(), key));
        Drawable legendIndicator;
        if (Utils.isNumber(key)) {

            legendIndicator = ContextCompat.getDrawable(mContext, Utils.getDrawableByCategory(Integer.valueOf(key)));
        } else {

            legendIndicator = ContextCompat.getDrawable(getContext(), R.drawable.ic_plus_black_24dp);

        }
        legendIndicator.setBounds(0, 0, Utils.dpToPx(mContext, 24), Utils.dpToPx(mContext, 24));
        categoryTextView.setCompoundDrawables(legendIndicator, null, null, null);
        categoryTextView.setCompoundDrawablePadding(Utils.dpToPx(mContext, 8));

        //setting value text view
        final RelativeLayout.LayoutParams valueLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        valueLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        valueLayoutParams.setMargins(0, 0, Utils.dpToPx(getContext(), 50), 0);
        final TextView valueTextView = new TextView(getContext());
        valueTextView.setLayoutParams(valueLayoutParams);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        valueTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", value));

        //adding category and value text views to relative layout
        relativeLayout.addView(categoryTextView);
        relativeLayout.addView(valueTextView);
        return relativeLayout;
    }


    private class IncomeHeaderInnerCard extends CardHeader {
        private TextView incomeTextView;


        public IncomeHeaderInnerCard(Context context) {
            super(context, R.layout.account_summary_income_card_header_inner_layout);

        }


        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);
            if (incomeTextView == null) {
                incomeTextView = (TextView) view.findViewById(R.id.tv_income);
            }


            String incomeString = String.format(Locale.getDefault(), "%1$,.2f", totalincome) + " " + MainActivity.defaultCurrency;
            incomeTextView.setText(incomeString);

            //check if any data fetched. If no data set empty views
            if (totalincome == 0) {
                view.findViewById(R.id.emptyIncome).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.emptyIncome).setVisibility(View.GONE);
            }
        }

    }
}
