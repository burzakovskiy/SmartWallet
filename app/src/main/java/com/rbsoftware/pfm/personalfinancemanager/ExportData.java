package com.rbsoftware.pfm.personalfinancemanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;

import com.opencsv.CSVWriter;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by burzakovskiy on 1/6/2016.
 * Holds static method for data export
 **/
public class ExportData {

    /**
     * Static method to export history data
     *
     * @param mContext object context
     * @param document finance document
     * @throws IOException
     **/
    public static void exportHistoryAsCsv(Context mContext, FinanceDocument document) throws IOException {

        File baseDir = mContext.getFilesDir();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = "doc-" + df.format(calendar.getTimeInMillis()) + ".csv";

        String filePath = baseDir.toString() + File.separator + fileName;

        File historyFile = new File(filePath);
        CSVWriter writer;
// File exist
        if (historyFile.exists() && !historyFile.isDirectory()) {
            FileWriter mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
        }
        List<String[]> data = new ArrayList<>();
        List<String> valuesList;

        data.add(new String[]{mContext.getResources().getString(R.string.document_date), DateUtils.getNormalDate(DateUtils.DATE_FORMAT_LONG, document.getDate())});
        data.add(new String[]{"", "", "", ""});

        data.add(new String[]{mContext.getString(R.string.comment), document.getComments()});
        data.add(new String[]{"", "", "", ""});


        HashMap<String, List<String>> allValueMap = document.getValuesMap();
        for (Map.Entry<String, List<String>> entry : allValueMap.entrySet()) {
            valuesList = entry.getValue();
            if (valuesList != null) {
                if (Float.valueOf(valuesList.get(0)) != 0) {
                    data.add(new String[]{Utils.keyToString(mContext, entry.getKey()), valuesList.get(0), valuesList.get(1)});

                }
            }
        }

        writer.writeAll(data);

        writer.close();

        try {
            Uri fileUri = FileProvider.getUriForFile(
                    mContext,
                    "com.rbsoftware.pfm.personalfinancemanager.fileprovider",
                    historyFile);


            Intent sendIntent = new Intent("com.rbsoftware.pfm.personalfinancemanager.ACTION_RETURN_FILE");
            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                sendIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.setType("text/comma-separated-values");
            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share)));

        } catch (IllegalArgumentException e) {
            Log.e("File Selector",
                    "The selected file can't be shared: " +
                            historyFile.getName());
            e.printStackTrace();
        }


    }


    /**
     * Static method to export account summary data
     *
     * @param mContext  object context
     * @param inputData list of account summary fields
     * @throws IOException
     **/
    public static void exportSummaryAsCsv(Context mContext, List<String[]> inputData) throws IOException {
        File baseDir = mContext.getFilesDir();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String fileName = "summary-" + df.format(calendar.getTimeInMillis()) + ".csv";
        String filePath = baseDir.toString() + File.separator + fileName;
        File summaryFile = new File(filePath);
        CSVWriter writer;
// File exist
        if (summaryFile.exists() && !summaryFile.isDirectory()) {
            FileWriter mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
        }

        writer.writeAll(inputData);

        writer.close();

        try {
            Uri fileUri = FileProvider.getUriForFile(
                    mContext,
                    "com.rbsoftware.pfm.personalfinancemanager.fileprovider",
                    summaryFile);


            Intent sendIntent = new Intent("com.rbsoftware.pfm.personalfinancemanager.ACTION_RETURN_FILE");
            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                sendIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.setType("text/comma-separated-values");
            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share)));

        } catch (IllegalArgumentException e) {
            Log.e("File Selector",
                    "The selected file can't be shared: " +
                            summaryFile.getName());
            e.printStackTrace();
        }

    }


    /**
     * Static method to export budget data
     *
     * @param mContext  object context
     * @param inputData list of budget fields
     * @throws IOException
     **/
    public static void exportBudgetAsCsv(Context mContext, List<String[]> inputData) throws IOException {
        File baseDir = mContext.getFilesDir();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String fileName = "budget-" + df.format(calendar.getTimeInMillis()) + ".csv";
        String filePath = baseDir.toString() + File.separator + fileName;
        File summaryFile = new File(filePath);
        CSVWriter writer;
// File exist
        if (summaryFile.exists() && !summaryFile.isDirectory()) {
            FileWriter mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
        }

        writer.writeAll(inputData);

        writer.close();

        try {
            Uri fileUri = FileProvider.getUriForFile(
                    mContext,
                    "com.rbsoftware.pfm.personalfinancemanager.fileprovider",
                    summaryFile);


            Intent sendIntent = new Intent("com.rbsoftware.pfm.personalfinancemanager.ACTION_RETURN_FILE");
            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                sendIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.setType("text/comma-separated-values");
            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share)));

        } catch (IllegalArgumentException e) {
            Log.e("File Selector",
                    "The selected file can't be shared: " +
                            summaryFile.getName());
            e.printStackTrace();
        }

    }

    /**
     * Static method to export charts data
     *
     * @param mContext object context
     * @param views    charts array to be converted into image
     * @throws IOException
     **/
    public static void exportChartAsPng(Context mContext, View[] views) throws IOException {
        File baseDir = mContext.getFilesDir();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Intent sendIntent = new Intent("com.rbsoftware.pfm.personalfinancemanager.ACTION_RETURN_FILE");
        ArrayList<Uri> files = new ArrayList<>();
        for (int i = 0; i < views.length; i++) {
            String fileName = "chart-" + i + df.format(calendar.getTimeInMillis()) + ".png";
            String filePath = baseDir.toString() + File.separator + fileName;
            File chartFile = new File(filePath);

            OutputStream outStream = new FileOutputStream(chartFile);
            getBitmapFromView(views[i]).compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();


            Uri fileUri = FileProvider.getUriForFile(
                    mContext,
                    "com.rbsoftware.pfm.personalfinancemanager.fileprovider",
                    chartFile);


            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                files.add(fileUri);
                sendIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);

        sendIntent.setType("image/png");


        mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share)));

    }


    /**
     * Static method to export goals data
     *
     * @param mContext object context
     * @param view     card to be converted into image
     * @throws IOException
     **/
    public static void exportGoalAsPng(Context mContext, Card view) throws IOException {
        File baseDir = mContext.getFilesDir();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String fileName = "goal-" + df.format(calendar.getTimeInMillis()) + ".png";
        String filePath = baseDir.toString() + File.separator + fileName;
        File chartFile = new File(filePath);

        OutputStream outStream = new FileOutputStream(chartFile);
        getBitmapFromView(view.getCardView().getInternalMainCardLayout()).compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();

        try {
            Uri fileUri = FileProvider.getUriForFile(
                    mContext,
                    "com.rbsoftware.pfm.personalfinancemanager.fileprovider",
                    chartFile);


            Intent sendIntent = new Intent("com.rbsoftware.pfm.personalfinancemanager.ACTION_RETURN_FILE");
            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                sendIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.setType("image/png");
            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share)));

        } catch (IllegalArgumentException e) {
            Log.e("File Selector",
                    "The selected file can't be shared: " +
                            chartFile.getName());
            e.printStackTrace();
        }

    }


    /**
     * Converts view into bitmap
     *
     * @param view chart to converted into image
     **/
    private static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}
