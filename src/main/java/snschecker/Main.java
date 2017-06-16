package snschecker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by APXEOLOG on 12.06.2017.
 */
public class Main {
    private static int[] CAPTWO = new int[] {12707,12714,12715,12716,12717,12718,12719,12720,12721,12722,12723,12724,12725,12726,12727,12728,12729,12759,12761,12763,12772,12773,12774,12777,12784,12785,12807,12809,12813,12820,12832,12833,12838,12842,12845,12846,12847,12850,12851,12852,12856,12857,12905,12909,12910,12912,12916,12917,12926,12927,12928,12929,12930,12931,12936,12937,12939,12941,12945,12946,12947,12948,13112,13114,13115,13118,13123,13125,13126,13131,13132,13133,13135,13137,13138,13141,13142,13144,13145,13146,13147,13156,13157,13158,13162,13164,13167,13174,13201,13202,13203,13209,13210,13211,13214,13217,13219,13222,13223,13226,13228,13229,13230,13231,13233,13234,13235,13240,13241,13243,13244,13405,13406,13408,13411,13414,13416,13417,13419,13420,13583,13586,13588,13787,13791,13950,13951,17293,19000,20080,20081,36988};
    private static int[] CAP = new int[] {27992,31277,31278,31431,31432,32271,32272,32273,32691,33943,33946,33970,33973,33974,33975,33976,33977,34550,34551,34553,34753,34754,34755,34760,34761,34762,34763,35133,35134,35135,35319,37527,37528,37529,37530,37531,37532,37538,37539,37540,37542,37544,37545,37599,37726,37727,37533,37534,37535,37536,37537,29426,32271,32272,32273,33946,33970,33974,33976,34550,34753,34756,34759,34761,34763,35134,35501,37528,37539,37728,37729,41929,41930,41931,42269,43161,43515,50643,50645,50976,52445,52603,52604,52605,52606,52756,52759,52761,52762,53845,54166,54167,54168,54170,54171,54172,54173,54174,54175,54176,54177,54624,54627,54633,54634,54635,54636,54637,55553,55554,55555,55706,54169,59338};

    public static void main(String[] args) throws UnirestException, IOException {
        int[] PV = CAP;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String login = args[0];
        String password = args[1];

        BasicCookieStore cookieStore = new BasicCookieStore();
        Unirest.setHttpClient(HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build());

        HttpResponse<JsonNode> loginResponse = Unirest.post("https://aline.devfactory.com/api/v3/login")
                .field("username", login)
                .field("password", password)
                .asJson();

        HttpResponse<String> graylogLoginResponse = Unirest.post("https://graylog2.devfactory.com/login")
                .field("destination", "/startpage")
                .field("username", login)
                .field("password", password)
                .field("submit", "")
                .asString();

        System.out.printf("Job Id\tProduct Version\tStatus\tDate\tBuild Status\n");
        for (int i = 0; i < PV.length; i++) {
            HttpResponse<String> jobResponse = Unirest.post("https://aline.devfactory.com/api/v3/jobs")
                    .header("dfauthtoken", loginResponse.getHeaders().get("dfauthtoken").get(0))
                    .field("limit", 3)
                    .field("offset", 0)
                    .field("productVersionId", PV[i])
                    .asString();

            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<HashMap<String, Object>> result =
                    objectMapper.readValue(jobResponse.getBody(), new TypeReference<ArrayList<HashMap<String, Object>>>() {});

            Integer jobId = null;
            String updated = null;
            LocalDate date = null;
            String status = null;
            for (HashMap<String, Object> job : result) {
                HashMap<String, Object> buildInfo = (HashMap<String, Object>) job.get("buildResult");
                HashMap<String, Object> metrics = (HashMap<String, Object>) job.get("metricsResult");
                if (status == null) {
                    status = (String) buildInfo.get("serviceResult");
                }
                if (buildInfo.get("serviceResult").equals("accepted") && !metrics.get("serviceResult").equals("error")) {
                    HashMap<String, Object> jobInfo = (HashMap<String, Object>) job.get("job");
                    jobId = (Integer) jobInfo.get("jobId");
                    updated = (String) jobInfo.get("updated");
                    date = LocalDate.parse(updated.substring(0, 10), dateTimeFormatter);
                    status = (String) buildInfo.get("serviceResult");
                    break;
                }
            }

            if (jobId == null) {
                System.out.printf("%s\t%d\t%s\t%s\t%s\n", "-", PV[i], "NOT SENT", "-", status.toUpperCase());
                continue;
            }

            HttpResponse<String> graylogFindResponse = Unirest.get("https://graylog2.devfactory.com/streams/55faa44fe4b0067ac45b2186/messages")
                    .queryString("rangetype", "absolute")
                    .queryString("fields", "message,source")
                    .queryString("from", dateTimeFormatter.format(date) + "T00:00:00.000Z")
                    .queryString("to", dateTimeFormatter.format(date.plusDays(2)) + "T00:00:00.000Z")
                    .queryString("q", "aline_job_id:" + jobId + " AND LoggerName:com.devfactory.aline.messaging.aws.SNSTopicMessagingService")
                    .asString();
            Document document = Jsoup.parse(graylogFindResponse.getBody());
            String searchData = document.select("#react-search-result").attr("data-search-result");
            String statusz = searchData.length() < 14000 ? "NOT SENT" : "SENT";
            System.out.printf("%s\t%d\t%s\t%s\t%s\n", jobId, PV[i], statusz, updated.substring(0, 10), "SUCCESS");
        }
    }
}
