package com.adaskin.android.watcher8.utilities;

import com.adaskin.android.watcher8.models.StockQuote;


@SuppressWarnings("SpellCheckingInspection")
class Parsers {

    public static boolean parseYAHOOResponse(final StockQuote quote, String response){
        String invalidSymbolMarker = "content=\"Symbol Lookup from Yahoo Finance\"";
        String nameStart1 = "Lh(18px)\" data-reactid=\"7\">" + quote.mSymbol + " - ";
        String nameEnd1 = "<";
        String nameStart2 = "content=\"" + quote.mSymbol + ", ";
        String nameEnd2 = ", ";
        String ppsStart = "\"regularMarketPrice\":{\"raw\":";
        String midPattern = ",\"fmt\":\"";
        String stopPattern = "\"}";
        String divStart = "\"dividendRate\":{\"raw\":";
        String yrStart = "FIFTY_TWO_WK_RANGE-value";
        String generalMid =">";
        String yrStop = "</td>";
        String analStart="\"recommendationMean\":{\"raw\":";
        String analStop=",\"fmt\"";
        String prevStart = "<span class=\"Trsdu(0.3s) \" data-reactid=\"";
        String prevStop = "</span>";

        // Make sure Invalid Symbol Marker isn't present
        int idxInvalid = response.indexOf(invalidSymbolMarker);
        if (idxInvalid != -1) {
            return false;
        }

        // Parse out Full Name
        String nameString1 = "";
        String nameString2 = "";
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

        if (nameString1.length() == 0) {
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
        String yrMaxString = yrString.substring(yrString.indexOf("- ")+2, yrString.length());
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

//    private static String findItemTrimString(String input, String startPattern, int startOffset, String stopPattern, StockQuote quote, int itemNumber) {
//        String remainder = input;
//        int startIdx = input.indexOf(startPattern);
//        if (startIdx != -1) {
//            String sub = input.substring(startIdx + startPattern.length() + startOffset);
//            int stopIdx = sub.indexOf(stopPattern);
//            if (stopIdx != -1)
//            {
//                String itemString = sub.substring(0, stopIdx);
//                remainder = sub.substring(stopIdx + stopPattern.length());
//                switch(itemNumber)  {
//                    case 0: quote.mFullName = itemString.replace("&amp;","&");
//                        break;
//                    case 1: quote.mPPS = parseFloatOrNA(itemString);
//                        break;
//                    case 2: // Do stuff involving previous value
//                        float previousClose = parseFloatOrNA(itemString);
//                        quote.compute(previousClose);
//                        break;
//                    case 3: // Parse range string, add pieces to quote
//                        String yrMinString = itemString.substring(0, itemString.indexOf(" -"));
//                        String yrMaxString = itemString.substring(itemString.indexOf("- ")+2, itemString.length());
//                        quote.mYrMin = parseFloatOrNA(yrMinString);
//                        quote.mYrMax = parseFloatOrNA(yrMaxString);
//                        break;
//                    case 4: quote.mDivPerShare = parseFloatOrNA(itemString);
//                        break;
//                }
//            }
//        }
//        return remainder;
//    }

    private static float parseFloatOrNA(String field) {
        float parsedFloat = 0.0f;
        if (!field.contains("N/A")) {
            parsedFloat = Float.valueOf(field.replace(",",""));
        }
        return parsedFloat;
    }
}
