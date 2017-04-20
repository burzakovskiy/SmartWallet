package com.rbsoftware.pfm.personalfinancemanager.accounts;

import android.os.AsyncTask;

import com.cloudant.sync.datastore.ConflictException;
import com.cloudant.sync.datastore.DocumentException;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocumentModel;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.budget.BudgetDocument;
import com.rbsoftware.pfm.personalfinancemanager.goals.GoalDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;

import java.util.List;

/**
 * Holds methods for removing account data in background
 *
 * @author Roman Burzakovskiy
 */
public class RemoveAccountDataTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "RemoveAccountDataTask";
    private final String mAccountId;

    /**
     * interface to notify about task completion
     */
    public interface AsyncResponse {
        void processFinish();
    }

    public AsyncResponse mDelegate = null;

    /**
     * Public task constructor
     *
     * @param delegate  interface
     * @param accountId account id to remove
     */
    public RemoveAccountDataTask(AsyncResponse delegate, String accountId) {
        mDelegate = delegate;
        mAccountId = accountId;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Removing finance documents
        List<FinanceDocument> financeDocumentList =
                MainActivity
                        .financeDocumentModel
                        .queryFinanceDocumentsByDate(DateUtils.FROM_START,
                                MainActivity.getUserId(),
                                FinanceDocument.DOC_TYPE,
                                mAccountId);

        for (FinanceDocument doc : financeDocumentList) {
            try {
                MainActivity.financeDocumentModel.deleteDocument(doc);
            } catch (ConflictException e) {
                e.printStackTrace();
            }
        }

        //Removing budget documents
        List<BudgetDocument> budgetDocumentList = MainActivity
                .financeDocumentModel
                .queryBudgetDocumentsByDate(DateUtils.FROM_START,
                        MainActivity.getUserId(),
                        BudgetDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        mAccountId);
        for (BudgetDocument doc : budgetDocumentList) {
            try {
                MainActivity.financeDocumentModel.deleteDocument(doc);
            } catch (ConflictException e) {
                e.printStackTrace();
            }
        }

        //Removing goals documents

        List<GoalDocument> goalDocumentList = MainActivity
                .financeDocumentModel
                .queryGoalDocumentsByDate(DateUtils.FROM_START,
                        MainActivity.getUserId(),
                        GoalDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        mAccountId);
        for (GoalDocument doc : goalDocumentList) {
            try {
                MainActivity.financeDocumentModel.deleteDocument(doc.getDocumentRevision().getId());
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mDelegate.processFinish();
    }
}
