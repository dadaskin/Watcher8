package com.adaskin.android.watcher8;

import org.junit.Test;

import static org.junit.Assert.*;

import com.adaskin.android.watcher8.utilities.ParserStrings;
import com.adaskin.android.watcher8.utilities.Parsers;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


public class Test_parseYAHOOResponse {
    @Test
    public void Test_AEP_parseFullName()
    {
        Volley.newRequestQueue()
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_AEP_220316.txt");

        String expected = "American Electric Power Company, Inc.";
        String actual = p.parseFullName("AEP", response, pStrings);

        assertEquals("AEP FullName", expected, actual);
    }
    @Test
    public void Test_TWST_parseFullName()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        String expected = "Twist Bioscience Corporation";
        String actual = p.parseFullName("TWST", response, pStrings);

        assertEquals("TWST FullName", expected, actual);
    }
    @Test
    public void Test_AEP_parsePPS()
    {
       Parsers p = Parsers.getInstance();
       ParserStrings pStrings = getParserStrings();
       String response = getResponseString("Decoy_AEP_220316.txt");

       float expected = 95.07f;
       float actual = p.parseCurrentPrice(response, pStrings);

       assertEquals("AEP current price", expected, actual, 0.005);
    }

    @Test
    public void Test_TWST_parsePPS()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        float expected = 47.52f;
        float actual = p.parseCurrentPrice(response, pStrings);

        assertEquals("TWST current price", expected, actual, 0.005);
    }

    @Test
    public void Test_AEP_parsePrevClose()
    {
       Parsers p = Parsers.getInstance();
       ParserStrings pStrings = getParserStrings();
       String response = getResponseString("Decoy_AEP_220316.txt");

       float expected = 96.11f;
       float actual = p.parsePreviousClosePrice(response, pStrings);

       assertEquals("AEP previous close", expected, actual, 0.005);
    }

    @Test
    public void Test_TWST_parsePrevClose()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        float expected = 46.98f;
        float actual = p.parsePreviousClosePrice(response, pStrings);

        assertEquals("TWST previous close", expected, actual, 0.005);
    }

    @Test
    public void Test_AEP_parseMinRange()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_AEP_220316.txt");

        float expected = 80.22f;
        Parsers.PriceRange actualObject = p.parsePriceRange(response, pStrings);
        float actual = actualObject.minimum;

        assertEquals("AEP minimum range", expected, actual, 0.005);
    }

    @Test
    public void Test_TWST_parseMinRange()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        float expected = 38.08f;
        Parsers.PriceRange actualObject = p.parsePriceRange(response, pStrings);
        float actual = actualObject.minimum;

        assertEquals("TWST minimum range", expected, actual, 0.005);
    }

    @Test
    public void Test_AEP_parseMaxRange()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_AEP_220316.txt");

        float expected = 98.15f;
        Parsers.PriceRange actualObject = p.parsePriceRange(response, pStrings);
        float actual = actualObject.maximum;

        assertEquals("AEP maximum range", expected, actual, 0.005);
    }

    @Test
    public void Test_TWST_parseMaxRange()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        float expected = 150.25f;
        Parsers.PriceRange actualObject = p.parsePriceRange(response, pStrings);
        float actual = actualObject.maximum;

        assertEquals("TWST maximum range", expected, actual, 0.005);
    }

    @Test
    public void Test_AEP_parseDiv()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_AEP_220316.txt");

        float expected = 3.12f;
        float actual = p.parseDividend(response, pStrings);

        assertEquals("AEP dividend", expected, actual, 0.005);
    }

    @Test
    public void Test_TWST_parseDiv()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        float expected = 0.00f;
        float actual = p.parseDividend(response, pStrings);

        assertEquals("TWST dividend", expected, actual, 0.005);
    }

    @Test
    public void Test_AEP_Opinion()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_AEP_220316.txt");

        float expected = 2.2f;
        float actual = p.parseAnalystsOpinion(response, pStrings);

        assertEquals("AEP analyst option", expected, actual, 0.05);
    }

    @Test
    public void Test_TWST_Opinion()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_TWST_220324.txt");

        float expected = 2.3f;
        float actual = p.parseAnalystsOpinion(response, pStrings);

        assertEquals("TWST analyst option", expected, actual, 0.05);
    }

    @Test
    public void Test_BCHYX_parseFullName()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        String expected = "American Century California High Yield Municipal Fund Investor Class";
        String actual = p.parseFullName("BCHYX", response, pStrings);

        assertEquals("BCHYX FullName", expected, actual);
    }

    @Test
    public void Test_BCHYX_parsePPS()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        float expected = 10.53f;
        float actual = p.parseCurrentPrice(response, pStrings);

        assertEquals("BCHYX current price", expected, actual, 0.005);
    }

    @Test
    public void Test_BCHYX_parsePrevClose()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        float expected = 10.52f;
        float actual = p.parsePreviousClosePrice(response, pStrings);

        assertEquals("BCHYX previous close", expected, actual, 0.005);
    }

    @Test
    public void Test_BCHYX_parseMinRange()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        float expected = 0.0f;
        Parsers.PriceRange actualObject = p.parsePriceRange(response, pStrings);
        float actual = actualObject.minimum;

        assertEquals("BCHYX minimum range", expected, actual, 0.005);
    }

    @Test
    public void Test_BCHYX_parseMaxRange()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        float expected = 0.0f;
        Parsers.PriceRange actualObject = p.parsePriceRange(response, pStrings);
        float actual = actualObject.maximum;

        assertEquals("BCHYX maximum range", expected, actual, 0.005);
    }

    @Test
    public void Test_BCHYX_parseDiv()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        float expected = 0.0f;
        float actual = p.parseDividend(response, pStrings);

        assertEquals("BCHYX dividend", expected, actual, 0.005);
    }

    @Test
    public void Test_BCHYX_Opinion()
    {
        Parsers p = Parsers.getInstance();
        ParserStrings pStrings = getParserStrings();
        String response = getResponseString("Decoy_BCHYX_220317.txt");

        float expected = 0.0f;
        float actual = p.parseAnalystsOpinion(response, pStrings);

        assertEquals("BCHYX analyst option", expected, actual, 0.05);
    }



    public ParserStrings getParserStrings()
    {
        ParserStrings p = new ParserStrings();
        p.invalidSymbolMarker = "content=\"Symbol Lookup from Yahoo Finance\"";
        p.nameStart = "content=\"";
        p.nameMid =  ", ";
        p.nameOffset = 0;
        p.nameStop =  ", ";

        p.ppsStart = "\"qsp-price\" data-field=\"regularMarketPrice\"";
        p.ppsMid =  "value=\"";
        p.ppsOffset = 0;
        p.ppsStop = "\"";

        p.divStart = "DIVIDEND_AND_YIELD-value\">";
        p.divMid = "";
        p.divOffset = 0;
        p.divStop = " (";

        p.yrStart = "FIFTY_TWO_WK_RANGE-value";
        p.yrMid = ">";
        p.yrOffset = 0;
        p.yrStop =  "</td>";

        p.analStart = "\"recommendationMean\":{\"raw\":";
        p.analMid = "";
        p.analOffset = 0;
        p.analStop = ",\"fmt\"";

        p.prevStart = "data-test=\"PREV_CLOSE-value\">";
        p.prevMid = "";
        p.prevOffset = 0;
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
