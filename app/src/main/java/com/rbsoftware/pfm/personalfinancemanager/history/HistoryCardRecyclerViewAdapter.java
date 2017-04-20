package com.rbsoftware.pfm.personalfinancemanager.history;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rbsoftware.pfm.personalfinancemanager.history.HistoryCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.BaseRecyclerViewAdapter;

/**
 * Class for history cards adapter
 *
 * @author Roman Burzakovskiy
 */
public class HistoryCardRecyclerViewAdapter extends BaseRecyclerViewAdapter {

    private static final String TAG = "HistoryCardAdapter";
    /**
     * Internal objects
     */
    private List<HistoryCard> mCards;

    /**
     * Constructor
     *
     * @param context The current context.
     */
    public HistoryCardRecyclerViewAdapter(Context context, List<HistoryCard> cards) {
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

    /**
     * Sets the card's list
     *
     * @param cards list
     */
    public void setCards(List<HistoryCard> cards) {
        mCards = cards;
    }

    /**
     * Appends the specified element to the end of the {@code List}.
     *
     * @param card the object to add.
     * @return always true.
     */
    @Override
    public boolean add(@NonNull final Card card) {
        boolean result = mCards.add((HistoryCard) card);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Appends the specified element into the index specified {@code List}.
     *
     * @param index of element
     * @param card to add
     */
    @Override
    public void add(final int index, @NonNull final Card card) {
        mCards.add(index, (HistoryCard) card);
        notifyItemInserted(index);
    }

    /**
     * Adds the objects in the specified collection to the end of this List. The objects are added in the order in which they are returned from the collection's iterator.
     *
     * @param collection the collection of objects.
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     */
    public boolean addAll(@NonNull final Collection<? extends Card> collection) {
        //noinspection unchecked
        @SuppressWarnings("unchecked") boolean result = mCards.addAll((Collection<? extends HistoryCard>) collection);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Check if the list contains the element
     *
     * @param card to check
     * @return true if card is in list
     */
    @Override
    public boolean contains(final Card card) {
        return mCards.contains(card);
    }

    /**
     * Clears the list
     */
    @Override
    public void clear() {
        mCards.clear();
        notifyDataSetChanged();
    }

    /**
     * Removes the specified element
     *
     * @param card to remove
     * @return true if card was removed
     */
    @Override
    public boolean remove(@NonNull final Card card) {
        boolean result = mCards.remove(card);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Removes the element at position
     *
     * @param position of card
     * @return true if card was removed
     */
    @NonNull
    @Override
    public Card remove(final int position) {
        Card result = mCards.remove(position);
        notifyItemRemoved(position);
        return result;
    }

}
