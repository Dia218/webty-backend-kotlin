export const DB_CONFIG = {
    host: __ENV.DB_HOST || "localhost",
    port: __ENV.DB_PORT || "3307",
    user: __ENV.DB_USER || "k6_user",
    password: __ENV.DB_PASSWORD || "k6_password",
    database: __ENV.DB_NAME || "webty_k6_db",
};
