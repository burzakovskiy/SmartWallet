package com.rbsoftware.pfm.personalfinancemanager.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

/**
 * Hold methods for managing history card
 *
 * @author Roman Burzakovskiy
 */
public class HistoryCard extends Card {
    /**
     * Finance document
     */
    private final FinanceDocument doc;
    /**
     * Weak reference to context of app
     */
    private WeakReference<Context> weakContext;
    /**
     * Map of custom categories
     * key - category id
     * value - 0- category name, 1- income/expense
     */
    private HashMap<String, List<String>> customCategoriesMap;


    /**
     * public constructor
     *
     * @param context application context
     * @param doc     finance document
     */
    public HistoryCard(Context context, FinanceDocument doc) {
        super(context, R.layout.history_card_main_inner_layout);
        this.weakContext = new WeakReference<>(context);
        this.doc = doc;
        //reading custom categories data
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        customCategoriesMap = categoryDocument.getCategoriesMap();

        this.setHeader();
        this.setExpand();
    }

    /**
     * Gets finance document
     *
     * @return finance document
     */
    public FinanceDocument getDocument() {
        return doc;
    }

    /**
     * Sets header to card
     */
    private void setHeader() {
        //Create a CardHeader
        HistoryHeaderInnerCard header = new HistoryHeaderInnerCard(weakContext.get(), doc.getDate(), doc.getTotalIncome(), doc.getTotalExpense());
        this.addCardHeader(header);
    }

    /**
     * Set exandable part of card
     */
    private void setExpand() {
        HistoryExpandCard expand = new HistoryExpandCard(weakContext.get(), doc);


        //Add expand to card
        this.addCardExpand(expand);

        ViewToClickToExpand viewToClickToExpand =
                ViewToClickToExpand.builder()
                        .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
        this.setViewToClickToExpand(viewToClickToExpand);

    }


    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        TextView commentTextView = (TextView) view.findViewById(R.id.history_card_comment_textview);
        if (!doc.getComments().isEmpty()) {
            commentTextView.setVisibility(View.VISIBLE);
            commentTextView.setText(doc.getComments());
        }else{
            commentTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Helper class to customize card header
     */

    private class HistoryHeaderInnerCard extends CardHeader {
        private final String income;
        private final String expense;
        private final String date;

        public HistoryHeaderInnerCard(Context context, String date, float totalIncome, float totalExpense) {
            super(context, R.layout.history_card_header_inner_layout);
            this.date = DateUtils.getNormalDate(DateUtils.DATE_FORMAT_LONG, date);
            this.income = "+" + String.format(Locale.getDefault(), "%1$,.2f", totalIncome) + " " + MainActivity.defaultCurrency;
            this.expense = "-" + String.format(Locale.getDefault(), "%1$,.2f", totalExpense) + " " + MainActivity.defaultCurrency;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);
            if (view != null) {


                TextView dateView = (TextView) view.findViewById(R.id.report_card_date_text_view);
                if (dateView != null)
                    dateView.setText(date);

                TextView incomeView = (TextView) view.findViewById(R.id.textViewIncome);
                if (incomeView != null)
                    incomeView.setText(income);

                TextView expenseView = (TextView) view.findViewById(R.id.textViewExpense);
                if (expenseView != null)
                    expenseView.setText(expense);
            }
        }
    }

    /**
     * Helper class to customize expand card layout
     */

    private class HistoryExpandCard extends CardExpand {
        private LinearLayout mLayout;
        private final FinanceDocument doc;

        //Use your resource ID for your inner layout
        public HistoryExpandCard(Context context, FinanceDocument doc) {
            super(context, R.layout.history_card_expand_inner_layout);
            this.doc = doc;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);
            List<String> valuesList;

            mLayout = (LinearLayout) view.findViewById(R.id.history_expand_card_layout);
            mLayout.removeAllViewsInLayout();
            HashMap<String, List<String>> allValueMap = doc.getValuesMap();
            for (Map.Entry<String, List<String>> entry : allValueMap.entrySet()) {
                valuesList = entry.getValue();
                if (valuesList != null) {

                    String output = String.format(Locale.getDefault(), "%1$,.2f", Float.valueOf(valuesList.get(0))) + " " + valuesList.get(1);
                    if (Utils.isNumber(entry.getKey())) {
                        mLayout.addView(createNewTextView(Integer.valueOf(entry.getKey()), output));
                    } else {
                        mLayout.addView(createNewTextView(entry.getKey(), output));

                    }


                }
            }

        }

        /**
         * Generates expanded card text views
         *
         * @param i     position in hash map
         * @param value hash map value
         * @return RelativeLayout with two TextViews
         */
        private RelativeLayout createNewTextView(int i, String value) {
            final RelativeLayout mRelativeLayout = new RelativeLayout(weakContext.get());
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            final RelativeLayout.LayoutParams layoutParamsCategory = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            final RelativeLayout.LayoutParams layoutParamsData = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            layoutParamsCategory.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParamsData.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            final TextView mTextViewCategory = new TextView(weakContext.get());
            final TextView mTextViewData = new TextView(weakContext.get());
            mRelativeLayout.setLayoutParams(layoutParams);
            mTextViewCategory.setLayoutParams(layoutParamsCategory);
            mTextViewData.setLayoutParams(layoutParamsData);
            String sign = (i < 6) ? "+" : "-";
            String rowCategory = Utils.keyToString(getContext(), String.valueOf(i));
            String rowData = sign + value;
            mTextViewCategory.setText(rowCategory);
            mTextViewCategory.setTextColor(ContextCompat.getColor(weakContext.get(), R.color.grey));

            //set drawable to category
            Drawable legendIndicator = ContextCompat.getDrawable(weakContext.get(), Utils.getDrawableByCategory(i));
            legendIndicator.setBounds(0, 0, Utils.dpToPx(weakContext.get(), 12), Utils.dpToPx(weakContext.get(), 12));
            mTextViewCategory.setCompoundDrawables(legendIndicator, null, null, null);
            mTextViewCategory.setCompoundDrawablePadding(Utils.dpToPx(weakContext.get(), 8));

            mTextViewData.setText(rowData);
            mTextViewData.setTextColor(ContextCompat.getColor(weakContext.get(), R.color.grey));
            mRelativeLayout.addView(mTextViewCategory);
            mRelativeLayout.addView(mTextViewData);
            return mRelativeLayout;
        }

        /**
         * Generates expanded card text views
         *
         * @param id    of custom category
         * @param value hash map value
         * @return RelativeLayout with two TextViews
         */
        private RelativeLayout createNewTextView(String id, String value) {
            final RelativeLayout mRelativeLayout = new RelativeLayout(weakContext.get());
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            final RelativeLayout.LayoutParams layoutParamsCategory = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            final RelativeLayout.LayoutParams layoutParamsData = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            layoutParamsCategory.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParamsData.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            final TextView mTextViewCategory = new TextView(weakContext.get());
            final TextView mTextViewData = new TextView(weakContext.get());
            mRelativeLayout.setLayoutParams(layoutParams);
            mTextViewCategory.setLayoutParams(layoutParamsCategory);
            mTextViewData.setLayoutParams(layoutParamsData);

            String sign = "-";
            Drawable legendIndicator = ContextCompat.getDrawable(getContext(), R.drawable.ic_minus_black_24dp);
            if (customCategoriesMap.get(id).get(1).equals("0")) {
                sign = "+";
                legendIndicator = ContextCompat.getDrawable(getContext(), R.drawable.ic_plus_black_24dp);
            }
            String rowCategory = customCategoriesMap.get(id).get(0);
            String rowData = sign + value;
            mTextViewCategory.setText(rowCategory);

            mTextViewCategory.setTextColor(ContextCompat.getColor(weakContext.get(), R.color.grey));

            //set drawable to category

            legendIndicator.setBounds(0, 0, Utils.dpToPx(weakContext.get(), 12), Utils.dpToPx(weakContext.get(), 12));
            mTextViewCategory.setCompoundDrawables(legendIndicator, null, null, null);
            mTextViewCategory.setCompoundDrawablePadding(Utils.dpToPx(weakContext.get(), 8));

            mTextViewData.setText(rowData);
            mTextViewData.setTextColor(ContextCompat.getColor(weakContext.get(), R.color.grey));
            mRelativeLayout.addView(mTextViewCategory);
            mRelativeLayout.addView(mTextViewData);
            return mRelativeLayout;
        }

        /**
         * Generates divider between income and expense categories
         *
         * @return divider View
         */
        private View createDivider() {
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(weakContext.get(), 1));
            layoutParams.setMargins(0, Utils.dpToPx(weakContext.get(), 6), 0, Utils.dpToPx(weakContext.get(), 6));
            final View divider = new View(weakContext.get());
            divider.setLayoutParams(layoutParams);
            divider.setBackgroundColor(ContextCompat.getColor(weakContext.get(), R.color.grey));
            return divider;
        }


    }


}
