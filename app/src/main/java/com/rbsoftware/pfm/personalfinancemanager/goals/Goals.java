package com.rbsoftware.pfm.personalfinancemanager.goals;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudant.sync.datastore.ConflictException;
import com.cloudant.sync.datastore.DocumentException;
import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.ExportData;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.gmariotti.cardslib.library.cards.actions.BaseSupplementalAction;
import it.gmariotti.cardslib.library.cards.actions.IconSupplementalAction;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class Goals extends Fragment {
    private final static String TAG = "Goal";
    private final static int RESULT_LOAD_IMAGE = 0;
    private final int GOALS_LOADER_ID = 6;
    private AlertDialog mDialog;
    private FloatingActionButton btnCreateGoal;
    private boolean isCreateGoalPopupWindowOpen;
    private boolean isEditGoalPopupWindowOpen;
    private GoalsCardRecyclerViewAdapter mCardArrayAdapter;
    private String docId;
    private CardRecyclerView mRecyclerView;
    private TextView mEmptyView;
    private EditText editTextGoalValue;
    private ConnectionDetector mConnectionDetector;
    private Uri selectedImage;

    public Goals() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[2]);

        mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.goals_card_recycler_view);
        mEmptyView = (TextView) getActivity().findViewById(R.id.emptyGoals);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getLoaderManager().initLoader(GOALS_LOADER_ID, null, loaderCallbacks);

        isCreateGoalPopupWindowOpen = savedInstanceState != null && savedInstanceState.getBoolean("isCreateGoalPopupWindowOpen");
        isEditGoalPopupWindowOpen = savedInstanceState != null && savedInstanceState.getBoolean("isEditGoalPopupWindowOpen");
        if (savedInstanceState != null) {
            docId = savedInstanceState.getString("docId");
        }
        if (isCreateGoalPopupWindowOpen) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCreateGoalPopupWindow(savedInstanceState);
                }
            }, 100);

        }
        if (isEditGoalPopupWindowOpen) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showEditGoalPopupWindow(savedInstanceState, docId);
                }
            }, 100);

        }


        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(getContext());
        }
        MainActivity.mTracker.setScreenName(TAG);
        btnCreateGoal = (FloatingActionButton) getActivity().findViewById(R.id.btn_create_goal);
        btnCreateGoal.show();
        btnCreateGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGoalPopupWindow(null);

            }
        });
        int status = getContext().getSharedPreferences("material_showcaseview_prefs", Context.MODE_PRIVATE)
                .getInt("status_" + TAG, 0);
        if (status != -1) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startShowcase();
                }
            }, 500);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //check if network is available and send analytics tracker

        if (mConnectionDetector.isConnectingToInternet()) {

            MainActivity.mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("Open").build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btnCreateGoal.hide();
        if (isCreateGoalPopupWindowOpen || isEditGoalPopupWindowOpen) {
            mDialog.dismiss();
            isCreateGoalPopupWindowOpen = true;
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isCreateGoalPopupWindowOpen", isCreateGoalPopupWindowOpen);
        outState.putBoolean("isEditGoalPopupWindowOpen", isEditGoalPopupWindowOpen);
        outState.putString("docId", docId);
        if (mDialog != null) {
            outState.putString("editTextGoalValue", editTextGoalValue.getText().toString());
            //noinspection ConstantConditions
            outState.putString("editTextGoalName", ((EditText) mDialog.findViewById(R.id.et_goal_name)).getText().toString());
            outState.putInt("goalCurrencySpinner", ((Spinner) mDialog.findViewById(R.id.goal_currency_spinner)).getSelectedItemPosition());
            if (selectedImage != null) {
                outState.putString("selectedImage", selectedImage.toString());
            }
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            selectedImage = data.getData();
            ImageView imageView = (ImageView) mDialog.findViewById(R.id.goal_create_card_image_view);

            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Picasso.with(getContext()).load(selectedImage).fit().into(imageView);
            } else {

                // No explanation needed, we can request the permission.

                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

            }
        }
    }

    /**
     * Generates goals cards from asynctaskloader
     *
     * @param cards GoalCards
     */
    private void generateGoals(List<GoalCard> cards) {
        for (GoalCard card : cards) {
            card.setLayout_supplemental_actions_id(R.layout.goal_card_supplemental_actions);
            IconSupplementalAction edit = new IconSupplementalAction(getContext(), R.id.goal_card_edit);
            edit.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    docId = ((GoalCard) card).getDocument().getDocumentRevision().getId();
                    showEditGoalPopupWindow(null, docId);

                }
            });

            IconSupplementalAction complete = new IconSupplementalAction(getContext(), R.id.goal_card_complete);
            complete.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    try {
                        Toast.makeText(getContext(), getString(R.string.goals_congrats),
                                Toast.LENGTH_LONG).show();
                        MainActivity.financeDocumentModel.deleteDocument(((GoalCard) card).getDocument().getDocumentRevision().getId());
                        mCardArrayAdapter.remove(card);
                        updateGoals();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
            });
            IconSupplementalAction share = new IconSupplementalAction(getContext(), R.id.goal_card_share);
            share.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    try {
                        ExportData.exportGoalAsPng(getContext(), card);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            card.addSupplementalAction(edit);
            card.addSupplementalAction(complete);
            card.addSupplementalAction(share);
        }


        mCardArrayAdapter = new GoalsCardRecyclerViewAdapter(getActivity(), cards);

        if (mCardArrayAdapter.getItemCount() >= 3) {

            btnCreateGoal.hide();
        } else {
            btnCreateGoal.show();
        }
        //Staggered grid view
        mCardArrayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();

            }
        });

        //Set the empty view
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mCardArrayAdapter);
            checkAdapterIsEmpty();


        }

    }

    /**
     * Checks whether recycler view is empty
     * And switches to empty view
     */
    private void checkAdapterIsEmpty() {
        if (mCardArrayAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);

        }
    }

    /**
     * Sends broadcast intent to update history
     */
    public void updateGoals() {
        Intent intent = new Intent(GoalLoader.ACTION);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private final LoaderManager.LoaderCallbacks<List<GoalCard>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<GoalCard>>() {
        @Override
        public Loader<List<GoalCard>> onCreateLoader(int id, Bundle args) {
            return new GoalLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<GoalCard>> loader, List<GoalCard> data) {
            generateGoals(data);
        }

        @Override
        public void onLoaderReset(Loader<List<GoalCard>> loader) {
        }
    };

    /**
     * Generates create goal popup window
     */
    private void showCreateGoalPopupWindow(Bundle savedInstanceState) {
        selectedImage = null; //remove all previously loaded images
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialog = new AlertDialog.Builder(getContext())
                .setView(layoutInflater.inflate(R.layout.goal_create_card_layout, null))
                .setTitle(getString(R.string.create_goal))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), null)
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();
        final EditText editTextGoalName = (EditText) mDialog.findViewById(R.id.et_goal_name);

        //setting up currency spinner
        final Spinner goalCurrencySpinner = (Spinner) mDialog.findViewById(R.id.goal_currency_spinner);
        ArrayAdapter<CharSequence> goalCurrencySpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.report_activity_currency_spinner, R.layout.select_default_currency_list_item);
        goalCurrencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalCurrencySpinner.setAdapter(goalCurrencySpinnerAdapter);

        editTextGoalValue = (EditText) mDialog.findViewById(R.id.et_goal_value);
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        if (savedInstanceState != null) {

            editTextGoalName.setText(savedInstanceState.getString("editTextGoalName"));
            editTextGoalValue.setText(savedInstanceState.getString("editTextGoalValue"));
            goalCurrencySpinner.setSelection(savedInstanceState.getInt("goalCurrencySpinner"));

            if (savedInstanceState.getString("selectedImage") != null) {
                selectedImage = Uri.parse(savedInstanceState.getString("selectedImage"));
                ImageView imageView = (ImageView) mDialog.findViewById(R.id.goal_create_card_image_view);
                Picasso.with(getContext()).load(selectedImage).fit().into(imageView);
            }
        } else {
            //if saved instance state is null set default currency to currency spinner
            int pos = goalCurrencySpinnerAdapter.getPosition(MainActivity.defaultCurrency);
            goalCurrencySpinner.setSelection(pos);
        }
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    mDialog.dismiss();
                    createNewGoalDocument(MainActivity.getUserId(),


                            transformGoalName(editTextGoalName.getText().toString()),

                            new ArrayList<>(Arrays.asList(
                                    editTextGoalValue.getText().toString().replaceFirst("^0+(?!$)", ""),
                                    goalCurrencySpinner.getSelectedItem().toString())
                            ),

                            GoalDocument.PRIORITY_NORMAL);
                }
            }
        });
        ImageButton uploadImage = (ImageButton) mDialog.findViewById(R.id.goal_create_card_upload_image);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isCreateGoalPopupWindowOpen = false;
            }
        });
        isCreateGoalPopupWindowOpen = true;
    }

    /**
     * generates edit goal popup window
     *
     * @param docId id of goal document
     */
    private void showEditGoalPopupWindow(Bundle savedInstanceState, final String docId) {
        selectedImage = null; //remove all previously loaded images
        GoalDocument doc = MainActivity.financeDocumentModel.getGoalDocument(docId);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialog = new AlertDialog.Builder(getContext())
                .setView(layoutInflater.inflate(R.layout.goal_create_card_layout, null))
                .setTitle(getString(R.string.edit_goal))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), null)
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();


        final EditText editTextGoalName = (EditText) mDialog.findViewById(R.id.et_goal_name);


        //setting up currency spinner
        final Spinner goalCurrencySpinner = (Spinner) mDialog.findViewById(R.id.goal_currency_spinner);
        ArrayAdapter<CharSequence> goalCurrencySpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.report_activity_currency_spinner, R.layout.select_default_currency_list_item);
        goalCurrencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalCurrencySpinner.setAdapter(goalCurrencySpinnerAdapter);
        editTextGoalValue = (EditText) mDialog.findViewById(R.id.et_goal_value);
        ImageView imageView = (ImageView) mDialog.findViewById(R.id.goal_create_card_image_view);
        if (doc.getImageName() != null) {

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(MainActivity.financeDocumentModel.getGoalImage(doc.getDocumentRevision().getId(), doc.getImageName()));
        }
        if (savedInstanceState != null) {

            editTextGoalName.setText(savedInstanceState.getString("editTextGoalName"));
            editTextGoalValue.setText(savedInstanceState.getString("editTextGoalValue"));
            goalCurrencySpinner.setSelection(savedInstanceState.getInt("goalCurrencySpinner"));
            if (savedInstanceState.getString("selectedImage") != null) {
                selectedImage = Uri.parse(savedInstanceState.getString("selectedImage"));

                Picasso.with(getContext()).load(selectedImage).fit().into(imageView);
            }
        } else {

            editTextGoalName.setText(doc.getName());
            editTextGoalValue.setText(Float.toString(doc.getValue()));
            int pos = goalCurrencySpinnerAdapter.getPosition(doc.getCurrency());
            goalCurrencySpinner.setSelection(pos);

        }

        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    mDialog.dismiss();
                    updateGoalDocument(docId, MainActivity.getUserId(),


                            transformGoalName(editTextGoalName.getText().toString()),

                            new ArrayList<>(Arrays.asList(
                                    editTextGoalValue.getText().toString().replaceFirst("^0+(?!$)", ""),
                                    goalCurrencySpinner.getSelectedItem().toString())
                            ),

                            GoalDocument.PRIORITY_NORMAL);
                }
            }
        });

        ImageButton uploadImage = (ImageButton) mDialog.findViewById(R.id.goal_create_card_upload_image);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isEditGoalPopupWindowOpen = false;

            }
        });
        isEditGoalPopupWindowOpen = true;


    }

    /**
     * Starts get image intent
     */
    private void getImageFromGallery() {

        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImageView imageView = (ImageView) mDialog.findViewById(R.id.goal_create_card_image_view);
                    Picasso.with(getContext()).load(selectedImage).fit().into(imageView);

                } else {
                    selectedImage = null;
                    Toast.makeText(getContext(), getString(R.string.permission_required),
                            Toast.LENGTH_SHORT).show();
                }
            }


        }
    }

    /**
     * Checks if entered data is ok
     *
     * @return true if edit text value is ok
     */
    private boolean validateFields() {
        String value = editTextGoalValue.getText().toString();
        if (value.isEmpty() || !Utils.isNumber(value) || value.matches("0.")) {
            editTextGoalValue.requestFocus();
            Toast.makeText(getContext(), getContext().getString(R.string.set_value), Toast.LENGTH_LONG).show();
            return false;
        }

        if (Float.valueOf(value) == 0) {
            editTextGoalValue.requestFocus();
            Toast.makeText(getContext(), getContext().getString(R.string.set_non_zero_value), Toast.LENGTH_LONG).show();

            return false;
        }
        return true;
    }

    /**
     * Generates name for goal if field was empty
     *
     * @param name of goal
     * @return string of goal name
     */
    private String transformGoalName(String name) {
        if (name.isEmpty()) {
            int count = mCardArrayAdapter.getItemCount() + 1;
            return getContext().getString(R.string.my_goal) + " " + count;
        } else {
            return name;
        }
    }

    /**
     * Creates new goal document
     *
     * @param userId   id of current user
     * @param name     of goal
     * @param value    of goal
     * @param priority of goal
     */
    private void createNewGoalDocument(String userId, String name, ArrayList<String> value, int priority) {

        String path = Utils.getRealPathFromUri(getContext(), selectedImage);
        GoalDocument goalDocument = new GoalDocument(userId, name, value, priority, Utils.getImageName(path));
        MainActivity.financeDocumentModel.createDocument(goalDocument, path);
        updateGoals();

    }

    /**
     * Updates goal document
     *
     * @param docId    id of old document
     * @param userId   id of current user
     * @param name     of goal
     * @param value    of goal
     * @param priority of goal
     */
    private void updateGoalDocument(String docId, String userId, String name, ArrayList<String> value, int priority) {
        String path = Utils.getRealPathFromUri(getContext(), selectedImage);
        String imageName = Utils.getImageName(path);
        if (imageName == null)
            imageName = MainActivity.financeDocumentModel.getGoalDocument(docId).getImageName();
        try {
            MainActivity.financeDocumentModel.updateGoalDocument(MainActivity.financeDocumentModel.getGoalDocument(docId),
                    new GoalDocument(userId, name, value, priority, imageName), path);
            updateGoals();
        } catch (ConflictException e) {
            e.printStackTrace();
        }

    }

    /**
     * Runs showcase presentation on fragment start
     **/
    private void startShowcase() {
        if (getActivity().findViewById(R.id.btn_create_goal) != null) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view
            config.setDismissTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), TAG);
            sequence.setConfig(config);
            sequence.addSequenceItem(getActivity().findViewById(R.id.btn_create_goal), getString(R.string.goal_showcase), getString(R.string.ok));
            sequence.start();
        }
    }
}
