package com.peolly.securityserver.usermicroservice.services;


import com.peolly.securityserver.usermicroservice.model.AuthDeviceInfo;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.repositories.AuthDeviceInfoRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthDeviceInfoService {

    @Value("${onlinestore.version}")
    private String projectVersion;

    private final AuthDeviceInfoRepository authDeviceInfoRepository;

    @Transactional
    public void saveSession(User user) {
        AuthDeviceInfo authDeviceInfo = AuthDeviceInfo.builder()
                .deviceName(System.getProperty("os.name") + System.getProperty("os.version"))
                .applicationVersion(projectVersion)
                .authTime(LocalDateTime.now())
                .ipAddress(getExternalIp())
                .location(getLocationByIp(getExternalIp()))
                .client(user)
                .build();
        authDeviceInfoRepository.save(authDeviceInfo);
    }

    @Transactional(readOnly = true)
    public List<AuthDeviceInfo> getAllSessions(User user) {
        return authDeviceInfoRepository.findAllByClient(user);
    }

    public String getLocationByIp(InetAddress ipAddress) {
        String ip = ipAddress.getHostAddress();
        String apiUrl = "http://ip-api.com/json/" + ip;

        try {
            JSONObject json = getJsonObject(apiUrl);
            String city = json.getString("city");
            String country = json.getString("country");

            return country + ", " + city;
        } catch (Exception e) {
            return "Unknown location";
        }
    }

    private static JSONObject getJsonObject(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONObject(response.toString());
    }

    public static InetAddress getExternalIp() {
        try {
            URL url = new URL("http://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ip = in.readLine();
            in.close();

            return InetAddress.getByName(ip);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
