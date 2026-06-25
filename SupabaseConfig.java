package com.kartonplus.config;

public class SupabaseConfig {
    public static final String SUPABASE_URL = "https://vcwknxtiuvanpcmyzkrp.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZjd2tueHRpdXZhbnBjbXl6a3JwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODE0Mzk4MTgsImV4cCI6MjA5NzAxNTgxOH0.ErJ62eBz-W8Z_DBEx_w1CFi21fM2JfCujL1rm6BsPS4";

    public static String getTableUrl(String table) {
        return SUPABASE_URL + "/rest/v1/" + table;
    }
}