keytool -genkeypair -alias certificatekey -keyalg RSA -validity 365 -keystore .keystore
keytool -export -alias certificatekey -keystore .keystore -rfc -file server.cer
keytool -import -alias certificatekey -file server.cer -keystore .truststore