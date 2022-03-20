package com.adaskin.android.watcher8;

import org.junit.Test;

import static org.junit.Assert.*;

import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.ParserStrings;
import com.adaskin.android.watcher8.utilities.Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


public class Test_parseYAHOOResponse {
    @Test
    public void Test_AEP()
    {
        Parsers p = Parsers.getInstance();

        StockQuote quote = new StockQuote("AEP", 0.0f, 0.0f, 900.0f);

        ParserStrings pStrings= getParserStrings();
        String response = getResponseString("Decoy_AEP_220316.txt");

        p.parseYAHOOResponse2(pStrings, quote, response);

        String expectedFullName = "American Electric Power Company";
        double expectedYrMin = 80.22f;
        double expectedYrMax = 98.15f;
        double expectedOpinion = 2.2f;
        double expectedPPS = 95.07f;
        double expectedDailyChange = -1.1f;
//        double expectedDiv = 3.12f;

        assertEquals(expectedFullName, quote.mFullName);
        assertEquals(expectedYrMin, quote.mYrMin, 0.001);
        assertEquals(expectedYrMax, quote.mYrMax, 0.001);
        assertEquals(expectedOpinion, quote.mAnalystsOpinion, 0.01);
        assertEquals(expectedPPS, quote.mPPS, 0.001);
        assertEquals(expectedDailyChange, quote.mPctChangeSinceLastClose, 0.05);
    //    assertEquals(expectedDiv, quote.mDivPerShare, 0.0001);
    }

    @Test
    public void Test_BCHYX()
    {
        Parsers p = Parsers.getInstance();

        StockQuote quote = new StockQuote("BCHYX", 0.0f, 0.0f, 900.0f);

        ParserStrings pStrings= getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        p.parseYAHOOResponse2(pStrings, quote, response);

        String expectedFullName = "American Century California High Yield Municipal Fund Investor Class";
        double expectedPPS = 10.53f;
        double expectedDailyChange = 0.1f;

        assertEquals(expectedFullName, quote.mFullName);
        assertEquals(expectedPPS, quote.mPPS, 0.001);
        assertEquals(expectedDailyChange, quote.mPctChangeSinceLastClose, 0.01);
    }



    public ParserStrings getParserStrings()
    {
        ParserStrings p = new ParserStrings();
        p.invalidSymbolMarker = "content=\"Symbol Lookup from Yahoo Finance\"";
        p.nameStartA = "Lh(18px)\" data-reactid=\"7\">";
        p.nameMidA = " - ";
        p.nameEnd1 = "<";
        p.nameStartB = "content=\"";
        p.nameMidB =  ", ";
        p.nameEnd2 =  ", ";
        p.ppsStart = "\"qsp-price\" data-field=\"regularMarketPrice\"";
        p.midPattern =  "value=\"";
        p.stopPattern = "\"";
        p.divStart = "\"dividendRate\":{\"raw\":";
        p.yrStart = "FIFTY_TWO_WK_RANGE-value";
        p.generalMid = ">";
        p.yrStop =  "</td>";
        p.analStart = "\"recommendationMean\":{\"raw\":";
        p.analStop = ",\"fmt\"";
        p.prevStart = "data-test=\"PREV_CLOSE-value\"";
        p.prevStop = "</td>";

        return p;
    }

    public String getResponseString(String decoyFileName)
    {
        StringBuilder sb = new StringBuilder();
        File file;
        file = new File("D:\\AndroidstudioProjects\\Watcher8\\app\\src\\test\\java\\com\\adaskin\\android\\watcher8", decoyFileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            Reader isReader = new InputStreamReader(fis);
            BufferedReader buffReader = new BufferedReader(isReader);
            String line;
            while ((line = buffReader.readLine()) != null)
                sb.append(line);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
