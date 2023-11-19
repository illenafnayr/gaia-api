package com.gaia.api.monitoringservice;

        import com.opencsv.CSVReader;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;
        import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
        import software.amazon.awssdk.auth.credentials.AwsCredentials;
        import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
        import software.amazon.awssdk.regions.Region;
        import software.amazon.awssdk.services.s3.S3Client;
        import software.amazon.awssdk.services.s3.model.GetObjectRequest;
        import software.amazon.awssdk.services.s3.model.GetObjectResponse;
        import software.amazon.awssdk.core.ResponseInputStream;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

@RestController
@RequestMapping("/api/csv")
public class LoggingController {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.fileName}")
    private String fileName;

    @GetMapping
    public ResponseEntity<List<Map<String, List<Map<String, String>>>>> readCsv() {
        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_1) // Change the region accordingly
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try (ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
             InputStream objectData = responseInputStream) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> csvData = csvReader.readAll();

            List<Map<String, List<Map<String, String>>>> jsonDataList = convertCSVToJson(csvData);

            return new ResponseEntity<>(jsonDataList, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception and return an appropriate HTTP response
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Map<String, List<Map<String, String>>>> convertCSVToJson(List<String[]> csvData) {
        List<Map<String, List<Map<String, String>>>> jsonDataList = new ArrayList<>();

        String[] headers = csvData.get(0);

        for (int j = 1; j < headers.length; j++) {
            String header = headers[j];
            Map<String, List<Map<String, String>>> headerDataMap = new HashMap<>();

            for (int i = 1; i < csvData.size(); i++) {
                String[] row = csvData.get(i);
                String timestamp = row[0]; // Assuming timestamp is in the first column
                String value = row[j];

                Map<String, String> valueMap = new HashMap<>();
                valueMap.put("timestamp", timestamp);
                valueMap.put("value", value);

                List<Map<String, String>> headerData = headerDataMap.getOrDefault(header, new ArrayList<>());
                headerData.add(valueMap);

                headerDataMap.put(header, headerData);
            }

            jsonDataList.add(headerDataMap);
        }

        return jsonDataList;
    }
}

