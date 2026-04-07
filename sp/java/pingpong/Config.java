import org.json.JSONObject;

public class Config {
    public static final String CA_CERT = "-----BEGIN CERTIFICATE-----\n" +
        "MIIDhjCCAm6gAwIBAgIUNPzDzKDcB8zeiIEKXE011wd2iuwwDQYJKoZIhvcNAQEL\n" +
        "BQAwXTELMAkGA1UEBhMCU0UxEDAOBgNVBAgMB1VQUFNBTEExFDASBgNVBAcMC8OD\n" +
        "wpZyZWdydW5kMQwwCgYDVQQKDAM0REExGDAWBgNVBAMMDzEwMy4xNzcuMjQ5LjEx\n" +
        "ODAeFw0yNjA0MDUxMjU4NTdaFw0yNzA0MDUxMjU4NTdaMF0xCzAJBgNVBAYTAlNF\n" +
        "MRAwDgYDVQQIDAdVUFBTQUxBMRQwEgYDVQQHDAvDg8KWcmVncnVuZDEMMAoGA1UE\n" +
        "CgwDNERBMRgwFgYDVQQDDA8xMDMuMTc3LjI0OS4xMTgwggEiMA0GCSqGSIb3DQEB\n" +
        "AQUAA4IBDwAwggEKAoIBAQCvdcQV3L/1n+6G5fGVw39WOvGm8s3a0GA6k8CI1wmF\n" +
        "o6Buvjqrbbafyj/qOXAfsufu5JP6Ny9Kh6rXQDOsKjxQoOlrOUhb1ZBrI8huuyM2\n" +
        "KU68ocXf3r0c2+e/hgoEujQqUmhZAzWGR/SAfpZt8B2k3GO2CmDQ4paj1Bh8bpJJ\n" +
        "H9vDquga7QS/fyiOfoaLCIA1PBFYeyIkSH9JyZ9yHHKvhPqvZPYbq+UMniaSDaqN\n" +
        "8JdII0TCTqwPDv9TDOdxGCF+YacaPFFPQiCAvx+L3p2yX3yCF1dLEljeArSQl+l1\n" +
        "WCsjqA3z332mEk96cHyirDChVDIO9lGy2e9xbvtl4pqbAgMBAAGjPjA8MBsGA1Ud\n" +
        "EQQUMBKHBMKjssqHBJoM9Y6HBGex+XYwHQYDVR0OBBYEFBkOEEYjQw/k7Gt1pP0W\n" +
        "k25czA4TMA0GCSqGSIb3DQEBCwUAA4IBAQABctWTv4yUokoDfbZkEYsTZnlLpHoU\n" +
        "z6DhuqDr65eaPJcJK+ozmSU1TOHG5oWrvDgw9dM3IlSdOuLwXVKHR7hffqEX54Yu\n" +
        "q27S3RcQrjDh95YMPLhSwfmeW1HyuF2oE5CM/eQLDBXTXBFfXx5qOxuS7SjkkFI+\n" +
        "OczgAWtv+lmP7xMHlgZJ5UGR2oh/BZlLVTqOpPKZDXCt2hxqmFPUKrjNymcQjvuY\n" +
        "NHKYJgb36zYHHQLbD/cWea5Md9VyZ/1aA7xr/jGXZaJU6tUdTDDQ4MlgyUaBHYh/\n" +
        "EKQqNgEi8YLsgqSxKRJ4xJfjD/Eg13fAaUP6vfuc9fHj4TUSbtiMpLV4\n" +
        "-----END CERTIFICATE-----";

    public static JSONObject getSystemConfig() {
        JSONObject config = new JSONObject();
        config.put("host", "wss://103.177.249.118:30050");
        
        JSONObject credential = new JSONObject();
        credential.put("accesskey", "ACSKEY");
        credential.put("password", "PSSWRD");
        credential.put("address", "name.maya.4da");
        credential.put("context", new JSONObject());
        config.put("credential", credential);

        JSONObject peers = new JSONObject();
        peers.put("pingpong", "pingpong.maya.4da");
        config.put("peers", peers);

        return config;
    }
}