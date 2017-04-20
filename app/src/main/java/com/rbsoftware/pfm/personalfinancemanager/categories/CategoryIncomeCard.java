package com.rbsoftware.pfm.personalfinancemanager.categories;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for income categories card management
 *
 * @author Roman Burzakovskiy
 */
public class CategoryIncomeCard extends Card {
    private HashMap<String, List<String>> mIncomeMap;
    private OnCategoryRemoveListener onCategoryRemoveListener;

    public CategoryIncomeCard(Context context, HashMap<String, List<String>> incomeMap, OnCategoryRemoveListener onCategoryRemoveListener) {
        super(context, R.layout.category_management_income_card_main_inner_layout);
        mIncomeMap = incomeMap;
        this.onCategoryRemoveListener = onCategoryRemoveListener;
        CategoryIncomeCardHeader header = new CategoryIncomeCardHeader(getContext());
        addCardHeader(header);
    }


    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        LinearLayout wrapperLayout = (LinearLayout) view.findViewById(R.id.category_management_wrapper_layout);
        wrapperLayout.removeAllViews();
        //generating default categories list
        for (int i = 1; i <= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES; i++) {
            wrapperLayout.addView(generateCategoryRow(String.valueOf(i)));
        }

        //generating custom categories list
        for (Map.Entry<String, List<String>> entry : mIncomeMap.entrySet()) {
            wrapperLayout.addView(generateCustomCategoryRow(entry.getKey(), entry.getValue().get(0)));
        }
    }

    /**
     * Generates custom category row
     *
     * @param categoryId of custom category
     * @param name       of custom category
     * @return custom category row
     */
    private RelativeLayout generateCustomCategoryRow(final String categoryId, String name) {
        final RelativeLayout relativeLayout = new RelativeLayout(getContext());
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final RelativeLayout.LayoutParams layoutParamsCategory = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final RelativeLayout.LayoutParams layoutParamsDelete = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(getContext(), 24));
        layoutParams.setMargins(0, 0, 0, Utils.dpToPx(getContext(), 12));

        layoutParamsCategory.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParamsCategory.setMargins(0, 0, 0, Utils.dpToPx(getContext(), 4));

        layoutParamsDelete.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        final TextView textViewCategory = new TextView(getContext());
        relativeLayout.setLayoutParams(layoutParams);
        textViewCategory.setLayoutParams(layoutParamsCategory);

        textViewCategory.setText(name);
        textViewCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textViewCategory.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
        Drawable cardIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_plus_black_24dp);
        cardIcon.setBounds(0, 0, Utils.dpToPx(mContext, 24), Utils.dpToPx(mContext, 24));
        textViewCategory.setCompoundDrawables(cardIcon, null, null, null);
        textViewCategory.setCompoundDrawablePadding(Utils.dpToPx(getContext(), 8));

        final ImageButton deleteButton = new ImageButton(mContext);
        deleteButton.setLayoutParams(layoutParamsDelete);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        deleteButton.setImageResource(R.drawable.ic_remove_grey_24dp);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCategoryRemoveListener.onRemove(categoryId);
            }
        });

        relativeLayout.addView(textViewCategory);
        relativeLayout.addView(deleteButton);
        return relativeLayout;

    }

    /**
     * Generates category row
     *
     * @param key row id
     * @return category row
     */
    private RelativeLayout generateCategoryRow(String key) {
        final RelativeLayout relativeLayout = new RelativeLayout(getContext());
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final RelativeLayout.LayoutParams layoutParamsCategory = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, Utils.dpToPx(getContext(), 12));

        layoutParamsCategory.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParamsCategory.setMargins(0, 0, 0, Utils.dpToPx(getContext(), 4));
        final TextView textViewCategory = new TextView(getContext());
        relativeLayout.setLayoutParams(layoutParams);
        textViewCategory.setLayoutParams(layoutParamsCategory);

        String rowCategory = Utils.keyToString(getContext(), key);
        textViewCategory.setText(rowCategory);
        textViewCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textViewCategory.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
        Drawable legendIndicator = ContextCompat.getDrawable(getContext(), Utils.getDrawableByCategory(Integer.valueOf(key)));
        legendIndicator.setBounds(0, 0, Utils.dpToPx(getContext(), 24), Utils.dpToPx(getContext(), 24));
        textViewCategory.setCompoundDrawables(legendIndicator, null, null, null);
        textViewCategory.setCompoundDrawablePadding(Utils.dpToPx(getContext(), 8));
        relativeLayout.addView(textViewCategory);
        return relativeLayout;

    }

    private class CategoryIncomeCardHeader extends CardHeader {
        public CategoryIncomeCardHeader(Context context) {
            super(context, R.layout.category_management_income_card_header_inner_layout);
        }


    }
}
