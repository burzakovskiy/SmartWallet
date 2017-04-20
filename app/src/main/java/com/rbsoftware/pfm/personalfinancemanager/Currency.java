package com.rbsoftware.pfm.personalfinancemanager;

import android.util.Log;

import com.cloudant.sync.datastore.DocumentRevision;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bogdan on 1/5/2016.
 * Holds method to work with currency
 */
public class Currency {

    public final static String CURRENCY_ID = "CurrencyID";
    private DocumentRevision rev;
    private static final String DOC_TYPE = "CurrencyDocument";
    private String input;

    private Double EUR = 1.0;
    private Double USD = 1.0;
    private Double RUB = 1.0;
    private Double BTC = 1.0;
    private Double GBP = 1.0;
    private Double PLN = 1.0;
    private Double KRW = 0.022;

    //UAH
    private String EURtoUAH;
    private String USDtoUAH;
    private String RUBtoUAH;
    private String BTCtoUAH;
    private String GBPtoUAH;
    private String PLNtoUAH;
    private String KRWtoUAH;

    //USD
    private String EURtoUSD;   // if I want to sell EUR and buy USD then I need to use EURtoUSD
    private String RUBtoUSD;
    private String UAHtoUSD;   // if I want to sell UAH and buy USD then I need to use UAHtoUSD
    private String BTCtoUSD;
    private String GBPtoUSD;
    private String PLNtoUSD;
    private String KRWtoUSD;

    //EUR
    private String USDtoEUR;
    private String RUBtoEUR;
    private String UAHtoEUR;
    private String BTCtoEUR;
    private String GBPtoEUR;
    private String PLNtoEUR;
    private String KRWtoEUR;

    //RUB
    private String EURtoRUB;
    private String USDtoRUB;
    private String UAHtoRUB;
    private String BTCtoRUB;
    private String GBPtoRUB;
    private String PLNtoRUB;
    private String KRWtoRUB;

    //BTC
    private String EURtoBTC;
    private String USDtoBTC;
    private String UAHtoBTC;
    private String RUBtoBTC;
    private String GBPtoBTC;
    private String PLNtoBTC;
    private String KRWtoBTC;

    //GBP
    private String EURtoGBP;
    private String USDtoGBP;
    private String UAHtoGBP;
    private String RUBtoGBP;
    private String BTCtoGBP;
    private String PLNtoGBP;
    private String KRWtoGBP;

    //PLN
    private String EURtoPLN;
    private String USDtoPLN;
    private String UAHtoPLN;
    private String RUBtoPLN;
    private String BTCtoPLN;
    private String GBPtoPLN;
    private String KRWtoPLN;

    //KRW
    private String EURtoKRW;
    private String USDtoKRW;
    private String UAHtoKRW;
    private String RUBtoKRW;
    private String BTCtoKRW;
    private String GBPtoKRW;
    private String PLNtoKRW;

    private Currency() {
    }

    public Currency(String input) {
        this.input = input;
        parser();
    }


    private void setEURtoUSD(String EURtoUSD) {
        this.EURtoUSD = EURtoUSD;
    }

    private void setEURtoRUB(String EURtoRUB) {
        this.EURtoRUB = EURtoRUB;
    }

    private void setEURtoUAH(String EURtoUAH) {
        this.EURtoUAH = EURtoUAH;
    }

    private void setEURtoBTC(String EURtoBTC) {
        this.EURtoBTC = EURtoBTC;
    }

    private void setEURtoGBP(String EURtoGBP) {
        this.EURtoGBP = EURtoGBP;
    }

    private void setEURtoPLN(String EURtoPLN) {
        this.EURtoPLN = EURtoPLN;
    }

    public void setEURtoKRW(String EURtoKRW) {
        this.EURtoKRW = EURtoKRW;
    }

    private void setUSDtoEUR(String USDtoEUR) {
        this.USDtoEUR = USDtoEUR;
    }

    private void setUSDtoRUB(String USDtoRUB) {
        this.USDtoRUB = USDtoRUB;
    }

    private void setUSDtoUAH(String USDtoUAH) {
        this.USDtoUAH = USDtoUAH;
    }

    private void setUSDtoBTC(String USDtoBTC) {
        this.USDtoBTC = USDtoBTC;
    }

    private void setUSDtoGBP(String USDtoGBP) {
        this.USDtoGBP = USDtoGBP;
    }

    private void setUSDtoPLN(String USDtoPLN) {
        this.USDtoPLN = USDtoPLN;
    }

    public void setUSDtoKRW(String USDtoKRW) {
        this.USDtoKRW = USDtoKRW;
    }

    private void setRUBtoEUR(String RUBtoEUR) {
        this.RUBtoEUR = RUBtoEUR;
    }

    private void setRUBtoUSD(String RUBtoUSD) {
        this.RUBtoUSD = RUBtoUSD;
    }

    private void setRUBtoUAH(String RUBtoUAH) {
        this.RUBtoUAH = RUBtoUAH;
    }

    private void setRUBtoBTC(String RUBtoBTC) {
        this.RUBtoBTC = RUBtoBTC;
    }

    private void setRUBtoGBP(String RUBtoGBP) {
        this.RUBtoGBP = RUBtoGBP;
    }

    public void setRUBtoKRW(String RUBtoKRW) {
        this.RUBtoKRW = RUBtoKRW;
    }

    private void setRUBtoPLN(String RUBtoPLN) {
        this.RUBtoPLN = RUBtoPLN;
    }

    private void setUAHtoEUR(String UAHtoEUR) {
        this.UAHtoEUR = UAHtoEUR;
    }

    private void setUAHtoRUB(String UAHtoRUB) {
        this.UAHtoRUB = UAHtoRUB;
    }

    private void setUAHtoUSD(String UAHtoUSD) {
        this.UAHtoUSD = UAHtoUSD;
    }

    private void setUAHtoBTC(String UAHtoBTC) {
        this.UAHtoBTC = UAHtoBTC;
    }

    private void setUAHtoGBP(String UAHtoGBP) {
        this.UAHtoGBP = UAHtoGBP;
    }

    private void setUAHtoPLN(String UAHtoPLN) {
        this.UAHtoPLN = UAHtoPLN;
    }

    public void setUAHtoKRW(String UAHtoKRW) {
        this.UAHtoKRW = UAHtoKRW;
    }

    private void setBTCtoEUR(String BTCtoEUR) {
        this.BTCtoEUR = BTCtoEUR;
    }

    private void setBTCtoUSD(String BTCtoUSD) {
        this.BTCtoUSD = BTCtoUSD;
    }

    private void setBTCtoRUB(String BTCtoRUB) {
        this.BTCtoRUB = BTCtoRUB;
    }

    private void setBTCtoUAH(String BTCtoUAH) {
        this.BTCtoUAH = BTCtoUAH;
    }

    private void setBTCtoGBP(String BTCtoGBP) {
        this.BTCtoGBP = BTCtoGBP;
    }

    private void setBTCtoPLN(String BTCtoPLN) {
        this.BTCtoPLN = BTCtoPLN;
    }

    public void setBTCtoKRW(String BTCtoKRW) {
        this.BTCtoKRW = BTCtoKRW;
    }

    private void setGBPtoUSD(String GBPtoUSD) {
        this.GBPtoUSD = GBPtoUSD;
    }

    private void setGBPtoEUR(String GBPtoEUR) {
        this.GBPtoEUR = GBPtoEUR;
    }

    private void setGBPtoRUB(String GBPtoRUB) {
        this.GBPtoRUB = GBPtoRUB;
    }

    private void setGBPtoUAH(String GBPtoUAH) {
        this.GBPtoUAH = GBPtoUAH;
    }

    private void setGBPtoBTC(String GBPtoBTC) {
        this.GBPtoBTC = GBPtoBTC;
    }

    private void setGBPtoPLN(String GBPtoPLN) {
        this.GBPtoPLN = GBPtoPLN;
    }

    public void setGBPtoKRW(String GBPtoKRW) {
        this.GBPtoKRW = GBPtoKRW;
    }

    private void setPLNtoUSD(String PLNtoUSD) {
        this.PLNtoUSD = PLNtoUSD;
    }

    private void setPLNtoEUR(String PLNtoEUR) {
        this.PLNtoEUR = PLNtoEUR;
    }

    private void setPLNtoRUB(String PLNtoRUB) {
        this.PLNtoRUB = PLNtoRUB;
    }

    private void setPLNtoUAH(String PLNtoUAH) {
        this.PLNtoUAH = PLNtoUAH;
    }

    private void setPLNtoBTC(String PLNtoBTC) {
        this.PLNtoBTC = PLNtoBTC;
    }

    private void setPLNtoGBP(String PLNtoGBP) {
        this.PLNtoGBP = PLNtoGBP;
    }

    public void setPLNtoKRW(String PLNtoKRW) {
        this.PLNtoKRW = PLNtoKRW;
    }

    public void setKRWtoUAH(String KRWtoUAH) {
        this.KRWtoUAH = KRWtoUAH;
    }

    public void setKRWtoUSD(String KRWtoUSD) {
        this.KRWtoUSD = KRWtoUSD;
    }

    public void setKRWtoEUR(String KRWtoEUR) {
        this.KRWtoEUR = KRWtoEUR;
    }

    public void setKRWtoRUB(String KRWtoRUB) {
        this.KRWtoRUB = KRWtoRUB;
    }

    public void setKRWtoBTC(String KRWtoBTC) {
        this.KRWtoBTC = KRWtoBTC;
    }

    public void setKRWtoGBP(String KRWtoGBP) {
        this.KRWtoGBP = KRWtoGBP;
    }

    public void setKRWtoPLN(String KRWtoPLN) {
        this.KRWtoPLN = KRWtoPLN;
    }


    public Double getEURtoUSD() {
        return Double.valueOf(EURtoUSD);
    }

    public Double getEURtoRUB() {
        return Double.valueOf(EURtoRUB);
    }

    public Double getEURtoUAH() {
        return Double.valueOf(EURtoUAH);
    }

    public Double getEURtoBTC() {
        return Double.valueOf(EURtoBTC);
    }

    public Double getEURtoGBP() {
        return Double.valueOf(EURtoGBP);
    }


    public Double getKRWtoUAH() {
        if (KRWtoUAH != null) {
            return Double.valueOf(KRWtoUAH);
        } else {
            return 0.022;
        }
    }

    public Double getKRWtoUSD() {
        if (KRWtoUSD != null) {
            return Double.valueOf(KRWtoUSD);
        } else {
            return 0.00088;
        }
    }


    public Double getKRWtoEUR() {
        if (KRWtoEUR != null) {
            return Double.valueOf(KRWtoEUR);
        } else {
            return 0.00080;
        }
    }

    public Double getKRWtoRUB() {
        if(KRWtoRUB !=null) {
            return Double.valueOf(KRWtoRUB);
        }else{
            return 0.055;
        }
    }

    public Double getKRWtoBTC() {
        if(KRWtoBTC != null) {
            return Double.valueOf(KRWtoBTC);
        }else{
            return 0.0000013;
        }
    }

    public Double getKRWtoGBP() {
        if(KRWtoGBP != null) {
            return Double.valueOf(KRWtoGBP);
        }else{
            return 0.00067;
        }
    }
    public Double getKRWtoPLN() {
        if(KRWtoPLN != null) {
            return Double.valueOf(KRWtoPLN);
        }else{
            return 0.0035;
        }
    }


    public Double getEURtoKRW() {
        if(EURtoKRW != null) {
            return Double.valueOf(EURtoKRW);
        }else{
            return 1255.75;
        }
    }

    public Double getUSDtoKRW() {
        if(USDtoKRW != null) {
            return Double.valueOf(USDtoKRW);
        }else{
            return 1142.47;
        }
    }

    public Double getUAHtoKRW() {
        if(UAHtoKRW != null) {
            return Double.valueOf(UAHtoKRW);
        }else{
            return 46.06;
        }
    }

    public Double getRUBtoKRW() {
        if(RUBtoKRW != null) {
            return Double.valueOf(RUBtoKRW);
        }else{
            return 18.02;
        }
    }

    public Double getBTCtoKRW() {
        if(BTCtoKRW !=null) {
            return Double.valueOf(BTCtoKRW);
        }else{
            return 769153.46;
        }
    }

    public Double getGBPtoKRW() {
        if(GBPtoKRW !=null) {
            return Double.valueOf(GBPtoKRW);
        }else{
            return 1494.81;
        }
    }

    public Double getPLNtoKRW() {
        if(PLNtoKRW != null) {
            return Double.valueOf(PLNtoKRW);
        }else{
            return 286.94;
        }
    }


    public Double getEURtoPLN() {
        if (EURtoPLN != null) {
            return Double.valueOf(EURtoPLN);
        } else {
            return 4.369612977;
        }
    }

    public Double getUSDtoEUR() {
        return Double.valueOf(USDtoEUR);
    }

    public Double getUSDtoRUB() {
        return Double.valueOf(USDtoRUB);
    }

    public Double getUSDtoUAH() {
        return Double.valueOf(USDtoUAH);
    }

    public Double getUSDtoBTC() {
        return Double.valueOf(USDtoBTC);
    }

    public Double getUSDtoGBP() {
        return Double.valueOf(USDtoGBP);
    }

    public Double getUSDtoPLN() {
        if (USDtoPLN != null) {
            return Double.valueOf(USDtoPLN);
        } else {
            return 3.85475291;

        }
    }

    public Double getRUBtoEUR() {
        return Double.valueOf(RUBtoEUR);
    }

    public Double getRUBtoUSD() {
        return Double.valueOf(RUBtoUSD);
    }

    public Double getRUBtoUAH() {
        return Double.valueOf(RUBtoUAH);
    }

    public Double getRUBtoBTC() {
        return Double.valueOf(RUBtoBTC);
    }

    public Double getRUBtoGBP() {
        return Double.valueOf(RUBtoGBP);
    }

    public Double getRUBtoPLN() {
        if (RUBtoPLN != null) {
            return Double.valueOf(RUBtoPLN);
        } else {
            return 0.059455709;
        }
    }

    public Double getUAHtoEUR() {
        return Double.valueOf(UAHtoEUR);
    }

    public Double getUAHtoUSD() {
        return Double.valueOf(UAHtoUSD);
    }

    public Double getUAHtoRUB() {
        return Double.valueOf(UAHtoRUB);
    }

    public Double getUAHtoBTC() {
        return Double.valueOf(UAHtoBTC);
    }

    public Double getUAHtoGBP() {
        return Double.valueOf(UAHtoGBP);
    }

    public Double getUAHtoPLN() {

        if (UAHtoPLN != null) {
            return Double.valueOf(UAHtoPLN);
        } else {
            return 0.152207002;
        }
    }

    public Double getBTCtoEUR() {
        return Double.valueOf(BTCtoEUR);
    }

    public Double getBTCtoUSD() {
        return Double.valueOf(BTCtoUSD);
    }

    public Double getBTCtoRUB() {
        return Double.valueOf(BTCtoRUB);
    }

    public Double getBTCtoUAH() {
        return Double.valueOf(BTCtoUAH);
    }

    public Double getBTCtoGBP() {
        return Double.valueOf(BTCtoGBP);
    }

    public Double getBTCtoPLN() {
        if (BTCtoPLN != null) {
            return Double.valueOf(BTCtoPLN);
        } else {
            return 1763.0;
        }
    }

    public Double getGBPtoUSD() {
        return Double.valueOf(GBPtoUSD);
    }

    public Double getGBPtoEUR() {
        return Double.valueOf(GBPtoEUR);
    }

    public Double getGBPtoRUB() {
        return Double.valueOf(GBPtoRUB);
    }

    public Double getGBPtoUAH() {
        return Double.valueOf(GBPtoUAH);
    }

    public Double getGBPtoBTC() {
        return Double.valueOf(GBPtoBTC);
    }

    public Double getGBPtoPLN() {
        if (GBPtoPLN != null) {
            return Double.valueOf(GBPtoPLN);
        } else {
            return 5.555;
        }
    }

    public Double getPLNtoUSD() {
        if (PLNtoUSD != null) {
            return Double.valueOf(PLNtoUSD);
        } else {
            return 0.25942;
        }
    }

    public Double getPLNtoEUR() {
        if (PLNtoEUR != null) {
            return Double.valueOf(PLNtoEUR);
        } else {
            return 0.22885322;
        }
    }

    public Double getPLNtoRUB() {
        if (PLNtoRUB != null) {
            return Double.valueOf(PLNtoRUB);
        } else {
            return 16.8192427;
        }
    }

    public Double getPLNtoUAH() {
        if (PLNtoUAH != null) {
            return Double.valueOf(PLNtoUAH);
        } else {
            return 6.57;
        }
    }

    public Double getPLNtoBTC() {
        if (PLNtoBTC != null) {
            return Double.valueOf(PLNtoBTC);
        } else {
            return 0.000567215;


        }
    }

    public Double getPLNtoGBP() {
        if (PLNtoGBP != null) {
            return Double.valueOf(PLNtoGBP);
        } else {
            return 0.180308043;
        }
    }

    private void parser() {
        String ccy;
//        String base_ccy;
        double buy;
        double sale;

        try {
            JSONArray jArray = new JSONArray(input);
            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jObject = jArray.getJSONObject(i);

                ccy = jObject.getString("ccy");
//                base_ccy = jObject.getString("base_ccy");
                buy = jObject.getDouble("buy");
                sale = jObject.getDouble("sale");

                if (ccy.equals("EUR")) {
                    EUR = (sale + buy) / 2;
                }
                if (ccy.equals("USD")) {
                    USD = (sale + buy) / 2;
                }
                if (ccy.equals("RUR")) {
                    RUB = (sale + buy) / 2;
                }
                if (ccy.equals("BTC")) {
                    BTC = (sale + buy) / 2;
                }
                if (ccy.equals("GBP")) {
                    GBP = (sale + buy) / 2;
                }
                if (ccy.equals("PLZ")) {
                    PLN = (sale + buy) / 2;
                }
            } // End Loop
        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        }// catch (JSONException e) */


        EURtoUAH = Double.toString(EUR);
        USDtoUAH = Double.toString(USD);
        RUBtoUAH = Double.toString(RUB);
        GBPtoUAH = Double.toString(GBP);
        PLNtoUAH = Double.toString(PLN);
        KRWtoUAH = Double.toString(KRW);
        EURtoUSD = Double.toString(EUR / USD);
        RUBtoUSD = Double.toString(RUB / USD);
        GBPtoUSD = Double.toString(GBP / USD);
        PLNtoUSD = Double.toString(PLN / USD);
        UAHtoUSD = Double.toString(1 / USD);
        KRWtoUSD = Double.toString(KRW / USD);
        USDtoEUR = Double.toString(USD / EUR);
        RUBtoEUR = Double.toString(RUB / EUR);
        GBPtoEUR = Double.toString(GBP / EUR);
        PLNtoEUR = Double.toString(PLN / EUR);
        UAHtoEUR = Double.toString(1 / EUR);
        KRWtoEUR = Double.toString(KRW / EUR);
        EURtoRUB = Double.toString(EUR / RUB);
        USDtoRUB = Double.toString(USD / RUB);
        GBPtoRUB = Double.toString(GBP / RUB);
        PLNtoRUB = Double.toString(PLN / RUB);
        UAHtoRUB = Double.toString(1 / RUB);
        KRWtoRUB = Double.toString(KRW / RUB);
        USDtoGBP = Double.toString(USD / GBP);
        RUBtoGBP = Double.toString(RUB / GBP);
        EURtoGBP = Double.toString(EUR / GBP);
        PLNtoGBP = Double.toString(PLN / GBP);
        UAHtoGBP = Double.toString(1 / GBP);
        KRWtoGBP = Double.toString(KRW / GBP);
        USDtoPLN = Double.toString(USD / PLN);
        RUBtoPLN = Double.toString(RUB / PLN);
        GBPtoPLN = Double.toString(GBP / PLN);
        EURtoPLN = Double.toString(EUR / PLN);
        UAHtoPLN = Double.toString(1 / PLN);
        KRWtoPLN = Double.toString(KRW / PLN);
        EURtoKRW = Double.toString(EUR / KRW);
        USDtoKRW = Double.toString(USD / KRW);
        GBPtoKRW = Double.toString(GBP / KRW);
        PLNtoKRW = Double.toString(PLN / KRW);
        UAHtoKRW = Double.toString(1 / KRW);
        RUBtoKRW = Double.toString(RUB / KRW);


        //BTC
        BTCtoUSD = Double.toString(BTC);
        BTCtoEUR = Double.toString((BTC) * (USD / EUR));
        BTCtoGBP = Double.toString((BTC) * (USD / GBP));
        BTCtoPLN = Double.toString((BTC) * (USD / PLN));
        BTCtoRUB = Double.toString((BTC) * (USD / RUB));
        BTCtoUAH = Double.toString((BTC) * (USD)); // 1 BTC = 410*26 = 10K UAH
        BTCtoKRW = Double.toString((BTC) * (USD / KRW));

        USDtoBTC = Double.toString(1 / BTC);
        EURtoBTC = Double.toString(EUR / (BTC * USD));
        GBPtoBTC = Double.toString(GBP / (BTC * USD));
        PLNtoBTC = Double.toString(PLN / (BTC * USD));
        RUBtoBTC = Double.toString(RUB / (BTC * USD));
        UAHtoBTC = Double.toString((1 / (BTC * USD))); // 1/(410*26)
        KRWtoBTC = Double.toString(KRW / (BTC * USD));

    } // public void parser()

    /**
     * Map
     *
     * @return map of
     */
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("EURtoUSD", EURtoUSD);
        map.put("EURtoRUB", EURtoRUB);
        map.put("EURtoUAH", EURtoUAH);
        map.put("EURtoBTC", EURtoBTC);
        map.put("EURtoGBP", EURtoGBP);
        map.put("EURtoPLN", EURtoPLN);
        map.put("EURtoKRW", EURtoKRW);
        map.put("USDtoEUR", USDtoEUR);
        map.put("USDtoRUB", USDtoRUB);
        map.put("USDtoUAH", USDtoUAH);
        map.put("USDtoBTC", USDtoBTC);
        map.put("USDtoGBP", USDtoGBP);
        map.put("USDtoPLN", USDtoPLN);
        map.put("USDtoKRW", USDtoKRW);
        map.put("RUBtoEUR", RUBtoEUR);
        map.put("RUBtoUSD", RUBtoUSD);
        map.put("RUBtoUAH", RUBtoUAH);
        map.put("RUBtoBTC", RUBtoBTC);
        map.put("RUBtoGBP", RUBtoGBP);
        map.put("RUBtoPLN", RUBtoPLN);
        map.put("RUBtoKRW", RUBtoKRW);
        map.put("UAHtoEUR", UAHtoEUR);
        map.put("UAHtoUSD", UAHtoUSD);
        map.put("UAHtoRUB", UAHtoRUB);
        map.put("UAHtoBTC", UAHtoBTC);
        map.put("UAHtoGBP", UAHtoGBP);
        map.put("UAHtoPLN", UAHtoPLN);
        map.put("UAHtoKRW", UAHtoKRW);
        map.put("BTCtoEUR", BTCtoEUR);
        map.put("BTCtoUSD", BTCtoUSD);
        map.put("BTCtoRUB", BTCtoRUB);
        map.put("BTCtoUAH", BTCtoUAH);
        map.put("BTCtoGBP", BTCtoGBP);
        map.put("BTCtoPLN", BTCtoPLN);
        map.put("BTCtoKRW", BTCtoKRW);
        map.put("GBPtoUSD", GBPtoUSD);
        map.put("GBPtoEUR", GBPtoEUR);
        map.put("GBPtoRUB", GBPtoRUB);
        map.put("GBPtoUAH", GBPtoUAH);
        map.put("GBPtoBTC", GBPtoBTC);
        map.put("GBPtoPLN", GBPtoPLN);
        map.put("GBPtoKRW", GBPtoKRW);
        map.put("PLNtoUSD", PLNtoUSD);
        map.put("PLNtoEUR", PLNtoEUR);
        map.put("PLNtoRUB", PLNtoRUB);
        map.put("PLNtoUAH", PLNtoUAH);
        map.put("PLNtoBTC", PLNtoBTC);
        map.put("PLNtoGBP", PLNtoGBP);
        map.put("PLNtoKRW", PLNtoKRW);
        map.put("KRWtoUSD", KRWtoUSD);
        map.put("KRWtoEUR", KRWtoEUR);
        map.put("KRWtoRUB", KRWtoRUB);
        map.put("KRWtoUAH", KRWtoUAH);
        map.put("KRWtoBTC", KRWtoBTC);
        map.put("KRWtoGBP", KRWtoGBP);
        map.put("KRWtoPLN", KRWtoPLN);
        map.put("type", DOC_TYPE);

        return map;
    }


    public DocumentRevision getDocumentRevision() {
        return rev;
    }

    public static Currency fromRevision(DocumentRevision rev) {
        Currency t = new Currency();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(Currency.DOC_TYPE)) {
            t.setEURtoUSD((String) map.get("EURtoUSD"));
            t.setEURtoRUB((String) map.get("EURtoRUB"));
            t.setEURtoUAH((String) map.get("EURtoUAH"));
            t.setEURtoBTC((String) map.get("EURtoBTC"));
            t.setEURtoGBP((String) map.get("EURtoGBP"));
            t.setEURtoPLN((String) map.get("EURtoPLN"));
            t.setEURtoKRW((String) map.get("EURtoKRW"));
            t.setUSDtoEUR((String) map.get("USDtoEUR"));
            t.setUSDtoRUB((String) map.get("USDtoRUB"));
            t.setUSDtoUAH((String) map.get("USDtoUAH"));
            t.setUSDtoBTC((String) map.get("USDtoBTC"));
            t.setUSDtoGBP((String) map.get("USDtoGBP"));
            t.setUSDtoPLN((String) map.get("USDtoPLN"));
            t.setUSDtoKRW((String) map.get("USDtoKRW"));
            t.setRUBtoEUR((String) map.get("RUBtoEUR"));
            t.setRUBtoUSD((String) map.get("RUBtoUSD"));
            t.setRUBtoUAH((String) map.get("RUBtoUAH"));
            t.setRUBtoBTC((String) map.get("RUBtoBTC"));
            t.setRUBtoGBP((String) map.get("RUBtoGBP"));
            t.setRUBtoPLN((String) map.get("RUBtoPLN"));
            t.setRUBtoKRW((String) map.get("RUBtoKRW"));
            t.setUAHtoEUR((String) map.get("UAHtoEUR"));
            t.setUAHtoUSD((String) map.get("UAHtoUSD"));
            t.setUAHtoRUB((String) map.get("UAHtoRUB"));
            t.setUAHtoBTC((String) map.get("UAHtoBTC"));
            t.setUAHtoGBP((String) map.get("UAHtoGBP"));
            t.setUAHtoPLN((String) map.get("UAHtoPLN"));
            t.setUAHtoKRW((String) map.get("UAHtoKRW"));
            t.setBTCtoEUR((String) map.get("BTCtoEUR"));
            t.setBTCtoUSD((String) map.get("BTCtoUSD"));
            t.setBTCtoRUB((String) map.get("BTCtoRUB"));
            t.setBTCtoUAH((String) map.get("BTCtoUAH"));
            t.setBTCtoGBP((String) map.get("BTCtoGBP"));
            t.setBTCtoPLN((String) map.get("BTCtoPLN"));
            t.setBTCtoKRW((String) map.get("BTCtoKRW"));
            t.setGBPtoEUR((String) map.get("GBPtoEUR"));
            t.setGBPtoUSD((String) map.get("GBPtoUSD"));
            t.setGBPtoRUB((String) map.get("GBPtoRUB"));
            t.setGBPtoUAH((String) map.get("GBPtoUAH"));
            t.setGBPtoBTC((String) map.get("GBPtoBTC"));
            t.setGBPtoPLN((String) map.get("GBPtoPLN"));
            t.setGBPtoKRW((String) map.get("GBPtoKRW"));
            t.setPLNtoEUR((String) map.get("PLNtoEUR"));
            t.setPLNtoUSD((String) map.get("PLNtoUSD"));
            t.setPLNtoRUB((String) map.get("PLNtoRUB"));
            t.setPLNtoUAH((String) map.get("PLNtoUAH"));
            t.setPLNtoBTC((String) map.get("PLNtoBTC"));
            t.setPLNtoGBP((String) map.get("PLNtoGBP"));
            t.setPLNtoKRW((String) map.get("PLNtoKRW"));
            t.setKRWtoEUR((String) map.get("KRWtoEUR"));
            t.setKRWtoUSD((String) map.get("KRWtoUSD"));
            t.setKRWtoRUB((String) map.get("KRWtoRUB"));
            t.setKRWtoUAH((String) map.get("KRWtoUAH"));
            t.setKRWtoBTC((String) map.get("KRWtoBTC"));
            t.setKRWtoGBP((String) map.get("KRWtoGBP"));
            t.setKRWtoPLN((String) map.get("KRWtoPLN"));

            return t;
        }
        return null;
    }
}