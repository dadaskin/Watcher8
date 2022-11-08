package com.adaskin.android.watcher8.utilities;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
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

    public boolean parseYAHOOResponse2(@NonNull ParserStrings parserStrings, final StockQuote quote, @NonNull String response){
        // Make sure Invalid Symbol Marker isn't present
        int idxInvalid = response.indexOf(parserStrings.invalidSymbolMarker);
        if (idxInvalid != -1) {
            return false;
        }

        quote.mFullName = parseFullName(quote.mSymbol, response, parserStrings);
        quote.mPPS = parseCurrentPrice(response, parserStrings);
        quote.mDivPerShare = parseDividend(response, parserStrings);

        PriceRange yrRange = parsePriceRange(response, parserStrings);
        quote.mYrMin = yrRange.minimum;
        quote.mYrMax = yrRange.maximum;

        quote.mAnalystsOpinion = parseAnalystsOpinion(response, parserStrings);
        quote.mPrevClose = parsePreviousClosePrice(response, parserStrings);
        quote.compute(quote.mPrevClose);

        return true;
    }

    @NonNull
    public PriceRange parsePriceRange(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String yrString;
        int idxYrStart = response.indexOf(parserStrings.yrStart);
        if (idxYrStart == -1) {
            yrString = "0 - 0";
        } else {
            yrString = response.substring(idxYrStart + parserStrings.yrStart.length());
            int idxYrMid = yrString.indexOf(parserStrings.yrMid);
            if (idxYrMid == -1) {
                yrString = "0 - 0";
            } else {
                yrString = yrString.substring(idxYrMid + parserStrings.yrMid.length());
                int idxYrStop = yrString.indexOf(parserStrings.yrStop);
                if (idxYrStop == -1) {
                    yrString = "0 - 0";
                } else {
                    yrString = yrString.substring(0, idxYrStop);
                }
            }
        }
        String yrMinString = yrString.substring(0, yrString.indexOf(" -"));
        String yrMaxString = yrString.substring(yrString.indexOf("- ")+2);

        return new PriceRange(parseFloatOrNA(yrMinString), parseFloatOrNA(yrMaxString));
    }

    public float parseAnalystsOpinion(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String analString;
        int idxAnalStart = response.indexOf(parserStrings.analStart);
        if (idxAnalStart == -1) {
            analString = "N/A";
        } else {
            analString = response.substring(idxAnalStart + parserStrings.analStart.length());
            int idxAnalStop = analString.indexOf(parserStrings.analStop);
            if (idxAnalStop == -1) {
                analString = "N/A";
            } else {
                analString = analString.substring(0, idxAnalStop);
            }
        }
        return parseFloatOrNA(analString);
    }

    public float parseCurrentPrice(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String s;
        int idxStart = response.indexOf(parserStrings.ppsStart);
        if (idxStart == -1) {
            s = "N/A";
        } else {
            s = response.substring(idxStart + parserStrings.ppsStart.length());
            int idxMid = s.indexOf(parserStrings.ppsMid);
            if (idxMid == -1) {
                s = "N/A";
            } else {
                s = s.substring(idxMid + parserStrings.ppsMid.length());
                int idxStop = s.indexOf(parserStrings.ppsStop);
                if (idxStop == -1) {
                    s = "N/A";
                } else {
                    s = s.substring(0, idxStop);
                }
            }
        }
        return parseFloatOrNA(s);
    }

    public float parseDividend(@NonNull String response, @NonNull ParserStrings parserStrings) {

        String patStart = parserStrings.divStart;
        String patMid = parserStrings.divMid;
        String patStop = parserStrings.divStop;

        String ppsString;
        int idxPpsStart = response.indexOf(patStart);
        if (idxPpsStart == -1) {
            ppsString = "N/A";
        } else {
            ppsString = response.substring(idxPpsStart + patStart.length());
            int idxPpsMid = ppsString.indexOf(patMid);
            if (idxPpsMid == -1) {
                ppsString = "N/A";
            } else {
                ppsString = ppsString.substring(idxPpsMid + patMid.length());
                int idxPpsStop = ppsString.indexOf(patStop);
                if (idxPpsStop == -1) {
                    ppsString = "N/A";
                } else {
                    ppsString = ppsString.substring(0, idxPpsStop);
                }
            }
        }
        return parseFloatOrNA(ppsString);
    }

    public float parsePreviousClosePrice(@NonNull String response, @NonNull ParserStrings parserStrings) {
        String prev;
        int idxPpsStart = response.indexOf(parserStrings.prevStart);
        if (idxPpsStart == -1) {
            prev = "N/A";
        } else {
            prev = response.substring(idxPpsStart + parserStrings.prevStart.length());
            int idxPpsMid = prev.indexOf(parserStrings.prevMid);
            if (idxPpsMid == -1) {
                prev = "N/A";
            } else {
                prev = prev.substring(idxPpsMid + parserStrings.prevMid.length());
                int idxPpsStop = prev.indexOf(parserStrings.prevStop);
                if (idxPpsStop == -1) {
                    prev = "N/A";
                } else {
                    prev = prev.substring(0, idxPpsStop);
                }
            }
        }
        return parseFloatOrNA(prev);
    }

    @NonNull
    public String parseFullName(String symbol, @NonNull String response, @NonNull ParserStrings parserStrings ) {
        String start = parserStrings.nameStart + symbol + parserStrings.nameMid;
        String name = "-";
        int idxNameStart;

        String end = parserStrings.nameStop + symbol;

        idxNameStart = response.indexOf(start);
        if (idxNameStart != -1) {
            name = response.substring(idxNameStart + start.length());
            int idxNameStop = name.indexOf(end);
            if (idxNameStop != -1)
                name = name.substring(0, idxNameStop).replaceFirst("&amp;", "&");
        }
        return name;
    }

    private float parseFloatOrNA(@NonNull String field) {
        float parsedFloat = 0.0f;
        if (!field.contains("N/A")) {
            parsedFloat = Float.parseFloat(field.replace(",",""));
        }
        return parsedFloat;
    }

    public static class PriceRange
    {
        public float minimum;
        public float maximum;

        public PriceRange(float minimum, float maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }


}
