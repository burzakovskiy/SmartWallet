package com.rbsoftware.pfm.personalfinancemanager.accounts;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.BaseRecyclerViewAdapter;

/**
 * Holds methods for accounts recyclerview adapter
 *
 * @author Roman Burzakovskiy
 */
public class AccountsCardRecyclerViewAdapter extends BaseRecyclerViewAdapter {

    /**
     * Internal objects
     */
    private final List<AccountCard> mCards;
    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards account cards
     */
    public AccountsCardRecyclerViewAdapter(Context context, List<AccountCard> cards) {
        super(context);
        this.mCards = cards;
    }

    @Override
    public Card getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    @Override
    public boolean add(@NonNull Card card) {
        boolean result = mCards.add((AccountCard) card);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public void add(int index, @NonNull Card card) {
        mCards.add(index, (AccountCard) card);
        notifyItemInserted(index);
    }

    @Override
    public boolean remove(@NonNull Card card) {
        boolean result = mCards.remove(card);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public Card remove(int position) {
        Card result = mCards.remove(position);
        notifyItemRemoved(position);
        return result;
    }

    @Override
    public boolean contains(Card object) {
        return mCards.contains(object);
    }

    @Override
    public void clear() {
        mCards.clear();
        notifyDataSetChanged();
    }
}
