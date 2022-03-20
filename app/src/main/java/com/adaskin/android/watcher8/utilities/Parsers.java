package com.adaskin.android.watcher8.utilities;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.adaskin.android.watcher8.models.StockQuote;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;


public class Parsers {
    private static Parsers mInstance;
    private static boolean mLoaded;
    private ParserStrings mParserStrings;

    private Parsers() {
        mLoaded = false;
    }

    public static Parsers getInstance() {
        if (mInstance == null) {
            mInstance = new Parsers();
        }
        return mInstance;
    }


    public void LoadStrings(Context context){
        final File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        File file = new File(folder, "Watcher8_Parser");
        try {
            FileInputStream fis = new FileInputStream(file);
            Reader isReader = new InputStreamReader(fis);
            Type ParserStringType = new TypeToken<ParserStrings>(){}.getType();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            mParserStrings = gson.fromJson(isReader, ParserStringType);
            isReader.close();
            if (mParserStrings == null) {
                Toast.makeText(context, "Could not read parser strings.", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(context, "Parser strings loaded", Toast.LENGTH_LONG).show();
            mLoaded = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean parseYAHOOResponse(Context context, final StockQuote quote, String response){
        if (!mLoaded) {
            LoadStrings(context);
        }
         return parseYAHOOResponse2(mParserStrings, quote, response);

    }

    public boolean parseYAHOOResponse2(ParserStrings parserStrings, final StockQuote quote, String response){
        String invalidSymbolMarker = parserStrings.invalidSymbolMarker;
        String yrStart = parserStrings.yrStart;
        String generalMid = parserStrings.generalMid;
        String yrStop = parserStrings.yrStop;
        String analStart = parserStrings.analStart;
        String analStop = parserStrings.analStop;

        // Make sure Invalid Symbol Marker isn't present
        int idxInvalid = response.indexOf(invalidSymbolMarker);
        if (idxInvalid != -1) {
            return false;
        }

        quote.mFullName = parseFullName(quote.mSymbol, response, parserStrings);
        quote.mPPS = parseCurrentPrice(response, parserStrings);
        quote.mDivPerShare = parseDividend(response, parserStrings);

        // Parse out 52-week range
        String yrString;
        int idxYrStart = response.indexOf(yrStart);
        if (idxYrStart == -1) {
            yrString = "0 - 0";
        } else {
            yrString = response.substring(idxYrStart + yrStart.length());
            int idxYrMid = yrString.indexOf(generalMid);
            if (idxYrMid == -1) {
                yrString = "0 - 0";
            } else {
                yrString = yrString.substring(idxYrMid + generalMid.length());
                int idxYrStop = yrString.indexOf(yrStop);
                if (idxYrStop == -1) {
                    yrString = "0 - 0";
                } else {
                    yrString = yrString.substring(0, idxYrStop);
                }
            }
        }
        String yrMinString = yrString.substring(0, yrString.indexOf(" -"));
        String yrMaxString = yrString.substring(yrString.indexOf("- ")+2);
        quote.mYrMin = parseFloatOrNA(yrMinString);
        quote.mYrMax = parseFloatOrNA(yrMaxString);

        // Parse out Analysts opinion
        quote.mAnalystsOpinion = parseAnalystsOpinion(response, analStart, analStop);

        // Parse out previous close pps and compute percent changes.
        quote.compute(parsePreviousClosePrice(response, parserStrings));

        return true;
    }

    private float parseAnalystsOpinion(@NonNull String response, String analStart, String analStop) {
        String analString;
        int idxAnalStart = response.indexOf(analStart);
        if (idxAnalStart == -1) {
            analString = "N/A";
        } else {
            analString = response.substring(idxAnalStart + analStart.length());
            int idxAnalStop = analString.indexOf(analStop);
            if (idxAnalStop == -1) {
                analString = "N/A";
            } else {
                analString = analString.substring(0, idxAnalStop);
            }
        }
        return parseFloatOrNA(analString);
    }

    private float parseCurrentPrice(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String ppsString;
        int idxPpsStart = response.indexOf(parserStrings.ppsStart);
        if (idxPpsStart == -1) {
            ppsString = "N/A";
        } else {
            ppsString = response.substring(idxPpsStart + parserStrings.ppsStart.length());
            int idxPpsMid = ppsString.indexOf(parserStrings.midPattern);
            if (idxPpsMid == -1) {
                ppsString = "N/A";
            } else {
                ppsString = ppsString.substring(idxPpsMid + parserStrings.midPattern.length());
                int idxPpsStop = ppsString.indexOf(parserStrings.stopPattern);
                if (idxPpsStop == -1) {
                    ppsString = "N/A";
                } else {
                    ppsString = ppsString.substring(0, idxPpsStop);
                }
            }
        }
        return parseFloatOrNA(ppsString);
    }

    private float parseDividend(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String ppsString;
        int idxPpsStart = response.indexOf(parserStrings.divStart);
        if (idxPpsStart == -1) {
            ppsString = "N/A";
        } else {
            ppsString = response.substring(idxPpsStart + parserStrings.divStart.length());
            int idxPpsMid = ppsString.indexOf(parserStrings.midPattern);
            if (idxPpsMid == -1) {
                ppsString = "N/A";
            } else {
                ppsString = ppsString.substring(idxPpsMid + parserStrings.midPattern.length());
                int idxPpsStop = ppsString.indexOf(parserStrings.stopPattern);
                if (idxPpsStop == -1) {
                    ppsString = "N/A";
                } else {
                    ppsString = ppsString.substring(0, idxPpsStop);
                }
            }
        }
        return parseFloatOrNA(ppsString);
    }
    private float parsePreviousClosePrice(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String ppsString;
        int idxPpsStart = response.indexOf(parserStrings.prevStart);
        if (idxPpsStart == -1) {
            ppsString = "N/A";
        } else {
            ppsString = response.substring(idxPpsStart + parserStrings.prevStart.length());
            int idxPpsMid = ppsString.indexOf(parserStrings.generalMid);
            if (idxPpsMid == -1) {
                ppsString = "N/A";
            } else {
                ppsString = ppsString.substring(idxPpsMid + parserStrings.generalMid.length());
                int idxPpsStop = ppsString.indexOf(parserStrings.prevStop);
                if (idxPpsStop == -1) {
                    ppsString = "N/A";
                } else {
                    ppsString = ppsString.substring(0, idxPpsStop);
                }
            }
        }
        return parseFloatOrNA(ppsString);
    }

    @NonNull
    private String parseFullName(String symbol, @NonNull String response, @NonNull ParserStrings parserStrings ) {

        String nameStart1 = parserStrings.nameStartA + symbol + parserStrings.nameMidA;
        String nameStart2 = parserStrings.nameStartB + symbol + parserStrings.nameMidB;

        String nameString1 = "-";
        String nameString2 = "-";
        int idxNameStart = response.indexOf(nameStart1);
        if (idxNameStart != -1) {
            nameString1 = response.substring(idxNameStart + nameStart1.length());
            int idxNameStop = nameString1.indexOf(parserStrings.nameEnd1);
            if (idxNameStop != -1) {
                nameString1 = nameString1.substring(0, idxNameStop).replaceFirst("&amp;", "&");
            }
        }

        if (nameString1.equals(nameString2)) {
            idxNameStart = response.indexOf(nameStart2);
            if (idxNameStart != -1) {
                nameString2 = response.substring(idxNameStart + nameStart2.length());
                int idxNameStop = nameString2.indexOf(parserStrings.nameEnd2);
                if (idxNameStop != -1) {
                    nameString2 = nameString2.substring(0, idxNameStop).replaceFirst("&amp;", "&");
                }
            }
        }

        String fullName = nameString1;
        if (nameString2.length() > nameString1.length()) {
            fullName = nameString2;
        }else {
            String msg = "Using string 1 for " + symbol;
            Log.d("foo", msg);
        }
        return fullName;
    }

    private float parseFloatOrNA(String field) {
        float parsedFloat = 0.0f;
        if (!field.contains("N/A")) {
            parsedFloat = Float.parseFloat(field.replace(",",""));
        }
        return parsedFloat;
    }
}
