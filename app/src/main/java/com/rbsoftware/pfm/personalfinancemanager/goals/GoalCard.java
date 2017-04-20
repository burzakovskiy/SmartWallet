package com.rbsoftware.pfm.personalfinancemanager.goals;

import android.content.Context;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;

/**
 * Holds methods for Goal card
 *
 * @author Roman Burzakovskiy
 */
public class GoalCard extends MaterialLargeImageCard {
    private GoalDocument doc;

    public GoalCard(Context context) {
        super(context);
    }

    public GoalDocument getDocument() {
        return doc;
    }

    public void setDocument(GoalDocument doc) {
        this.doc = doc;
    }


}
