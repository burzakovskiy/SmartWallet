package com.rbsoftware.pfm.personalfinancemanager;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cloudant.http.interceptors.BasicAuthInterceptor;
import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.event.Subscribe;
import com.cloudant.sync.notifications.ReplicationCompleted;
import com.cloudant.sync.notifications.ReplicationErrored;
import com.cloudant.sync.query.IndexManager;
import com.cloudant.sync.replication.ErrorInfo;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Holds method for data replication
 *
 * @author Roman Burzakovskiy
 */
public class DataReplication {
    private static final String TAG = "DataReplication";
    private static final String CLOUDANT_DB = "envoy";
    private static final String CLOUDANT_AUTH = "_auth";
    private static final String CLOUDANT_HOST = "envoy-smartwallet.mybluemix.net";
    private static final String CLOUDANT_CREATE_USER = "_adduser";


    /**
     * Application context
     */
    private Context mContext;
    /**
     * Volley request queue
     */
    private RequestQueue requestQueue;
    /**
     * username
     */
    private String apiKey;
    /**
     * user password
     */
    private String apiSecret;
    /**
     * push replicator
     */
    private Replicator mPushReplicator;
    /**
     * pull replicator
     */
    private Replicator mPullReplicator;
    /**
     * local datastore
     */
    private Datastore mDatastore;

    /**
     * Datastore index manager
     */
    private IndexManager mIndexManager;

    /**
     * Remote database URI
     */
    private URI mUri;


    /**
     * true if user is authenticated
     */
    private boolean isEnvoyAuth = false;
    /**
     * true if user is created in envoy db
     */
    private boolean isEnvoyUserCreated = true;

    /**
     * Data replication constructor
     *
     * @param context              of application
     * @param financeDocumentModel finance document model
     */
    public DataReplication(Context context, FinanceDocumentModel financeDocumentModel) {
        this.mContext = context;
        this.mDatastore = financeDocumentModel.getDatastore();
        mIndexManager = financeDocumentModel.getIndexManager();
        requestQueue = Volley.newRequestQueue(mContext);

        try {
            mUri = this.createServerURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        authEnvoy();
    }

    /**
     * Builds URI for replication
     *
     * @return URI for remote db
     * @throws URISyntaxException
     */
    private URI createServerURI()
            throws URISyntaxException {

        apiKey = MainActivity.getUserId();
        apiSecret = Utils.encrypt(apiKey).substring(0, 7);
        return new URI("https", apiKey + ":" + apiSecret, CLOUDANT_HOST, 443, "/" + CLOUDANT_DB, null, null);
    }

    private void authEnvoy() {
        try {
            URI uri = new URI("https", null, CLOUDANT_HOST, 443, "/" + CLOUDANT_AUTH, null, null);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, uri.toString(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    try {
                        if (Boolean.valueOf(response.get("loggedin").toString()) && response.get("username").toString().equals(apiKey)) {
                            isEnvoyAuth = true;
                            startPullReplication();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null && error.toString().contains("AuthFailureError")) {
                        Log.e(TAG, error.toString());
                        isEnvoyUserCreated = false;
                        createNewEnvoyUser();


                    }

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization",
                            String.format("Basic %s", Base64.encodeToString(
                                    String.format("%s:%s", apiKey, apiSecret).getBytes(), Base64.DEFAULT)));
                    params.put("username", apiKey);
                    params.put("password", apiSecret);
                    return params;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", apiKey);
                    params.put("password", apiSecret);
                    return params;
                }
            };

            requestQueue.add(stringRequest);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates new envoy user
     */
    public void createNewEnvoyUser() {
        try {

            URI uri = new URI("https", null, CLOUDANT_HOST, 443, "/" + CLOUDANT_CREATE_USER, null, null);

            HashMap<String, String> userCredentials = new HashMap<>();
            userCredentials.put("username", apiKey);
            userCredentials.put("password", apiSecret);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri.toString(), new JSONObject(userCredentials), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    try {

                        if (response.get("ok").toString().equals("true")) {
                            isEnvoyUserCreated = true;
                            isEnvoyAuth = true;
                            startPullReplication();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error: " + error
                            + ">>" + error.networkResponse.statusCode
                            + ">>" + error.getCause()
                            + ">>" + error.getMessage());
                }
            });

            requestQueue.add(jsonObjectRequest);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Stops running replications.</p>
     * <p/>
     * <p>The stop() methods stops the replications asynchronously, see the
     * replicator docs for more information.</p>
     */
    public void stopAllReplications() {
        if (this.mPullReplicator != null) {
            this.mPullReplicator.stop();
        }
        if (mPushReplicator != null) {
            mPushReplicator.stop();
        }
    }


    /**
     * <p>Starts the configured push replication.</p>
     */
    public synchronized void startPushReplication() {
        if (isEnvoyAuth) {
            BasicAuthInterceptor bai = new BasicAuthInterceptor(apiKey + ":" + apiSecret);
            mPushReplicator = ReplicatorBuilder.push().from(mDatastore).to(mUri).addRequestInterceptors(bai).build();

            CountDownLatch latch = new CountDownLatch(1);
            ReplicationListener listener = new ReplicationListener(latch);
            mPushReplicator.getEventBus().register(listener);
            mPushReplicator.start();
            try {
                latch.await();
                mPushReplicator.getEventBus().unregister(listener);
                if (mPushReplicator.getState() != Replicator.State.COMPLETE) {
                    Log.e(TAG, "Error replicating TO remote");
                    System.out.println(listener.error.toString());
                } else {
                    Log.i(TAG, String.format("Replicated %d documents in %d batches",
                            listener.documentsReplicated, listener.batchesReplicated));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Starts the configured pull replication.</p>
     */
    public synchronized void startPullReplication() {
        if (isEnvoyAuth) {
            // envoy only supports basic auth, so switch it on
            BasicAuthInterceptor bai = new BasicAuthInterceptor(apiKey + ":" + apiSecret);
            mPullReplicator = ReplicatorBuilder.pull().to(mDatastore).from(mUri).addRequestInterceptors(bai).build();
            CountDownLatch latch = new CountDownLatch(1);
            ReplicationListener listener = new ReplicationListener(latch);
            mPullReplicator.getEventBus().register(listener);
            mPullReplicator.start();
            try {
                latch.await();
                mPullReplicator.getEventBus().unregister(listener);
                if (mPullReplicator.getState() != Replicator.State.COMPLETE) {
                    Log.e(TAG, listener.error.toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mIndexManager.updateAllIndexes();
        }
    }


    /**
     * Listens to replication progress
     */
    private class ReplicationListener {

        private final CountDownLatch latch;
        public ErrorInfo error = null;
        public int documentsReplicated;
        public int batchesReplicated;

        ReplicationListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Subscribe
        public void complete(ReplicationCompleted event) {
            this.documentsReplicated = event.documentsReplicated;
            this.batchesReplicated = event.batchesReplicated;
            latch.countDown();
        }

        @Subscribe
        public void error(ReplicationErrored event) {
            this.error = event.errorInfo;
            latch.countDown();
        }
    }
}
