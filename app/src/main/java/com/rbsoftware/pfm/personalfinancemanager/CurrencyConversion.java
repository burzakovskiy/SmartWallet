package com.rbsoftware.pfm.personalfinancemanager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cloudant.sync.datastore.ConflictException;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CurrencyConversion extends AsyncTask<String, String, String> {
    private static final String TAG = "CurrencyConversion";
    private HttpURLConnection urlConnection;
    private final Context mContext;

    public CurrencyConversion(Context context) {

        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {

        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL("https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=12");
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String output) {
        Currency currency = new Currency(output);
        Calendar c = Calendar.getInstance(TimeZone.getDefault());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = df.format(c.getTime());

        if (MainActivity.financeDocumentModel.getCurrencyDocument(Currency.CURRENCY_ID) == null) {
            MainActivity.financeDocumentModel.createDocument(currency);
            Log.d(TAG, "Currency document was created successfully");
        } else {

            try {
                MainActivity.financeDocumentModel.updateCurrencyDocument(MainActivity.financeDocumentModel.getCurrencyDocument(Currency.CURRENCY_ID), currency);
                Log.d(TAG, "Currency rates were updated successfully");
            } catch (ConflictException e) {
                e.printStackTrace();
            }
        }
        Utils.saveToSharedPreferences(mContext, "updatedDate", currentDate);

    } // protected void onPostExecute(Void v)


    /**
     * Static method for currency conversion
     *
     * @param in          input value
     * @param curr        input currency
     * @param defaultCurr default currency
     * @return converted to default currency value
     */
    public static float convertCurrency(float in, String curr, String defaultCurr) {
        Currency convCurr = MainActivity.financeDocumentModel.getCurrencyDocument(Currency.CURRENCY_ID);
        switch (defaultCurr) {
            case "USD": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoUSD().floatValue();
                    }
                    case "USD": {
                        return in;
                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoUSD().floatValue();

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoUSD().floatValue();

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoUSD().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoUSD().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoUSD().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoUSD().floatValue();

                    }
                }
            }

            case "EUR": {
                switch (curr) {
                    case "EUR": {
                        return in;

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoEUR().floatValue();

                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoEUR().floatValue();

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoEUR().floatValue();

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoEUR().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoEUR().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoEUR().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoEUR().floatValue();

                    }
                }
            }

            case "RUB": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoRUB().floatValue();

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoRUB().floatValue();

                    }
                    case "RUB": {
                        return in;

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoRUB().floatValue();

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoRUB().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoRUB().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoRUB().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoRUB().floatValue();

                    }
                }
            }

            case "UAH": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoUAH().floatValue();

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoUAH().floatValue();

                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoUAH().floatValue();

                    }
                    case "UAH": {
                        return in;

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoUAH().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoUAH().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoUAH().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoUAH().floatValue();

                    }
                }
            }

            case "BTC": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoBTC().floatValue();

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoBTC().floatValue();

                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoBTC().floatValue();

                    }
                    case "BTC": {
                        return in;

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoBTC().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoBTC().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoBTC().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoBTC().floatValue();

                    }
                }
            }

            case "GBP": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoGBP().floatValue();

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoGBP().floatValue();

                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoGBP().floatValue();

                    }
                    case "GBP": {
                        return in;

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoGBP().floatValue();

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoGBP().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoGBP().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoGBP().floatValue();

                    }
                }
            }

            case "PLN": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoPLN().floatValue();

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoPLN().floatValue();

                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoPLN().floatValue();

                    }
                    case "PLN": {
                        return in;

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoPLN().floatValue();

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoPLN().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoPLN().floatValue();

                    }
                    case "KRW": {
                        return in * convCurr.getKRWtoPLN().floatValue();

                    }
                }
            }

            case "KRW": {
                switch (curr) {
                    case "EUR": {
                        return in * convCurr.getEURtoKRW().floatValue();

                    }
                    case "USD": {
                        return in * convCurr.getUSDtoKRW().floatValue();

                    }
                    case "RUB": {
                        return in * convCurr.getRUBtoKRW().floatValue();

                    }
                    case "KRW": {
                        return in;

                    }
                    case "UAH": {
                        return in * convCurr.getUAHtoKRW().floatValue();

                    }
                    case "BTC": {
                        return in * convCurr.getBTCtoKRW().floatValue();

                    }
                    case "GBP": {
                        return in * convCurr.getGBPtoKRW().floatValue();

                    }
                    case "PLN": {
                        return in * convCurr.getPLNtoKRW().floatValue();

                    }
                }
            }
            default:
                return in;
        }

    }


} //class MyAsyncTask extends AsyncTask<String, String, Void>
