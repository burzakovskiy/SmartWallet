package com.rbsoftware.pfm.personalfinancemanager.goals;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.BaseRecyclerViewAdapter;

/**
 * Holds methods for goals cards recyclerview adapter
 *
 * @author Roman Burzakovskiy
 */
public class GoalsCardRecyclerViewAdapter extends BaseRecyclerViewAdapter {
    /**
     * Internal objects
     */
    private final List<GoalCard> mCards;

    /**
     * Constructor
     *
     * @param context The current context.
     */
    public GoalsCardRecyclerViewAdapter(Context context, List<GoalCard> cards) {
        super(context);
        if (cards != null) {
            mCards = cards;
        } else {
            mCards = new ArrayList<>();
        }
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
        boolean result = mCards.add((GoalCard) card);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public void add(int index, @NonNull Card card) {
        mCards.add(index, (GoalCard) card);
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
