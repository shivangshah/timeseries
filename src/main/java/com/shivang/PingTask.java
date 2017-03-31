package com.shivang;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by shivang on 3/30/17.
 */
@Component
public class PingTask {

    private static final Random random = new Random();
    private int sequence = 0;
    private final TransportClient transportClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PingTask() throws UnknownHostException {
        String schemaJson = "{\n" +
                "    \"template\": \"timeseries\",\n" +
                "    \"settings\": {\n" +
                "        \"number_of_shards\": 1,\n" +
                "        \"number_of_replicas\": 0\n" +
                "    },\n" +
                "    \"mappings\": {\n" +
                "        \"_default_\": {\n" +
                "            \"_all\": {\n" +
                "                \"enabled\": true\n" +
                "            },\n" +
                "            \"properties\": {\n" +
                "                \"created\": {\n" +
                "                    \"type\": \"date\",\n" +
                "                    \"format\": \"epoch_millis\",\n" +
                "                    \"include_in_all\": true\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Settings settings = Settings.builder()
                .put("cluster.name", "timeseries")
                .put("client.transport.sniff", true)
                .put("xpack.security.user", "elastic:changeme")
                .build();
        transportClient = new PreBuiltXPackTransportClient(settings);
        transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        this.objectMapper = new ObjectMapper();
        transportClient.admin().indices()
                .preparePutTemplate("timeseries-template")
                .setSource(schemaJson).execute().actionGet();

    }

    @Scheduled(fixedDelay = 1000)
    public void schedule1() throws IOException {
        addPingData("from1");
    }

    @Scheduled(fixedDelay = 1000)
    public void schedule2() throws IOException {
        addPingData("from2");
    }

    @Scheduled(fixedDelay = 500)
    public void schedule3() throws IOException {
        addPingData("from3");
    }

    @Scheduled(fixedDelay = 500)
    public void schedule4() throws IOException {
        addPingData("from4");
    }

    public void addPingData(String from) throws IOException {
        String line = "64 bytes from 1.2.3.4 " + sequence + " " + random.nextInt(100) + " " + random.nextInt(100);
        sequence++;
        System.out.println(from + " " + line);
        try {
            String[] data = line.split(" ");
            String ip = data[3];
            String seq = data[4];
            String ttl = data[5];
            String time = data[6];
            PingObject pingObject = new PingObject();
            pingObject.setIp(ip);
            pingObject.setSequence(seq);
            pingObject.setTtl(ttl);
            pingObject.setTimeInMillis(Long.parseLong(time));
            transportClient.prepareIndex()
                    .setType("ping")
                    .setSource(objectMapper.convertValue(pingObject, HashMap.class))
                    .setIndex("timeseries").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
