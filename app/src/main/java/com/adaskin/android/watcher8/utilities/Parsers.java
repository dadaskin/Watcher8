package com.adaskin.android.watcher8.utilities;

import android.content.Context;
import android.os.Environment;
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
        String invalidSymbolMarker = mParserStrings.invalidSymbolMarker;
        String nameStartA = mParserStrings.nameStartA;
        String nameMidA = mParserStrings.nameMidA;
        String nameEnd1 = mParserStrings.nameEnd1;
        String nameStartB = mParserStrings.nameStartB;
        String nameMidB = mParserStrings.nameMidB;
        String nameEnd2 = mParserStrings.nameEnd2;
        String ppsStart = mParserStrings.ppsStart;
        String midPattern = mParserStrings.midPattern;
        String stopPattern = mParserStrings.stopPattern;
        String divStart = mParserStrings.divStart;
        String yrStart = mParserStrings.yrStart;
        String generalMid =mParserStrings.generalMid;
        String yrStop = mParserStrings.yrStop;
        String analStart = mParserStrings.analStart;
        String analStop = mParserStrings.analStop;
        String prevStart = mParserStrings.prevStart;
        String prevStop = mParserStrings.prevStop;

        String nameStart1 = nameStartA + quote.mSymbol + nameMidA;
        String nameStart2 = nameStartB + quote.mSymbol + nameMidB;

        // Make sure Invalid Symbol Marker isn't present
        int idxInvalid = response.indexOf(invalidSymbolMarker);
        if (idxInvalid != -1) {
            return false;
        }

        // Parse out Full Name
        String nameString1 = "-";
        String nameString2 = "-";
        int idxNameStart = response.indexOf(nameStart1);
        if (idxNameStart == -1) {
            quote.mFullName = "N/A";
        }else {
            nameString1 = response.substring(idxNameStart + nameStart1.length());
            int idxNameStop = nameString1.indexOf(nameEnd1);
            if (idxNameStop == -1) {
                quote.mFullName = "N/A";
            }
            nameString1 = nameString1.substring(0, idxNameStop).replaceFirst("&amp;", "&");
        }

        if (nameString1.equals(nameString2)) {
            idxNameStart = response.indexOf(nameStart2);
            if (idxNameStart == -1) {
                quote.mFullName = "N/A";
            } else {
                nameString2 = response.substring(idxNameStart + nameStart2.length());
                int idxNameStop = nameString2.indexOf(nameEnd2);
                if (idxNameStop == -1) {
                    quote.mFullName = "N/A";
                }
                nameString2 = nameString2.substring(0, idxNameStop).replaceFirst("&amp;", "&");
            }
        }
        quote.mFullName = nameString1;
        if (nameString2.length() > nameString1.length()) {
            quote.mFullName = nameString2;
        }else {
            String msg = "Using string 1 for " + quote.mSymbol;
            Log.d("foo", msg);
        }

        // Parse out PPS string
        String ppsString;
        int idxPpsStart = response.indexOf(ppsStart);
        if (idxPpsStart == -1) {
            ppsString = "N/A";
        } else {
            ppsString = response.substring(idxPpsStart + ppsStart.length());
            int idxPpsMid = ppsString.indexOf(midPattern);
            if (idxPpsMid == -1) {
                ppsString = "N/A";
            } else {
                ppsString = ppsString.substring(idxPpsMid + midPattern.length());
                int idxPpsStop = ppsString.indexOf(stopPattern);
                if (idxPpsStop == -1) {
                    ppsString = "N/A";
                } else {
                    ppsString = ppsString.substring(0, idxPpsStop);
                }
            }
        }
        quote.mPPS = parseFloatOrNA(ppsString);

        // Parse out Dividend string
        String divString;
        int idxDivStart = response.indexOf(divStart);
        if (idxDivStart == -1) {
            divString = "N/A";
        } else {
            divString = response.substring(idxDivStart + divStart.length());
            int idxDivMid = divString.indexOf(midPattern);
            if (idxDivMid == -1) {
                divString = "N/A";
            } else {
                divString = divString.substring(idxDivMid + midPattern.length());
                int idxDivStop = divString.indexOf(stopPattern);
                if (idxDivStop == -1) {
                    divString = "N/A";
                } else {
                    divString = divString.substring(0, idxDivStop);
                }
            }
        }
        quote.mDivPerShare = parseFloatOrNA(divString);

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
        quote.mAnalystsOpinion = parseFloatOrNA(analString);

        // Parse out previous close pps
        String prevString;
        int idxPrevStart = response.indexOf(prevStart);
        if (idxPrevStart == -1) {
            prevString = "N/A";
        } else {
            prevString = response.substring(idxPrevStart + prevStart.length());
            int idxPrevMid = prevString.indexOf(generalMid);
            if (idxPrevMid == -1) {
                prevString = "N/A";
            } else {
                prevString = prevString.substring(idxPrevMid+ generalMid.length());
                int idxPrevStop = prevString.indexOf(prevStop);
                if (idxPrevStop == -1) {
                    prevString = "N/A";
                } else {
                    prevString = prevString.substring(0, idxPrevStop);
                }
            }
        }
        quote.compute(parseFloatOrNA(prevString));

        return true;
    }

    private float parseFloatOrNA(String field) {
        float parsedFloat = 0.0f;
        if (!field.contains("N/A")) {
            parsedFloat = Float.parseFloat(field.replace(",",""));
        }
        return parsedFloat;
    }
}
