package com.rtometer.data;

import com.rtometer.data.db.BankHoliday;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BankHolidayFetcher {

    private static final String BASE = "https://date.nager.at/api/v3/PublicHolidays/%d/%s";

    public static List<BankHoliday> fetch(String countryCode, int year) throws Exception {
        HttpURLConnection conn = (HttpURLConnection)
                new URL(String.format(BASE, year, countryCode)).openConnection();
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);

        int status = conn.getResponseCode();
        if (status != 200) {
            conn.disconnect();
            throw new Exception("Nager.Date HTTP " + status + " for " + countryCode + "/" + year);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        } finally {
            conn.disconnect();
        }

        List<BankHoliday> result = new ArrayList<>();
        JSONArray arr = new JSONArray(sb.toString());
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String localName = o.optString("localName", "").trim();
            String name     = o.optString("name", "").trim();

            BankHoliday bh = new BankHoliday();
            bh.date = LocalDate.parse(o.getString("date"));
            bh.name = !localName.isEmpty() ? localName : (!name.isEmpty() ? name : null);
            bh.countryCode = countryCode;
            bh.year = year;
            result.add(bh);
        }
        return result;
    }
}
