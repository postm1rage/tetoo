package com.example.tetoo.util;

public final class Constants {

    private Constants() {
        // запрещаем создание экземпляров
    }

    // === DATABASE (Supabase / PostgreSQL) ===
    public static final String DB_URL =
            "jdbc:postgresql://db.eruzshyrvfwjcehpwjmp.supabase.co:5432/postgres?sslmode=require";

    public static final String DB_USER = "postgres";

    public static final String DB_PASSWORD = "forProject3!";
}
